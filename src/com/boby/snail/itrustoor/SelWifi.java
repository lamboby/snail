package com.boby.snail.itrustoor;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.boby.snail.itrustoor.R;
import com.boby.snail.itrustoor.HttpUtil.HttpCallbackListener;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class SelWifi extends Activity {
	Button show;
	Button select;
	Button deselect;
	ListView lv;
	Context mContext;
	MyListAdapter adapter;
	Data myconfig;// 全局变量

	/** 定义WifiManager对象 */
	private WifiManager mainWifi;
	/** 扫描出的网络连接列表 */
	private List<ScanResult> wifiList;
	/** 扫描完毕接收器 */
	private WifiReceiver receiverWifi;

	private ProgressDialog dialog;
	private EditText addonemac, commonpassword;
	private int once = 0;

	private Button btnsearchwifi;
	private String createschool;
	private Button btnuploadschool;
	String macs;
	List<Integer> selected = new ArrayList<Integer>();
	private List<Item> items;

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.selwifi);
		SysApplication.getInstance().addActivity(this);
		// 添加返回按钮
		if (Build.VERSION.SDK_INT >= 11) {
			ActionBar actionBar = getActionBar();
			actionBar.setDisplayHomeAsUpEnabled(true);
		}

		// ActionBar actionBar = getActionBar();
		// actionBar.setDisplayHomeAsUpEnabled(true);

		myconfig = (Data) getApplication();
		btnsearchwifi = (Button) findViewById(R.id.btnupdate);

		btnuploadschool = (Button) findViewById(R.id.btnsave);

		// 文本输入框不自动获取焦点
		getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

		mainWifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		receiverWifi = new WifiReceiver();
		items = new ArrayList<Item>();

		lv = (ListView) findViewById(R.id.listWifiPos);// WIFI列表
		once = 1;
		scanWifi();

		mContext = SelWifi.this;
		// 上传定位点信息
		btnuploadschool.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				int inum = items.size();
				macs = "";
				boolean oned = false;
				if (inum > 0) {
					macs = "[";

					for (int p = 0; p < items.size(); p++) {
						if (items.get(p).check) {
							if (oned)
								macs = macs + ",";
							macs = macs + "\"" + items.get(p).address + "\"";

							oned = true;
						}
					}
					macs = macs + "]";
					if (macs.length() < 10) {
						new AlertDialog.Builder(SelWifi.this).setTitle("错误:")
								.setMessage("数据为空,请选选择Wifi定位点")
								.setPositiveButton("确定", null).show();
						return;
					}
				}
				final EditText input = new EditText(SelWifi.this);

				// 弹出对话框,输入定位点信息.
				new AlertDialog.Builder(SelWifi.this)
						.setTitle("请输入此定位点名称:")
						.setView(input)
						.setPositiveButton("确定",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int whichButton) {
										createschool = "macs="
												+ macs
												+ "&alias="
												+ input.getText().toString()
														.trim();
										// 上传定位点信息
										HttpUtil.sendHttpPostRequest(
												myconfig.getdebugmode(),
												"/wifi/createWifiPos",
												createschool,
												new HttpCallbackListener() {
													@Override
													public void onFinish(
															String response) {

														Message message = new Message();
														message.what = 1;
														message.obj = response
																.toString();
														handler.sendMessage(message);
													}

													@Override
													public void onError(
															Exception e) {

														Looper.prepare();

														new AlertDialog.Builder(
																SelWifi.this)
																.setTitle("退出")
																.setMessage(
																		"上传信息失败,请检查网络设置或联系客服!")
																.setPositiveButton(
																		"确定",
																		null)
																.show();
														Looper.loop();

													}
												});
									}

								}).setNegativeButton("取消", null).show();

			}
		});

		// 手动添加 MAC地址
		// btnaddwifi.setOnClickListener(new OnClickListener() {
		// @Override
		// public void onClick(View v) {
		// lv = (ListView) findViewById(R.id.listWifiPos);
		// Item item = new Item();
		// item.name = "None";
		// item.address = addonemac.getText().toString().trim();
		// items.add(item);
		// adapter = new MyListAdapter(items);
		// lv.setAdapter(adapter);
		// adapter.notifyDataSetChanged();
		// setListViewHeightBasedOnChildren(lv);
		// }
		// });

		// 搜索WIFI热点
		btnsearchwifi.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				once = 1;
				scanWifi();
			}
		});
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

	void scanWifi() {
		OpenWifi();
		mainWifi.startScan();
		dialog = ProgressDialog.show(SelWifi.this, "", "正在扫描附近WIFI热点,请稍候");
	}

	// 自定义ListView适配器
	class MyListAdapter extends BaseAdapter {

		LayoutInflater inflater;
		public List<Item> items;

		public MyListAdapter(List<Item> items) {
			this.items = items;
			inflater = LayoutInflater.from(mContext);
		}

		@Override
		public int getCount() {
			// 返回值控制该Adapter将会显示多少个列表项
			return items == null ? 0 : items.size();
		}

		@Override
		public Object getItem(int position) {
			// 返回值决定第position处的列表项的内容
			return items.get(position);
		}

		@Override
		public long getItemId(int position) {
			// 返回值决定第position处的列表项的ID
			return position;
		}

		@Override
		public View getView(final int position, View convertView,
				ViewGroup parent) {
			ViewHolder holder = null;
			Item item = items.get(position);
			if (convertView == null) {

				convertView = inflater.inflate(R.layout.listitem, null);
				holder = new ViewHolder();
				// holder.btnDel = (Button) convertView
				// .findViewById(R.id.btn_deletewifi);

				holder.tvSelWifi = (CheckBox) convertView
						.findViewById(R.id.checkWifi);
				holder.tvName = (TextView) convertView
						.findViewById(R.id.tvName);
				holder.tvAddress = (TextView) convertView
						.findViewById(R.id.tvMac);
				holder.tvwifilevel = (ImageView) convertView
						.findViewById(R.id.imgWifiLevel);

				holder.tvName.setText(item.name);
				holder.tvAddress.setText(item.address);

				if (Math.abs(item.wifilevel) > 100) {

					holder.tvwifilevel.setImageDrawable(getResources()
							.getDrawable(R.drawable.stat_sys_signal_0_fully));

				} else if (Math.abs(item.wifilevel) > 70) {

					holder.tvwifilevel.setImageDrawable(getResources()
							.getDrawable(R.drawable.stat_sys_signal_1_fully));

				} else if (Math.abs(item.wifilevel) > 60) {

					holder.tvwifilevel.setImageDrawable(getResources()
							.getDrawable(R.drawable.stat_sys_signal_2_fully));

				} else if (Math.abs(item.wifilevel) > 50) {

					holder.tvwifilevel.setImageDrawable(getResources()
							.getDrawable(R.drawable.stat_sys_signal_3_fully));

				} else {

					holder.tvwifilevel.setImageDrawable(getResources()
							.getDrawable(R.drawable.stat_sys_signal_4_fully));

				}

				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
				holder.tvName.setText(item.name);
				holder.tvAddress.setText(item.address);
			}
			holder.tvSelWifi.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					// 删除list中的数据
					// items.remove(position);
					items.get(position).check = !items.get(position).check;
					// 通知列表数据修改
					// adapter.notifyDataSetChanged();
					// setListViewHeightBasedOnChildren(lv);
				}
			});

			return convertView;
		}
	}

	// 动态改变列表高度
	public void setListViewHeightBasedOnChildren(ListView listView) {

		ListAdapter listAdapter = listView.getAdapter();
		if (listAdapter == null) {
			return;
		}
		int totalHeight = 0;
		for (int i = 0; i < listAdapter.getCount(); i++) {
			View listItem = listAdapter.getView(i, null, listView);
			listItem.measure(0, 0);
			totalHeight += listItem.getMeasuredHeight();
		}

		ViewGroup.LayoutParams params = listView.getLayoutParams();
		params.height = totalHeight
				+ (listView.getDividerHeight() * (listAdapter.getCount() - 1));
		((MarginLayoutParams) params).setMargins(10, 10, 10, 10); // 可删除
		listView.setLayoutParams(params);
	}

	static class ViewHolder {
		public TextView tvName;
		public TextView tvAddress;
		public ImageView tvwifilevel;
		// public Button btnDel;
		public CheckBox tvSelWifi;

	}

	class Item {
		private String name;
		private String address;
		private boolean check;
		private int wifilevel;
	}

	/**
	 * 打开WIFI
	 */
	public void OpenWifi() {
		if (!mainWifi.isWifiEnabled()) {
			mainWifi.setWifiEnabled(true);
		}
	}

	protected void onPause() {
		super.onPause();
		unregisterReceiver(receiverWifi);// 注销广播
	}

	// 学生绑定
	public void BindWifiCard(String HttpString) {
		dialog = ProgressDialog.show(this, "", "Wifi定位点绑定学生...");
		HttpUtil.sendHttpPostRequest(myconfig.getdebugmode(),
				"/wifi/bindWifiCard", HttpString, new HttpCallbackListener() {
					@Override
					public void onFinish(String response) {
						dialog.dismiss();
						Message message = new Message();
						message.what = 2;
						message.obj = response.toString();
						handler.sendMessage(message);
					}

					@Override
					public void onError(Exception e) {
						Looper.prepare();
						dialog.dismiss();
						new AlertDialog.Builder(SelWifi.this).setTitle("退出")
								.setMessage("绑定定位点失败,请检查网络设置或联系客服!")
								.setPositiveButton("确定", null).show();
						Looper.loop();
					}
				});

	}

	// 接收程序消息
	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {

			String response = (String) msg.obj;
			List<MyListItem> list = new ArrayList<MyListItem>();
			JSONObject resObject;
			try {
				resObject = new JSONObject(response);
				if (resObject.getInt("Code") == 0) {
					JSONArray jsonArray;
					switch (msg.what) {
					case 1:// 上传定位点数据
						jsonArray = new JSONObject(response)
								.getJSONArray("Data");
						JSONObject jsonObject = jsonArray.getJSONObject(0);

						int schid = jsonObject.getInt("id");
						// .getJSONArray("cards")
						// .getJSONObject(0).getString("card");// 学号ID
						// Toast.makeText(SelWifi.this, "学校信息上传完成", 0).show();
						// 同步数据
						String bindstudent = "stu_id=" + myconfig.getid()
								+ "&sch_id=" + schid + "&fml_id="
								+ myconfig.getfamilyid() + "&card=" + "W"
								+ schid;
						BindWifiCard(bindstudent);
						break;
					case 2:// 学生绑定定位点
						syncstu("stu_id=" + myconfig.getid());
						break;
					case 3:
						Log.v("debug", "处理第二次同步数据");
						// 同步数据,如未绑定学校,提示用户学校信息不存在,请扫描二维码添加学校信息
						List<WifiCard> wificardlist = new ArrayList<WifiCard>();
						jsonArray = new JSONObject(response)
								.getJSONArray("Data");
						Log.v("debug", jsonArray.toString());
						Log.v("debug",
								String.valueOf(jsonArray.getJSONObject(0)
										.getString("wifi").equals("null")));
						Log.v("debug",
								String.valueOf(jsonArray.getJSONObject(0)
										.getString("wifi").equals("")));
						if ((!jsonArray.getJSONObject(0).getString("wifi")
								.equals("null"))
								& (!jsonArray.getJSONObject(0)
										.getString("wifi").equals(""))) {
							jsonArray = jsonArray.getJSONObject(0)
									.getJSONArray("wifi");
							for (int i = 0; i < jsonArray.length(); i++) {
								if ((!jsonArray.getJSONObject(i)
										.getString("macs").equals("null"))
										& (!jsonArray.getJSONObject(i)
												.getString("macs").equals(""))) {
									WifiCard stu = new WifiCard();
									jsonObject = jsonArray.getJSONObject(i);

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

							stopservice();
							startservice();

						}
						new AlertDialog.Builder(SelWifi.this).setTitle("提示:")
								.setMessage("新定位点设置完成.")
								.setPositiveButton("确定", null).show();
						break;
					default:
						break;
					}
				} else {
					String reserr = "";
					switch (resObject.getInt("Code")) {
					case 1100:
						reserr = "数据库处理错误";
						break;
					case 1009:
						reserr = "参数错误";
						break;
					default:
						reserr = "未知错误";
						break;
					}

					new AlertDialog.Builder(SelWifi.this)
							.setTitle("服务器返回码:" + resObject.getString("Code"))
							.setMessage(resObject.getString(reserr))
							.setPositiveButton("确定", null).show(); // 如果返回码不等于0
				}
			} catch (Exception e) {
			}
		}

	};

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

	public void syncstu(String HttpString) {
		// 同步手动添加的定位点数据
		Log.v("debug", "第二次同步");
		HttpUtil.sendHttpPostRequest(myconfig.getdebugmode(), "/wifi/syncWifi",
				HttpString, new HttpCallbackListener() {
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

						new AlertDialog.Builder(SelWifi.this).setTitle("退出")
								.setMessage("数据同步失败.")// "Wifi定位点同步失败,请检查网络设置或联系客服!")
								.setPositiveButton("确定", null).show();
						Looper.loop();
					}
				});
	}

	// 活动运行注册WIFI广播
	protected void onResume() {
		super.onResume();
		IntentFilter filter = new IntentFilter();
		filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
		registerReceiver(receiverWifi, filter);// 注册广播
	}

	// 接收WIFI广播
	class WifiReceiver extends BroadcastReceiver {
		public String[] getString(List<ScanResult> wifiList) {
			ArrayList<String> listStr = new ArrayList<String>();
			for (int i = 0; i < wifiList.size(); i++) {
				listStr.add(wifiList.get(i).toString());
			}
			return listStr.toArray(new String[0]);
		}

		public void onReceive(Context context, Intent intent) {
			if (once == 1) {
				ScanResult result = null;
				items.clear();
				wifiList = mainWifi.getScanResults();
				dialog.dismiss();
				Toast.makeText(context, "扫描到 " + wifiList.size() + " 个热点信息.",
						Toast.LENGTH_LONG).show();
				for (int i = 0; i < wifiList.size(); i++) {
					result = wifiList.get(i);
					Item item = new Item();
					item.name = result.SSID;
					item.address = result.BSSID;
					item.wifilevel = result.level;
					item.check = false;
					items.add(item);
				}
				adapter = new MyListAdapter(items);
				lv.setAdapter(adapter);
				setListViewHeightBasedOnChildren(lv);
				once = 0;
			}
		}
	}
}
