package com.certusnet.download;

/**
 * 下载状态
 * 
 * @author lig
 * @version 1.0
 * @since 1.0 2013-2-1
 */
public class DownloadState {
	/** 初始化 ,开始请求下载*/
	public static final int INITIALIZE = 0;
	/** 下载中 */
	public static final int DOWNLOADING = 1;
	/** 初始化成功，（断点获取文件大小等） */
	public static final int INITISUCCESS = 2;
	/** 下载失败 */
	public static final int FAILED = 3;
	/** 下载完成 */
	public static final int FINISHED = 4;
	/** 下载完成,不处理 */
	public static final int FINISHED1 = 41;
	/** 下载暂停 */
	public static final int PAUSE = 5;
	/** 下载停止 */
	public static final int STOP = 6;
}