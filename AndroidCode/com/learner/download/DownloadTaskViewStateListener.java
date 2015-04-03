package com.learner.download;

import android.content.Context;
import android.os.Message;

public class DownloadTaskViewStateListener extends DownloadTaskListener {
	private ViewState progress;
	private ViewState mView;

	public DownloadTaskViewStateListener(DownloadTask task, Context context, ViewState view) {
		super();
		mTask = task;
		this.mContext = context;
		mView = view;
	}

	@Override
	public void handleMessage(Message msg) {
		this.progress = (ViewState) mView.findViewSateWithTag(mTask);
		if (progress == null) {
			return;
		}
		switch (msg.what) {
		case DownloadState.INITIALIZE:
			progress.updateStateInit();
			break;
		case DownloadState.INITISUCCESS:
			progress.updateStateInit();
			break;
		case DownloadState.STOP:
			progress.updateStateStop();
			break;
		case DownloadState.DOWNLOADING:
			progress.updateStateDownloading(msg.arg1);
			break;
		case DownloadState.FINISHED:
			String path = msg.obj.toString();
			progress.updateStateFinished(path);
			break;
		case DownloadState.FINISHED1:
			path = msg.obj.toString();
			progress.updateStateFinished1();
			break;
		case DownloadState.FAILED:
			String m =null;
			if(msg.obj!=null)
				m = msg.obj.toString();
			progress.updateStateFailed(msg.arg1, m);
			break;
		case DownloadState.PAUSE:
			progress.updateStatePause(msg.arg1);
			break;
		}

	}
}
