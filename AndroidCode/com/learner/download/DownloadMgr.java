package com.certusnet.download;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.certusnet.icity.mobile.secret.Md5Cryptor;

public class DownloadMgr {
	public static Context mCtx;
	private static DownloadMgr instance;
	/** appCode subchanel */
	private static final ConcurrentMap<String, DownloadTask> down = new ConcurrentHashMap<String, DownloadTask>();
	private static final String tag = DownloadMgr.class.getSimpleName();
	private static ExecutorService appExecutorService;
	public static List<String> downLoadingFileList = new ArrayList<String>();

	public static DownloadMgr getInstance(Context ctx) {
		if (instance == null) {
			instance = new DownloadMgr(ctx);
		}
		DownloadDB.getInstance(ctx);
		return instance;
	}

	private DownloadMgr(Context ctx) {
		this.mCtx = ctx;
	}

	public static ExecutorService getAppExecutorService() {
		if (appExecutorService == null) {
			appExecutorService = Executors.newFixedThreadPool(DownloadConstants.ThreadPoolSize);
		}
		return appExecutorService;
	}

	/**
	 * @param key_appcode
	 *            app唯一标识subchannle
	 * @return
	 */
	public static DownloadTask findTask(String key_appcode) {
		if(TextUtils.isEmpty(key_appcode))return null;
		return down.get(key_appcode);
	}
 

	public static List<DownloadTask> getDownloadTasks() {
		if (down.isEmpty())
			return new ArrayList<DownloadTask>(0);
		List<DownloadTask> list = new ArrayList<DownloadTask>();
		Set<Map.Entry<String, DownloadTask>> set = down.entrySet();
		for (Map.Entry<String, DownloadTask> entry : set) {
			list.add(entry.getValue());
		}
		return list;
	}

	/**
	 * @param pause
	 *            是否暂停模式加载
	 */
	public void loadTask(boolean pause) {
		List<DownloadTask> lst = DownloadDB.loadDownloadTask();
		if (lst != null) {
			for (DownloadTask task : lst) {
				task.setPause(pause);
				task.execute(mCtx);
			}
		}
	}

	public static void addTask(DownloadTask task) {
		down.put(task.getId(), task);
		if (!DownloadDB.existDownloadTask(task.getId()))
			DownloadDB.insertDownloadTask(task);
	}

	public static void deleteTask(String taskid) {
		down.remove(taskid);
		DownloadDB.deleteDownloadTask(taskid);
		Log.d(tag, "deleteTask" + taskid);
	}

	public static void stopDownloadTask(String taskid) {
		DownloadTask task = down.remove(taskid);
		if (task != null)
			task.stop();
		DownloadDB.deleteDownloadTask(taskid);
		Log.d(tag, "removeTask" + taskid);
	}

	public static void updateProgress(String id, String keyListener, Context context, View view) {
		DownloadTask task = down.get(id);
		if (task != null) {
			DownloadTaskListener listener = new DownloadTaskListener(task, context, view);
			task.addListener(keyListener, listener);
			if (task.getStatus() == DownloadState.FINISHED) {
				DownloadMgr.deleteTask(task.getId());
				if (new Md5Cryptor().encrypt(task.getPath()).equalsIgnoreCase(task.getMD5())) {
					if (listener != null)
						listener.sendMessage(listener.obtainMessage(DownloadConstants.FileDownLoadSusscess, task
								.getPath()));
				} else {
					if (listener != null)
						listener.sendEmptyMessage(DownloadConstants.FileCryptFailred);
				}
			}
			listener.sendMessage(listener.obtainMessage(DownloadConstants.RefreshProgressBar, task.getCurrentProgress(), 0));
		}
	}

}
