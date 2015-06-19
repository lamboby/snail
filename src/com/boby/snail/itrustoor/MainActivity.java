package com.boby.snail.itrustoor;

import com.boby.snail.itrustoor.R;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.FragmentActivity;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends FragmentActivity implements OnClickListener {
	private PlaceFragment placeFragment;
	private SetFragment setFragment;
	private MsgFragment messageFragment;
	private View placeLayout, setLayout, messageLayout;
	private ImageView placeImage, setImage, messageImage;
	private TextView messageText, setText, placeText;
	private FragmentManager fragmentManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		initViews();
		fragmentManager = getSupportFragmentManager();
		if (savedInstanceState == null) {
			setTabSelection(0);
			setTabSelection(1);
		}
	}

	private void initViews() {
		setLayout = findViewById(R.id.set_layout);
		messageLayout = findViewById(R.id.message_layout);
		placeLayout = findViewById(R.id.place_layout);
		setImage = (ImageView) findViewById(R.id.set_image);
		messageImage = (ImageView) findViewById(R.id.message_image);
		placeImage = (ImageView) findViewById(R.id.place_image);
		setText = (TextView) findViewById(R.id.set_text);
		messageText = (TextView) findViewById(R.id.message_text);
		placeText = (TextView) findViewById(R.id.place_text);
		setLayout.setOnClickListener(this);
		placeLayout.setOnClickListener(this);
		messageLayout.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.place_layout:
			setTabSelection(0);
			break;
		case R.id.message_layout:
			setTabSelection(1);
			break;
		case R.id.set_layout:
			setTabSelection(2);
			break;
		default:
			break;
		}
	}
	//防止重叠显示
	@Override  
	public void onAttachFragment(Fragment fragment) {  
		// TODO Auto-generated method stub  
		super.onAttachFragment(fragment);  
		Log.d("TAG","onAttachFragment");  
		if (placeFragment == null && fragment instanceof PlaceFragment) {  
			placeFragment = (PlaceFragment)fragment;  
		}else if (messageFragment == null && fragment instanceof MsgFragment) {  
			messageFragment = (MsgFragment)fragment;  
		}else if (setFragment == null && fragment instanceof SetFragment) {  
			setFragment = (SetFragment)fragment;  
		}  
	 }  

	private void setTabSelection(int index) {
		// 每次选中之前先清楚掉上次的选中状态
		clearSelection();
		// 开启一个Fragment事务
		FragmentTransaction transaction = fragmentManager.beginTransaction();
		// 先隐藏掉所有的Fragment，以防止有多个Fragment显示在界面上的情况
		hideFragments(transaction);
		switch (index) {
		case 0:
			placeImage.setImageResource(R.drawable.placesel);
			placeText.setTextColor(Color.rgb(5, 171, 5));
			if (placeFragment == null) {
				placeFragment = new PlaceFragment();
				transaction.add(R.id.content, placeFragment);
			} else {
				transaction.show(placeFragment);
			}
			break;
		case 1:
			messageImage.setImageResource(R.drawable.msgsel);
			messageText.setTextColor(Color.rgb(5, 171, 5));
			if (messageFragment == null) {
				messageFragment = new MsgFragment();
				transaction.add(R.id.content, messageFragment);
			} else {
				transaction.show(messageFragment);
			}
			break;
		case 2:
			// 当点击了设置tab时，改变控件的图片和文字颜色
			setImage.setImageResource(R.drawable.setsel);
			setText.setTextColor(Color.rgb(5, 171, 5));
			if (setFragment == null) {
				// 如果SettingFragment为空，则创建一个并添加到界面上
				setFragment = new SetFragment();
				transaction.add(R.id.content, setFragment);
			} else {
				// 如果SettingFragment不为空，则直接将它显示出来
				transaction.show(setFragment);
			}
			break;
		default:

		}
		transaction.commit();
	}

	private void clearSelection() {
		placeImage.setImageResource(R.drawable.placeunsel);
		placeText.setTextColor(Color.parseColor("#999999"));
		messageImage.setImageResource(R.drawable.msgunsel);
		messageText.setTextColor(Color.parseColor("#999999"));
		setImage.setImageResource(R.drawable.setunsel);
		setText.setTextColor(Color.parseColor("#999999"));
	}

	private void hideFragments(FragmentTransaction transaction) {
		if (placeFragment != null) {
			transaction.hide(placeFragment);
		}
		if (messageFragment != null) {
			transaction.hide(messageFragment);
		}

		if (setFragment != null) {
			transaction.hide(setFragment);
		}
	}
	

	// 屏蔽返回健
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (KeyEvent.KEYCODE_BACK == keyCode)
			return false;
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
