package com.learner.download;

/**
 * 名称: ViewState.java<br>
 * 描述: View状态抽象接口<br>
 * 类型: JAVA<br>
 * 最近修改时间:2013-6-17 下午7:56:15<br>
 * 
 * @since 2013-6-17
 * @author lig
 */
public interface ViewState {

	void updateStateInit();

	void updateStateDownloading(int percent);

	void updateStatePause(int percent);

	void updateStateStop();

	void updateStateFinished(String path);

	void updateStateFinished1();

	void updateStateFailed(int code, String msg);

	/**
	 * 查找View，根据tag
	 * 
	 * @param tag
	 * @return
	 */
	ViewState findViewSateWithTag(Object tag);

}
