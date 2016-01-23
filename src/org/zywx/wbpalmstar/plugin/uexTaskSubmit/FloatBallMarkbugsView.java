package org.zywx.wbpalmstar.plugin.uexTaskSubmit;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.zywx.wbpalmstar.engine.universalex.EUExUtil;
import org.zywx.wbpalmstar.plugin.uexTaskSubmit.Activitys.CreateTaskActivity;
import org.zywx.wbpalmstar.plugin.uexTaskSubmit.Activitys.LoginDialogActivity;
import org.zywx.wbpalmstar.plugin.uexTaskSubmit.Activitys.MarkbugsActivity;
import org.zywx.wbpalmstar.plugin.uexTaskSubmit.Activitys.QuitDialogActivity;
import org.zywx.wbpalmstar.plugin.uexTaskSubmit.DrawView.PictureTagLayout;
import org.zywx.wbpalmstar.plugin.uexTaskSubmit.Interface.CallbackFloatBallMarkbugsActivity;
import org.zywx.wbpalmstar.plugin.uexTaskSubmit.JSONBean.CreateResourcePost;
import org.zywx.wbpalmstar.plugin.uexTaskSubmit.JSONBean.CreateResourcePostResponse;
import org.zywx.wbpalmstar.plugin.uexTaskSubmit.JSONBean.CreateTaskPost;
import org.zywx.wbpalmstar.plugin.uexTaskSubmit.JSONBean.CreateTaskPostResponse;
import org.zywx.wbpalmstar.plugin.uexTaskSubmit.JSONBean.DeviceInfo;
import org.zywx.wbpalmstar.plugin.uexTaskSubmit.JSONBean.PrjId;
import org.zywx.wbpalmstar.plugin.uexTaskSubmit.errorcatch.LogCatch;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.view.animation.BounceInterpolator;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

/**
 * 专门为MarkbugsActivity定制的浮动小球类
 * 
 * @author waka
 *
 */

public class FloatBallMarkbugsView implements OnTouchListener, OnClickListener {
	// 标识Handler的what，用以区分线程
	private static int HANDLER_WHAT_PRJID = 0;
	private static int HANDLER_WHAT_CREATE_RESOURCE = 1;
	private static int HANDLER_WHAT_CREATE_TASK = 2;

	private Context context;
	private MarkbugsActivity markbugsActivity;
	private static final String TAG = "FloatBallMarkbugsActivity";// logcat标识
	private boolean CLICK_FLAG = false;// 浮动小球点击开关
	private WindowManager mWindowManager;
	private WindowManager.LayoutParams wmParams;
	private LayoutInflater layoutInflater;
	private LinearLayout floatLayout;
	private Button btnFloat, btnScreenShot, btnLogin;
	int paramX, paramY;
	int lastX, lastY;
	private PrjId prjId;// 根据appId获取对应的项目ID
	private CreateTaskPostResponse createTaskPostResponse;// 创建任务返回的JSON
	private DeviceInfo deviceInfo;// 设备信息
	private String fileName = "";// 新生成的截图的文件路径名
	private ProgressDialog progressDialog;// 进度条

