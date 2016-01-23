package org.zywx.wbpalmstar.plugin.uexTaskSubmit.Activitys;

import org.zywx.wbpalmstar.engine.universalex.EUExUtil;
import org.zywx.wbpalmstar.plugin.uexTaskSubmit.HttpRequest;
import org.zywx.wbpalmstar.plugin.uexTaskSubmit.DrawView.PictureTagLayout;
import org.zywx.wbpalmstar.plugin.uexTaskSubmit.JSONBean.CreateTaskPost;
import org.zywx.wbpalmstar.plugin.uexTaskSubmit.JSONBean.UserLoginResponse;
import org.zywx.wbpalmstar.plugin.uexTaskSubmit.util.TaskSubmitUtils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;

/**
 * 登录界面Activity，主题设置为对话框样式
 * 
 * @author waka
 *
 */
public class LoginDialogActivity extends Activity {
	private static final String TAG = "LoginDialogActivity";
	private EditText etAccount, etPassword;
	private Button btnLogin, btnCancel;
	private CheckBox cbSavePassword;
	private static boolean IS_SAVE_PASSWORD = false;// 是否保存密码的标识量，保存密码选择框被选中，则置为true；未选中则置为false
	private CreateTaskPost createTaskPost;
	private UserLoginResponse userLoginResponse;
	private ProgressDialog progressDialog;
	// private EncryptionDES encryptionDES;// DES加密类
	private LoginThread threadLogin;

