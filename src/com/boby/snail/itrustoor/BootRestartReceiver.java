package com.boby.snail.itrustoor;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class BootRestartReceiver extends BroadcastReceiver {
	private final String ACTION = "android.intent.action.BOOT_COMPLETED";

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals(ACTION)) { 
			Intent startIntent = new Intent(context, ScanWifiService.class);
			context.startService(startIntent);
		}
	}

}
