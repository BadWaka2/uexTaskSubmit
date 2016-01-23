package org.zywx.wbpalmstar.plugin.uexTaskSubmit.Activitys;

import java.util.Calendar;
import org.zywx.wbpalmstar.engine.universalex.EUExUtil;
import org.zywx.wbpalmstar.plugin.uexTaskSubmit.EUExTaskSubmit;
import org.zywx.wbpalmstar.plugin.uexTaskSubmit.HttpRequest;
import org.zywx.wbpalmstar.plugin.uexTaskSubmit.JSONBean.CreateTaskPost;
import org.zywx.wbpalmstar.plugin.uexTaskSubmit.JSONBean.MobilePrjMembers;
import org.zywx.wbpalmstar.plugin.uexTaskSubmit.JSONBean.MobileProcess;
import org.zywx.wbpalmstar.plugin.uexTaskSubmit.util.TaskSubmitUtils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 创建任务Activity
 * 
 * @author waka
 *
 */
public class CreateTaskActivity extends Activity implements OnClickListener {
	private static final String TAG = "CreateTaskActivity";
	private TextView tvPriorityTips, tvProcessTips, tvTagTips, tvDeadlineTips, tvLeaderTips;
	private EditText etName, etDetail;
	private InputMethodManager inputMethodManager;
	private RelativeLayout layoutPriority, layoutProcess, layoutTag, layoutDeadline, layoutLeader;
	private Button btnConfirm, btnCancel;
	private MobileProcess mobileProcess;// 根据appId获取流程列表
	private MobilePrjMembers mobilePrjMembers;// 根据appId获取项目正式成员
	private CreateTaskPost createTaskPost;
	private String[] itemsPriority = { "NORMAL", "URGENT", "VERY_URGENT" };
	private String itemsMemberId = "";
	private ProgressDialog progressDialog;
	private boolean isLoading = false;

