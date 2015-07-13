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
	private int schoolid,studentid;
	private int Isin;
	private ConnectivityManager mCM;
	private WifiManager mainWifi;
	private Data myconfig;
	private boolean changewifi = false;
	private boolean change3G = false;

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
		studentid= intent.getIntExtra("datastudentid",0);
		schoolid = intent.getIntExtra("dataschoolid", 0);
		Isin = intent.getIntExtra("isin", 1);
		card=intent.getStringExtra("datacard");
		final String strtvbox = "card=" + card + "&att_time=" + atttime
				+ "&type=" + Isin + "&sch_id=" + schoolid+"&stu_id="+studentid;

		new Thread(new Runnable() {
			@Override
			public void run() {
				localBroadcastManager = LocalBroadcastManager
						.getInstance(getApplicationContext());
				UpdataToIcloud(0, strtvbox);
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

	// 上传数据到服务器
	public void UpdataToIcloud(final int i, final String strt) {
		// int[] delays= { 5000, 10000, 45000 };
		myconfig.setdisablescanwifi(true);
		if (i == 0) {
			if (CheckWifi()) {
				//延迟15秒发送,防止WIFI未连接
				myconfig.setdisablescanwifi(true);
				changewifi = false;
				change3G = false;
				Thread.currentThread();
				try {
					Thread.sleep(15000);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}

		} else if (i == 1) {
			// 关闭扫描,关闭WIFI
			if (CheckWifi()) {
				CloseWifi();
				changewifi = true;
			}
			if (!check3G()) {
				open3G();
				change3G = true;
			}
		}
		Log.v("debug", "延时前" + String.valueOf(i));
		if (i > 0) {
			try {
				Thread.currentThread();
				Thread.sleep(15000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		Log.v("debug", "延时后发送数据" + String.valueOf(i));
		HttpUtil.sendHttpPostRequest(myconfig.getdebugmode(),"/wifi/wifiAttends", strt,
				
					new HttpCallbackListener() {
					@Override
					public void onFinish(String response) {
						sendLocalBroadcast("2", "完成上传数据到云端服务器");
						Log.v("Debug", response);
						if (changewifi)
							OpenWifi();
						if (change3G)
							close3G();
						changewifi = false;
						change3G = false;
						myconfig.setdisablescanwifi(false);
						stopSelf();
					}

					@Override
					public void onError(Exception e) {
						sendLocalBroadcast("3", "第" + String.valueOf(i + 1)
								+ "次上传数据到云端服务器失败");
						if (i == 0)
							UpdataToIcloud(1, strt);
						else if (i == 1) {
							UpdataToIcloud(2, strt);
						} else if (i == 2) {
							UpdataToIcloud(3, strt);
						} else if (i == 3) {
							if (changewifi)
								OpenWifi();
							if (change3G)
								close3G();
							changewifi = false;
							change3G = false;
							myconfig.setdisablescanwifi(false);

							// 添加数据到缓存
							DataBuffer errdata = new DataBuffer();
							errdata.setatttime(atttime);
							errdata.setIsin(Isin);
							errdata.setid(studentid);
							errdata.setcard(card);
							errdata.setschoolid(schoolid);
							myconfig.additem(errdata);
							stopSelf();
						}
					}
				});
	}
}
