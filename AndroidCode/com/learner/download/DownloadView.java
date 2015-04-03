package com.learner.download;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

/**
 * @author lig
 *
 */
public abstract class DownloadView implements ViewState {

	protected View mView;
	protected Context mContext;

	public DownloadView(Context ctx, int resource) {
		mView = LayoutInflater.from(ctx).inflate(resource, null);
		mContext = ctx;
	}

	public DownloadView(View v) {
		mView = v;
		mContext = v.getContext();
	}

	@Override
	public ViewState findViewSateWithTag(Object tag) {
		Object viewTag = mView.getTag();
		if (viewTag == tag)
			return this;
		return null;
	}

}
