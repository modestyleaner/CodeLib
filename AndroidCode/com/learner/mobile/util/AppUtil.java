package com.learner.mobile.util;

import java.io.File;
import java.util.Iterator;
import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.net.Uri;

/**
 * Android 应用相关工具类
 * 
 * @author modestylearner
 * 
 */
public class AppUtil {

	/**
	 * 获取应用的icon
	 * 
	 * @param context
	 * @return
	 */
	public static Drawable getAppIcon(Context context) {
		return context.getApplicationInfo().loadIcon(context.getPackageManager());
	}

	/**
	 * 获取应用的名称
	 * 
	 * @param context
	 * @return
	 */
	public static String getAppName(Context context) {
		return context.getApplicationInfo().loadLabel(context.getPackageManager()).toString();
	}

	/**
	 * 获取应用名称
	 * 
	 * @param context
	 * @param pkgName
	 * @return
	 */
	public static String getAppName(Context context, String pkgName) {
		try {
			PackageManager pm = context.getPackageManager();
			return pm.getApplicationLabel(pm.getApplicationInfo(pkgName, PackageManager.GET_META_DATA)).toString();
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return "";
	}

	/**
	 * 获取应用的包名
	 * 
	 * @param context
	 * @return
	 */
	public static String getPackageName(Context context) {
		return context.getPackageName();
	}

	/**
	 * 获取正在运行的进程Id PID
	 * 
	 * @param context
	 * @param processName
	 *            进程名
	 * @return
	 */
	public static int getAppId(Context context, String processName) {
		Iterator<RunningAppProcessInfo> itr = ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE))
				.getRunningAppProcesses().iterator();
		ActivityManager.RunningAppProcessInfo runningAppProcessInfo;
		do {
			if (!itr.hasNext())
				return 0;
			runningAppProcessInfo = itr.next();
		} while (!processName.equals(runningAppProcessInfo.processName));
		return runningAppProcessInfo.pid;
	}

	/**
	 * 获取应用版本号
	 * 
	 * @param context
	 * @return
	 */
	public static int getAppVersionCode(Context context) {
		try {
			int versionCode = context.getPackageManager().getPackageInfo(context.getPackageName(), 16384).versionCode;
			return versionCode;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return 0;
	}

	public static int getAppVersionCode(Context context, String pkgName) {
		try {
			int versionCode = context.getPackageManager().getPackageInfo(pkgName, 16384).versionCode;
			return versionCode;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return 0;
	}

	/**
	 * 获取应用的版本名称
	 * 
	 * @param context
	 * @return
	 */
	public static String getAppVersionName(Context context) {
		try {
			String versionName = context.getPackageManager().getPackageInfo(context.getPackageName(), 16348).versionName;
			return versionName;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return "";
	}

	public static String getAppVersionName(Context context, String pkgName) {
		try {
			String versionName = context.getPackageManager().getPackageInfo(pkgName, 16348).versionName;
			return versionName;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return "";
	}

	/**
	 * 未安装应用的信息
	 * 
	 * @param context
	 * @param archiveFilePath
	 *            apk路径
	 * @return
	 */
	public static PackageInfo getPackageInfo(Context context, String archiveFilePath) {
		PackageInfo packageInfo = context.getPackageManager().getPackageArchiveInfo(archiveFilePath,
				PackageManager.GET_ACTIVITIES);
		if (packageInfo.applicationInfo != null) {
			packageInfo.applicationInfo.sourceDir = archiveFilePath;
			packageInfo.applicationInfo.publicSourceDir = archiveFilePath;
		}
		return packageInfo;
	}

	/**
	 * 是否为系统应用
	 * 
	 * @param context
	 * @param pkgName
	 *            应用包名
	 * @return
	 */
	public static boolean isSystemApplication(Context context, String pkgName) {
		PackageManager pm = context.getPackageManager();
		try {
			PackageInfo pkgInfo = pm.getPackageInfo(pkgName, PackageManager.GET_CONFIGURATIONS);
			if (new File("/data/app/" + pkgInfo.packageName + ".apk").exists()) {
				return true;
			}
			if (pkgInfo.versionName != null && pkgInfo.applicationInfo.uid < 10000) {
				return true;
			}
			if ((pkgInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
				return true;
			}
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * 启动新的应用
	 * 
	 * @param context
	 * @param pkgName
	 * @param launchableActivity
	 */
	public static void startApplicatoin(Context context, String pkgName, String launchableActivity) {
		context.startActivity(getActivityIntent(pkgName, launchableActivity));
	}

	public static Intent getActivityIntent(String pkgName, String launchableActivity) {
		ComponentName cn = new ComponentName(pkgName, launchableActivity);
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_LAUNCHER);
		intent.setComponent(cn);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
		return intent;
	}

	/**
	 * 
	 * @param context
	 * @param pkgName
	 * @param launchableActivity
	 * @return
	 */
	public static boolean existApplication(Context context, String pkgName, String launchableActivity) {
		PackageManager packageManager = context.getPackageManager();
		List<ResolveInfo> activities = packageManager.queryIntentActivities(
				getActivityIntent(pkgName, launchableActivity), 0);
		return (activities.size() > 0);
	}

	/**
	 * 安装apk
	 * 
	 * @param context
	 * @param path
	 */
	public static void installAPK(Context context, String path) {
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setDataAndType(Uri.fromFile(new File(path)), "application/vnd.android.package-archive");
		context.startActivity(intent);
	}
}
