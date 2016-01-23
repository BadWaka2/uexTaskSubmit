package org.zywx.wbpalmstar.plugin.uexTaskSubmit;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.zywx.wbpalmstar.engine.universalex.EUExUtil;
import org.zywx.wbpalmstar.plugin.uexTaskSubmit.Activitys.MarkbugsActivity;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.view.animation.BounceInterpolator;
import android.widget.Button;
import android.widget.LinearLayout;

/**
 * 浮动小球类
 * 
 * @author waka
 *
 */

public class FloatBall implements OnTouchListener {
	private Context context;
	private static final String TAG = "FloatBall";// logcat标识
	@SuppressWarnings("unused") // 没有用到浮动小球开关
	private boolean CLICK_FLAG = false;// 浮动小球点击开关
	private WindowManager mWindowManager;
	private WindowManager.LayoutParams wmParams;
	private LayoutInflater layoutInflater;
	private LinearLayout floatLayout;
	private Button btnFloat, btnScreenShot, btnLogin;
	int paramX, paramY;
	int lastX, lastY;

	/**
	 * 构造方法
	 * 
	 * @param context
	 */
	public FloatBall(Context context) {
		this.context = context;
	}

	/**
	 * 创建浮动小球
	 */
	@SuppressWarnings("deprecation") // 获取屏幕宽度和高度的方法过时
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
	@SuppressLint({ "ClickableViewAccessibility", "NewApi" })
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		// 判断如果是btnFloat
		if (v.getId() == EUExUtil.getResIdID("plugin_task_submit_btnFloat")) {
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				btnFloat.setBackgroundResource(EUExUtil.getResDrawableID("plugin_task_submit_task_transparent"));
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
				btnFloat.setBackgroundResource(EUExUtil.getResDrawableID("plugin_task_submit_task"));

				// 获得抬起的x，y
				int endX = (int) event.getRawX();
				int endY = (int) event.getRawY();

				/**
				 * 如果挪动的范围很小，则判定为单击
				 */
				if (Math.abs(endX - lastX) < 15 && Math.abs(endY - lastY) < 15) {
					screenShot();// 截屏并跳转
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
	 * 截屏，将截图写入sdcard根目录，并跳转到MarkbugsActivity
	 */
	public void screenShot() {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.US);
		String fileName = EUExTaskSubmit.imgPath + "/" + simpleDateFormat.format(new Date()) + ".png";
		File file = new File(EUExTaskSubmit.imgPath);
		if (!file.exists()) {
			file.mkdirs();// 如果不存在，则创建所有的父文件夹
		}
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
				// Toast.makeText(context, fileName, Toast.LENGTH_SHORT).show();

				// if(!bitmap.isRecycled()){
				// bitmap.recycle();
				// }
				// 跳转到MarkbugsActivity（标记bug），需传递一个图片地址
				Intent intent = new Intent(context, MarkbugsActivity.class);
				// intent.putExtra("screenShot",
				// bitmap);//直接传bitmap不行，图片的size不能超过40k
				intent.putExtra("fileName", fileName);// 传递图片保存路径
				context.startActivity(intent);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			} catch (OutOfMemoryError e) {
				e.printStackTrace();
			}
		} else {
			Log.i(TAG, "bitmap is null!");
		}
	}

}
