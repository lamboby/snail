package com.boby.snail.itrustoor;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.boby.snail.itrustoor.HttpUtil.HttpCallbackListener;
import com.boby.snail.itrustoor.R;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.SystemClock;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

public class ScanWifiService extends Service {
	private LocalBroadcastManager localBroadcastManager;
	private List<ScanResult> wifiList;
	private WifiManager mainWifi;
	private WifiReceiver receiverWifi;
	List<Wifilist> schlist = new ArrayList<Wifilist>();
	private int status = 0;
	private int WifiStatus = 0, once = 0;
	private int intin;
	private int schoolid;
	private String card, schoolname;
	public int intheschool;// 学生当前所在学校所属数组
	Data myconfig;
	int[] ifrequency = { 15, 30, 60 };
	private PowerManager.WakeLock wakeLock = null;

	@Override
	public void onCreate() {
		super.onCreate();
		Notification notification = new Notification(R.drawable.ic_launcher3,
				"启动小蜗牛定位服务", System.currentTimeMillis());
		Intent notificationIntent = new Intent(this, MainActivity.class);
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
				notificationIntent, 0);
		notification.setLatestEventInfo(this, "小蜗牛报平安", "荆州忠帮信息技术有限公司",
				pendingIntent);
		startForeground(1, notification);
		myconfig = (Data) getApplication();
		String savestring = myconfig.getschool();// 保存的MAC地址与学校对应信息
		intheschool = myconfig.getinschool(); // 保存的学生当前位置
		JSONArray jsonArray;
		try {
			jsonArray = new JSONArray(savestring);
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject jsonObject;
				jsonObject = jsonArray.getJSONObject(i);
				int sschoolid = jsonObject.getInt("schid");
				String scardid = jsonObject.getString("cardid");
				String sschoolname = jsonObject.getString("schoolname");
				String smac = jsonObject.getString("mac");
				Wifilist tempwifi = new Wifilist();
				tempwifi.setschid(sschoolid);
				tempwifi.setcard(scardid);
				tempwifi.setschoollist(smac);
				tempwifi.setschoolname(sschoolname);
				schlist.add(tempwifi);
			}

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Log.v("debug", "后台服务启动");

		mainWifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		localBroadcastManager = LocalBroadcastManager.getInstance(this);
		receiverWifi = new WifiReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
		registerReceiver(receiverWifi, filter);// 注册广播
		sendLocalBroadcast("1", "后台WIFI扫描服务开始运行");
		acquireWakeLock(this);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		releaseWakeLock();
		Log.v("debug", "后台服务停止");
		sendLocalBroadcast("1", "后台WIFI扫描服务关闭");
		unregisterReceiver(receiverWifi);
		sendLocalBroadcast("3", "后台服务停止运行");
	}

	// 发送本地广播
	private void sendLocalBroadcast(String a, String b) {
		Intent mIntent = new Intent("com.boby.snail.itrustoor.myboard");
		mIntent.putExtra("Type", a);
		mIntent.putExtra("Value", b);
		localBroadcastManager.sendBroadcast(mIntent);
		// 发送广播后取消本地广播的注册
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	void scanWifi() {
		Log.v("debug", "扫描WIFI");
		mainWifi.startScan();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				Log.v("debug", "定时服务运行");
				// 服务处理程序
				// stopSelf();
				// 查询WIFI状态,置标志位
				if (CheckWifi() == 1) {
					WifiStatus = 1;
					sendLocalBroadcast("5", "用户手动打开");
				} else {
					OpenWifi();
					WifiStatus = 0;
				}
				// 如WIFI为关,打开WIFI,发送扫描WIFI信息.如果WIFI标志为开,则仅在首次启动时发送扫描WIFI
				// if (WifiStatus == 0 || once == 0) {
				// Log.v("关", "查询WIFI状态");
				scanWifi();

				// }

			}
		}).start();

		AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
		int anHour = ifrequency[myconfig.getfrequency()] * 1000;
		Log.v("debug", Integer.toString(anHour));
		long triggerAtTime = SystemClock.elapsedRealtime() + anHour;
		Intent i = new Intent(this, AlarmReceiver.class);
		PendingIntent pi = PendingIntent.getBroadcast(this, 0, i, 0);
		manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pi);
		return super.onStartCommand(intent, flags, startId);
	}

	class WifiReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			ScanResult result = null;

			Log.v("debug", "收到WIFI广播");
			int checkintheschool = 1029;
			wifiList = mainWifi.getScanResults();
			// 扫描到WIFI后做出判断,上传数据
			sendLocalBroadcast("4", "扫描到" + wifiList.size() + "个WIFI热点");
			for (int i = 0; i < wifiList.size(); i++) {
				result = wifiList.get(i);
				intin = wifiin(result.BSSID);
				if (intin != -1) {
					// sendLocalBroadcast("1","扫描到培训学校MAC地址");
					status = 4;
					checkintheschool = 1023;
					card = schlist.get(intin).getcard();
					schoolid = schlist.get(intin).getschid();
					schoolname = schlist.get(intin).getschoolname();
					if (schoolid != intheschool) {
						intheschool = schoolid;
						myconfig.setinschool(intheschool);
						sendLocalBroadcast("0", "到达 " + schoolname);
						SimpleDateFormat formatter = new SimpleDateFormat(
								"yyyy-MM-dd  HH:mm:ss");
						// 获取当前时间
						Date curDate = new Date(System.currentTimeMillis());
						String atttime = formatter.format(curDate);

						String strtvbox = "card=" + card + "&att_time="
								+ atttime + "&type=0&sch_id=" + schoolid
								+ "&kind=0&entex_id=1";

						myconfig.setatplace("到达: "+ schoolname);

						myconfig.setatplacetime(atttime);
						Log.v("debug","保存位置信息");
						
						HttpUtil.sendHttpPostRequest("/tvbox/attends",
								strtvbox, new HttpCallbackListener() {
									@Override
									public void onFinish(String response) {
										sendLocalBroadcast("2", "上传数据到云端");
										Log.v("Debug", response);
									}

									@Override
									public void onError(Exception e) {
										sendLocalBroadcast("3", "上传数据到云端遇到错误");
									}
								});

						status = 4;
						break;

					}
				}
			}

			if ((checkintheschool == 1029) && (once == 0)) {
				intheschool = -1;
				once = 1;
				Log.v("debug", String.valueOf(once));
			}

			if (status > 0) {
				status--;
				if (status == 0) {
					sendLocalBroadcast("0", "离开 " + schoolname);

					intheschool = -1;
					myconfig.setinschool(intheschool);
					SimpleDateFormat formatter = new SimpleDateFormat(
							"yyyy-MM-dd  HH:mm:ss");
					Date curDate = new Date(System.currentTimeMillis());// 获取当前时间
					String atttime = formatter.format(curDate);
					String strtvbox = "card=" + card + "&att_time=" + atttime
							+ "&type=1&sch_id=" + schoolid
							+ "&kind=0&entex_id=1";

					myconfig.setatplace("离开: "+schoolname);
					myconfig.setatplacetime(atttime);
					Log.v("debug","保存位置信息");
					HttpUtil.sendHttpPostRequest("/tvbox/attends", strtvbox,
							new HttpCallbackListener() {
								@Override
								public void onFinish(String response) {
									sendLocalBroadcast("2", "上传数据到云端");
									Log.v("Debug", response);
								}

								@Override
								public void onError(Exception e) {
									sendLocalBroadcast("3", "上传数据到云端遇到错误");
								}
							});

				}
			}
			if (WifiStatus == 0) {
				CloseWifi();
			}

		}
	}

	// 打开WIFI
	public void OpenWifi() {
		if (!mainWifi.isWifiEnabled()) {
			mainWifi.setWifiEnabled(true);
			sendLocalBroadcast("5", "小蜗牛自动打开WIFI");
		}
	}

	public int wifiin(String mac) {
		int in = -1;
		for (int i = 0; i < schlist.size(); i++) {
			if (schlist.get(i).getschool().contains(mac)) {
				in = i;
				break;
			}
		}
		return in;
	}

	public int CheckWifi() {
		WifiManager manager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		int wifi = manager.getWifiState();
		// 判断wifi已连接的条件
		if (wifi == WifiManager.WIFI_STATE_ENABLED
				|| wifi == WifiManager.WIFI_STATE_ENABLING)
			return 1;
		else
			return 0;
	}

	// 关闭WIFI
	public void CloseWifi() {
		if (mainWifi.isWifiEnabled()) {
			sendLocalBroadcast("5", "小蜗牛自动关闭WIFI");
			mainWifi.setWifiEnabled(false);
			WifiStatus = 2;
		}
	}

	// 获取锁
	public void acquireWakeLock(Context context) {
		if (wakeLock == null) {
			PowerManager powerManager = (PowerManager) (context
					.getSystemService(Context.POWER_SERVICE));
			wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK
					| PowerManager.ON_AFTER_RELEASE, "My Tag");
			wakeLock.acquire();
			// PowerManager.PARTIAL_WAKE_LOCK|PowerManager.ON_AFTER_RELEASE
			// PowerManager.SCREEN_DIM_WAKE_LOCK
		}
	}

	// 释放锁
	public void releaseWakeLock() {
		if (wakeLock != null && wakeLock.isHeld()) {
			wakeLock.release();
			wakeLock = null;
		}
	}
}