	/**
	 * 异步处理登录返回的信息
	 */
	@SuppressLint("HandlerLeak")
	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {

			// 未超时
			if (msg.what == 1) {
				if(threadLogin != null){
					threadLogin.setLoginCancel(true);
					threadLogin = null;
				}
				userLoginResponse = (UserLoginResponse) msg.obj;
				progressDialog.dismiss();
				if (userLoginResponse != null && userLoginResponse.status.equals("success")) {
					PictureTagLayout.IS_LOGIN = true;// 将已登录设为true
					Intent intent = new Intent(LoginDialogActivity.this, CreateTaskActivity.class);// 跳转到CreateTaskActivity
					startActivityForResult(intent, 2);
					saveUserInfo();
				} else {
					String message = EUExUtil.getString("uexTaskSubmit_login_error");
					if(userLoginResponse != null && !TextUtils.isEmpty(userLoginResponse.statusInfo)){
						//message = userLoginResponse.statusInfo;
					}
					new AlertDialog.Builder(LoginDialogActivity.this).setTitle(EUExUtil.getResStringID("prompt"))
							.setMessage(message).setCancelable(true)
							.setPositiveButton(EUExUtil.getResStringID("confirm"), null).create().show();
				}
			}
			// 2代表返回的ticket为null
			if (msg.what == 2) {
				progressDialog.dismiss();
				new AlertDialog.Builder(LoginDialogActivity.this).setTitle(EUExUtil.getResStringID("prompt"))
						.setMessage(EUExUtil.getResStringID("uexTaskSubmit_ticket_null")).setCancelable(true)
						.setPositiveButton(EUExUtil.getResStringID("confirm"), null).create().show();
			}
			// 3代表http格式错误
			if (msg.what == 3) {
				progressDialog.dismiss();
				new AlertDialog.Builder(LoginDialogActivity.this).setTitle(EUExUtil.getResStringID("prompt"))
						.setMessage(EUExUtil.getResStringID("uexTaskSubmit_http_format_error")).setCancelable(true)
						.setPositiveButton(EUExUtil.getResStringID("confirm"), null).create().show();
			}
		};
	};

	/**
	 * onCreate
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(EUExUtil.getResLayoutID("plugin_task_submit_dialog_login"));
		this.setFinishOnTouchOutside(false);// 点击别处不能退出
		initView();
		initData();
		initEvent();
	}

	/**
	 * onDestroy，在这里将账号密码信息储存在SharedPreferences
	 */
	@Override
	protected void onDestroy() {
		Log.i(TAG, "onDestroy");
		if(threadLogin != null){
			threadLogin.setLoginCancel(true);
			threadLogin = null;
		}
		super.onDestroy();
		//saveUserInfo();// 在这里将账号密码信息储存在SharedPreferences
	}

	/**
	 * 初始化控件
	 */
	private void initView() {
		etAccount = (EditText) findViewById(EUExUtil.getResIdID("plugin_task_submit_etAccount"));
		etPassword = (EditText) findViewById(EUExUtil.getResIdID("plugin_task_submit_etPassword"));
		btnLogin = (Button) findViewById(EUExUtil.getResIdID("plugin_task_submit_btnLogin"));
		btnCancel = (Button) findViewById(EUExUtil.getResIdID("plugin_task_submit_btnCancel"));
		cbSavePassword = (CheckBox) findViewById(EUExUtil.getResIdID("plugin_task_submit_cbSavePassword"));
	}

	/**
	 * 初始化数据
	 */
	private void initData() {
		createTaskPost = new CreateTaskPost();
		// encryptionDES = new EncryptionDES();// 初始化加密类对象
		readUserInfo();// 将用户账户密码从SharedPreferences中读出来
	}

	/**
	 * 初始化事件
	 */
	private void initEvent() {
		// 登录按钮点击事件
		btnLogin.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				//int netType = getNetworkType();
				// 检查网络状态
				if (!TaskSubmitUtils.isNetworkAvailable(LoginDialogActivity.this)) {
					new AlertDialog.Builder(LoginDialogActivity.this).setTitle(EUExUtil.getResStringID("prompt"))
							.setMessage(EUExUtil.getResStringID("uexTaskSubmit_login_network_tips")).setCancelable(true)
							.setPositiveButton(EUExUtil.getResStringID("confirm"), null).create().show();
				} else {
					final String username = etAccount.getText().toString();
					final String password = etPassword.getText().toString();
					hideKeyBoard();
					progressDialog = ProgressDialog.show(LoginDialogActivity.this, "正在登录...", "请稍后...", true, false);// 进度条对话框
					progressDialog.setCancelable(true);// 按返回键时关闭
					progressDialog.setCanceledOnTouchOutside(false);
					progressDialog.setOnCancelListener(new OnCancelListener() {
						
						@Override
						public void onCancel(DialogInterface dialog) {
							if(threadLogin != null){
								threadLogin.setLoginCancel(true);
								threadLogin = null;
							}
						}
					});
					// 新建线程访问网络，登录
					if(threadLogin == null){
						threadLogin = new LoginThread(username,password);
						threadLogin.start();
					}
				}
			}
		});

		// 取消按钮点击事件
		btnCancel.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
				MarkbugsActivity.removeView();
			}
		});

		// 保存密码多选框状态改变响应事件
		cbSavePassword.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					IS_SAVE_PASSWORD = true;
				} else {
					IS_SAVE_PASSWORD = false;
				}
			}
		});
	}

	/**
	 * 隐藏键盘
	 */
	private void hideKeyBoard() {
		InputMethodManager mInputManager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
		mInputManager.hideSoftInputFromWindow(etAccount.getWindowToken(), 0);
	}

	/**
	 * 接受Intent回传的数据
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		// 2代表从CreateTaskActivity传回的数据
		case 2:
			// 接受从CreateTaskActivity传回的数据
			if (resultCode == RESULT_OK) {
				Bundle bundle = data.getExtras();// 获得Bundle
				createTaskPost = bundle.getParcelable("createTaskPost");// 接收传过来的自定义Bean:createTaskPost
				// 将接受的数据回传给MarkbugsActivity
				Intent intent = new Intent();
				bundle = new Bundle();
				bundle.putParcelable("createTaskPost", createTaskPost);
				intent.putExtras(bundle);
				setResult(RESULT_OK, intent);
			}
			finish();
			break;

		default:
			break;
		}
	}

	/**
	 * 重写返回键
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			MarkbugsActivity.removeView();
		}
		return super.onKeyDown(keyCode, event);
	}

	/**
	 * 将用户账户密码和保存密码多选框选中状态储存在SharedPreferences中
	 */
	private void saveUserInfo() {
		SharedPreferences.Editor editor = getPreferences(MODE_PRIVATE).edit();// 获得SharedPreferences的Editor对象
		editor.putString("account", etAccount.getText().toString());// 存入账号信息
		editor.putBoolean("IS_SAVE_PASSWORD", IS_SAVE_PASSWORD);// 存入保存密码多选框的标识信息

		// 不放入sharedPreferences时加密解析正常
		// String jiami =
		// encryptionDES.Encryptor(etAccount.getText().toString());
		// String accountDecryptor = encryptionDES.Decryptor(jiami);
		// Log.i("LoginDialogActivity", "before_accountEncryptor------->>>" +
		// jiami);
		// Log.i("LoginDialogActivity", "before_accountDecryptor------->>>" +
		// accountDecryptor);
		// editor.putString("jiami", jiami);

		if (IS_SAVE_PASSWORD) {
			editor.putString("password", etPassword.getText().toString());// 如果保存密码被选中，则将密码存入
		} else {
			editor.remove("password");// 如果保存密码未被选中，则将密码删除
		}
		editor.commit();// 提交
	}

	/**
	 * 将用户账户密码和保存密码多选框选中状态从SharedPreferences中读出来，并写入etAccount
	 */
	private void readUserInfo() {
		SharedPreferences sharedPreferences = getPreferences(MODE_PRIVATE);
		String account = sharedPreferences.getString("account", "");// 若读取不到则写""
		String password = sharedPreferences.getString("password", "");// 若读取不到则写""
		IS_SAVE_PASSWORD = sharedPreferences.getBoolean("IS_SAVE_PASSWORD", false);// 若读取不到则写false

		// 从sharedPreferences中取出来后解析失败，获取的字符串会莫名其妙的多出一行
		// String jiami = sharedPreferences.getString("jiami", "");// 若读取不到则写""
		// String accountDecryptor = encryptionDES.Decryptor(jiami);
		// Log.i("LoginDialogActivity", "accountEncryptor------->>>" + jiami);
		// Log.i("LoginDialogActivity", "accountDecryptor------->>>" +
		// accountDecryptor);

		etAccount.setText(account);
		etPassword.setText(password);
		cbSavePassword.setChecked(IS_SAVE_PASSWORD);// 设置之前的选中状态
	}
	
	/**
	 * 登录线程
	 * @author d
	 */
	private class LoginThread extends Thread {
		private boolean loginCancel = false;
		private String username;
		private String password;
		
		public LoginThread(String username,String password) {
			this.username = username;
			this.password = password;
		}
		
		@Override
		public void run() {
			try {
				if(loginCancel)
					return;
				UserLoginResponse userLoginResponse = HttpRequest.login(LoginDialogActivity.this, username, password);
				if(!loginCancel){
					Message message = Message.obtain(handler);
					message.obj = userLoginResponse;
					message.what = 1;
					message.sendToTarget();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			interrupt();
		}
		
		public void setLoginCancel(boolean isCancel){
			this.loginCancel = isCancel;
		}
	}

}
