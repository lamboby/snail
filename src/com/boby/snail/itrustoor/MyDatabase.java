package com.boby.snail.itrustoor;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class MyDatabase extends SQLiteOpenHelper {

	private Context mContext;
	public static final String CREATE_TABLE = "create table DataBuffer("
			+ "id integer primary key autoincrement,card "
			+ " text,atttime text,schoolid integer,studentid integer,isin integer)";

	public MyDatabase(Context context, String name, CursorFactory factory,
			int version) {
		super(context, name, factory, version);
		mContext = context;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(CREATE_TABLE);
		Log.v("debug","建立数据库");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("drop table if exists DataBuffer");
		onCreate(db);

	}

}
