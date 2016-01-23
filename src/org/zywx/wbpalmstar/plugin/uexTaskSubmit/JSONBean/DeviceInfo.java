package org.zywx.wbpalmstar.plugin.uexTaskSubmit.JSONBean;

import android.content.Context;
import android.util.DisplayMetrics;

/**
 * 设备信息Bean
 * 
 * @author waka
 *
 */
public class DeviceInfo {
	// private static final String TAG = "DeviceInfo";

	private static DeviceInfo mIntance;

	public String androidVersion = "";// android版本，例：android 4.4.2 (19)
	public String deviceModle = "";// 设备型号，例：Huawei:H60-L12
	public String resolution = "";// 分辨率，例：1080x1920
	public String deviceDPI = "";// 设备DPI，例：480 DPI (3.0)
	public String deviceMemory = "";// 设备内存，例：可用：1302.29MB，已用：1710.85MB
	public String deviceStorage = "";// 设备储存，例：可用：16.66GB，已用：34.90GB
	public String operator = "";// 运营商和信号来源，例： 中国联通 (HSPA+)；中国联通 (UMTS)
	public String wifiName = "";// 所连wifi名称，无则写N/A，例："AD-7-5G"；N/A
	public String batteryStatus = "";// 电池状态，例： 100%，Full； 70%，Charging
	public String location = "";// 位置，例：北京市
	public String thisVersion = "";// 插件版本，例： 1.0.5 (build 6)
	public String postTime = "";// 上传时间，例：00:00:03

	public static DeviceInfo getIntance(Context context) {
		if (mIntance == null) {
			mIntance = new DeviceInfo(context);
		}
		return mIntance;
	}

	/**
	 * 构造方法
	 */
	private DeviceInfo(Context context) {
		androidVersion = android.os.Build.VERSION.RELEASE + " (" + android.os.Build.VERSION.SDK_INT + ")";// 例：4.4.2(19)
		deviceModle = android.os.Build.MANUFACTURER + ":" + android.os.Build.MODEL;// 例：Huawei:H60-L12
		DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
		int screenWidth = displayMetrics.widthPixels;
		int screenHeight = displayMetrics.heightPixels;
		resolution = screenWidth + "x" + screenHeight;
		deviceDPI = displayMetrics.densityDpi + "";
	}

}