	/**
	 * Handler异步处理
	 */
	@SuppressLint("HandlerLeak")
	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			// 获取prjId线程的异步处理
			if (msg.what == HANDLER_WHAT_PRJID) {
				// 如果获取prjId成功
				if (prjId.status.equals("success")) {
					if (fileName.equals("")) {
						fileName = screenShot();// 如果之前不存在截图，直接截屏
					} else {
						deleteFile(fileName);
						fileName = screenShot();// 如果之前存在截图，先删掉之前的，再进行截屏
					}

					// 新建线程,创建资源,createResourcePost
					new Thread() {
						public void run() {
							// 如果文件名不为空，截屏存在
							if (!fileName.equals("")) {

								// screenShot（截屏）资源
								CreateResourcePost screenShotPost = new CreateResourcePost();
								screenShotPost.file = fileName;
								screenShotPost.parentId = "-1";
								screenShotPost.projectId = "" + prjId.message;

								// TODO 新增的，log（logcat日志）资源
								CreateResourcePost logPost = null;
								EUExTaskSubmit.logFileName = LogCatch.getLog();// 获得保存log的文件名
								if (!EUExTaskSubmit.logFileName.isEmpty()) {
									logPost = new CreateResourcePost();
									logPost.file = EUExTaskSubmit.logFileName;
									logPost.parentId = "-1";
									logPost.projectId = "" + prjId.message;
								}

								// TODO 新增的，crash（崩溃日志）资源
								CreateResourcePost crashPost = null;
								if (!EUExTaskSubmit.lastCrashFileName.isEmpty()) {
									crashPost = new CreateResourcePost();
									crashPost.file = EUExTaskSubmit.lastCrashFileName;
									crashPost.parentId = "-1";
									crashPost.projectId = "" + prjId.message;
								}

								/* 创建资源，并将Response放到map中 */
								Map<String, CreateResourcePostResponse> map = new HashMap<String, CreateResourcePostResponse>();// 使用map存储对象
								CreateResourcePostResponse screenShotResponse = HttpRequest
										.createResource(markbugsActivity, screenShotPost);
								map.put(Constant.CREATE_RESOURCE_POST_SCREEN_SHOT, screenShotResponse);

								// 如果log日志文件存在
								if (logPost != null) {
									CreateResourcePostResponse logResponse = HttpRequest
											.createResource(markbugsActivity, logPost);// 进行网络请求上传崩溃日志资源
									map.put(Constant.CREATE_RESOURCE_POST_LOG, logResponse);// 将crashResponse放入map中
								}

								// 如果崩溃日志文件存在
								if (crashPost != null) {
									CreateResourcePostResponse crashResponse = HttpRequest
											.createResource(markbugsActivity, crashPost);// 进行网络请求上传崩溃日志资源
									map.put(Constant.CREATE_RESOURCE_POST_CRASH, crashResponse);// 将crashResponse放入map中
								}

								Message message = Message.obtain(handler);
								message.what = HANDLER_WHAT_CREATE_RESOURCE;
								message.obj = map;
								message.sendToTarget();
							}
						};
					}.start();
				} else {
					progressDialog.dismiss();
					Toast.makeText(context, "获取工程ID失败！", Toast.LENGTH_SHORT).show();
				}
			}
			// createResource线程的异步处理
			else if (msg.what == HANDLER_WHAT_CREATE_RESOURCE) {

				/* 获得传来的map对象，取得"截屏资源response"和"崩溃日志资源response" */
				@SuppressWarnings("unchecked")
				Map<String, CreateResourcePostResponse> map = (Map<String, CreateResourcePostResponse>) msg.obj;

				// 如果map里有screenShot截屏，其实截屏Response一定会有
				CreateResourcePostResponse screenShotResponse = null;
				if (map.containsKey(Constant.CREATE_RESOURCE_POST_SCREEN_SHOT)) {
					screenShotResponse = map.get(Constant.CREATE_RESOURCE_POST_SCREEN_SHOT);
				}

				// 如果map里有log日志
				CreateResourcePostResponse logResponse = null;
				if (map.containsKey(Constant.CREATE_RESOURCE_POST_LOG)) {
					logResponse = map.get(Constant.CREATE_RESOURCE_POST_LOG);
				}

				// 如果map里有崩溃日志
				CreateResourcePostResponse crashResponse = null;
				if (map.containsKey(Constant.CREATE_RESOURCE_POST_CRASH)) {
					crashResponse = map.get(Constant.CREATE_RESOURCE_POST_CRASH);
				}

				if (screenShotResponse.status.equals("success")) {

					Log.i(TAG, "markbugsActivity.createTaskPost---->>>>" + markbugsActivity.createTaskPost.toString());
					final CreateTaskPost createTaskPost = markbugsActivity.createTaskPost;
					createTaskPost.resource = screenShotResponse.message.id;
					// 添加log日志资源id____进任务资源id组
					if (logResponse != null) {
						createTaskPost.resource = createTaskPost.resource + "," + logResponse.message.id;
					}
					// 添加崩溃日志资源id____进任务资源id组
					if (crashResponse != null) {
						createTaskPost.resource = createTaskPost.resource + "," + crashResponse.message.id;
					}
					addDeviceInfo(createTaskPost);
					Log.i(TAG, "createTaskPostResponse---->>>>" + createTaskPost.toString());
					// 新建线程访问网络，创建任务，createTaskPost
					new Thread() {
						public void run() {
							CreateTaskPostResponse createTaskPostResponse = HttpRequest.createTask(context,
									createTaskPost);
							Message message = Message.obtain(handler);
							message.what = HANDLER_WHAT_CREATE_TASK;
							message.obj = createTaskPostResponse;
							message.sendToTarget();
						};
					}.start();
				} else {
					progressDialog.dismiss();
					Toast.makeText(context, "创建资源失败！", Toast.LENGTH_SHORT).show();
				}

			}
			// createTask线程的异步处理
			else if (msg.what == HANDLER_WHAT_CREATE_TASK) {
				progressDialog.dismiss();
				createTaskPostResponse = (CreateTaskPostResponse) msg.obj;
				if (createTaskPostResponse.status.equals("success")) {
					Toast.makeText(context, "上传任务成功！", Toast.LENGTH_SHORT).show();

					// 删除截图
					deleteFile(fileName);

					// 删除log日志
					deleteFile(EUExTaskSubmit.logFileName);

					// 把上一次崩溃的日志文件名存到SharedPreferences中的已上传过的崩溃日志文件名字段中，即标记已传过，再不传了
					SharedPreferences.Editor editor = context
							.getSharedPreferences(Constant.SHARED_PREFERENCES_TABLE_NAME, Context.MODE_PRIVATE).edit();
					editor.putString(Constant.SHARED_PREFERENCES_ALREADY_POST_KEY_NAME,
							EUExTaskSubmit.lastCrashFileName);
					editor.commit();
					EUExTaskSubmit.lastCrashFileName = "";// 置为空，避免当用户不退出、连续上传时，重复上传已上传过的崩溃日志的问题

					// 调用回调通知MarkbugsActivity
					markbugsActivity.callback(new CallbackFloatBallMarkbugsActivity() {
						@Override
						public void callback() {

						}
					});
				} else {
					Toast.makeText(context, "上传任务失败！", Toast.LENGTH_SHORT).show();
				}
			}
		};
	};

	/**
	 * 构造方法
	 * 
	 * @param context
	 */
	public FloatBallMarkbugsView(Context context) {
		this.context = context;
		markbugsActivity = (MarkbugsActivity) context;
	}

	/**
	 * 创建浮动小球
	 */
	@SuppressWarnings("deprecation")
	public void creatFloatBall() {
		// 获得WindowManager和LayoutParams
		mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		wmParams = new WindowManager.LayoutParams();
		// 初始化LayoutParams属性
		wmParams.type = LayoutParams.TYPE_PHONE;// 设置类型为手机
		wmParams.format = PixelFormat.RGBA_8888;// 设置为透明
		wmParams.flags = LayoutParams.FLAG_NOT_FOCUSABLE;// 设置不可获得焦点
		wmParams.gravity = Gravity.LEFT | Gravity.TOP;// 位置为左侧置顶
		wmParams.x = 0;// 初始化x，默认左上角为0
		wmParams.y = 0;// 初始化y，默认左上角为0
		wmParams.width = LayoutParams.WRAP_CONTENT;// 设置宽
		wmParams.height = LayoutParams.WRAP_CONTENT;// 设置高

		// 获得布局，获得布局中的控件
		layoutInflater = LayoutInflater.from(context);// 从App中获得LayoutInflater
		floatLayout = (LinearLayout) layoutInflater
				.inflate(EUExUtil.getResLayoutID("plugin_task_submit_float_ball_layout"), null);
		btnFloat = (Button) floatLayout.findViewById(EUExUtil.getResIdID("plugin_task_submit_btnFloat"));
		btnFloat.setBackgroundResource(EUExUtil.getResDrawableID("plugin_task_submit_send"));
		btnScreenShot = (Button) floatLayout.findViewById(EUExUtil.getResIdID("plugin_task_submit_btnScreenShot"));
		btnLogin = (Button) floatLayout.findViewById(EUExUtil.getResIdID("plugin_task_submit_btnLogin"));
		btnScreenShot.setVisibility(View.GONE);
		btnLogin.setVisibility(View.GONE);

		// 向WindowManager中添加Layout和LayoutParams
		wmParams.x = mWindowManager.getDefaultDisplay().getWidth();// 屏幕宽度
		wmParams.y = mWindowManager.getDefaultDisplay().getHeight() / 3;
		mWindowManager.addView(floatLayout, wmParams);
		// btnFloat设置事件监听器
		btnFloat.setOnTouchListener(this);
		btnFloat.setOnClickListener(this);
		btnScreenShot.setOnClickListener(this);
		btnLogin.setOnClickListener(this);
	}

	/**
	 * 移除浮动小球
	 */
	public void removeFloatBall() {
		if (floatLayout != null) {
			mWindowManager.removeView(floatLayout);// 移除小球
		}
	}

	/**
	 * 浮动小球触摸事件
	 */
	@SuppressWarnings("deprecation")
	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		// 判断如果是btnFloat
		if (v.getId() == EUExUtil.getResIdID("plugin_task_submit_btnFloat")) {
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				btnFloat.setBackgroundResource(EUExUtil.getResDrawableID("plugin_task_submit_send_transparent"));
				// 获得按下的x，y
				lastX = (int) event.getRawX();
				lastY = (int) event.getRawY();
				paramX = wmParams.x;
				if (paramX != 0) {
					paramX = mWindowManager.getDefaultDisplay().getWidth() - btnFloat.getMeasuredWidth();
				}
				paramY = wmParams.y;
				break;
			case MotionEvent.ACTION_MOVE:
				wmParams.x = paramX + ((int) event.getRawX() - lastX);
				wmParams.y = paramY + ((int) event.getRawY() - lastY);
				mWindowManager.updateViewLayout(floatLayout, wmParams);// 更新View
				break;
			case MotionEvent.ACTION_UP:
				btnFloat.setBackgroundResource(EUExUtil.getResDrawableID("plugin_task_submit_send"));
				// 获得抬起的x，y
				int endX = (int) event.getRawX();
				int endY = (int) event.getRawY();

				/**
				 * 如果挪动的范围很小，则判定为单击
				 */
				if (Math.abs(endX - lastX) < 15 && Math.abs(endY - lastY) < 15) {
					// if (CLICK_FLAG == false) {
					// addFloatBallItems();// 添加小球选项
					// } else if (CLICK_FLAG == true) {
					// removeFloatBallItems();// 移除小球选项
					// }

					// 判断标签是否存在
					if (!PictureTagLayout.IS_SINGLE) {
						Toast.makeText(context, "请先标记!", Toast.LENGTH_SHORT).show();
					} else {
						Intent intent = new Intent(markbugsActivity, QuitDialogActivity.class);
						intent.putExtra("tips", EUExUtil.getString("plugin_task_submit_confirm_tips"));// "确定要提交吗？"
						markbugsActivity.startActivityForResult(intent, 4);// 4代表从QuitDialog(但是是确认发送对话框)传过来的result
					}
				} else {
					try {
						// 小球自动对齐到边框
						final int screenWidth = mWindowManager.getDefaultDisplay().getWidth();// 获取屏幕宽度
						ObjectAnimator animator = null;
						ObjectAnimator animator2 = null;
						AnimatorSet animatorSet = new AnimatorSet();
						if ((wmParams.x + floatLayout.getMeasuredWidth() / 2) <= screenWidth / 2) {
							// wmParams.x = 0;// 浮动到左边
							animator = ObjectAnimator.ofFloat(v, "", wmParams.x, 0);
							animator.addUpdateListener(new AnimatorUpdateListener() {

								@Override
								public void onAnimationUpdate(ValueAnimator animation) {
									wmParams.x = ((Float) animation.getAnimatedValue()).intValue();
									mWindowManager.updateViewLayout(floatLayout, wmParams);
								}
							});
							animator.setInterpolator(new BounceInterpolator());
							animator2 = ObjectAnimator.ofFloat(v, "rotation", 0, 360);
							animator2.setInterpolator(new BounceInterpolator());
							animatorSet.play(animator).with(animator2);
							animatorSet.setDuration(Constant.FLOAT_BALL_ANIMATION_TIME);
							animatorSet.start();
						} else {
							// wmParams.x = screenWidth -
							// btnFloat.getMeasuredWidth();//
							// 浮动到右边
							animator = ObjectAnimator.ofFloat(v, "", wmParams.x,
									screenWidth - btnFloat.getMeasuredWidth());
							animator.addUpdateListener(new AnimatorUpdateListener() {

								@Override
								public void onAnimationUpdate(ValueAnimator animation) {
									wmParams.x = ((Float) animation.getAnimatedValue()).intValue();
									mWindowManager.updateViewLayout(floatLayout, wmParams);
								}
							});
							animator.setInterpolator(new BounceInterpolator());
							animator2 = ObjectAnimator.ofFloat(v, "rotation", 360, 0);
							animator2.setInterpolator(new BounceInterpolator());
							animatorSet.play(animator).with(animator2);
							animatorSet.setDuration(Constant.FLOAT_BALL_ANIMATION_TIME);
							animatorSet.start();
						}
					} catch (IllegalArgumentException e) {
						Log.e(TAG, "IllegalArgumentException");
						e.printStackTrace();
					}
				}
				break;
			}
		}
		return true;// 使点击事件获取不到监听
	}

	/**
	 * 点击事件
	 */
	@Override
	public void onClick(View v) {
		Activity activity = (Activity) context;// 强转为Activity，因为只有activity有startActivityForResult方法
		// 浮动小球点击事件
		if (v.getId() == EUExUtil.getResIdID("plugin_task_submit_btnFloat")) {
			if (CLICK_FLAG == false) {
				addFloatBallItems();// 添加小球选项
			} else if (CLICK_FLAG == true) {
				removeFloatBallItems();// 移除小球选项
			}
		}
		// 截图按钮点击事件
		else if (v.getId() == EUExUtil.getResIdID("plugin_task_submit_btnScreenShot")) {
			if (PictureTagLayout.IS_SINGLE) {
				Intent intent1 = new Intent(activity, CreateTaskActivity.class);
				activity.startActivityForResult(intent1, 2);// 使用这个方法接收后CreateTaskActivity回传回来的数据
			} else {
				Toast.makeText(context, "请先标记！", Toast.LENGTH_SHORT).show();
			}
		}
		// 登录按钮点击事件
		else if (v.getId() == EUExUtil.getResIdID("plugin_task_submit_btnLogin")) {
			if (PictureTagLayout.IS_SINGLE) {
				Intent intent = new Intent(context, LoginDialogActivity.class);
				context.startActivity(intent);
			} else {
				Toast.makeText(context, "请先标记！", Toast.LENGTH_SHORT).show();
			}

		}
	}

	/**
	 * 添加小球选项
	 */
	public void addFloatBallItems() {
		btnScreenShot.setVisibility(View.VISIBLE);
		btnLogin.setVisibility(View.VISIBLE);
		CLICK_FLAG = true;
	}

	/**
	 * 移除小球选项
	 */
	public void removeFloatBallItems() {
		btnScreenShot.setVisibility(View.GONE);
		btnLogin.setVisibility(View.GONE);
		CLICK_FLAG = false;
	}

	/**
	 * 截屏，将截图写入sdcard根目录
	 */
	public String screenShot() {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.US);
		String fileName = EUExTaskSubmit.imgPath + "/" + simpleDateFormat.format(new Date()) + ".png";
		Activity activity = (Activity) context;
		View view = activity.getWindow().getDecorView();
		view.setDrawingCacheEnabled(true);
		view.buildDrawingCache();
		Bitmap bitmap = view.getDrawingCache();
		// 将图片写入sdcard根目录
		if (bitmap != null) {
			Log.i(TAG, "bitmap got!");
			try {
				FileOutputStream out = new FileOutputStream(fileName);
				bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
				return fileName;
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (OutOfMemoryError e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			Log.i(TAG, "bitmap is null!");
		}
		return "";
	}

	/**
	 * 删除文件
	 * 
	 * @param fileName
	 */
	private boolean deleteFile(String filePath) {
		File file = new File(filePath);
		if (file.exists()) {
			return file.delete();
		} else {
			Toast.makeText(context, "找不到文件", Toast.LENGTH_SHORT).show();
			return false;
		}
	}

	/**
	 * 添加设备信息
	 * 
	 * @param createTaskPost
	 */

	private void addDeviceInfo(CreateTaskPost createTaskPost) {
		// 初始化
		deviceInfo = DeviceInfo.getIntance(context);
		// 在createTaskPost.detail属性的末尾加上DeviceInfo，使用字符串拼接————————那可真蠢！！
		createTaskPost.detail = createTaskPost.detail + "\n" + "\n" + "android版本：" + deviceInfo.androidVersion + "\n"
				+ "设备型号：" + deviceInfo.deviceModle + "\n" + "屏幕分辨率：" + deviceInfo.resolution + "\n";
	}

	/**
	 * 开始传送
	 */
	public void startPost() {
		progressDialog = ProgressDialog.show(markbugsActivity, "正在上传...", "请稍后...", true, false);// 进度条。。。不过好像没什么用
		// 新建线程获取pijId
		new Thread() {
			@Override
			public void run() {
				String appId = EUExTaskSubmit.appId;
				prjId = HttpRequest.getPrjId(context, appId);
				Log.i(TAG, "prjId" + prjId.message);
				Message message = Message.obtain(handler);
				message.what = HANDLER_WHAT_PRJID;
				message.obj = prjId;
				message.sendToTarget();
			}
		}.start();
	}
}
