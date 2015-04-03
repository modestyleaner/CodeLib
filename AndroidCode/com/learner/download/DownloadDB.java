package com.learner.download;


import java.util.List;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class DownloadDB extends SQLiteOpenHelper {
	private static final String DB_NAME = "certusnet-download";
	private static DownloadDB coredb;
	public static final String AppCode = "taskid";
	public static final String TaskName = "taskname";
	public static final String Path = "path";
	public static final String TaskSize = "size";
	public static final String TaskAddr = "url";
	public static final String AppVersionCode = "VersionCode";
	public static final String StartTime = "start_time";
	public static final String EndTime = "end_time";
	public static final String MD5 = "md5";
	public static final String TABLE_NAME = "download_task";

	public static DownloadDB getInstance(Context context) {
		if (coredb == null)
			coredb = new DownloadDB(context, DB_NAME, null, 1);
		return coredb;
	}

	public DownloadDB(Context context, String name, CursorFactory factory, int version,
			DatabaseErrorHandler errorHandler) {
		super(context, name, factory, version, errorHandler);
	}

	public DownloadDB(Context context, String name, CursorFactory factory, int version) {
		super(context, name, factory, version);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_NAME + "(" + "_ID" + " INTEGER PRIMARY KEY," + AppCode
				+ " VARCHAR,"
				+ TaskName + " VARCHAR," + TaskSize + " VARCHAR," + TaskAddr + " VARCHAR," + MD5 + " VARCHAR," + Path
				+ " VARCHAR," + AppVersionCode + " VARCHAR," + StartTime + " VARCHAR," + EndTime + " VARCHAR);");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	}

	public static List<DownloadTask> loadDownloadTask() {
		return null;
	}

	public static boolean existDownloadTask(String id) {
		// TODO Auto-generated method stub
		return false;
	}

	public static void insertDownloadTask(DownloadTask task) {
		// TODO Auto-generated method stub
		
	}

	public static void deleteDownloadTask(String key_appcode) {
		// TODO Auto-generated method stub
		
	}

	public static void updateDownloadTask(DownloadTask downloadTask) {
		
	}

}
