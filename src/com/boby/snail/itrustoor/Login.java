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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);
		Button btnLogin = (Button) findViewById(R.id.btn_login);
		SharedPreferences pref = getSharedPreferences("snail", MODE_PRIVATE);
		String susername = pref.getString("username", "");
		String spassword = pref.getString("userpassword", "");
		EditText edtPhone = (EditText) findViewById(R.id.edtphone);
		EditText edtPassword = (EditText) findViewById(R.id.edtpassword);
		edtPhone.setText(susername);
		edtPassword.setText(spassword);
		// 密码，长度要是8的倍数
		final String password = "itrustor";
		if (susername != "" && spassword != "") {
			// 登录
			dialog = ProgressDialog.show(Login.this, "", "正在登录云端服务器,请稍候");
			String strlogin;

			user_name = susername;
			user_password = spassword;
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
						}

						@Override
						public void onError(Exception e) {
							Looper.prepare();
							dialog.dismiss();
							new AlertDialog.Builder(Login.this).setTitle("退出")
									.setMessage("登录失败,请检查网络设置或联系客服!")
									.setPositiveButton("确定", null).show();
							Looper.loop();
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
						jsonArray = new JSONObject(response)
								.getJSONArray("Data");
						student_id =jsonArray.getJSONObject(0).getInt("stu_id");
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

						editor.putString("username", user_name);
						editor.putString("userpassword", user_password);
						editor.commit();
						
						final Data myconfig = (Data)getApplication();
						myconfig.setfamilyid(fam_id);
						myconfig.setid(student_id);
						break;
					case 2:
						// 同步数据,如未绑定学校,提示用户学校信息不存在,请扫描二维码添加学校信息
						List<Student> stulist = new ArrayList<Student>();
						jsonArray = new JSONObject(response)
								.getJSONArray("Data");
						if (jsonArray.getJSONObject(0).getString("schools")
								.equals("null")) {
							new AlertDialog.Builder(Login.this)
									.setTitle("提示:")
									.setMessage("未绑定学校信息,请进入系统后扫描二维码进行学校绑定.")
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
											.getString("short_name");// 学校ID
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
							}
							school = school + "]";
							editor.putString("school", school);
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
					new AlertDialog.Builder(Login.this)
							.setTitle("返回错误码:" + resObject.getString("Code"))
							.setMessage(resObject.getString("Msg"))
							.setPositiveButton("确定", null).show();
				}
			} catch (Exception e) {
				Log.i("debug", e.getMessage());
			}
		}
	};

	// 同步数据
	public void syncstu(String HttpString) {
		dialog = ProgressDialog.show(Login.this, "", "正在同步数据...");
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
						new AlertDialog.Builder(Login.this).setTitle("退出")
								.setMessage("数据同步失败,请检查网络设置或联系客服!")
								.setPositiveButton("确定", null).show();
						Looper.loop();
					}
				});
	};

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