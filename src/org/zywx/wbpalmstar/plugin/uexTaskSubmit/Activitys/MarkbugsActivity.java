package org.zywx.wbpalmstar.plugin.uexTaskSubmit.Activitys;

import java.io.File;

import org.zywx.wbpalmstar.engine.universalex.EUExUtil;
import org.zywx.wbpalmstar.plugin.uexTaskSubmit.FloatBallMarkbugsView;
import org.zywx.wbpalmstar.plugin.uexTaskSubmit.DrawView.PictureTagLayout;
import org.zywx.wbpalmstar.plugin.uexTaskSubmit.Interface.CallbackFloatBallMarkbugsActivity;
import org.zywx.wbpalmstar.plugin.uexTaskSubmit.JSONBean.CreateResourcePost;
import org.zywx.wbpalmstar.plugin.uexTaskSubmit.JSONBean.CreateTaskPost;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Window;
import android.widget.TextView;

/**
 * 标记BugActivity
 * 
 * @author waka
 *
 */
public class MarkbugsActivity extends Activity {
	private static PictureTagLayout layoutScreenShot;// 背景自定义布局
	private static final String TAG = "MarkbugsActivity";// Log标记
	private String fileName_previous = "";// 传递过来的文件位置
	public CreateTaskPost createTaskPost;// 要上传到服务器上的数据
	public CreateResourcePost createResourcePost;// 要上传到服务器上的资源
	private FloatBallMarkbugsView floatBallMarkbugsActivity;// 专为Markbugs定制的浮动小球

	/**
	 * onCreate
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);// 不显示标题栏
		setContentView(EUExUtil.getResLayoutID("plugin_task_submit_activity_markbugs"));
		initView();
		initDatas();
		initEvent();
		loadBitmap();
	}

	/**
	 * 重写onResume()方法，在这里创建小球
	 */
	@Override
	protected void onResume() {
		super.onResume();
		floatBallMarkbugsActivity.creatFloatBall();
	}

	/**
	 * 重写onPause()方法，在这里移除小球
	 */
	@Override
	protected void onPause() {
		super.onPause();
		floatBallMarkbugsActivity.removeFloatBall();
	}

	/**
	 * onDestroy，在销毁时删除图片
	 */
	@Override
	protected void onDestroy() {
		deleteImageFile(fileName_previous);
		super.onDestroy();
	}

	/**
	 * 初始化控件
	 */
	private void initView() {
		// TODO 故意不初始化，让程序崩溃
		layoutScreenShot = (PictureTagLayout) findViewById(EUExUtil.getResIdID("plugin_task_submit_layoutScreenShot"));
		floatBallMarkbugsActivity = new FloatBallMarkbugsView(this);// 初始化小球
		PictureTagLayout.IS_SINGLE = false;
	}

	/**
	 * 初始化数据
	 */
	private void initDatas() {
		createTaskPost = new CreateTaskPost();// 创建一个用于上传的数据Bean
		// 接收Intent传递过来的文件路径
		Intent intent = getIntent();
		if (intent != null) {
			String fileName = intent.getStringExtra("fileName");
			fileName_previous = fileName;
			Log.i(TAG, fileName);
		}
	}

	/**
	 * 初始化事件
	 */
	private void initEvent() {

	}

	/**
	 * 重写后退键，在退出该Activity时删除图片
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (PictureTagLayout.IS_SINGLE) {
				Intent intent = new Intent(MarkbugsActivity.this, QuitDialogActivity.class);
				intent.putExtra("tips", EUExUtil.getString("plugin_task_submit_quit_post_tips"));
				startActivityForResult(intent, 3);// 3代表从QuitDialog传过来的result
			} else {
				deleteImageFile(fileName_previous);
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	/**
	 * 读取图片数据，将layout背景赋为传过来的bitmap
	 */
	@SuppressWarnings("deprecation")
	private void loadBitmap() {
		// 读取图片数据
		Bitmap bitmap = null;
		if (bitmap == null) {
			try {
				bitmap = BitmapFactory.decodeFile(fileName_previous, null);// 根据路径读取图片
				// 裁剪图片
				int width = bitmap.getWidth(); // 得到图片的宽，高
				int height = bitmap.getHeight();
				int retX = 0;// 基于原图，取正方形左上角x坐标
				int retY = 75;
				Bitmap bitmap2 = Bitmap.createBitmap(bitmap, retX, retY, width, height - 75, null, false);
				// 将layoutBitmap的背景设置为传递过来的bitmap
				BitmapDrawable bitmapDrawable = new BitmapDrawable(bitmap2);// 将bitmap转换为drawable对象
				layoutScreenShot.setBackgroundDrawable(bitmapDrawable);
			} catch (Exception e) {
				e.printStackTrace();
			} catch (OutOfMemoryError e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 在这里处理接收CreateTaskActivity回传的数据
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		// 2代表从LoginDialogActivity传回的数据
		case 1:
			if (resultCode == RESULT_OK) {
				Bundle bundle = data.getExtras();// 获得Bundle
				createTaskPost = bundle.getParcelable("createTaskPost");
				TextView textView = (TextView) findViewById(
						EUExUtil.getResIdID("plugin_task_submit_tvPictureTagLabel"));
				textView.setText(createTaskPost.name);
				Log.i(TAG, createTaskPost.toString());
			}
			break;
		// 2代表从CreateTaskActivity传回的数据
		case 2:
			if (resultCode == RESULT_OK) {
				Bundle bundle = data.getExtras();// 获得Bundle
				createTaskPost = bundle.getParcelable("createTaskPost");
				TextView textView = (TextView) findViewById(
						EUExUtil.getResIdID("plugin_task_submit_tvPictureTagLabel"));
				textView.setText(createTaskPost.name);
			}
			break;
		// 3代表从QuitDialog(确认退出编辑对话框)传过来的result
		case 3:
			if (resultCode == RESULT_OK) {
				MarkbugsActivity.removeView();// 移除标签View
				deleteImageFile(fileName_previous);
				finish();
			} else if (resultCode == RESULT_CANCELED) {

			}
			break;
		// 4代表从QuitDialog(但是是确认发送对话框)传过来的result
		case 4:
			if (resultCode == RESULT_OK) {
				floatBallMarkbugsActivity.startPost();// 开始传送
			} else if (resultCode == RESULT_CANCELED) {
				MarkbugsActivity.removeView();// 移除标签View
				deleteImageFile(fileName_previous);
				finish();
			}
			break;

		default:
			break;
		}
	}

	/**
	 * 移除标记view 静态方法
	 */
	public static void removeView() {
		layoutScreenShot.removeView();
		PictureTagLayout.IS_SINGLE = false;
	}

	/**
	 * 删除图片文件
	 * 
	 * @param fileName
	 */
	private boolean deleteImageFile(String filePath) {
		File file = new File(filePath);
		if (file.exists()) {
			return file.delete();
		} else {
			// Toast.makeText(this, "找不到文件", Toast.LENGTH_SHORT).show();
			return false;
		}
	}

	/**
	 * 回调FloatBallMarkbugsActivity，在这里处理
	 * 
	 * @param myInterface
	 */
	public void callback(CallbackFloatBallMarkbugsActivity myInterface) {
		myInterface.callback();
		deleteImageFile(fileName_previous);
		finish();
	}

	/**
	 * 启动MarkbugsActivity
	 * 
	 * @param context
	 * @param fileName
	 */
	public static void actionStart(Context context, String fileName) {
		Intent intent = new Intent(context, MarkbugsActivity.class);
		intent.putExtra("fileName", fileName);
		context.startActivity(intent);
	}
}
