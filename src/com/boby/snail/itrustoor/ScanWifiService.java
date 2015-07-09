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
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.Uri;
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
	private int studentid;
	private String   schoolname;
	public int intheschool;// 学生当前所在学校所属数组
	private boolean appscanwifi = false;
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
		myconfig.setenablestartservice(true);
		String savestring = myconfig.getwifi();// 保存的MAC地址与学校对应信息
	
		
		intheschool = myconfig.getinschool(); // 保存的学生当前位置
		if (intheschool > 0){
			status = 4;
			schoolname=myconfig.getschoolname();
			schoolid=myconfig.getid();
		}
		
		studentid=myconfig.getid();
		JSONArray jsonArray;
		try {
			jsonArray = new JSONArray(savestring);
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject jsonObject;
				jsonObject = jsonArray.getJSONObject(i);
				int sschoolid = jsonObject.getInt("macid");			 
				String sschoolname = jsonObject.getString("macname");
				String smac = jsonObject.getString("macs");
				Wifilist tempwifi = new Wifilist();
				tempwifi.setschid(sschoolid);
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
		// acquireWakeLock(this);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		// releaseWakeLock();
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
		// 查看缓存中是否有数据,如有数据,检查网络连接,网络连接正常则发送数据到云端
		final DataBuffer buffer;
		buffer = myconfig.getlist();
		if ((buffer != null) && (!myconfig.getdisablescanwifi())) {
			if (isNetworkAvailable()) {
				String strtvbox = "card=" + buffer.getcard() + "&att_time="
						+ buffer.getatttime() + "&type=" + buffer.getIsin()
						+ "&sch_id=" + buffer.getschoolid()+"&stu_id="+buffer.getid();
				Log.v("debug", strtvbox);
				HttpUtil.sendHttpPostRequest("/wifi/wifiAttends", strtvbox,
						new HttpCallbackListener() {
							@Override
							public void onFinish(String response) {
								sendLocalBroadcast("2", "上传缓存队列数据成功");
								Log.v("Debug", response);
								myconfig.delitem(buffer.getid());
							}

							@Override
							public void onError(Exception e) {
								sendLocalBroadcast("3", e.toString());

							}
						});

			}
		}
		new Thread(new Runnable() {
			@Override
			public void run() {
				if (!myconfig.getdisablescanwifi()) {
					Log.v("debug", "定时服务运行");
					appscanwifi = true;
					if (CheckWifi() == 1) {
						WifiStatus = 1;

					} else {
						OpenWifi();
						WifiStatus = 0;
					}
					scanWifi();
				}
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
			if (intent.getAction().equals(
					WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
					&& (appscanwifi)) {
				ScanResult result = null;
				appscanwifi = false;
				Log.v("debug", "收到WIFI广播");
				// int checkintheschool = 1029;
				wifiList = mainWifi.getScanResults();
				// 扫描到WIFI后做出判断,上传数据
				sendLocalBroadcast("14", "扫描到" + wifiList.size() + "个WIFI热点");
				for (int i = 0; i < wifiList.size(); i++) {
					result = wifiList.get(i);
					intin = wifiin(result.BSSID);
					if (intin != -1) {
						// sendLocalBroadcast("1","扫描到培训学校MAC地址");
						status = 4;
						// checkintheschool = 1023;
					 
						schoolid = schlist.get(intin).getschid();
						schoolname = schlist.get(intin).getschoolname();
						if (schoolid != intheschool) {
							intheschool = schoolid;
							myconfig.setinschool(intheschool);
							myconfig.setschoolname(schoolname);
							sendLocalBroadcast("0", "到达 " + schoolname);
							Uri notification = RingtoneManager
									.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
							Ringtone r = RingtoneManager.getRingtone(
									getApplicationContext(), notification);
							r.play();

							SimpleDateFormat formatter = new SimpleDateFormat(
									"yyyy-MM-dd  HH:mm:ss");
							// 获取当前时间
							Date curDate = new Date(System.currentTimeMillis());
							String atttime = formatter.format(curDate);
							myconfig.setatplace("到达: " + schoolname);
							myconfig.setatplacetime(atttime);
							Log.v("debug", "保存位置信息");
							Intent iupdate = new Intent(context,
									UpdateService.class);
							iupdate.putExtra("datastudentid",studentid);
							iupdate.putExtra("dataatttime", atttime);
							iupdate.putExtra("dataschoolid", schoolid);
							iupdate.putExtra("datacard", "W"+schoolid);
							iupdate.putExtra("isin", 0);
							startService(iupdate);
							status = 4;
							break;

						}
					}
				}

				/*
				 * if ((checkintheschool == 1029) && (once == 0)) { intheschool
				 * = -1; once = 1; Log.v("debug", String.valueOf(once)); }
				 */

				if (status > 0) {
					status--;
					if (status == 0) {
						sendLocalBroadcast("0", "离开 " + schoolname);
						Uri notification = RingtoneManager
								.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
						Ringtone r = RingtoneManager.getRingtone(
								getApplicationContext(), notification);
						r.play();
						intheschool = -1;
						myconfig.setinschool(intheschool);
						SimpleDateFormat formatter = new SimpleDateFormat(
								"yyyy-MM-dd  HH:mm:ss");
						Date curDate = new Date(System.currentTimeMillis());// 获取当前时间
						String atttime = formatter.format(curDate);
						myconfig.setatplace("离开: " + schoolname);
						myconfig.setatplacetime(atttime);
						Log.v("debug", "保存位置信息");
						Intent iupdate = new Intent(context,
								UpdateService.class);
						iupdate.putExtra("datastudentid", studentid);
						iupdate.putExtra("dataatttime", atttime);
						iupdate.putExtra("dataschoolid", schoolid);
						iupdate.putExtra("datacard", "W"+schoolid);
						iupdate.putExtra("isin", 1);
						startService(iupdate);
					}
				}
				if (WifiStatus == 0) {
					CloseWifi();
				}

			}
		}
	}

	// 打开WIFI
	public void OpenWifi() {
		if (!mainWifi.isWifiEnabled()) {
			mainWifi.setWifiEnabled(true);

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

			mainWifi.setWifiEnabled(false);
			WifiStatus = 2;
		}
	}

	/*
	 * // 获取锁 public void acquireWakeLock(Context context) { if (wakeLock ==
	 * null) { PowerManager powerManager = (PowerManager) (context
	 * .getSystemService(Context.POWER_SERVICE)); wakeLock =
	 * powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK |
	 * PowerManager.ON_AFTER_RELEASE, "My Tag"); wakeLock.acquire(); //
	 * PowerManager.PARTIAL_WAKE_LOCK|PowerManager.ON_AFTER_RELEASE //
	 * PowerManager.SCREEN_DIM_WAKE_LOCK } }
	 * 
	 * // 释放锁 public void releaseWakeLock() { if (wakeLock != null &&
	 * wakeLock.isHeld()) { wakeLock.release(); wakeLock = null; } }
	 */

	// 检查网络是否能连通
	private boolean isNetworkAvailable() {
		// 得到网络连接信息
		ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		// 去进行判断网络是否连接
		if (manager.getActiveNetworkInfo() != null) {
			return manager.getActiveNetworkInfo().isAvailable();
		}
		return false;
	}

}