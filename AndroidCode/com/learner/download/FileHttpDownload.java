package com.learner.download;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.util.Log;


/**
* 名称: FileHttpDownload.java<br>
* 描述: 文件断点下载<br>
* 类型: JAVA<br>
* 最近修改时间:2013-6-17 下午9:03:42<br>
* @since  2013-6-17
* @author 马全迎
*/
public class FileHttpDownload implements Runnable {
	// http APK 下载限速,单位KB，0不限速
	public static final int mobileLimiter = 6;
	public static final int wifiLimiter = 128;

	public static int limitRate = wifiLimiter;
	/**
	 * 下载过程监听，消息 Created on 2012-11-13下午4:58:20
	 * 
	 * @author lig
	 * 
	 */
	public static interface DownloadingListener {
		/**
		 * <pre>
		 * 下载结束通知，是否成功，等
		 * </pre>
		 * 
		 * @param type
		 *            请见{@link DownloadState}
		 * @param msg
		 *            成功返回 文件保存路径；失败返回 失败原因消息
		 */
		public void onMessage(int type, String msg);

		/**
		 * <pre>
		 * 下载进度，单位B
		 * 每次下载4096B，更新进度监听
		 * </pre>
		 * 
		 * @param completedSize
		 *            已下载大小
		 * @param totalSize
		 *            文件总大小。
		 */
		public void onProgress(long completedSize, long totalSize);
	}

	private static transient final String tag = FileHttpDownload.class
			.getSimpleName();
	private HttpURLConnection connection;
	private URL url;
	private String filePath;
	private long totalSize;
	private RandomAccessFile randomAccessFile;
	private LimitFlowInputStream limiterInputStream;
	private StringBuffer message;
	private long completedSize = 0;
	private DownloadingListener listener;
	private Object mLock;
	/** 最大请求次数 */
	private final int maxRequestCount = 50;

	/** 当服务器不返回流时，重复请求链接次数 */
	private final int repeatRequestCount = 10;
	/** 获取文件大小失败次数 */
	private final int initCount = 5;

	private boolean pause;
	private boolean stop;
	/** 限速大小，0不限速，100限速为100KB */
	private int rate = 0;

	/**
	 * @param url
	 *            下载地址
	 * @param filePath
	 *            文件保存路径含名称
	 * @throws MalformedURLException
	 */
	public FileHttpDownload(String url, File filePath)
			throws MalformedURLException {
		this(new URL(url), filePath);
	}

	/**
	 * @param url
	 *            下载地址
	 * @param filePath
	 *            文件保存路径含名称
	 * @throws MalformedURLException
	 */
	public FileHttpDownload(String url, String filePath)
			throws MalformedURLException {
		this(new URL(url), filePath,  limitRate);
	}

	/**
	 * @param url
	 *            下载地址
	 * @param filePath
	 *            文件保存路径含名称
	 */
	public FileHttpDownload(URL url, File filePath) {
		this(url, filePath.getAbsolutePath(), limitRate);
	}

	/**
	 * @param url
	 *            下载地址
	 * @param filePath
	 *            文件保存路径含名称
	 * 
	 * @param rate
	 *            下载速度限制KB，0 不限速
	 */
	public FileHttpDownload(URL url, String filePath, int rate) {
		this.url = url;
		this.filePath = filePath;
		this.message = new StringBuffer();
		this.rate = rate;
		this.mLock = new Object();
		this.pause = false;
		this.stop = false;
	}