	@SuppressLint("HandlerLeak")
	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			// Toast.makeText(CreateTaskActivity.this, "获取数据完成",
			// Toast.LENGTH_SHORT).show();
			isLoading = false;
			if (progressDialog != null)
				progressDialog.dismiss();
		};
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(EUExUtil.getResLayoutID("plugin_task_submit_activity_create_task"));
		initView();
		initData();
		initEvent();
	}

	/**
	 * 重写后退按钮
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			Intent intent = new Intent(CreateTaskActivity.this, QuitDialogActivity.class);
			intent.putExtra("tips", EUExUtil.getString("plugin_task_submit_quit_tips"));
			startActivityForResult(intent, 3);// 3代表从QuitDialog传过来的result
		}
		return super.onKeyDown(keyCode, event);
	}

	/**
	 * 初始化控件
	 */
	private void initView() {
		tvPriorityTips = (TextView) findViewById(EUExUtil.getResIdID("plugin_task_submit_tvPriorityTips"));
		tvProcessTips = (TextView) findViewById(EUExUtil.getResIdID("plugin_task_submit_tvProcessTips"));
		tvTagTips = (TextView) findViewById(EUExUtil.getResIdID("plugin_task_submit_tvTagTips"));
		tvDeadlineTips = (TextView) findViewById(EUExUtil.getResIdID("plugin_task_submit_tvDeadlineTips"));
		tvLeaderTips = (TextView) findViewById(EUExUtil.getResIdID("plugin_task_submit_tvLeaderTips"));
		etName = (EditText) findViewById(EUExUtil.getResIdID("plugin_task_submit_etName"));
		etDetail = (EditText) findViewById(EUExUtil.getResIdID("plugin_task_submit_etDetail"));
		layoutPriority = (RelativeLayout) findViewById(EUExUtil.getResIdID("plugin_task_submit_layoutPriority"));
		layoutProcess = (RelativeLayout) findViewById(EUExUtil.getResIdID("plugin_task_submit_layoutProcess"));
		layoutTag = (RelativeLayout) findViewById(EUExUtil.getResIdID("plugin_task_submit_layoutTag"));
		layoutDeadline = (RelativeLayout) findViewById(EUExUtil.getResIdID("plugin_task_submit_layoutDeadline"));
		layoutLeader = (RelativeLayout) findViewById(EUExUtil.getResIdID("plugin_task_submit_layoutLeader"));
		btnConfirm = (Button) findViewById(EUExUtil.getResIdID("plugin_task_submit_btnConfirm"));
		btnCancel = (Button) findViewById(EUExUtil.getResIdID("plugin_task_submit_btnCancel"));
		inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);// 获得输入法管理器
	}

	/**
	 * 初始化数据
	 */
	private void initData() {
		if (createTaskPost == null)
			createTaskPost = new CreateTaskPost();
		// 新建线程访问网络，获取流程列表，和项目成员列表
		if (!TaskSubmitUtils.isNetworkAvailable(CreateTaskActivity.this)) {
			new AlertDialog.Builder(CreateTaskActivity.this).setTitle(EUExUtil.getResStringID("prompt"))
					.setMessage(EUExUtil.getResStringID("uexTaskSubmit_login_network_tips")).setCancelable(true)
					.setPositiveButton(EUExUtil.getResStringID("confirm"), null).create().show();
		} else {
			progressDialog = ProgressDialog.show(CreateTaskActivity.this, "数据加载...", "请稍后...", true, false);// 进度条对话框
			progressDialog.setCancelable(true);// 按返回键时关闭
			progressDialog.setCanceledOnTouchOutside(false);
			progressDialog.setOnCancelListener(new OnCancelListener() {

				@Override
				public void onCancel(DialogInterface dialog) {

				}
			});
			if (!isLoading) {
				new Thread() {
					public void run() {
						if (isLoading)
							return;
						isLoading = true;
						try {
							String appId = EUExTaskSubmit.appId;
							mobileProcess = HttpRequest.getMobileProcess(CreateTaskActivity.this, appId);
							mobilePrjMembers = HttpRequest.getMobilePrjMembers(CreateTaskActivity.this, appId);
							Log.i(TAG, mobileProcess.toString());
							Log.i(TAG, mobilePrjMembers.toString());
							Message message = Message.obtain(handler);
							message.sendToTarget();
						} catch (Exception e) {
							e.printStackTrace();
						}
					};
				}.start();
			}
		}
	}

	/**
	 * 初始化事件
	 */
	private void initEvent() {
		layoutPriority.setOnClickListener(this);
		layoutProcess.setOnClickListener(this);
		layoutTag.setOnClickListener(this);
		layoutDeadline.setOnClickListener(this);
		layoutLeader.setOnClickListener(this);
		btnConfirm.setOnClickListener(this);
		btnCancel.setOnClickListener(this);
	}

	/**
	 * 单击事件
	 * 
	 * @param v
	 */
	@Override
	public void onClick(View v) {
		String title;
		String[] items;
		if (v.getId() == EUExUtil.getResIdID("plugin_task_submit_layoutPriority")) {
			closeInputMethod();// 关闭键盘
			title = "优先级";
			items = new String[] { "正常", "紧急", "非常紧急" };
			dialogList(title, items);
		} else if (v.getId() == EUExUtil.getResIdID("plugin_task_submit_layoutProcess")) {
			closeInputMethod();// 关闭键盘
			title = "流程";
			if (mobileProcess == null && !isLoading) {
				initData();
				return;
			}
			items = new String[mobileProcess.messageVector.size()];
			for (int i = 0; i < mobileProcess.messageVector.size(); i++) {
				items[i] = mobileProcess.messageVector.get(i).name;
			}
			dialogList(title, items);
		} else if (v.getId() == EUExUtil.getResIdID("plugin_task_submit_layoutTag")) {
			closeInputMethod();// 关闭键盘
			title = "标签";
			items = new String[] { "需求", "Bug" };
			dialogList(title, items);
		} else if (v.getId() == EUExUtil.getResIdID("plugin_task_submit_layoutDeadline")) {
			closeInputMethod();// 关闭键盘
			chooseDate();
		} else if (v.getId() == EUExUtil.getResIdID("plugin_task_submit_layoutLeader")) {
			closeInputMethod();// 关闭键盘
			title = "负责人";
			if (mobilePrjMembers == null && !isLoading) {
				initData();
				return;
			}
			items = new String[mobilePrjMembers.messageVector.size()];
			for (int i = 0; i < mobilePrjMembers.messageVector.size(); i++) {
				items[i] = mobilePrjMembers.messageVector.get(i).userName;
			}
			dialogList(title, items);
		} else if (v.getId() == EUExUtil.getResIdID("plugin_task_submit_btnConfirm")) {
			if (etName.getText().toString().equals("")) {
				Toast.makeText(this, "请输入标题", Toast.LENGTH_SHORT).show();
			}
			// else if (etDetail.getText().toString().equals("")) {
			// Toast.makeText(this, "请输入描述", Toast.LENGTH_SHORT).show();
			// }
			else if (tvPriorityTips.getText().equals("")) {
				Toast.makeText(this, "请选择优先级", Toast.LENGTH_SHORT).show();
			} else if (tvProcessTips.getText().equals("")) {
				Toast.makeText(this, "请选择流程", Toast.LENGTH_SHORT).show();
			} else if (tvTagTips.getText().equals("")) {
				Toast.makeText(this, "请输入标签", Toast.LENGTH_SHORT).show();
			} else if (tvDeadlineTips.getText().equals("")) {
				Toast.makeText(this, "请选择截止日期", Toast.LENGTH_SHORT).show();
			} else if (tvLeaderTips.getText().equals("")) {
				Toast.makeText(this, "请选择负责人", Toast.LENGTH_SHORT).show();
			}

			else {
				// 回传数据
				Intent intent = new Intent();
				Bundle bundle = new Bundle();
				createTaskPost.name = etName.getText().toString();
				createTaskPost.detail = etDetail.getText().toString();
				createTaskPost.appId = EUExTaskSubmit.appId;
				createTaskPost.repeatable = "NONE";
				createTaskPost.deadline = tvDeadlineTips.getText().toString();
				for (int i = 0; i < mobilePrjMembers.messageVector.size(); i++) {
					// 如果是第一个，则不加逗号
					if (i == 0) {
						itemsMemberId = String.valueOf(mobilePrjMembers.messageVector.get(i).id);
					} else {
						itemsMemberId = itemsMemberId + "," + String.valueOf(mobilePrjMembers.messageVector.get(i).id);
					}
				}
				createTaskPost.member = itemsMemberId;
				bundle.putParcelable("createTaskPost", createTaskPost);
				intent.putExtras(bundle);
				setResult(RESULT_OK, intent);
				finish();
			}
		} else if (v.getId() == EUExUtil.getResIdID("plugin_task_submit_btnCancel")) {
			Intent intent = new Intent(CreateTaskActivity.this, QuitDialogActivity.class);
			intent.putExtra("tips", EUExUtil.getString("plugin_task_submit_quit_tips"));
			startActivityForResult(intent, 3);// 3代表从QuitDialog传过来的result
		}
	}

	/**
	 * 统一列表对话框
	 */
	@SuppressWarnings("deprecation")
	private void dialogList(final String title, final String[] items) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this); // 先建构造器
		builder.setTitle(title); // 设置标题
		// 设置列表项
		builder.setItems(items, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// 根据传进来的不同的title，进行不同的点击事件
				if (title.equals("优先级")) {
					tvPriorityTips.setText(items[which]);
					createTaskPost.priority = itemsPriority[which];// 找到对应的优先级英文，放入bean中
				} else if (title.equals("流程")) {
					tvProcessTips.setText(items[which]);
					createTaskPost.processId = "" + mobileProcess.messageVector.get(which).id;// 找到对应的流程id，放入bean中
				} else if (title.equals("标签")) {
					tvTagTips.setText(items[which]);
					createTaskPost.tag = items[which];// 放入标签
				} else if (title.equals("负责人")) {
					tvLeaderTips.setText(items[which]);
					createTaskPost.leader = "" + mobilePrjMembers.messageVector.get(which).id;// 找到对应的负责人id，放入bean中
				}

			}
		});
		// 设置取消按钮
		builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		AlertDialog dialog = builder.create();
		dialog.show();

		// 设置dialog大小，必须放在show()之后
		Window dialogWindow = dialog.getWindow();
		WindowManager.LayoutParams lp = dialogWindow.getAttributes();
		WindowManager windowManager = (WindowManager) getApplication().getSystemService(Context.WINDOW_SERVICE);
		lp.width = (int) (windowManager.getDefaultDisplay().getWidth() * 0.95); // 宽度
		lp.height = (int) (windowManager.getDefaultDisplay().getHeight() * 0.6); // 高度
		dialogWindow.setAttributes(lp);

	}

	/**
	 * 截止日期，日期选择器
	 */
	private void chooseDate() {
		Calendar nowDate = Calendar.getInstance();
		new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {

			@Override
			public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
				tvDeadlineTips.setText(year + "-" + (monthOfYear + 1) + "-" + dayOfMonth);
			}
		}, nowDate.get(Calendar.YEAR), nowDate.get(Calendar.MONTH), nowDate.get(Calendar.DAY_OF_MONTH)).show();
	}

	/**
	 * 接受Intent跳转所传回的数据
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case 3:
			if (resultCode == RESULT_OK) {
				MarkbugsActivity.removeView();// 移除标签View
				finish();
			} else if (resultCode == RESULT_CANCELED) {

			}
			break;

		default:
			break;
		}
	}

	/**
	 * 判断如果键盘打开，则关闭
	 */
	private void closeInputMethod() {
		// 如果键盘是打开的
		if (inputMethodManager.isActive()) {
			inputMethodManager.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(),
					InputMethodManager.HIDE_NOT_ALWAYS);// 关闭它
		}
	}

	@Override
	protected void onDestroy() {
		Log.i(TAG, "onDestroy");
		isLoading = false;
		super.onDestroy();
	}
}
