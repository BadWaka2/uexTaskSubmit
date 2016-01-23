package org.zywx.wbpalmstar.plugin.uexTaskSubmit.Activitys;

import org.zywx.wbpalmstar.engine.universalex.EUExUtil;
import org.zywx.wbpalmstar.widgetone.uextasksubmit.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

/**
 * 是否要退出Activity，主题为对话框
 * 
 * @author waka
 *
 */
public class QuitDialogActivity extends Activity implements OnClickListener {
	private Button btnConfirm, btnCancel;
	private TextView tvTips;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.plugin_task_submit_dialog_quit);
		this.setFinishOnTouchOutside(false);// 点击别处不能退出
		initView();
		initData();
		initEvent();
	}

	private void initView() {
		btnConfirm = (Button) findViewById(EUExUtil.getResIdID("plugin_task_submit_btnConfirm"));
		btnCancel = (Button) findViewById(EUExUtil.getResIdID("plugin_task_submit_btnCancel"));
		tvTips = (TextView) findViewById(EUExUtil.getResIdID("plugin_task_submit_tvTips"));
	}

	private void initData() {
		Intent intent = getIntent();
		String tips = intent.getStringExtra("tips");
		tvTips.setText(tips);
	}

	private void initEvent() {
		btnConfirm.setOnClickListener(this);
		btnCancel.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		Intent intent = new Intent();
		if (v.getId() == EUExUtil.getResIdID("plugin_task_submit_btnConfirm")) {
			setResult(RESULT_OK, intent);
			finish();
		} else if (v.getId() == EUExUtil.getResIdID("plugin_task_submit_btnCancel")) {
			setResult(RESULT_CANCELED, intent);
			finish();
		}
	}
}
