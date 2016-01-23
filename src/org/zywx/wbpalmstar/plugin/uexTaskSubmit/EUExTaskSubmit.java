package org.zywx.wbpalmstar.plugin.uexTaskSubmit;

import org.json.JSONException;
import org.json.JSONObject;
import org.zywx.wbpalmstar.engine.EBrowserActivity;
import org.zywx.wbpalmstar.engine.EBrowserView;
import org.zywx.wbpalmstar.engine.universalex.EUExBase;
import org.zywx.wbpalmstar.engine.universalex.EUExCallback;
import org.zywx.wbpalmstar.engine.universalex.EUExUtil;
import org.zywx.wbpalmstar.plugin.uexTaskSubmit.errorcatch.CrashCatchHandler;
import org.zywx.wbpalmstar.plugin.uexTaskSubmit.util.TaskSubmitUtils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;

/**
 * 插件入口类
 * 
 * @author waka
 *
 */
public class EUExTaskSubmit extends EUExBase {

	private static final String TAG = "EUExTaskSubmit";

	private static FloatBall floatBall;

	// 全局静态变量
	public static String appId = "";// appId
	public static String imgPath = "";// 保存的截图路径
	public static String logPath = "";// 保存的log路径
	public static String crashPath = "";// 保存的crash路径

	public static String logFileName = "";// 这一次保存的log日志
	public static String lastCrashFileName = "";// 上一次崩溃时的崩溃日志
	public static String alreadyPostCrashFileName = "";// 已经上传过的崩溃日志，为了不重复上传已上传过的崩溃日志而设置

	public static boolean logCatchSwitch = true;// 抓取log开关
	public static String configLoginURL = "";// 从config.xml文件中读取的loginURL
	public static String configBaseURL = "";// 从config.xml文件中读取的baseURL

	// 应用程序开始前是否启动小球的开关
	private static boolean isStartFloatBall = true;

	// 回调
	private static final String FUNC_GET_HOST_URL_CALLBACK = "uexTaskSubmit.cbGetHostURL";// 获取URL信息回调
	@SuppressWarnings("unused") // 还没用到这个回调
	private static final String FUNC_FLOAT_SWITCH_CALLBACK = "uexTaskSubmit.cbFloatSwitch";// 浮动小球开关回调

	/**
	 * 构造方法，入口类需传入context和EBrowserView浏览器Activity
	 * @throws Exception 
	 */
	public EUExTaskSubmit(Context context, EBrowserView browserView) {
		super(context, browserView);

		if (TextUtils.isEmpty(appId)) {
			appId = mBrwView.getRootWidget().m_appId;
		}
		if (TextUtils.isEmpty(imgPath)) {
			imgPath = mBrwView.getWidgetPath() + "screenshot";
		}
		if (TextUtils.isEmpty(logPath)) {
			logPath = mBrwView.getWidgetPath() + "log";
		}
		if (TextUtils.isEmpty(crashPath)) {
			crashPath = mBrwView.getWidgetPath() + "crash";
		}

		// 从SharedPreferences中获得上一次崩溃日志的文件名
		if (TextUtils.isEmpty(lastCrashFileName)) {
			SharedPreferences sharedPreferences = mContext.getSharedPreferences(Constant.SHARED_PREFERENCES_TABLE_NAME,
					Activity.MODE_PRIVATE);
			lastCrashFileName = sharedPreferences.getString(Constant.SHARED_PREFERENCES_LAST_KEY_NAME, "");
			Log.i(TAG, "lastCrashFileName---->" + lastCrashFileName);
		}
		// 从SharedPreferences中获得已经上传的崩溃日志的文件名
		if (TextUtils.isEmpty(alreadyPostCrashFileName)) {
			SharedPreferences sharedPreferences = mContext.getSharedPreferences(Constant.SHARED_PREFERENCES_TABLE_NAME,
					Activity.MODE_PRIVATE);
			alreadyPostCrashFileName = sharedPreferences.getString(Constant.SHARED_PREFERENCES_ALREADY_POST_KEY_NAME,
					"");
			Log.i(TAG, "alreadyPostCrashFileName---->" + alreadyPostCrashFileName);
		}
		// 如果上一次崩溃的日志文件名，和已经上传过的日志文件名相同
		if (lastCrashFileName.equals(alreadyPostCrashFileName)) {
			lastCrashFileName = "";// 上一次崩溃的日志文件名为空
		}

		if (TextUtils.isEmpty(configLoginURL)) {
			configLoginURL = TaskSubmitUtils.getConfigLabelValue(context, "useAppCanTaskSubmitSSOHost");
		}
		if (TextUtils.isEmpty(configBaseURL)) {
			configBaseURL = TaskSubmitUtils.getConfigLabelValue(context, "useAppCanTaskSubmitHost");
		}
		// 如果在config里没有这两个地址，则读我们默认的
		if (TextUtils.isEmpty(configLoginURL)) {
			configLoginURL = EUExUtil.getString("plugin_task_submit_sso_host");
		}
		if (TextUtils.isEmpty(configBaseURL)) {
			configBaseURL = EUExUtil.getString("plugin_task_submit_host");
		}
	}

