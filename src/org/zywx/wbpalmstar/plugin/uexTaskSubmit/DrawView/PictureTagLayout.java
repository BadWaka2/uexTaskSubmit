package org.zywx.wbpalmstar.plugin.uexTaskSubmit.DrawView;

import org.zywx.wbpalmstar.plugin.uexTaskSubmit.Activitys.CreateTaskActivity;
import org.zywx.wbpalmstar.plugin.uexTaskSubmit.Activitys.LoginDialogActivity;
import org.zywx.wbpalmstar.plugin.uexTaskSubmit.DrawView.PictureTagView.Direction;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.RelativeLayout;

/**
 * 标签布局，继承RelativeLayout
 * 
 * @author unknown
 *
 */
public class PictureTagLayout extends RelativeLayout implements OnTouchListener {
	public static boolean IS_SINGLE = false;// 设置标签只能存在一个的标识
	public static boolean IS_LOGIN = false;// 是否登录的标记，静态变量
	// private static final String TAG = "PictureTagLayout";
	private Context context;// 需要与MarkbugsActivity进行通信
	private boolean TOUCH_VIEW_DIRECTION = false;// 默认为false，代表方向为左
	int startX = 0;
	int startY = 0;
	int startTouchViewLeft = 0;
	int startTouchViewTop = 0;
	private View touchView, view;

	public PictureTagLayout(Context context) {
		super(context, null);
	}

	/**
	 * 在xml文件使用自定义布局时必须必须要写的构造方法
	 * 
	 * @param context
	 * @param attrs
	 */
	public PictureTagLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
		init();
	}

	@SuppressLint("ClickableViewAccessibility")
	private void init() {
		this.setOnTouchListener(this);
	}

	/**
	 * 触摸事件
	 */
	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			startX = (int) event.getX();
			startY = (int) event.getY();
			// 判断如果存在当前数模位置存在View，获得左上角坐标值
			if (hasView(startX, startY)) {
				startTouchViewLeft = touchView.getLeft();
				startTouchViewTop = touchView.getTop();
			} else {
				// 如果IS_SINGLE为false，添加，一次只能添加一个标签
				if (!IS_SINGLE) {
					addItem(startX, startY);
					IS_SINGLE = true;
					Activity activity = (Activity) context;// 强转为Activity，因为只有activity有startActivityForResult方法

					// 如果未登录，跳转到LoginDialogActivity
					if (IS_LOGIN == false) {
						Intent intent = new Intent(activity, LoginDialogActivity.class);
						activity.startActivityForResult(intent, 1);// 使用这个方法接收后LoginDialogActivity回传回来的数据

					}
					// 如果已登录，跳转到CreateTaskActivity
					else {
						Intent intent = new Intent(activity, CreateTaskActivity.class);
						activity.startActivityForResult(intent, 2);// 使用这个方法接收后CreateTaskActivity回传回来的数据

					}
				}
			}
			break;
		case MotionEvent.ACTION_MOVE:
			moveView((int) event.getX(), (int) event.getY());
			break;
		case MotionEvent.ACTION_UP:
			int endX = (int) event.getX();
			int endY = (int) event.getY();

			/**
			 * 如果挪动的范围很小，则判定为单击
			 */
			if (touchView != null && Math.abs(endX - startX) < 5 && Math.abs(endY - startY) < 5) {
				// 如果为当前方向标识为左
				if (TOUCH_VIEW_DIRECTION == false) {
					((PictureTagView) touchView).direction = Direction.Right;// touchView方向置右
					((PictureTagView) touchView).directionChange();// 调用改变方向的方法
					TOUCH_VIEW_DIRECTION = true;// 将当前方向标识置右
				}
				// 如果为当前方向标识为右
				else {
					((PictureTagView) touchView).direction = Direction.Left;// touchView方向置左
					((PictureTagView) touchView).directionChange();// 调用改变方向的方法
					TOUCH_VIEW_DIRECTION = false;// 将当前方向标识置左
				}

			}
			break;
		}
		return true;
	}

	/**
	 * 添加标签项
	 * 
	 * @param x
	 * @param y
	 */
	private void addItem(int x, int y) {
		view = null;
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
		if (x > getWidth() * 0.5) {
			params.leftMargin = x - PictureTagView.getViewWidth();
			view = new PictureTagView(getContext(), Direction.Right);
		} else {
			params.leftMargin = x;
			view = new PictureTagView(getContext(), Direction.Left);
		}
		params.topMargin = y;
		// 上下位置在视图内
		if (params.topMargin < 0)
			params.topMargin = 0;
		else if ((params.topMargin + PictureTagView.getViewHeight()) > getHeight())
			params.topMargin = getHeight() - PictureTagView.getViewHeight();
		this.addView(view, params);
	}

	private void moveView(int x, int y) {
		if (touchView == null)
			return;
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
		params.leftMargin = x - startX + startTouchViewLeft;
		params.topMargin = y - startY + startTouchViewTop;
		// 限制子控件移动必须在视图范围内
		if (params.leftMargin < 0 || (params.leftMargin + touchView.getWidth()) > getWidth())
			params.leftMargin = touchView.getLeft();
		if (params.topMargin < 0 || (params.topMargin + touchView.getHeight()) > getHeight())
			params.topMargin = touchView.getTop();
		touchView.setLayoutParams(params);
	}

	/**
	 * 判断是否在当前触摸位置存在View
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	private boolean hasView(int x, int y) {
		// 循环获取子view，判断xy是否在子view上，即判断是否按住了子view
		for (int index = 0; index < this.getChildCount(); index++) {
			View view = this.getChildAt(index);
			int left = (int) view.getX();
			int top = (int) view.getY();
			int right = view.getRight();
			int bottom = view.getBottom();
			Rect rect = new Rect(left, top, right, bottom);
			boolean contains = rect.contains(x, y);
			// 如果是与子view重叠则返回真,表示已经有了view不需要添加新view了
			if (contains) {
				touchView = view;
				touchView.bringToFront();
				return true;
			}
		}
		touchView = null;
		return false;
	}

	public void removeView() {
		this.removeView(view);
	}

}
