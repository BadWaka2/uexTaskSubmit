package org.zywx.wbpalmstar.plugin.uexTaskSubmit.DrawView;

import org.zywx.wbpalmstar.engine.universalex.EUExUtil;

import android.content.Context;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

/**
 * 标签视图，继承RelativeLayout
 * 
 * @author unknown
 *
 */
public class PictureTagView extends RelativeLayout implements OnEditorActionListener {

	private Context context;
	private TextView tvPictureTagLabel;
	private EditText etPictureTagLabel;
	private View markLeft, markRight, frameLeft, frameRight;
	// private View loTag;
	private int leftsize = 0;
	private int rightsize = 0;
	private LinearLayout.LayoutParams leftLp, rightLp;

	public enum Status {
		Normal, Edit
	}// 枚举，代表状态

	public enum Direction {
		Left, Right
	}// 枚举，代表左右

	public Direction direction = Direction.Left;// 默认为左
	private InputMethodManager imm;// 软键盘管理器，用于隐藏键盘
	private static final int ViewWidth = 80;// 常量，View的宽和高
	private static final int ViewHeight = 50;

	/**
	 * 构造方法
	 * 
	 * @param context
	 * @param direction
	 */
	public PictureTagView(Context context, Direction direction) {
		super(context);
		this.context = context;
		this.direction = direction;
		initViews();
		initData();
		init();
		initEvents();
	}

	private void initData() {
		leftsize = dp2px(context, 6);
		rightsize = dp2px(context, 3);
		leftLp = (android.widget.LinearLayout.LayoutParams) frameLeft.getLayoutParams();
		rightLp = (android.widget.LinearLayout.LayoutParams) frameRight.getLayoutParams();
	}

	/** 初始化视图 **/
	protected void initViews() {
		LayoutInflater.from(context).inflate(EUExUtil.getResLayoutID("plugin_task_submit_picturetagview"), this, true);
		tvPictureTagLabel = (TextView) findViewById(EUExUtil.getResIdID("plugin_task_submit_tvPictureTagLabel"));
		etPictureTagLabel = (EditText) findViewById(EUExUtil.getResIdID("plugin_task_submit_etPictureTagLabel"));
		// loTag =
		// findViewById(EUExUtil.getResIdID("plugin_task_submit_loTag"));
		markLeft = findViewById(EUExUtil.getResIdID("plugin_task_submit_loTag_markleft"));
		markRight = findViewById(EUExUtil.getResIdID("plugin_task_submit_loTag_markright"));
		frameLeft = findViewById(EUExUtil.getResIdID("plugin_task_submit_loTag_frameleft"));
		frameRight = findViewById(EUExUtil.getResIdID("plugin_task_submit_loTag_frameright"));
	}

	/** 初始化 **/
	protected void init() {
		imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
		directionChange();
	}

	/** 初始化事件 **/
	protected void initEvents() {
		etPictureTagLabel.setOnEditorActionListener(this);
	}

	/** 设置状态标志status **/
	public void setStatus(Status status) {
		switch (status) {
		// 如果为正常，Normal
		case Normal:
			tvPictureTagLabel.setVisibility(View.VISIBLE);// 设置textView可见
			etPictureTagLabel.clearFocus();// 清除editText焦点
			tvPictureTagLabel.setText(etPictureTagLabel.getText());// 设置文本为默认文本“标签”
			etPictureTagLabel.setVisibility(View.GONE);// 设置editText为不可见且不占位置
			// 隐藏键盘
			imm.hideSoftInputFromWindow(etPictureTagLabel.getWindowToken(), 0);
			break;

		// 如果为编辑，Edit
		case Edit:
			tvPictureTagLabel.setVisibility(View.GONE);// 设置textView为不可见且不占位置
			etPictureTagLabel.setVisibility(View.VISIBLE);// 设置editText可见
			etPictureTagLabel.requestFocus();// editText接受焦点
			// 弹出键盘
			imm.toggleSoftInput(0, InputMethodManager.SHOW_FORCED);
			break;
		}
	}

	@Override
	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		setStatus(Status.Normal);
		return true;
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
		// View parent = (View) getParent();
		// int halfParentW = (int) (parent.getWidth() * 0.5);
		// int center = (int) (l + (this.getWidth() * 0.5));
		// 屏蔽自动变更方向功能
		// if (center <= halfParentW) {
		// direction = Direction.Left;
		// } else {
		// direction = Direction.Right;
		// }
		directionChange();
	}

	/**
	 * 改变布局显示方向
	 */
	public void directionChange() {
		switch (direction) {
		case Left:
			markLeft.setVisibility(View.VISIBLE);
			markRight.setVisibility(View.GONE);
			frameLeft.setBackgroundResource(EUExUtil.getResDrawableID("plugin_task_submit_m_left"));
			frameRight.setBackgroundResource(EUExUtil.getResDrawableID("plugin_task_submit_m_right"));
			tvPictureTagLabel.setBackgroundResource(EUExUtil.getResDrawableID("plugin_task_submit_m_center"));
			leftLp.width = leftsize;
			rightLp.width = rightsize;
			// loTag.setBackgroundResource(EUExUtil.getResDrawableID("plugin_task_submit_bg_picturetagview_tagview_left"));
			break;
		case Right:
			markRight.setVisibility(View.VISIBLE);
			markLeft.setVisibility(View.GONE);
			frameLeft.setBackgroundResource(EUExUtil.getResDrawableID("plugin_task_submit_m_left_2"));
			frameRight.setBackgroundResource(EUExUtil.getResDrawableID("plugin_task_submit_m_right_2"));
			tvPictureTagLabel.setBackgroundResource(EUExUtil.getResDrawableID("plugin_task_submit_m_center_2"));
			leftLp.width = rightsize;
			rightLp.width = leftsize;
			// loTag.setBackgroundResource(EUExUtil.getResDrawableID("plugin_task_submit_bg_picturetagview_tagview_right"));
			break;
		}
	}

	public static int getViewWidth() {
		return ViewWidth;
	}

	public static int getViewHeight() {
		return ViewHeight;
	}

	private static int dp2px(Context context, float dp) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dp * scale + 0.5f);
	}
}