	/**
	 * 重写引擎的Activity创建方法
	 * 
	 * @param context
	 */
	public static void onActivityCreate(Context context) {
		// 判断context是否为EBrowserActivity的实例
		if (context instanceof EBrowserActivity) {
			EBrowserActivity activity = (EBrowserActivity) context;

			// 将系统处理异常的类改为自定义的
			CrashCatchHandler crashCatchHandler = CrashCatchHandler.getInstance();// 获得单例
			crashCatchHandler.init(context);

			// 从config文件中读取是否开启FloatBall的标志
			String isStartFloatBall_string = TaskSubmitUtils.getConfigLabelValue(context,
					"useAppCanTaskSubmitFloatOpen");
			if (isStartFloatBall_string.equals("false")) {
				isStartFloatBall = false;
			} else {
				isStartFloatBall = true;
			}
			if (isStartFloatBall) {// 如果开关是开的，开启小球
				floatBall = new FloatBall(activity);
			}
		}
	}

	/**
	 * 重写引擎的onActivityResume方法
	 * 
	 * @param context
	 */
	public static void onActivityResume(Context context) {
		if (floatBall != null) {
			floatBall.creatFloatBall();
		}
	}

	/**
	 * 重写引擎的onActivityPause方法
	 * 
	 * @param context
	 */
	public static void onActivityPause(Context context) {
		if (floatBall != null) {
			floatBall.removeFloatBall();
		}
	}

	/**
	 * 获取appId
	 * 
	 * @return
	 */
	public String getAppId() {
		appId = mBrwView.getRootWidget().m_appId;
		return appId;
	}

	/**
	 * 获得URL信息
	 * 
	 * @param param
	 */
	public void getHostURL(String[] param) {
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("useAppCanTaskSubmitSSOHost", configLoginURL);
			jsonObject.put("useAppCanTaskSubmitHost", configBaseURL);
			Log.i(TAG, jsonObject.toString());
			// 给前端回调一个JSON对象，不是JSON字符串，是JSON对象
			String js = SCRIPT_HEADER + "if(" + FUNC_GET_HOST_URL_CALLBACK + "){" + FUNC_GET_HOST_URL_CALLBACK + "(" + 0
					+ "," + EUExCallback.F_C_JSON + "," + jsonObject.toString() + SCRIPT_TAIL;
			onCallback(js);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 浮动小球开关，运行中
	 * 
	 * @param param
	 */
	public void floatSwitch(String[] param) {
		if (param.length < 1) {
			return;
		}
		try {
			boolean floatSwitch = Boolean.valueOf(param[0]);
			if (floatSwitch == true) {
				if (floatBall == null) {
					((Activity) mContext).runOnUiThread(new Runnable() {
						@Override
						public void run() {
							floatBall = new FloatBall(mContext);
							floatBall.creatFloatBall();
						}
					});
				}
			} else if (floatSwitch == false) {
				if (floatBall != null) {
					((Activity) mContext).runOnUiThread(new Runnable() {
						@Override
						public void run() {
							floatBall.removeFloatBall();
							floatBall = null;
						}
					});
				}
			}
		} catch (NumberFormatException e) {
			Log.e(TAG, "NumberFormatException");
			e.printStackTrace();
		}
	}

	/**
	 * clean
	 */
	@Override
	protected boolean clean() {
		Log.i(TAG, "clean");
		return false;
	}

}
