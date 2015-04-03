package com.certusnet.download;

import java.io.File;
import java.net.MalformedURLException;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.certusnet.common.util.FileUtils;

/**
 * @author lig
 * 
 */
public class DownloadTask {
	private File file;
	private ConcurrentMap<String, DownloadTaskListener> map = new ConcurrentHashMap<String, DownloadTaskListener>();
	private DownloadTaskListener lastTaskListener;
	private int currentProgress;
	protected static final String tag = DownloadTask.class.getSimpleName();
	private int status = -10;
	private DownloadState downloadState;
	/** 不同的页面（不同的view）注册的监听的key ，默认采用groupCode */
	private String mListenerId;
	private boolean pause;
	private long lastime;
	FileHttpDownload f = null;
	private String downloadUrl;
	private String name;
	private String md5;
	private String id;
	private String path;
	private String size;

	/**
	 * @param id
	 * @param name
	 * @param downloadUrl
	 * @param md5
	 * @param listener
	 */
	public DownloadTask(String id, String name, String downloadUrl) {
		this(id, name, downloadUrl, null);
	}

	/**
	 * @param id
	 *            下载文件唯一标识
	 * @param name
	 * @param downloadUrl
	 * @param md5
	 * @param listenerId
	 *            String 任务唯一标识
	 * @param listener
	 *            DownloadTaskListener 任务下载监听
	 */
	public DownloadTask(String id, String name, String downloadUrl, String md5) {
		this.id = id;
		this.name = name;
		this.downloadUrl = downloadUrl;
		this.md5 = md5;
		lastime = System.currentTimeMillis();
	}

	private void sendMsg2AllListener(int messageWhat) {
		long currentime = System.currentTimeMillis();
		if (currentime - lastime > 500L || messageWhat != DownloadState.DOWNLOADING || getCurrentProgress() == 100) {
			lastime = currentime;
			Iterator<Entry<String, DownloadTaskListener>> iterator = map.entrySet().iterator();
			while (iterator.hasNext()) {
				Entry<String, DownloadTaskListener> entry = iterator.next();
				entry.getValue().sendMessage(entry.getValue().obtainMessage(messageWhat, getCurrentProgress(), 0));
			}
		}
	}

