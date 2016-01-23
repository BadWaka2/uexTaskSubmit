package org.zywx.wbpalmstar.plugin.uexTaskSubmit;

public class Constant {
	public static final int FLOAT_BALL_ANIMATION_TIME = 500;// 浮动小球动画时间

	// 创建资源时，特定资源的字段
	public static final String CREATE_RESOURCE_POST_SCREEN_SHOT = "screenShot";// 截屏资源
	public static final String CREATE_RESOURCE_POST_LOG = "log";// logcat日志
	public static final String CREATE_RESOURCE_POST_CRASH = "crash";// 崩溃日志

	// SharedPreferences中存储上一次崩溃的日志的文件名和字段名
	public static final String SHARED_PREFERENCES_TABLE_NAME = "crash";// 文件名
	public static final String SHARED_PREFERENCES_LAST_KEY_NAME = "saveCrashInfo2File";// 上一次崩溃时的崩溃的字段名
	public static final String SHARED_PREFERENCES_ALREADY_POST_KEY_NAME = "alreadyPostCrashFileName";// 已经上传过的崩溃日志的字段名
}
