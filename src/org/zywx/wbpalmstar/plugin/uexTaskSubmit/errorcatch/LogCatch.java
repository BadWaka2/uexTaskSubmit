package org.zywx.wbpalmstar.plugin.uexTaskSubmit.errorcatch;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import org.zywx.wbpalmstar.plugin.uexTaskSubmit.EUExTaskSubmit;

import android.util.Log;

/**
 * 截取logcat日志
 * 
 * 感谢 马涛 大神的无私分享
 * 这里是博客地址：http://www.cnblogs.com/mataojin/archive/2011/11/07/2239260.html
 * 
 * @author waka
 *
 */
public class LogCatch {

	private static final String TAG = "LogCatch";

	/**
	 * 捕获log，并写入文件中，返回文件名
	 * 
	 * @return
	 */
	public static String getOldLog() {

		ArrayList<String> cmdLine = new ArrayList<String>();// 设置命令logcat-d读取日志
		cmdLine.add("logcat");
		cmdLine.add("-d");

		ArrayList<String> clearLog = new ArrayList<String>();// 设置命令logcat-c清除日志
		clearLog.add("logcat");
		clearLog.add("-c");

		try {
			Process process = Runtime.getRuntime().exec(cmdLine.toArray(new String[cmdLine.size()]));// 捕获日志
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));// 将捕获内容转换为BufferdReader

			// 创建logXXXXXXXXXXXXX.txt文件，使用当前时间为文件名，避免重复
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.US);
			String logFileName = EUExTaskSubmit.logPath + "/log_" + simpleDateFormat.format(new Date()) + ".log";
			File file = new File(EUExTaskSubmit.logPath);
			if (!file.exists()) {
				file.mkdirs();// 如果不存在，则创建所有的父文件夹
			}

			// 开始读取日志，每次读取一行
			StringBuffer stringBuffer = new StringBuffer();
			String strLogLine = null;
			while ((strLogLine = bufferedReader.readLine()) != null) {
				// 清理日志....这里至关重要，不清理的话，任何操作都将产生新的日志，代码进入死循环，直到bufferreader满
				Runtime.getRuntime().exec(clearLog.toArray(new String[clearLog.size()]));
				stringBuffer.append(strLogLine + "\r\n");
				Log.i(TAG, strLogLine);// 输出到log里
			}
			FileOutputStream fos = new FileOutputStream(logFileName);
			fos.write(stringBuffer.toString().getBytes());
			fos.close();
			bufferedReader.close();
			return logFileName;
		} catch (IOException e) {
			Log.i(TAG, "IOException---->" + e.getMessage());// 输出到log里
			e.printStackTrace();
		} catch (NullPointerException e) {
			Log.i(TAG, "NullPointerException---->" + e.getMessage());// 输出到log里
			e.printStackTrace();
		}
		return null;
	}

	public static String getLog() {
		Log.i(TAG, "--------func start--------"); // 方法启动
		try {
			ArrayList<String> cmdLine = new ArrayList<String>(); // 设置命令 logcat
																	// -d 读取日志
			cmdLine.add("logcat");
			cmdLine.add("-d");

			ArrayList<String> clearLog = new ArrayList<String>(); // 设置命令 logcat
																	// -c 清除日志
			clearLog.add("logcat");
			clearLog.add("-c");

			Process process = Runtime.getRuntime().exec(cmdLine.toArray(new String[cmdLine.size()])); // 捕获日志
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream())); // 将捕获内容转换为BufferedReader

			// 创建logXXXXXXXXXXXXX.txt文件，使用当前时间为文件名，避免重复
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.US);
			String logFileName = EUExTaskSubmit.logPath + "/log_" + simpleDateFormat.format(new Date()) + ".log";
			File file = new File(EUExTaskSubmit.logPath);
			if (!file.exists()) {
				file.mkdirs();// 如果不存在，则创建所有的父文件夹
			}
			// Runtime.runFinalizersOnExit(true);
			String str = null;
			FileOutputStream fos = new FileOutputStream(logFileName);
			while ((str = bufferedReader.readLine()) != null) // 开始读取日志，每次读取一行
			{
				Runtime.getRuntime().exec(clearLog.toArray(new String[clearLog.size()])); // 清理日志....这里至关重要，不清理的话，任何操作都将产生新的日志，代码进入死循环，直到bufferreader满
				fos.write(str.getBytes());
				Log.i(TAG, str);// 输出到log里 //输出，在logcat中查看效果，也可以是其他操作，比如发送给服务器..
			}
			fos.close();
			if (str == null) {
				Log.i(TAG, "--   is null   --");
			}
			return logFileName;
		} catch (Exception e) {
			e.printStackTrace();
		}
		Log.i(TAG, "--------func end--------");
		return null;
	}
}
