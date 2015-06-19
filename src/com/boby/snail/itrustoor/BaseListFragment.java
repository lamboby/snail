package com.boby.snail.itrustoor;

import java.util.Map;

import android.app.Activity;
import android.support.v4.app.ListFragment;
import android.content.Context;
import android.os.Bundle;

public class BaseListFragment extends ListFragment {
	public Map<String, String> map;
    public String tag = this.getClass().getSimpleName(); // tag 用于测试log用
	public Context context; // 存储上下文对象
	public Activity activity; // 存储上下文对象
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		context = getActivity();
		activity = getActivity();
	}
}