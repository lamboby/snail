package com.boby.snail.itrustoor;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.boby.snail.itrustoor.HttpUtil.HttpCallbackListener;

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
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;

public class SetPos extends Activity {
	Context mContext;
	MyListAdapter adapter;
	Data myconfig;// 全局变量
	private LinearLayout linearsync, lineardeletepos;

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pos);
		// 添加返回按钮
		SysApplication.getInstance().addActivity(this);
		if (Build.VERSION.SDK_INT >= 11) {
			ActionBar actionBar = getActionBar();
			actionBar.setDisplayHomeAsUpEnabled(true);
		}
		myconfig = (Data) getApplication();
		linearsync = (LinearLayout) this.findViewById(R.id.linearSync);
		linearsync.setOnClickListener(onclicklistener);
		lineardeletepos = (LinearLayout) this.findViewById(R.id.linearDelete);
		lineardeletepos.setOnClickListener(onclicklistener);
	}

	public OnClickListener onclicklistener = new OnClickListener() {
		//
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.linearSync:
				syncstu("stu_id=" + myconfig.getid());
				break;
			case R.id.linearDelete:
				Intent delintent = new Intent(SetPos.this, DeleteWifi.class);
				startActivityForResult(delintent, 0);
				break;
			}
		}
	};
	// 接收程序消息
	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			String response = (String) msg.obj;
			JSONObject resObject;
			List<Student> stulist = new ArrayList<Student>();
			try {
				resObject = new JSONObject(response);
				if (resObject.getInt("Code") == 0) {
					JSONArray jsonArray;
					switch (msg.what) {
					case 3:
						Log.v("debug", "处理第二次同步数据");
						// 同步数据,如未绑定学校,提示用户学校信息不存在,请扫描二维码添加学校信息
						List<WifiCard> wificardlist = new ArrayList<WifiCard>();
						jsonArray = new JSONObject(response)
								.getJSONArray("Data");
						Log.v("debug",jsonArray.toString());
						Log.v("debug",String.valueOf( jsonArray.getJSONObject(0).getString("wifi")
								.equals("null")));
						Log.v("debug",String.valueOf( jsonArray.getJSONObject(0).getString("wifi")
								.equals("")));
						if ((!jsonArray.getJSONObject(0).getString("wifi")
								.equals("null")) & (!jsonArray.getJSONObject(0).getString("wifi")
								.equals("")) ) {
							jsonArray = jsonArray.getJSONObject(0)
									.getJSONArray("wifi");
							for (int i = 0; i < jsonArray.length(); i++) {
								if ((!jsonArray.getJSONObject(i)
										.getString("macs").equals("null"))
										& (!jsonArray.getJSONObject(i)
												.getString("macs").equals(""))) {
									WifiCard stu = new WifiCard();
									JSONObject jsonObject = jsonArray
											.getJSONObject(i);

									String macid = jsonObject
											.getString("macid");// 学校ID
									stu.setmacid(macid);
									String macname = jsonObject
											.getString("macname");// 学校ID
									stu.setmacname(macname);
									String mac = jsonObject.getString("macs");
									stu.setmacs(mac);
									wificardlist.add(stu);
								}
							}

							String school = "[";
							for (int i = 0; i < wificardlist.size(); i++) {
								school = school + "{\"macid\":" + "\""
										+ wificardlist.get(i).getmacid() + "\""
										+ ",\"macname\":" + "\""
										+ wificardlist.get(i).getmacname()
										+ "\"" + ",\"macs\":" + "\""
										+ wificardlist.get(i).getmacs() + "\"}";
								if (i != wificardlist.size() - 1)
									school = school + ",";
							}
							school = school + "]";
							myconfig.setwifi(school);
							new AlertDialog.Builder(SetPos.this)
									.setTitle("提示")
									.setMessage("数据同步完成")
									.setPositiveButton(
											"确定",
											new DialogInterface.OnClickListener() {
												public void onClick(
														DialogInterface dialog,
														int whichButton) {
													stopservice();
													startservice();
												}
											}).show();
						} else {
							myconfig.setwifi("");
							new AlertDialog.Builder(SetPos.this)
									.setTitle("提示")
									.setMessage("找不到定位点信息,请先扫描Wifi定位点")
									.setPositiveButton(
											"确定",
											new DialogInterface.OnClickListener() {
												public void onClick(
														DialogInterface dialog,
														int whichButton) {
													stopservice();
													startservice();
												}
											}).show();
						}
						break;
					default:
						break;
					}
				} else {
					new AlertDialog.Builder(SetPos.this)
							.setTitle("服务器返回码:" + resObject.getString("Code"))
							.setMessage(resObject.getString("Msg"))
							.setPositiveButton("确定", null).show(); // 如果返回码不等于0
				}
			} catch (Exception e) {
			}
		}
	};

	public void syncstu(String HttpString) {
		// 同步手动添加的定位点数据
		Log.v("debug", "第二次同步");
		HttpUtil.sendHttpPostRequest(myconfig.getdebugmode(),"/wifi/syncWifi", HttpString,
				new HttpCallbackListener() {
					@Override
					public void onFinish(String response) {

						Message message = new Message();
						message.what = 3;
						message.obj = response.toString();
						handler.sendMessage(message);
						Log.v("debug", response);
					}

					@Override
					public void onError(Exception e) {
						Looper.prepare();

						new AlertDialog.Builder(SetPos.this).setTitle("退出")
								.setMessage("数据同步失败.")// "Wifi定位点同步失败,请检查网络设置或联系客服!")
								.setPositiveButton("确定", null).show();
						Looper.loop();
					}
				});
	}

	// 停止后台服务
	public void stopservice() {
		Intent stopIntent = new Intent(this, ScanWifiService.class);
		this.stopService(stopIntent);

		Intent intent = new Intent(this, AlarmReceiver.class);
		PendingIntent pi = PendingIntent.getBroadcast(this, 0, intent, 0);
		AlarmManager am = (AlarmManager) this
				.getSystemService(Activity.ALARM_SERVICE);
		am.cancel(pi);

	}

	public void startservice() {
		Intent startIntent = new Intent(this, ScanWifiService.class);
		this.startService(startIntent);

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