	/**
	 * 执行任务
	 * 
	 * @param ctx
	 * @return
	 */
	public int execute(Context ctx) {
		DownloadTask task = DownloadMgr.findTask(id);
		if (TextUtils.isEmpty(downloadUrl)) {
			sendMsg2AllListener(DownloadState.FAILED);
			return DownloadState.FAILED;
		}
		DownloadMgr.downLoadingFileList.add(FileUtils.getStorePath(ctx)
				+ downloadUrl.substring(downloadUrl.lastIndexOf("/")));
		if (task != null) {// 已经下载
			if (mListenerId != null)
				task.addListener(mListenerId, map.get(mListenerId));
			return task.getStatus();
		} else {
			File file=getLocalPath(ctx, name, downloadUrl);
			if(file.exists()){
				if (lastTaskListener != null) {
					lastTaskListener.sendMessage(lastTaskListener.obtainMessage(DownloadState.FINISHED,
							file.getAbsolutePath()));
				}
				if (map != null) {
					Iterator<Entry<String, DownloadTaskListener>> iterator = map.entrySet().iterator();
					while (iterator.hasNext()) {
						Entry<String, DownloadTaskListener> entry = iterator.next();
						entry.getValue().sendMessage(
								entry.getValue().obtainMessage(DownloadState.FINISHED1, file.getAbsolutePath()));
					}
				}
				return 0;
			}
			
			sendMsg2AllListener(DownloadState.INITIALIZE);
			try {
				DownloadMgr.addTask(this);

				if (TextUtils.isEmpty(size) || TextUtils.isEmpty(path)) {
					file = getDownloadTempPath(ctx,name, downloadUrl);
					f = new FileHttpDownload(downloadUrl, file);
				} else {
					f = new FileHttpDownload(downloadUrl, path);
				}
				if (pause)
					f.pause();
				f.setDownloadingListener(new FileHttpDownload.DownloadingListener() {

					@Override
					public void onProgress(long completedSize, long totalSize) {
						Log.d(tag, completedSize + ":" + totalSize);
						setCurrentProgress(Long.valueOf(completedSize * 100L / totalSize).intValue());
						sendMsg2AllListener(DownloadState.DOWNLOADING);
					}

					@Override
					public void onMessage(int type, String msg) {
						DownloadTask.this.status = type;

						if (type == DownloadState.FINISHED) {
							String path = rename(msg);
							if (map != null) {
								Iterator<Entry<String, DownloadTaskListener>> iterator = map.entrySet().iterator();
								while (iterator.hasNext()) {
									Entry<String, DownloadTaskListener> entry = iterator.next();
									entry.getValue().sendMessage(
											entry.getValue().obtainMessage(DownloadState.FINISHED1, path));
								}
							}
								if (lastTaskListener != null) {
									lastTaskListener.sendMessage(lastTaskListener.obtainMessage(DownloadState.FINISHED,
											path));
								}
							DownloadMgr.deleteTask(DownloadTask.this.getId());
							DownloadMgr.downLoadingFileList.remove(path);
						} else if (type == DownloadState.INITIALIZE) {
							DownloadTask.this.path = (msg);
							sendMsg2AllListener(DownloadState.INITIALIZE);
						} else if (type == DownloadState.DOWNLOADING) {
							DownloadTask.this.size = (msg);
							if (DownloadDB.existDownloadTask(DownloadTask.this.getId()))
								DownloadDB.updateDownloadTask(DownloadTask.this);
							else
								DownloadDB.insertDownloadTask(DownloadTask.this);
						} else if (type == DownloadState.PAUSE) {
							sendMsg2AllListener(DownloadState.PAUSE);
						} else if (type == DownloadState.STOP) {
							DownloadMgr.deleteTask(id);
							sendMsg2AllListener(DownloadState.STOP);
						} else if (type == DownloadState.FAILED) {
							DownloadMgr.deleteTask(id);
							sendMsg2AllListener(DownloadState.FAILED);
						}
					}
				});
				DownloadMgr.getAppExecutorService().execute(f);
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
		}
		return -1;
	}

	public String rename(String path) {
		File source = new File(path);
		String result = path.substring(0, path.length() - 4);
		source.renameTo(new File(result));
		source.delete();
		return result;
	}

	/**本地文件路径，下载完成存储文件路径
	 * @param ctx
	 * @param name
	 * @param remote
	 * @return
	 */
	public static File getLocalPath(Context ctx,String name, String remote) {
		if (remote == null || !remote.contains("/"))
			return null;
		String fileName = remote.substring(remote.lastIndexOf("/"));
		return new File(FileUtils.getStorePath(ctx), fileName);
	}
	
	public static File getDownloadTempPath(Context ctx,String name,  String remote) {
		if (remote == null || !remote.contains("/"))
			return null;
		String fileName = remote.substring(remote.lastIndexOf("/"));
		return new File(FileUtils.getStorePath(ctx), fileName + ".tmp");
	}

	/**
	 * 
	 * 方法描述
	 * 
	 * @param key
	 * @param listener
	 * @变更记录 2013-6-17 下午8:20:57 马全迎 创建
	 */
	public DownloadTask addListener(String key, DownloadTaskListener listener) {
		lastTaskListener = listener;
		this.getMap().remove(key);
		this.getMap().put(key, listener);
		return this;
	}

	/**
	 * 方法描述
	 * 
	 * @return int 状态
	 * @变更记录 2013-6-17 下午8:26:05 马全迎 创建
	 */

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public ConcurrentMap<String, DownloadTaskListener> getMap() {
		return map;
	}

	public void setMap(ConcurrentMap<String, DownloadTaskListener> map) {
		this.map = map;
	}

	public int getCurrentProgress() {
		return currentProgress;
	}

	public void setCurrentProgress(int currentProgress) {
		this.currentProgress = currentProgress;
	}

	public DownloadState getDownloadState() {
		return downloadState;
	}

	public void setDownloadState(DownloadState downloadState) {
		this.downloadState = downloadState;
	}

	public void stop() {
		if (f != null)
			f.stop();
	}

	public void setPause(boolean pause) {
		this.pause = pause;
	}

	public void pause() {
		pause = true;
		if (f != null)
			f.pause();
		sendMsg2AllListener(DownloadState.PAUSE);
	}

	public void resume() {
		pause = false;
		if (f != null)
			f.resume();
		sendMsg2AllListener(DownloadState.DOWNLOADING);
	}

	public boolean isPause() {
		return pause;
	}

	public String getId() {
		return this.id;
	}

	public String getPath() {
		return this.path;
	}

	public String getMD5() {
		return this.md5;
	}
}
