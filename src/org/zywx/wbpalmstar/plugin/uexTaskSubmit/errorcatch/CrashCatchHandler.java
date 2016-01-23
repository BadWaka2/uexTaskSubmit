package org.zywx.wbpalmstar.plugin.uexTaskSubmit.errorcatch;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.zywx.wbpalmstar.plugin.uexTaskSubmit.Constant;
import org.zywx.wbpalmstar.plugin.uexTaskSubmit.EUExTaskSubmit;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

/**
 * 崩溃日志抓取
 * 
 * UncaughtException处理类,当程序发生Uncaught异常的时候,有该类来接管程序,并记录发送错误报告
 * 
 * 感谢 liuhe688 大神的无私分享
 * 
 * 这里是博客地址：http://blog.csdn.net/liuhe688/article/details/6584143#
 * 
 * @author waka
 *
 */
public class CrashCatchHandler implements UncaughtExceptionHandler {

	public static final String TAG = "CrashHandler";

	private static final CrashCatchHandler INSTANCE = new CrashCatchHandler();// 单例模式
	private Context mContext;
	private Thread.UncaughtExceptionHandler mDefaultHandler;// 系统默认的UncaughtException处理类
	private Map<String, String> mInfosMap = new HashMap<String, String>(); // 用来存储设备信息和异常信息

	/**
	 * 私有构造方法，保证只有一个CrashHandler实例
	 */
	private CrashCatchHandler() {

	}

	/**
	 * 获取CrashHandler，单例模式
	 * 
	 * @return
	 */
	public static CrashCatchHandler getInstance() {
		return INSTANCE;
	}

	/**
	 * 初始化
	 * 
	 * @param context
	 */
	public void init(Context context) {
		mContext = context;
		mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();// 获取系统默认的UncaughtException处理器
		Thread.setDefaultUncaughtExceptionHandler(this);// 设置当前CrashHandler为程序的默认处理器
	}

	/**
	 * 当UncaughtException发生时会转入该函数来处理
	 */
	@Override
	public void uncaughtException(Thread thread, Throwable ex) {
		if (!handleException(ex) && mDefaultHandler != null) {
			// 如果用户没有处理则让系统默认的异常处理器来处理
			mDefaultHandler.uncaughtException(thread, ex);
		} else {
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				Log.e(TAG, "exception : ", e);
				e.printStackTrace();
			}
			// 杀死进程
			android.os.Process.killProcess(android.os.Process.myPid());
			System.exit(1);
		}
	}

	/**
	 * 自定义错误处理,收集错误信息 发送错误报告等操作均在此完成.
	 * 
	 * @param ex
	 * @return 如果处理了该异常信息,返回true;否则返回false.
	 */
	private boolean handleException(Throwable ex) {
		if (ex == null) {
			return false;
		}
		// 使用Toast显示异常信息
		new Thread() {
			public void run() {
				Looper.prepare();
				Toast.makeText(mContext, "程序出现未捕获的异常，即将退出！", Toast.LENGTH_SHORT).show();
				Looper.loop();
			};
		}.start();

		collectDeviceInfo(mContext);// 收集设备参数信息
		saveCrashInfoToFile(ex);// 保存日志文件

		return true;
	}

	/**
	 * 收集设备信息
	 * 
	 * @param context
	 */
	public void collectDeviceInfo(Context context) {
		// 使用包管理器获取信息
		PackageManager pm = context.getPackageManager();
		try {
			PackageInfo pi = pm.getPackageInfo(context.getPackageName(), PackageManager.GET_ACTIVITIES);
			if (pi != null) {
				// TODO get the package info there!
				String versionName = pi.versionName == null ? "" : pi.versionName;// 版本名;若versionName==null，则="null"；否则=versionName
				String versionCode = pi.versionCode + "";// 版本号
				mInfosMap.put("versionName", versionName);
				mInfosMap.put("versionCode", versionCode);
			}
		} catch (NameNotFoundException e) {
			Log.e(TAG, "an NameNotFoundException occured when collect package info");
			e.printStackTrace();
		}

		// 使用反射获取获取系统的硬件信息
		Field[] fields = Build.class.getDeclaredFields();// 获得某个类的所有申明的字段，即包括public、private和proteced，
		for (Field field : fields) {
			field.setAccessible(true);// 暴力反射 ,获取私有的信息;类中的成员变量为private,故必须进行此操作
			try {
				mInfosMap.put(field.getName(), field.get(null).toString());
				Log.d(TAG, field.getName() + " : " + field.get(null));
			} catch (IllegalArgumentException e) {
				Log.e(TAG, "an IllegalArgumentException occured when collect reflect field info", e);
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				Log.e(TAG, "an IllegalAccessException occured when collect reflect field info", e);
				e.printStackTrace();
			}
		}
	}

	/**
	 * 保存错误信息到文件中
	 * 
	 * @param ex
	 * @return 返回文件名称
	 */
	@SuppressLint("CommitPrefEdits")
	private String saveCrashInfoToFile(Throwable ex) {
		// 字符串流
		StringBuffer stringBuffer = new StringBuffer();

		// 获得设备信息
		for (Map.Entry<String, String> entry : mInfosMap.entrySet()) {// 遍历map中的值
			String key = entry.getKey();
			String value = entry.getValue();
			stringBuffer.append(key + "=" + value + "\n");
		}

		// 获得错误信息
		Writer writer = new StringWriter();// 这个writer下面还会用到，所以需要它的实例
		PrintWriter printWriter = new PrintWriter(writer);// 输出错误栈信息需要用到PrintWriter
		ex.printStackTrace(printWriter);
		Throwable cause = ex.getCause();
		while (cause != null) {// 循环，把所有的cause都输出到printWriter中
			cause.printStackTrace(printWriter);
			cause = ex.getCause();
		}
		printWriter.close();
		String result = writer.toString();
		stringBuffer.append(result);

		// 写入文件
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.US);
		String crashFileName = EUExTaskSubmit.crashPath + "/crash_" + simpleDateFormat.format(new Date()) + ".log";
		File file = new File(EUExTaskSubmit.crashPath);
		if (!file.exists()) {
			file.mkdirs();// 如果不存在，则创建所有的父文件夹
		}
		try {
			FileOutputStream fos = new FileOutputStream(crashFileName);
			fos.write(stringBuffer.toString().getBytes());
			fos.close();

			// 把文件名存到SharedPreferences中
			SharedPreferences.Editor editor = mContext
					.getSharedPreferences(Constant.SHARED_PREFERENCES_TABLE_NAME, Context.MODE_PRIVATE).edit();
			editor.putString(Constant.SHARED_PREFERENCES_LAST_KEY_NAME, crashFileName);
			editor.commit();
			return crashFileName;
		} catch (FileNotFoundException e) {
			Log.e(TAG, "an FileNotFoundException occured when write crashfile to sdcard", e);
			e.printStackTrace();
		} catch (IOException e) {
			Log.e(TAG, "an IOException occured when write crashfile to sdcard", e);
			e.printStackTrace();
		}
		return null;
	}

}
