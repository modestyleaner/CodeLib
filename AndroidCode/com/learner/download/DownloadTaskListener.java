/**
 * 名称: DownloadTaskListener
 * 描述: 下载任务监听
 * 类型: JAVA
 * 最近修改时间: 2013-06-17
 * @since 2013-3-6下午3:11:12
 */
package com.learner.download;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ProgressBar;


public class DownloadTaskListener extends Handler {
	protected ProgressBar progress;
	protected Context mContext;
	protected View mView;
	protected DownloadTask mTask;

	/**
	 * 下载任务监听构造器
	 * 
	 * @param app
	 *            需下载的应用
	 * @param context
	 *            上下文
	 * @param view
	 *            需展示下载进度的view
	 */
	public DownloadTaskListener(DownloadTask app, Context context, View view) {
		mTask = app;
		this.mContext = context;
		mView = view;
	}

	public DownloadTaskListener() {
	}

	@Override
	public void handleMessage(Message msg) {
	}
}
