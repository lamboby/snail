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
import org.json.JSONException;
import org.json.JSONObject;

import com.boby.snail.itrustoor.R;
import com.boby.snail.itrustoor.HttpUtil.HttpCallbackListener;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class DeleteWifi extends Activity {
	// Button deleteWifi;
	ListView lv;
	Context mContext;
	int intDelPos;
	MyListAdapter adapter;
	Data myconfig;// 全局变量
	List<Integer> selected = new ArrayList<Integer>();
	private List<Item> items;

	@SuppressLint("NewApi")
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.deletewifi);
		SysApplication.getInstance().addActivity(this);
		// 添加返回按钮
		if (Build.VERSION.SDK_INT >= 11) {
			ActionBar actionBar = getActionBar();
			actionBar.setDisplayHomeAsUpEnabled(true);
		}
		// deleteWifi = (Button) findViewById(R.id.btnupdate);
		items = new ArrayList<Item>();
		lv = (ListView) findViewById(R.id.listWifiPos);// WIFI列表
		mContext = DeleteWifi.this;
		myconfig = (Data) getApplication();
		syncstu("stu_id=" + myconfig.getid());
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			Intent intent = new Intent(this, SetPos.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
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
				convertView = inflater.inflate(R.layout.listpos, null);
				holder = new ViewHolder();
				// holder.btnDel = (Button) convertView
				// .findViewById(R.id.btn_deletewifi);
				holder.tvDelPos = (Button) convertView
						.findViewById(R.id.btnDelete);
				holder.tvId = (TextView) convertView.findViewById(R.id.posId);
				holder.tvAlias = (TextView) convertView
						.findViewById(R.id.posAlias);

				holder.tvId.setText(item.ID);
				holder.tvAlias.setText(item.Alias);

				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
				holder.tvId.setText(item.ID);
				holder.tvAlias.setText(item.Alias);
			}
			holder.tvDelPos.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// 删除该条记录
					// TODO Auto-generated method stub
					// 删除list中的数据
					intDelPos = position;

					new AlertDialog.Builder(DeleteWifi.this)
							.setTitle("提示:")
							.setMessage("确认删除此定位点吗?")
							.setPositiveButton("确定",
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog,
												int whichButton) {
											String strHttp = "mac_id="
													+ items.get(position).ID;
											delPos(strHttp);
											;
										}

									}).setNegativeButton("取消", null).show();

				}
			});

			return convertView;
		}
	}

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

	// 接收程序消息
	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			String response = (String) msg.obj;
			JSONObject resObject;
			try {

				final List<MyFamily> list = new ArrayList<MyFamily>();
				resObject = new JSONObject(response);
				if (resObject.getInt("Code") == 0) {
					JSONArray jsonArray;

					switch (msg.what) {
					case 1:
						jsonArray = new JSONObject(response)
								.getJSONArray("Data");
						Log.v("debug", jsonArray.toString());
						if (jsonArray.getJSONObject(0).getString("wifi")
								.equals("null")
								| (jsonArray.getJSONObject(0).getString("wifi")
										.equals(""))) {
							new AlertDialog.Builder(DeleteWifi.this)
									.setTitle("提示:").setMessage("定位点信息为空")
									.setPositiveButton("确定", null).show();
						} else {
							jsonArray = jsonArray.getJSONObject(0)
									.getJSONArray("wifi");
							items.clear();
							for (int i = 0; i < jsonArray.length(); i++) {
								if ((!jsonArray.getJSONObject(i)
										.getString("macs").equals("null"))
										& (!jsonArray.getJSONObject(i)
												.getString("macs").equals(""))) {
									JSONObject jsonObject = jsonArray
											.getJSONObject(i);

									Item item = new Item();
									item.ID = jsonObject.getString("macid");// 学校ID
									Log.v("debug", item.ID);
									item.Alias = jsonObject
											.getString("macname");// 学校ID
									items.add(item);

									adapter = new MyListAdapter(items);
									lv.setAdapter(adapter);
									setListViewHeightBasedOnChildren(lv);
								}
							}
						}
						break;
					case 2:
						Log.v("debug", "删除定位点数据");
						items.remove(intDelPos);
						// 通知列表数据修改
						adapter.notifyDataSetChanged();
						setListViewHeightBasedOnChildren(lv);
						syncstu2("stu_id=" + myconfig.getid());
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
									JSONObject jsonObject = jsonArray
											.getJSONObject(i);

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
						} else {
							myconfig.setwifi("");
						}
						stopservice();
						startservice();

						new AlertDialog.Builder(DeleteWifi.this)
								.setTitle("提示:").setMessage("定位点已删除.")
								.setPositiveButton("确定", null).show();
						break;
					default:
						break;
					}
				} else {
					// 如果返回码不等于0
					String reserr = "";
					switch (resObject.getInt("Code")) {
					case 1100:
						reserr = "数据库处理错误";
						break;
					case 1009:
						reserr = "参数错误";
						break;
					default:
						Log.v("Debug", String.valueOf(resObject.getInt("Code")));
						reserr = resObject.getString("Msg");
						break;
					}

					new AlertDialog.Builder(DeleteWifi.this)
							.setTitle("返回错误码:" + resObject.getString("Code"))
							.setMessage(reserr).setPositiveButton("确定", null)
							.show();
				}

			} catch (Exception e) {
				Log.i("debug", e.getMessage());
			}
		}
	};

	public void delPos(String HttpString) {
		// 同步手动添加的定位点数据
		HttpUtil.sendHttpPostRequest(myconfig.getdebugmode(),
				"/wifi/delWifiPos", HttpString, new HttpCallbackListener() {
					@Override
					public void onFinish(String response) {
						Message message = new Message();
						message.what = 2;
						message.obj = response.toString();
						handler.sendMessage(message);
						Log.v("debug", response.toString());
					}

					@Override
					public void onError(Exception e) {
						Looper.prepare();
						new AlertDialog.Builder(DeleteWifi.this).setTitle("错误")
								.setMessage(e.toString())
								.setPositiveButton("确定", null).show();
						Looper.loop();
					}
				});
	}

	public void syncstu(String HttpString) {
		// 同步手动添加的定位点数据
		HttpUtil.sendHttpPostRequest(myconfig.getdebugmode(), "/wifi/syncWifi",
				HttpString, new HttpCallbackListener() {
					@Override
					public void onFinish(String response) {
						Message message = new Message();
						message.what = 1;
						message.obj = response.toString();
						handler.sendMessage(message);
					}

					@Override
					public void onError(Exception e) {
						Looper.prepare();
						new AlertDialog.Builder(DeleteWifi.this).setTitle("退出")
								.setMessage("数据同步失败.")// "无法查询到服务数据信息")//
														// "Wifi定位点同步失败,请检查网络设置或联系客服!")
								.setPositiveButton("确定", null).show();
						Looper.loop();
					}
				});
	}

	public void syncstu2(String HttpString) {
		// 同步手动添加的定位点数据
		HttpUtil.sendHttpPostRequest(myconfig.getdebugmode(), "/wifi/syncWifi",
				HttpString, new HttpCallbackListener() {
					@Override
					public void onFinish(String response) {
						Message message = new Message();
						message.what = 3;
						message.obj = response.toString();
						handler.sendMessage(message);
					}

					@Override
					public void onError(Exception e) {
						Looper.prepare();
						new AlertDialog.Builder(DeleteWifi.this).setTitle("退出")
								.setMessage("数据同步失败.")// "无法查询到服务数据信息")//
														// "Wifi定位点同步失败,请检查网络设置或联系客服!")
								.setPositiveButton("确定", null).show();
						Looper.loop();
					}
				});
	}

	static class ViewHolder {
		public TextView tvId;
		public TextView tvAlias;
		public Button tvDelPos;
	}

	class Item {
		private String ID;
		private String Alias;
	}

}
