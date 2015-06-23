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

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals(ACTION1)
				| intent.getAction().equals(ACTION2)) {
			Intent startIntent = new Intent(context, ScanWifiService.class);
			context.startService(startIntent);
		}
	}

	public boolean isServiceRunning(String serviceName) {
		ActivityManager manager = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
		for (RunningServiceInfo service : manager
				.getRunningServices(Integer.MAX_VALUE)) {
			if (serviceName.equals(service.service.getClassName())) {
				return true;
			}
		}
		return false;
	}

}
