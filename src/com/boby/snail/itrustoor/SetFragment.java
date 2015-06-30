package com.boby.snail.itrustoor;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.boby.snail.itrustoor.HttpUtil.HttpCallbackListener;
import com.boby.snail.itrustoor.R;
import com.zxing.activity.CaptureActivity;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.support.v4.app.Fragment;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class SetFragment extends Fragment {
	private Dialog dialog;
	private LinearLayout linearscanbarcode, linearlogout, linearsync,
			linearfrequency, linearexit;
	int sfamilyid;
	int sstudentid;
	Data myconfig;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View setLayout = inflater.inflate(R.layout.set, container, false);
		return setLayout;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		myconfig = (Data) getActivity().getApplication();
		linearscanbarcode = (LinearLayout) getActivity().findViewById(
				R.id.linearScanbarcode);
		linearscanbarcode.setOnClickListener(onclicklistener);

		linearlogout = (LinearLayout) getActivity().findViewById(
				R.id.linearLogout);
		linearlogout.setOnClickListener(onclicklistener);

		linearfrequency = (LinearLayout) getActivity().findViewById(
				R.id.linearFrequency);
		linearfrequency.setOnClickListener(onclicklistener);

		linearsync = (LinearLayout) getActivity().findViewById(R.id.linearSync);
		linearsync.setOnClickListener(onclicklistener);

		linearexit = (LinearLayout) getActivity().findViewById(R.id.linearExit);
		linearexit.setOnClickListener(onclicklistener);

		sfamilyid = myconfig.getfamilyid();
		sstudentid = myconfig.getid();
		TextView frequency = (TextView) getActivity().findViewById(
				R.id.frequency);
		switch (myconfig.getfrequency()) {
		case 0:
			frequency.setText("快(15秒)");
			break;
		case 1:
			frequency.setText("中(30秒)");
			break;
		case 2:
			frequency.setText("慢(60秒)");
			break;
		default:
			break;
		}

	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		// super.onActivityResult(requestCode, resultCode, data);
		// 处理扫描结果（在界面上显示）

		if (resultCode == Activity.RESULT_OK) {
			Bundle bundle = data.getExtras();
			String scanResult = bundle.getString("result");

			if (scanResult.substring(0, 5).equals("<sid>")
					& (scanResult.indexOf("<sid>") != -1)
					& (scanResult.indexOf("</sid>") != -1)) {
				String schoolid = scanResult.substring(
						scanResult.indexOf("<sid>") + 5,
						scanResult.indexOf("</sid>"));
				Log.v("debug",
						"sch_id=" + schoolid + "&stu_id="
								+ String.valueOf(sstudentid) + "&fml_id="
								+ String.valueOf(sfamilyid) + "&card="
								+ String.valueOf(sstudentid) + schoolid);
				// 绑定二维码
				bindcard("sch_id=" + schoolid + "&stu_id="
						+ String.valueOf(sstudentid) + "&fml_id="
						+ String.valueOf(sfamilyid) + "&card="
						+ String.valueOf(sstudentid) + "_" + schoolid);

			} else {
				new AlertDialog.Builder(getActivity()).setTitle("提示")
						.setMessage("向服务器提交绑定请求产生错误,返回信息:" + scanResult)
						.setPositiveButton("确定", null).show();
			}
		}
	}

	public OnClickListener onclicklistener = new OnClickListener() {
		//
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.linearScanbarcode:
				// 打开扫描界面扫描条形码或二维码
				Intent openCameraIntent = new Intent(getActivity(),
						CaptureActivity.class);
				startActivityForResult(openCameraIntent, 0);
				break;
			case R.id.linearExit:
				new AlertDialog.Builder(getActivity())
						.setTitle("确认")
						.setMessage("确定要退出程序?")
						.setPositiveButton("确定",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int whichButton) {
										stopservice();
										myconfig.setenablestartservice(false);
										getActivity().finish();
										
										
									
									}

								}).setNegativeButton("取消", null).show();
				break;
				
			case R.id.linearLogout:
				new AlertDialog.Builder(getActivity())
						.setTitle("退出当前帐号")
						.setMessage("确定要切换登录帐号?")
						.setPositiveButton("确定",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int whichButton) {
										stopservice();
										myconfig.logout();// 提交修改
										myconfig.setenablestartservice(false);
										getActivity().finish();
										Intent intent = new Intent(
												getActivity(), Login.class);
										startActivity(intent);
									}

								}).setNegativeButton("取消", null).show();

				// overridePendingTransition(R.anim.slide_in_right,
				// R.anim.slide_out_left);
				// finish();
				break;
			case R.id.linearSync:
				sync("stu_id=" + sstudentid);
				break;
			case R.id.linearFrequency:
				final String[] items = { "快(15秒)", "中(30秒)", "慢(60秒)" };

				new AlertDialog.Builder(getActivity())
						.setTitle("请选择")
						.setIcon(android.R.drawable.ic_dialog_info)
						.setSingleChoiceItems(items, myconfig.getfrequency(),
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int which) {
										TextView frequency = (TextView) getActivity()
												.findViewById(R.id.frequency);
										TextView scanfrequency = (TextView) getActivity()
												.findViewById(
														R.id.scanfrequency);
										frequency.setText(items[which]);
										scanfrequency.setText(items[which]);
										myconfig.setfrequency(which);

										dialog.dismiss();
									}
								}).setNegativeButton("取消", null).show();

				break;
			}
		}
	};

	// 停止后台服务
	public void stopservice() {
		Intent stopIntent = new Intent(getActivity(), ScanWifiService.class);
		getActivity().stopService(stopIntent);

		Intent intent = new Intent(getActivity(), AlarmReceiver.class);
		PendingIntent pi = PendingIntent.getBroadcast(getActivity(), 0, intent,
				0);
		AlarmManager am = (AlarmManager) getActivity().getSystemService(
				Activity.ALARM_SERVICE);
		am.cancel(pi);

	}

	//
	public void sync(String HttpString) {
		dialog = ProgressDialog.show(getActivity(), "", "云端数据同步...");
		HttpUtil.sendHttpPostRequest("/snail/sync", HttpString,
				new HttpCallbackListener() {
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
						new AlertDialog.Builder(getActivity()).setTitle("退出")
								.setMessage("数据同步失败,请检查网络设置或联系客服!")
								.setPositiveButton("确定", null).show();
						Looper.loop();
					}
				});

	}

	// 绑定二维码
	public void bindcard(String HttpString) {
		dialog = ProgressDialog.show(getActivity(), "", "数据绑定上传...");
		HttpUtil.sendHttpPostRequest("/snail/bindCard", HttpString,
				new HttpCallbackListener() {
					@Override
					public void onFinish(String response) {
						dialog.dismiss();
						Message message = new Message();
						message.what = 1;
						message.obj = response.toString();
						handler.sendMessage(message);
					}

					@Override
					public void onError(Exception e) {
						Looper.prepare();
						dialog.dismiss();
						new AlertDialog.Builder(getActivity()).setTitle("退出")
								.setMessage("绑定失败,请检查网络设置或联系客服!")
								.setPositiveButton("确定", null).show();
						Looper.loop();
					}
				});
	};

	public void startservice() {
		Intent startIntent = new Intent(getActivity(), ScanWifiService.class);
		getActivity().startService(startIntent);

	}

	// 接收程序消息
	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			String response = (String) msg.obj;
			JSONObject resObject;
			List<Student> stulist = new ArrayList<Student>();
			try {
				resObject = new JSONObject(response);
				if (resObject.getInt("Code") == 0) {
					JSONArray jsonArray;
					switch (msg.what) {
					case 1:
						// 绑定完成.
						sync("stu_id=" + String.valueOf(sstudentid));
						Log.v("debug", String.valueOf(sstudentid));
						break;
					case 2:
						// 数据同步
						jsonArray = new JSONObject(response)
								.getJSONArray("Data");
						if (!jsonArray.getJSONObject(0).getString("schools")
								.equals("null")) {
							jsonArray = jsonArray.getJSONObject(0)
									.getJSONArray("schools");
							for (int i = 0; i < jsonArray.length(); i++) {
								if ((!jsonArray.getJSONObject(i)
										.getString("macs").equals("null"))
										& (!jsonArray.getJSONObject(i)
												.getString("macs").equals(""))) {
									Student stu = new Student();
									JSONObject jsonObject = jsonArray
											.getJSONObject(i);
									String schid = jsonObject.getString("id");// 学校ID
									stu.setschid(schid);
									String schoolname = jsonObject
											.getString("short_name");// 学校名称
									stu.setschoolname(schoolname);

									String stuid = jsonObject
											.getJSONArray("cards")
											.getJSONObject(0).getString("card");// 学号ID
									stu.setcard(stuid);
									String maclist = "";
									for (int j = 0; j < jsonObject
											.getJSONArray("macs").length(); j++) {
										String mac = jsonObject
												.getJSONArray("macs")
												.getJSONObject(j)
												.getString("mac");
										maclist = maclist + " " + mac;
									}
									stu.setschoollist(maclist);
									stulist.add(stu);
								}
							}
							String strschool = "";
							String school = "[";
							for (int i = 0; i < stulist.size(); i++) {
								school = school + "{\"schid\":" + "\""
										+ stulist.get(i).getschid() + "\""
										+ ",\"cardid\":" + "\""
										+ stulist.get(i).getcard() + "\""
										+ ",\"schoolname\":" + "\""
										+ stulist.get(i).getschoolname() + "\""
										+ ",\"mac\":" + "\""
										+ stulist.get(i).getschool().toString()
										+ "\"}";
								if (i != stulist.size() - 1)
									school = school + ",";

								strschool = strschool + "["
										+ stulist.get(i).getschoolname() + "]";
							}
							school = school + "]";
							myconfig.setschool(school);

							TextView bindbarcode = (TextView) getActivity()
									.findViewById(R.id.bindbarcode);

							List<Wifilist> schlist = new ArrayList<Wifilist>();

							bindbarcode.setText(strschool);

							new AlertDialog.Builder(getActivity())
									.setTitle("提示")
									.setMessage("数据同步完成")
									.setPositiveButton(
											"确定",
											new DialogInterface.OnClickListener() {
												public void onClick(
														DialogInterface dialog,
														int whichButton) {
													stopservice();
													startservice();
												}

											}).show();
							;
						} else {
							Toast.makeText(getActivity(), "云端学校信息为空,同步失败", 0)
									.show();
						}

						break;
					default:
						break;
					}
				} else {
					new AlertDialog.Builder(getActivity())
							.setTitle("服务器返回码:" + resObject.getString("Code"))
							.setMessage(resObject.getString("Msg"))
							.setPositiveButton("确定", null).show(); // 如果返回码不等于0
				}
			} catch (Exception e) {
			}
		}
	};
}
