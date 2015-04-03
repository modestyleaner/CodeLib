

import java.io.File;
import java.io.PrintWriter;
import java.lang.Thread.UncaughtExceptionHandler;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.content.Context;

import com.learner.mobile.util.AppUtil;


public class CrashHandler implements UncaughtExceptionHandler {
	private static CrashHandler INSTANCE = null;
	private Context mContext;
	private UncaughtExceptionHandler mDefaultHandler;

	public static CrashHandler getInstance() {
		if (INSTANCE == null)
			INSTANCE = new CrashHandler();
		return INSTANCE;
	}

	private CrashHandler() {
	}

	public void init(Context context) {
		this.mContext = context;
		this.mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
		Thread.setDefaultUncaughtExceptionHandler(this);
	}

	@Override
	public void uncaughtException(Thread thread, Throwable ex) {
		handleException(ex);
		if ((this.mDefaultHandler != null)) {
			this.mDefaultHandler.uncaughtException(thread, ex);
		}
	}

	private boolean handleException(final Throwable tr) {
		if (tr == null)
			return false;
		try {
			File file = new File(FileUtils.getStorePath(mContext) + "/err/" + new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.CHINA).format(new Date()) + ".err");
			if (!file.getParentFile().exists())
				file.getParentFile().mkdirs();
			PrintWriter pw = new PrintWriter(file);
			//pw.println(DateUtil.getDateFormat().format(new Date()));
			pw.println("应用名称:" + AppUtil.getAppName(mContext));
			pw.println("版本名称:" + AppUtil.getVersionName(mContext));
			pw.println("版本号:" + AppUtil.getVersionCode(mContext));
			tr.printStackTrace(pw);
			pw.flush();
			pw.close();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
}
