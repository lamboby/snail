package com.boby.snail.itrustoor;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.boby.snail.itrustoor.R;
import com.boby.snail.itrustoor.HttpUtil.HttpCallbackListener;
import com.boby.snail.itrustoor.R.id;
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
	private LinearLayout linearscanbarcode,   linearsetpos,linearconfiglan,
			  linearexit, linearabout, linearscanwifi;
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
		linearscanwifi = (LinearLayout) getActivity().findViewById(
				R.id.linearScanWifi);
		linearscanwifi.setOnClickListener(onclicklistener);
		linearscanbarcode = (LinearLayout) getActivity().findViewById(
				R.id.linearScanbarcode);
		linearscanbarcode.setOnClickListener(onclicklistener);
		linearexit=(LinearLayout)getActivity().findViewById(R.id.linearExit);
		linearexit.setOnClickListener(onclicklistener);
	    
		linearabout=(LinearLayout)getActivity().findViewById(R.id.linearAbout);
		linearabout.setOnClickListener(onclicklistener);
		
		linearconfiglan=(LinearLayout)getActivity().findViewById(R.id.linearConfigLan);
		linearconfiglan.setOnClickListener(onclicklistener);
 
		linearsetpos=(LinearLayout)getActivity().findViewById(R.id.linearSetPos);
		linearsetpos.setOnClickListener(onclicklistener);

		
		sfamilyid = myconfig.getfamilyid();
		sstudentid = myconfig.getid();
		

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
			case R.id.linearScanWifi:
				Intent intent = new Intent(getActivity(),
						SelWifi.class);
				startActivityForResult(intent,0);
				break;
		
			case R.id.linearScanbarcode:
				// 打开扫描界面扫描条形码或二维码
				Intent openCameraIntent = new Intent(getActivity(),
						CaptureActivity.class);
				startActivityForResult(openCameraIntent, 0);
				break;
			case	R.id.linearExit:
				Intent exitIntent = new Intent(getActivity(),
						Exit.class);
				startActivityForResult(exitIntent, 0);
				break;
			case	R.id.linearConfigLan:
				Intent ConfigLanIntent = new Intent(getActivity(),
						ConfigLan.class);
				startActivityForResult(ConfigLanIntent, 0);
				break;
			case	R.id.linearAbout:
				Intent aboutIntent = new Intent(getActivity(),
						About.class);
				startActivityForResult(aboutIntent, 0);
				break;
			case R.id.linearSetPos:
				Intent posIntent = new Intent(getActivity(),
						SetPos.class);
				startActivityForResult(posIntent, 0);
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

		public void syncstu(String HttpString) {
			// 同步手动添加的定位点数据
			dialog = ProgressDialog.show(getActivity(), "", "正在同步扫描的定位点数据...");
			Log.v("debug", "第二次同步");
			HttpUtil.sendHttpPostRequest(myconfig.getdebugmode(),"/wifi/syncWifi", HttpString,
					new HttpCallbackListener() {
						@Override
						public void onFinish(String response) {
							dialog.dismiss();
							Message message = new Message();
							message.what = 3;
							message.obj = response.toString();
							handler.sendMessage(message);
							Log.v("debug", "第二次同步完成");
						}

						@Override
						public void onError(Exception e) {
							Looper.prepare();
							dialog.dismiss();
							new AlertDialog.Builder(getActivity()).setTitle("退出")
									.setMessage(e.toString())// "Wifi定位点同步失败,请检查网络设置或联系客服!")
									.setPositiveButton("确定", null).show();
							Looper.loop();
						}
					});
		}

	// 绑定二维码
	public void bindcard(String HttpString) {
		dialog = ProgressDialog.show(getActivity(), "", "数据绑定上传...");
		HttpUtil.sendHttpPostRequest(myconfig.getdebugmode(),"/snail/bindCard", HttpString,
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
						syncstu("stu_id=" + String.valueOf(sstudentid));
						Log.v("debug", String.valueOf(sstudentid));
						break;
					case 2:
//						
						break;
					case 3:
						Log.v("debug", "处理第二次同步数据");
						// 同步数据,如未绑定学校,提示用户学校信息不存在,请扫描二维码添加学校信息
						List<WifiCard> wificardlist = new ArrayList<WifiCard>();
						jsonArray = new JSONObject(response)
								.getJSONArray("Data");
						if (!jsonArray.getJSONObject(0).getString("wifi")
										.equals("null")) {							
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
						}
						else
							myconfig.setwifi("");
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
