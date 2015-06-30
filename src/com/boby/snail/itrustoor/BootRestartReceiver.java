package com.boby.snail.itrustoor;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;
public class BootRestartReceiver extends BroadcastReceiver {
	private final String ACTION1 = "android.intent.action.BOOT_COMPLETED";
	private final String ACTION2 = "android.intent.action.USER_PRESENT";
	private Data myconfig;
	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals(ACTION1)
				| intent.getAction().equals(ACTION2)) {
			Log.v("debug", "接收开机或解锁屏幕广播");
			myconfig = (Data)context.getApplicationContext();
			if (myconfig.getenablestartservice()) {
				if (!isServiceRunning(context,
						"com.boby.snail.itrustoor.ScanWifiService")) {
					Intent mIntent = new Intent(context, ScanWifiService.class);
					context.startService(mIntent);
					Log.v("debug", "服务启动服务" + ScanWifiService.class.getName());
				}
			}
		}
	}

	public boolean isServiceRunning(Context context, String serviceName) {
		ActivityManager manager = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		for (RunningServiceInfo service : manager
				.getRunningServices(Integer.MAX_VALUE)) {
			if (serviceName.equals(service.service.getClassName())) {
				return true;
			}
		}
		return false;
	}

}
