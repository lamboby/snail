package com.boby.snail.itrustoor;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.boby.snail.itrustoor.R;

import android.support.v4.app.ListFragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;


public class MsgFragment<User> extends ListFragment {
	List<Map<String, Object>> listItems;
	SimpleAdapter adapter;

	// private MyListAdapter adapter=null;
	private int[] images = new int[] { R.drawable.welcome_0, R.drawable.wifi_1,
			R.drawable.cloud_2, R.drawable.err_3 ,R.drawable.data_4};

	private IntentFilter intentFilter;
	private LocalReceiver localReceiver;
	private LocalBroadcastManager localBroadcastManager;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.msg, container, false);
		// list = (ListView) view.findViewById(android.R.id.list);
		return view;
	}
	public void startservice() {
		Intent startIntent = new Intent(getActivity(),
				ScanWifiService.class);
		getActivity().startService(startIntent);
		
	}
	// 接收本地广播
	class LocalReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			Data myconfig = (Data) getActivity().getApplication();
			TextView buffercount = (TextView) getActivity().findViewById(
					R.id.buffercount);
			buffercount.setText(String.valueOf(myconfig.getcount()));

			
			SimpleDateFormat formatter = new SimpleDateFormat(
					"HH:mm:ss");
			Date curDate = new Date(System.currentTimeMillis());// 获取当前时间
			String atttime = formatter.format(curDate);

			TextView viewSchoolname = (TextView) getActivity().findViewById(
					R.id.atschoolname);
			TextView viewTime = (TextView) getActivity().findViewById(
					R.id.atschooltime);
			TextView viewWifiscan = (TextView) getActivity().findViewById(
					R.id.wifiscan);


			if (intent.getStringExtra("Type") == "14") {
				viewWifiscan.setText(atttime+intent.getStringExtra("Value"));

			} else {
				if (intent.getStringExtra("Type") == "0") {
							
					viewSchoolname.setText(intent.getStringExtra("Value"));
					viewTime.setText(atttime);
					
				}
				addItem(atttime, intent.getStringExtra("Value"),
						Integer.parseInt(intent.getStringExtra("Type")));
			}
			
		}
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		localBroadcastManager = LocalBroadcastManager
				.getInstance(getActivity());
		intentFilter = new IntentFilter("com.boby.snail.itrustoor.myboard");
		localReceiver = new LocalReceiver();
		localBroadcastManager.registerReceiver(localReceiver, intentFilter);
		startservice();
		
	}
	
	@Override
	public void onDestroy(){
		super.onDestroy();
		localBroadcastManager.unregisterReceiver(localReceiver);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// setListAdapter(new ArrayAdapter<String>(getActivity(),
		// android.R.layout.simple_list_item_1, presidents));
		// adapter=new MyListAdapter(getActivity());
		// setListAdapter(adapter);

		listItems = new ArrayList<Map<String, Object>>();
		SimpleDateFormat formatter = new SimpleDateFormat(
				"yyyy-MM-dd  HH:mm:ss");
		Date curDate = new Date(System.currentTimeMillis());// 获取当前时间
		String atttime = formatter.format(curDate);
		// for (int i = 0; i < values.length; i++) {
		Map<String, Object> listItem = new HashMap<String, Object>();
		listItem.put("values", atttime);
		listItem.put("values2", "欢迎使用小蜗牛报平安服务");
		listItem.put("images", images[0]);
		listItems.add(listItem);
		// }
		adapter = new SimpleAdapter(getActivity(), listItems, R.layout.user,
				new String[] { "values2", "values", "images" }, new int[] {
						R.id.cn_word, R.id.en_word, R.id.animal });

		setListAdapter(adapter);
	}

	public void onListItemClick(ListView parent, View v, int position, long id) {
		// System.out.println("Click On List Item!!!");
		super.onListItemClick(parent, v, position, id);
	}

	private void addItem(String ttime, String tvalue, int i) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("values", ttime);
		map.put("values2", tvalue);
		map.put("images", images[i]);
		listItems.add(map);
		adapter.notifyDataSetChanged();
	}

}