	private int getTotalSize() {
		if (listener != null) {
			listener.onMessage(DownloadState.INITIALIZE, filePath);
		}
		for (int i = 1; i <= initCount; i++) {
			int resultType = init(i);
			if (resultType == DownloadState.INITISUCCESS)
				return resultType;
			if (resultType == DownloadState.FAILED){
				return resultType;
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
		}
		message.append("超过重试次数，获取文件大小失败!");
		return DownloadState.FAILED;
	}

	/**
	 * @return
	 */
	private int init(int count) {
		try {
			connection = (HttpURLConnection) url.openConnection();
			if(com.certusnet.common.util.Constants.timeout>0)
				connection.setConnectTimeout(com.certusnet.common.util.Constants.timeout);
			totalSize = connection.getContentLength();
			if (totalSize == -1) {
				message.append("获取文件大小失败!第" + count + "次");
				return DownloadState.FAILED;
			}
			if(404==connection.getResponseCode()){
				message.append("文件不存在!");
				return DownloadState.FAILED;	
			}
			randomAccessFile = new RandomAccessFile(filePath, "rwd");
			// randomAccessFile.setLength(totalSize);
			completedSize = randomAccessFile.length();
			return DownloadState.INITISUCCESS;
		} catch (FileNotFoundException e) {
			message.append(filePath + "文件路径不正确!");
		} catch (IOException e) {
			message.append(e.getMessage());
		} finally {
			connection.disconnect();
		}
		return DownloadState.FAILED;
	}

	private void partFileDownload() {
		try {
			connection = (HttpURLConnection) url.openConnection();
			connection.setConnectTimeout(com.certusnet.common.util.Constants.timeout);
			connection.setRequestProperty("Range", "bytes=" + (completedSize)
					+ "-");

			randomAccessFile.seek(completedSize);

			limiterInputStream = new LimitFlowInputStream(
					connection.getInputStream(),
					new LimitFlowInputStream.LimitFlow(rate));
			byte[] buffer = new byte[4096];
			int length = -1;
			while ((length = limiterInputStream.read(buffer)) != -1) {
				if (this.pause) {
					// 使用线程锁锁定该线程
					synchronized (mLock) {
						try {
							if (listener != null)
								listener.onMessage(DownloadState.PAUSE, null);
							mLock.wait();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
				if (this.stop)
					break;
				randomAccessFile.write(buffer, 0, length);
				completedSize += length;
				if (listener != null)
					listener.onProgress(completedSize, totalSize);

			}

		} catch (Exception e) {
			e.printStackTrace();
			// if (listener != null) {
			// listener.onMessage(DownloadState.FAILED, "");
			// }
		} finally {
			connection.disconnect();

		}

	}

	@Override
	public void run() {
		int resultType = getTotalSize();
		if (this.pause) {
			if (listener != null)
				listener.onProgress(completedSize, totalSize);
			// 使用线程锁锁定该线程
			synchronized (mLock) {
				try {
					if (listener != null)
						listener.onMessage(DownloadState.PAUSE, null);
					mLock.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		if (this.stop)
			return;
		if (resultType == DownloadState.INITISUCCESS) {
			try {
				if (listener != null) {
					listener.onMessage(DownloadState.DOWNLOADING, totalSize
							+ "");
					if (completedSize == totalSize) {
						this.listener.onMessage(DownloadState.FINISHED,
								filePath);
						return;
					}
				}
				int repeatCount = 0;
				int requestCount = 0;
				long lastCompeleteSize = 0;
				while (completedSize < totalSize) {
					if (this.pause) {
						// 使用线程锁锁定该线程
						synchronized (mLock) {
							try {
								mLock.wait();
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
					}
					if (this.stop)
						break;
					partFileDownload();
					requestCount++;
					if (lastCompeleteSize < completedSize) {
						lastCompeleteSize = completedSize;
						repeatCount = 0;
					} else {
						repeatCount++;
						if (repeatCount > repeatRequestCount) {
							message.append("长时间不能获取文件流,下载失败!");
							resultType = DownloadState.FAILED;
							break;
						}
						// 增加等待请求时间
						if (repeatCount >= repeatRequestCount / 2) {
							try {
								Thread.sleep(1000);
							} catch (Exception e) {
							}
						}
					}

					if (requestCount > maxRequestCount) {
						message.append("超过文件下载最大请求次数,下载失败!");
						resultType = DownloadState.FAILED;
						break;
					}
					// Log.d(tag, "文件总长度:" + totalSize + "已下载:" +
					// completedSize);
				}
			} catch (Exception e) {
				e.printStackTrace();
				// if (listener != null) {
				// listener.onMessage(DownloadState.FAILED, "");
				// }
			}
		}
		try {
			if (randomAccessFile != null)
				randomAccessFile.close();
		} catch (IOException e) {
			message.append(e.getMessage());
			resultType = DownloadState.FAILED;
		}
		if (this.pause) {
			// 使用线程锁锁定该线程
			synchronized (mLock) {
				try {
					if (listener != null)
						listener.onMessage(DownloadState.PAUSE, null);
					mLock.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		if (this.stop) {
			new File(filePath).delete();
			if (this.listener != null) {
				this.listener.onMessage(DownloadState.STOP, null);
			}
			return;
		}
		if (this.listener != null) {
			if (completedSize == totalSize) {
				try {
					Runtime.getRuntime().exec("chmod 755 " + this.filePath);
				} catch (IOException e) {
					e.printStackTrace();
				}
				this.listener.onMessage(DownloadState.FINISHED, filePath);
			} else
				this.listener.onMessage(DownloadState.FAILED,
						message.toString());
		}
		Log.d(tag,
				"结束--文件总长度:" + totalSize + "已下载:" + completedSize
						+ message.toString());
	}

	public void setDownloadingListener(DownloadingListener listener) {
		this.listener = listener;
	}

	public void stop() {
		this.stop = true;
		this.pause = false;
		synchronized (mLock) {
			mLock.notifyAll();
		}
	}

	public void pause() {
		this.pause = true;
		this.stop = false;
	}

	public void resume() {
		this.pause = false;
		this.stop = false;
		synchronized (mLock) {
			mLock.notifyAll();
		}
	}
}
