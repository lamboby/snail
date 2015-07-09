package com.boby.snail.itrustoor;

import java.util.ArrayList;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import java.net.URLEncoder;

import org.json.JSONArray;
import org.json.JSONObject;

import com.boby.snail.itrustoor.HttpUtil.HttpCallbackListener;
import com.boby.snail.itrustoor.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Base64;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.util.Log;

public class Login extends Activity {
	private Dialog dialog;
	/*
	 * 学生唯一编号 用户名 密码 学校唯一编号
	 */
	private int student_id;
	private String user_name;
	private String user_password;
	private int fam_id;
	private final String password = "itrustor";
	private boolean enableSaveLogin = true;
	private boolean schoolNull = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);
		SysApplication.getInstance().addActivity(this); 
		Button btnLogin = (Button) findViewById(R.id.btn_login);
		SharedPreferences pref = getSharedPreferences("snail", MODE_PRIVATE);
		user_name = pref.getString("username", "");
		user_password = pref.getString("userpassword", "");
		EditText edtPhone = (EditText) findViewById(R.id.edtphone);
		// EditText edtPassword = (EditText) findViewById(R.id.edtpassword);
		edtPhone.setText(user_name);
		// edtPassword.setText(spassword);
		// 密码，长度要是8的倍数
		if (user_name != "" && user_password != "") {
			// 登录
			dialog = ProgressDialog.show(Login.this, "", "正在登录云端服务器,请稍候");
			String strlogin;
			strlogin = "phone=" + user_name + "&password=" + user_password;
			HttpUtil.sendHttpPostRequest("/snail/login", strlogin,
					new HttpCallbackListener() {
						@Override
						public void onFinish(String response) {
							enableSaveLogin = false;
							dialog.dismiss();
							Message message = new Message();
							message.what = 1;
							message.obj = response.toString();
							Log.v("res", response.toString());
							handler.sendMessage(message);
						}

						@Override
						public void onError(Exception e) {
							// 登录错误,直接进入主界面
							Log.v("debug", "登录失败");
							Intent intent = new Intent(Login.this,
									MainActivity.class);
							startActivity(intent);
							overridePendingTransition(R.anim.slide_in_right,
									R.anim.slide_out_left);
							finish();
						}
					});
		}

		// 用户登录
		btnLogin.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// 登录
				dialog = ProgressDialog.show(Login.this, "", "正在登录云端服务器,请稍候");
				String strlogin;
				EditText edtPhone = (EditText) findViewById(R.id.edtphone);
				EditText edtPassword = (EditText) findViewById(R.id.edtpassword);
				user_name = edtPhone.getText().toString().trim();
				user_password = edtPassword.getText().toString().trim();
				strlogin = "phone=" + user_name + "&password="
						+ encrypt(user_password, password);
				HttpUtil.sendHttpPostRequest("/snail/login", strlogin,
						new HttpCallbackListener() {
							@Override
							public void onFinish(String response) {
								dialog.dismiss();
								Message message = new Message();
								message.what = 1;
								message.obj = response.toString();
								handler.sendMessage(message);
								enableSaveLogin = true;
							}

							@Override
							public void onError(Exception e) {
								Looper.prepare();
								dialog.dismiss();
								new AlertDialog.Builder(Login.this)
										.setTitle("退出")
										.setMessage("登录失败,请检查网络设置或联系客服!")
										.setPositiveButton("确定", null).show();
								Looper.loop();
							}
						});
			}
		});
	}

	// 接收程序消息
	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			String response = (String) msg.obj;
			JSONObject resObject;
			try {
				SharedPreferences.Editor editor = getSharedPreferences("snail",
						MODE_PRIVATE).edit();
				final List<MyFamily> list = new ArrayList<MyFamily>();
				resObject = new JSONObject(response);
				if (resObject.getInt("Code") == 0) {
					JSONArray jsonArray;
					switch (msg.what) {
					case 1:
						// 登录完成,开始同步数据.
						Log.v("debug", "登录完成");
						jsonArray = new JSONObject(response)
								.getJSONArray("Data");
						student_id = jsonArray.getJSONObject(0)
								.getInt("stu_id");
						jsonArray = jsonArray.getJSONObject(0).getJSONArray(
								"families");
						for (int i = 0; i < jsonArray.length(); i++) {
							JSONObject jsonObject = jsonArray.getJSONObject(i);
							int id = jsonObject.getInt("id");
							String name = jsonObject.getString("name");
							MyFamily myListItem = new MyFamily();
							myListItem.setName(name);
							myListItem.setid(id);
							list.add(myListItem);
						}

						if (list.size() > 1) {
							// 家庭数目超过1个,选择家庭,绑定家庭
							AlertDialog.Builder builder = new AlertDialog.Builder(
									Login.this);
							builder.setIcon(R.drawable.ic_launcher2);
							builder.setTitle("选择一个家庭");
							// 指定下拉列表的显示数据
							final String[] cities;
							cities = new String[list.size()];
							for (int i = 0; i < list.size(); i++) {
								cities[i] = list.get(i).getName();
							}
							// 设置一个下拉的列表选择项
							builder.setItems(cities,
									new DialogInterface.OnClickListener() {
										@Override
										public void onClick(
												DialogInterface dialog,
												int which) {
											fam_id = list.get(which).getid();
										}
									});
							builder.show();
							syncstu("stu_id=" + student_id);
						}
						if (list.size() == 1) {
							// 家庭唯一,直接同步信息
							fam_id = list.get(0).getid();
							syncstu("stu_id=" + student_id);
						}
						if (list.size() < 1) {
							// 没有家庭,通知用户必须选注册家庭才能正常使用小蜗牛
							new AlertDialog.Builder(Login.this).setTitle("提示")
									.setMessage("家庭信息为空,请先使用小叮当APP设置家庭信息.")
									.setPositiveButton("确定", null).show();
						}
						// 保存数据
						if (enableSaveLogin) {
							editor.putString("username", user_name);
							editor.putString("userpassword",
									encrypt(user_password, password));
							editor.commit();
						}
						final Data myconfig = (Data) getApplication();
						myconfig.setfamilyid(fam_id);
						myconfig.setid(student_id);
						break;
					case 2:
						break;
					case 3:
						Log.v("debug", "处理第二次同步数据");
						// 同步数据,如未绑定学校,提示用户学校信息不存在,请扫描二维码添加学校信息
						List<WifiCard> wificardlist = new ArrayList<WifiCard>();
						jsonArray = new JSONObject(response)
								.getJSONArray("Data");
						if  (jsonArray.getJSONObject(0).getString("wifi")
										.equals("null")) {
							editor.putString("wifi", "");
							editor.commit();
							new AlertDialog.Builder(Login.this)
									.setTitle("提示:")
									.setMessage("未绑定Wifi定位点信息,请进入软件后进行绑定.")
									.setPositiveButton(
											"确定",
											new DialogInterface.OnClickListener() {
												@Override
												public void onClick(
														DialogInterface dialog,
														int i) {
													
													Intent intent = new Intent(
															Login.this,
															MainActivity.class);
													startActivity(intent);
												}
											}).show();
						} else {
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
							editor.putString("wifi", school);
							editor.commit();

							Intent intent = new Intent(Login.this,
									MainActivity.class);
							startActivity(intent);
							overridePendingTransition(R.anim.slide_in_right,
									R.anim.slide_out_left);
							finish();
						}
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

					new AlertDialog.Builder(Login.this)
							.setTitle("返回错误码:" + resObject.getString("Code"))
							.setMessage(reserr).setPositiveButton("确定", null)
							.show();
				}
			} catch (Exception e) {
				Log.i("debug", e.getMessage());
			}
		}
	};

	public void syncstu(String HttpString) {
		// 同步手动添加的定位点数据
		dialog = ProgressDialog.show(Login.this, "", "正在同步扫描的定位点数据...");
		Log.v("debug", "第二次同步");
		HttpUtil.sendHttpPostRequest("/wifi/syncWifi", HttpString,
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
						new AlertDialog.Builder(Login.this).setTitle("退出")
								.setMessage(e.toString())// "Wifi定位点同步失败,请检查网络设置或联系客服!")
								.setPositiveButton("确定", null).show();
						Looper.loop();
					}
				});
	}

	// DES加密
	public String encrypt(String data, String key) {
		try {
			Cipher cipher = Cipher.getInstance("DES/ECB/NoPadding");
			int blockSize = cipher.getBlockSize();
			byte[] dataBytes = data.getBytes();
			int plaintextLength = dataBytes.length;
			if (plaintextLength % blockSize != 0) {
				plaintextLength = plaintextLength
						+ (blockSize - (plaintextLength % blockSize));
			}
			byte[] plaintext = new byte[plaintextLength];
			System.arraycopy(dataBytes, 0, plaintext, 0, dataBytes.length);
			SecretKeySpec keyspec = new SecretKeySpec(key.getBytes(), "DES");
			cipher.init(Cipher.ENCRYPT_MODE, keyspec);
			byte[] encrypted = cipher.doFinal(plaintext);
			String strt = new String(Base64.encode(encrypted, Base64.DEFAULT),
					"UTF-8");
			return URLEncoder.encode(strt, "utf8");
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
