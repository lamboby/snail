package com.boby.snail.itrustoor;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ConfigLan  extends Activity {
	private LocalBroadcastManager localBroadcastManager;
	Context mContext;
	MyListAdapter adapter;
	Data myconfig;// 全局变量
	private LinearLayout  linearFrequency;
	
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.configlan);
		// 添加返回按钮
		if (Build.VERSION.SDK_INT >= 11) {
			ActionBar actionBar = getActionBar();
			actionBar.setDisplayHomeAsUpEnabled(true);
		}
		SysApplication.getInstance().addActivity(this); 
		myconfig = (Data) getApplication();
		
		linearFrequency = (LinearLayout) this.findViewById(
				R.id.linearFrequency);
		linearFrequency.setOnClickListener(onclicklistener);
		localBroadcastManager = LocalBroadcastManager.getInstance(this);
		TextView frequency = (TextView) this.findViewById(
				R.id.frequency);
		
		switch (myconfig.getfrequency()) {
		case 0:
			frequency.setText("超快(15秒)");
			break;
		case 1:
			frequency.setText("快(30秒)");
			break;
		case 2:
			frequency.setText("中(120秒)");
			break;
		case 3:
			frequency.setText("慢(360秒)");
			break;
		default:
			break;
		}
	
	}
	public OnClickListener onclicklistener = new OnClickListener() {
		//
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.linearFrequency:
				final String[] items = { "超快(15秒)", "快(30秒)", "中(120秒)","慢(360秒)" };

				new AlertDialog.Builder(ConfigLan.this)
						.setTitle("请选择")
						.setIcon(android.R.drawable.ic_dialog_info)
						.setSingleChoiceItems(items, myconfig.getfrequency(),
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int which) {
										TextView frequency = (TextView) ConfigLan.this.findViewById(R.id.frequency);
										frequency.setText(items[which]);									 
										myconfig.setfrequency(which);
										dialog.dismiss();
										//发送本地广播
										sendLocalBroadcast("15", "改变频率");
										
									}
								}).setNegativeButton("取消", null).show();

				break;
			}
		}
	};
	// 发送本地广播
		private void sendLocalBroadcast(String a, String b) {
			Intent mIntent = new Intent("com.boby.snail.itrustoor.myboard");
			mIntent.putExtra("Type", a);
			mIntent.putExtra("Value", b);
			localBroadcastManager.sendBroadcast(mIntent);
			// 发送广播后取消本地广播的注册
		}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			Intent intent = new Intent(this, MainActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
}
