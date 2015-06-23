package com.boby.snail.itrustoor;

import java.lang.reflect.Method;

import com.boby.snail.itrustoor.HttpUtil.HttpCallbackListener;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.TextView;

public class UpdateService extends Service {
	private LocalBroadcastManager localBroadcastManager;
	private String card, atttime;
	private int schoolid;
	private int Isin;
	private ConnectivityManager mCM;
	private WifiManager mainWifi;
	private Data myconfig;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onCreate() {
		super.onCreate();
		myconfig = (Data) this.getApplication();
		mCM = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		mainWifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		atttime = intent.getStringExtra("dataatttime");
		card = intent.getStringExtra("datacard");
		schoolid = intent.getIntExtra("dataschoolid", 0);
		Isin = intent.getIntExtra("isin", 1);

		new Thread(new Runnable() {
			@Override
			public void run() {
				localBroadcastManager = LocalBroadcastManager
						.getInstance(getApplicationContext());
				String strtvbox = "card=" + card + "&att_time=" + atttime
						+ "&type=" + Isin + "&sch_id=" + schoolid
						+ "&kind=0&entex_id=1";
				HttpUtil.sendHttpPostRequest("/tvbox/attends", strtvbox,
						new HttpCallbackListener() {
							@Override
							public void onFinish(String response) {
								sendLocalBroadcast("2", "上传数据到云端");
								Log.v("Debug", response);
								stopSelf();
							}

							@Override
							public void onError(Exception e) {
								// boolean wifichange=false;

								sendLocalBroadcast("3", "上传数据到云端遇到错误");
								if (!check3G()) {
									// 关闭WIFI
									open3G();
									String strtvbox = "card=" + card
											+ "&att_time=" + atttime + "&type="
											+ Isin + "&sch_id=" + schoolid
											+ "&kind=0&entex_id=1";
									HttpUtil.sendHttpPostRequest(
											"/tvbox/attends", strtvbox,
											new HttpCallbackListener() {
												@Override
												public void onFinish(
														String response) {
													sendLocalBroadcast("2",
															"再次上传数据到云端成功");
													Log.v("Debug", response);
													close3G();
													stopSelf();

												}

												@Override
												public void onError(Exception e) {
													close3G();
													sendLocalBroadcast("3",
															"再次上传数据到云端遇到错误");
													// 添加数据到缓存
													DataBuffer errdata = new DataBuffer();
													errdata.setatttime(atttime);
													errdata.setIsin(Isin);
													errdata.setcard(card);
													errdata.setschoolid(schoolid);
													myconfig.additem(errdata);
												}
											});

								}
							}
						});

			}
		}).start();
		return super.onStartCommand(intent, flags, startId);
	}

	// 发送本地广播
	private void sendLocalBroadcast(String a, String b) {
		Intent mIntent = new Intent("com.boby.snail.itrustoor.myboard");
		mIntent.putExtra("Type", a);
		mIntent.putExtra("Value", b);
		localBroadcastManager.sendBroadcast(mIntent);
		// 发送广播后取消本地广播的注册
	}

	// 检测GPRS是否打开
	private boolean gprsIsOpenMethod(String methodName) {
		Class cmClass = mCM.getClass();
		Class[] argClasses = null;
		Object[] argObject = null;
		Boolean isOpen = false;
		try {
			Method method = cmClass.getMethod(methodName, argClasses);
			isOpen = (Boolean) method.invoke(mCM, argObject);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return isOpen;
	}

	public boolean check3G() {
		return gprsIsOpenMethod("getMobileDataEnabled");
	}

	public void open3G() {
		mCM = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		Object[] arg = null;
		try {
			boolean isMobileDataEnable = invokeMethod("getMobileDataEnabled",
					arg);
			if (!isMobileDataEnable) {
				invokeBooleanArgMethod("setMobileDataEnabled", true);
				sendLocalBroadcast("4", "尝试打开数据流量");
				Thread.currentThread().sleep(4000); // 延时4秒

			}
		} catch (Exception e) {

			e.printStackTrace();
		}

	}

	public void close3G() {
		mCM = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		Object[] arg = null;
		try {
			boolean isMobileDataEnable = invokeMethod("getMobileDataEnabled",
					arg);
			if (isMobileDataEnable) {
				invokeBooleanArgMethod("setMobileDataEnabled", false);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// 判断网络状态
	public boolean invokeMethod(String methodName, Object[] arg)
			throws Exception {

		ConnectivityManager mConnectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

		Class ownerClass = mConnectivityManager.getClass();

		Class[] argsClass = null;
		if (arg != null) {
			argsClass = new Class[1];
			argsClass[0] = arg.getClass();
		}

		Method method = ownerClass.getMethod(methodName, argsClass);

		Boolean isOpen = (Boolean) method.invoke(mConnectivityManager, arg);

		return isOpen;
	}

	// 开启/关闭网络
	public Object invokeBooleanArgMethod(String methodName, boolean value)
			throws Exception {

		ConnectivityManager mConnectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

		Class ownerClass = mConnectivityManager.getClass();

		Class[] argsClass = new Class[1];
		argsClass[0] = boolean.class;

		Method method = ownerClass.getMethod(methodName, argsClass);

		return method.invoke(mConnectivityManager, value);
	}

	public void OpenWifi() {
		if (!mainWifi.isWifiEnabled()) {
			mainWifi.setWifiEnabled(true);
		}
	}

	public boolean CheckWifi() {
		WifiManager manager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		int wifi = manager.getWifiState();
		// 判断wifi已连接的条件
		if (wifi == WifiManager.WIFI_STATE_ENABLED
				|| wifi == WifiManager.WIFI_STATE_ENABLING)
			return true;
		else
			return false;
	}

	// 关闭WIFI
	public void CloseWifi() {
		if (mainWifi.isWifiEnabled()) {
			mainWifi.setWifiEnabled(false);
		}
	}

}
