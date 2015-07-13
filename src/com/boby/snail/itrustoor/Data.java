package com.boby.snail.itrustoor;

import android.app.Application;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class Data extends Application {
	// wifi扫描频率
	private int wififrequency;
	private int wifiinschool;
	private int familyid;
	private int id;
	private boolean enable3G;
	private boolean enableWifi;
	private String atplace;
	private String atplacetime;
	private MyDatabase dbHelper;
	private boolean disenableScanWifi;
	private String schoolname;
	private boolean DebugMode; 
	public void setenablestartservice(boolean sstart){
		SharedPreferences.Editor editor = getSharedPreferences("snail",
				MODE_PRIVATE).edit();
		editor.putBoolean("startservice", sstart);
		editor.commit();
		
	}
	public Boolean getdebugmode(){
		return DebugMode;
	}
	public void setdebugmode(boolean tb){
		this.DebugMode=tb;
		SharedPreferences.Editor editor = getSharedPreferences("snail",
				MODE_PRIVATE).edit();
		editor.putBoolean("DebugMode", tb);
		editor.commit();
		
	}
	public Boolean getenablestartservice()
	{
		SharedPreferences pref = getSharedPreferences("snail", MODE_PRIVATE);
		return pref.getBoolean("startservice", true);
		
	}
	public String getschoolname(){
		return schoolname;
	}
	public void setschoolname(String sschoolname){
		this.schoolname=sschoolname;
		SharedPreferences.Editor editor = getSharedPreferences("snail",
				MODE_PRIVATE).edit();
		editor.putString("schoolname", sschoolname);
		editor.commit();
		
	}
    public boolean getdisablescanwifi(){
    	return disenableScanWifi;
    }
    public void setdisablescanwifi(boolean sscanwifi)    
    {
    	this.disenableScanWifi=sscanwifi;
    }

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
	public String getwifi() {
		SharedPreferences pref = getSharedPreferences("snail", MODE_PRIVATE);
		return pref.getString("wifi", "");
	}
	public void logout() {
		SharedPreferences sharedPreferences = getSharedPreferences("snail",
				Context.MODE_PRIVATE);
		Editor editor = sharedPreferences.edit();// 获取编辑器
		editor.clear();
		wifiinschool = -1;
		familyid = 0;
		id = 0;
		DebugMode=false;
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
	
	public void setwifi(String sschool) {
		SharedPreferences.Editor editor = getSharedPreferences("snail",
				MODE_PRIVATE).edit();
		editor.putString("wifi", sschool);
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
	
	public boolean getchange3G(){
		return enable3G;
	}
	public void setchange3G(boolean tb){
		this.enable3G=tb;
		SharedPreferences.Editor editor = getSharedPreferences("snail",
				MODE_PRIVATE).edit();
		editor.putBoolean("Enable3G", tb);
		editor.commit();		
	}
	
	public boolean getchangewifi(){
		return enableWifi;
	}
	public void setchangewifi(boolean tb){
		this.enableWifi=tb;
		SharedPreferences.Editor editor = getSharedPreferences("snail",
				MODE_PRIVATE).edit();
		editor.putBoolean("EnableWifi", tb);
		editor.commit();		
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
		// bufferList.add(object);
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("card", object.getcard());
		values.put("atttime", object.getatttime());
		values.put("schoolid", object.getschoolid());
		values.put("isin", object.getIsin());
		values.put("studentid", object.getid());
		db.insert("DataBuffer", null, values);

	}

	public DataBuffer getlist() {
		DataBuffer firstdatabuffer = new DataBuffer();
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		Cursor cursor = db.query("DataBuffer", null, null, null, null, null,
				null);
		if (cursor.getCount() > 0) {
			cursor.moveToFirst();
			firstdatabuffer.setatttime(cursor.getString(cursor
					.getColumnIndex("atttime")));
			firstdatabuffer.setcard(cursor.getString(cursor
					.getColumnIndex("card")));
			firstdatabuffer
					.setIsin(cursor.getInt(cursor.getColumnIndex("isin")));
			
			firstdatabuffer
			.setid(cursor.getInt(cursor.getColumnIndex("studentid")));
			
			firstdatabuffer.setschoolid(cursor.getInt(cursor
					.getColumnIndex("schoolid")));

			return firstdatabuffer;
		}

		else
			return null;

	}

	public int getcount() {
		DataBuffer firstdatabuffer = new DataBuffer();
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		Cursor cursor = db.query("DataBuffer", null, null, null, null, null,
				null);
		return cursor.getCount();
	}

	public void delitem(int delid) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		db.delete("DataBuffer", "studentid=?", new String[] { String.valueOf(delid) });
	}

	@Override
	public void onCreate() {
		SharedPreferences pref = getSharedPreferences("snail", MODE_PRIVATE);
		wififrequency = pref.getInt("wififrequency", 2);
		wifiinschool = pref.getInt("wifiinschool", -1);
		familyid = pref.getInt("familyid", -1);
		schoolname=pref.getString("schoolname", "");
		atplace = pref.getString("atplace", "");
		atplacetime = pref.getString("atplacetime", "");
		id = pref.getInt("id", 0);
		enable3G=pref.getBoolean("Enable3G", true);
		enableWifi=pref.getBoolean("EnableWifi", true);
		disenableScanWifi=false;
		dbHelper = new MyDatabase(this, "Snail.db", null, 2);
		DebugMode=pref.getBoolean("DebugMode", false);
		// dbHelper.getWritableDatabase();
		super.onCreate();
	}

};