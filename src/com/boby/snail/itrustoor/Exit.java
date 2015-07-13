package com.boby.snail.itrustoor;



import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;


public class Exit  extends Activity {
	
	Context mContext;
	MyListAdapter adapter;
	Data myconfig;// 全局变量
	private LinearLayout  linearlogout, linearexit  ;


	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.exit);
		// 添加返回按钮
		SysApplication.getInstance().addActivity(this); 
		if (Build.VERSION.SDK_INT >= 11) {
			ActionBar actionBar = getActionBar();
			actionBar.setDisplayHomeAsUpEnabled(true);
		}
		myconfig = (Data) getApplication();
		
		linearlogout = (LinearLayout) this.findViewById(
				R.id.linearSLogout);
		linearlogout.setOnClickListener(onclicklistener);
		linearexit = (LinearLayout) this.findViewById(R.id.linearSExit);
		linearexit.setOnClickListener(onclicklistener);
	
	}
	public OnClickListener onclicklistener = new OnClickListener() {
		//
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.linearSExit:
				new AlertDialog.Builder(Exit.this)
						.setTitle("确认")
						.setMessage("确定要退出程序?")
						.setPositiveButton("确定",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int whichButton) {
										stopservice();
										Log.v("debug","退出");
										myconfig.setenablestartservice(false);
										SysApplication.getInstance().exit();   
									}
								}).setNegativeButton("取消", null).show();
				break;

			case R.id.linearSLogout:
				new AlertDialog.Builder(Exit.this)
						.setTitle("退出当前帐号")
						.setMessage("确定要切换登录帐号?")
						.setPositiveButton("确定",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int whichButton) {
										stopservice();
										myconfig.logout();// 提交修改
										myconfig.setenablestartservice(false);
										Intent intent = new Intent(
												Exit.this, Login.class);
										startActivity(intent);
									}

								}).setNegativeButton("取消", null).show();
				break;
			
			}
		}
	};
	
	// 停止后台服务
		public void stopservice() {
			Intent stopIntent = new Intent(Exit.this, ScanWifiService.class);
			stopService(stopIntent);

			Intent intent = new Intent(Exit.this, AlarmReceiver.class);
			PendingIntent pi = PendingIntent.getBroadcast(Exit.this, 0, intent,
					0);
			AlarmManager am = (AlarmManager) this.getSystemService(
					Activity.ALARM_SERVICE);
			am.cancel(pi);

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

