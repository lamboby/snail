package com.boby.snail.itrustoor;

import java.util.ArrayList;
import java.util.List;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class Data extends Application {
	// wifi扫描频率
	private int wififrequency;
	private int wifiinschool;
	private int familyid;
	private int id;
	private String atplace;
	private String atplacetime;

	private List<DataBuffer> bufferList;

	public String getatplace() {
		return atplace;
	}

	public void setatplace(String satplace) {
		SharedPreferences.Editor editor = getSharedPreferences("snail",
				MODE_PRIVATE).edit();
		editor.putString("atplace", satplace);
		editor.commit();
		atplace = satplace;

	}

	public String getatplacetime() {
		return atplacetime;
	}

	public void setatplacetime(String satplacetime) {
		SharedPreferences.Editor editor = getSharedPreferences("snail",
				MODE_PRIVATE).edit();
		editor.putString("atplacetime", satplacetime);
		atplacetime = satplacetime;
		editor.commit();
	}

	public String getschool() {
		SharedPreferences pref = getSharedPreferences("snail", MODE_PRIVATE);
		return pref.getString("school", "");
	}

	public void logout() {
		SharedPreferences sharedPreferences = getSharedPreferences("snail",
				Context.MODE_PRIVATE);
		Editor editor = sharedPreferences.edit();// 获取编辑器
		editor.clear();
		wifiinschool = -1;
		familyid = 0;
		id = 0;
		atplace = "";
		atplacetime = "";
		editor.commit();// 提交修改
	}

	public void setschool(String sschool) {
		SharedPreferences.Editor editor = getSharedPreferences("snail",
				MODE_PRIVATE).edit();
		editor.putString("school", sschool);
		editor.commit();
	}

	public int getfamilyid() {
		return familyid;
	}

	public void setfamilyid(int sfamilyid) {
		SharedPreferences.Editor editor = getSharedPreferences("snail",
				MODE_PRIVATE).edit();
		editor.putInt("familyid", sfamilyid);
		editor.commit();
		this.familyid = sfamilyid;

	}

	public int getfrequency() {
		return wififrequency;
	}

	public void setfrequency(int sfrequency) {
		this.wififrequency = sfrequency;
		SharedPreferences.Editor editor = getSharedPreferences("snail",
				MODE_PRIVATE).edit();
		editor.putInt("wififrequency", sfrequency);
		editor.commit();
	}

	public void setinschool(int sinschool) {
		this.wifiinschool = sinschool;
		SharedPreferences.Editor editor = getSharedPreferences("snail",
				MODE_PRIVATE).edit();
		editor.putInt("wifiinschool", sinschool);
		editor.commit();
	}

	public int getinschool() {
		return wifiinschool;
	}

	public void setid(int sid) {
		this.id = sid;
		SharedPreferences.Editor editor = getSharedPreferences("snail",
				MODE_PRIVATE).edit();
		editor.putInt("id", sid);
		editor.commit();
	}

	public int getid() {
		return id;
	}

	public void additem(DataBuffer object) {
		bufferList.add(object);
	}

	public DataBuffer getlist() {
		if (bufferList.size() > 0) {
			return bufferList.get(0);
		} else
			return null;

	}
	
	public int getcount(){
		return bufferList.size();
	}

	public void delitem() {
		if (bufferList.size() > 0) {
			bufferList.remove(0);
		}
	}

	@Override
	public void onCreate() {
		SharedPreferences pref = getSharedPreferences("snail", MODE_PRIVATE);
		wififrequency = pref.getInt("wififrequency", 0);
		wifiinschool = pref.getInt("wifiinschool", -1);
		familyid = pref.getInt("familyid", -1);
		atplace = pref.getString("atplace", "");
		atplacetime = pref.getString("atplacetime", "");
		id = pref.getInt("id", 0);
		bufferList = new ArrayList<DataBuffer>();
		super.onCreate();
	}

}