package com.itrustoor.boby.snail;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.itrustoor.boby.snail.R;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo.State;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

public class ScanWifiService extends Service {

	private List<ScanResult> wifiList;
	private WifiManager mainWifi;
	private WifiReceiver receiverWifi;

	private Date httptime;
	private Date wifitime;
	private String stustatus;

	List<Wifilist> schlist = new ArrayList<Wifilist>();

	private int status = 0;
	private int WifiStatus = 0, once = 0;

	@Override
	public void onCreate() {
		super.onCreate();
		Notification notification = new Notification(R.drawable.ic_launcher2,
				"启动小蜗牛定位服务", System.currentTimeMillis());
		Intent notificationIntent = new Intent(this, MainActivity.class);
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
				notificationIntent, 0);
		notification
				.setLatestEventInfo(this, "小蜗牛", "定位点扫描服务程序", pendingIntent);
		startForeground(0, notification);

		SharedPreferences pref = getSharedPreferences("snial", MODE_PRIVATE);
		String savestring = pref.getString("school", "");

		JSONArray jsonArray;
		try {
			jsonArray = new JSONArray(savestring);
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject jsonObject;

				jsonObject = jsonArray.getJSONObject(i);
				String schoolid = jsonObject.getString("schid");
				String studentid = jsonObject.getString("stuid");
				String mac = jsonObject.getString("mac");
				Wifilist tempwifi = new Wifilist();
				tempwifi.setschid(schoolid);
				tempwifi.setcard(studentid);
				tempwifi.setschoollist(mac);
				schlist.add(tempwifi);
			}

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		mainWifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		receiverWifi = new WifiReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
		registerReceiver(receiverWifi, filter);// 注册广播

	}

	private DownloadBinder mBinder = new DownloadBinder();

	class DownloadBinder extends Binder {
		public void startDownload() {
			Log.d("myservice", "startdownload executed");
		}

		public int getProgeress() {
			Log.d("myservice", "getprogress executed");
			return 0;
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	void scanWifi() {

		mainWifi.startScan();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				// 服务处理程序
				// stopSelf();
				// 查询WIFI状态,置标志位
				if (CheckWifi() == 1) {
					WifiStatus = 1;
					Log.v("开", "调用系统广播");
				} else {
					OpenWifi();
					WifiStatus = 0;
				}
				// 如WIFI为关,打开WIFI,发送扫描WIFI信息.如果WIFI标志为开,则仅在首次启动时发送扫描WIFI
				if (WifiStatus == 0 || once == 0) {
					Log.v("关", "查询WIFI状态");
					scanWifi();
					once = 1;
				}

			}
		}).start();
		AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
		int anHour = 30 * 1000;
		long triggerAtTime = SystemClock.elapsedRealtime() + anHour;
		Intent i = new Intent(this, AlarmReceiver.class);
		PendingIntent pi = PendingIntent.getBroadcast(this, 0, i, 0);
		manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pi);
		return super.onStartCommand(intent, flags, startId);
	}

	public void onDestory() {
		super.onDestroy();
		Log.d("服务", "服务销毁");
	}

	class WifiReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			ScanResult result = null;
			wifiList = mainWifi.getScanResults();
			// Toast.makeText(context, "扫描到 "+wifiList.size()+" 个热点信息.",
			// Toast.LENGTH_LONG).show();

			// 扫描到WIFI后做出判断,上传数据
			for (int i = 0; i < wifiList.size(); i++) {
				result = wifiList.get(i);
				int intin = wifiin(result.toString());
				if (i != 0) {
					if (status != 3) {
						if (status == 0) {

							// 到达
						}
						status = 3;
						break;
					}
				}
			}
			if (status > 0) {
				status--;
				if (status == 0) {
					// 离开
				}
			}

			if (WifiStatus == 0) {
				CloseWifi();
				Log.v("关闭", "关闭WIFI");
			}

			for (int i = 0; i < wifiList.size(); i++) {
				result = wifiList.get(i);
				// if (result.BSSID).
				// 根据标志位,判断是否关闭WIFI
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
		int in = 0;
		for (int i = 0; i < schlist.size(); i++) {
			if (schlist.get(i).getschid().contains(mac)) {
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
}