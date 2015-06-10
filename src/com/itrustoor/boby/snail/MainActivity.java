package com.itrustoor.boby.snail;

import com.itrustoor.boby.snail.R;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.graphics.Color;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends Activity implements OnClickListener {
	private BarcodeFragment barcodeFragment;
	private SetFragment setFragment;
	private PlaceFragment placeFragment;
	private View barcodeLayout, setLayout, placeLayout;
	private ImageView barcodeImage, setImage, placeImage;
	private TextView barcodeText, setText, placeText;
	private FragmentManager fragmentManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		initViews();
		fragmentManager = getFragmentManager();
		if (savedInstanceState == null) {
			setTabSelection(0);
		}
	}

	private void initViews() {
		setLayout = findViewById(R.id.set_layout);
		placeLayout = findViewById(R.id.place_layout);
		barcodeLayout = findViewById(R.id.barcode_layout);
		setImage = (ImageView) findViewById(R.id.set_image);
		placeImage = (ImageView) findViewById(R.id.place_image);
		barcodeImage = (ImageView) findViewById(R.id.barcode_image);
		setText = (TextView) findViewById(R.id.set_text);
		placeText = (TextView) findViewById(R.id.place_text);
		barcodeText = (TextView) findViewById(R.id.barcode_text);
		setLayout.setOnClickListener(this);
		placeLayout.setOnClickListener(this);
		barcodeLayout.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.barcode_layout:
			setTabSelection(0);
			break;
		case R.id.place_layout:
			setTabSelection(1);
			break;
		case R.id.set_layout:
			setTabSelection(2);
			break;
		default:
			break;
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
			barcodeImage.setImageResource(R.drawable.placesel);
			barcodeText.setTextColor(Color.rgb(5, 171, 5));
			if (barcodeFragment == null) {
				barcodeFragment = new BarcodeFragment();
				transaction.add(R.id.content, barcodeFragment);
			} else {
				transaction.show(barcodeFragment);
			}
			break;
		case 1:
			placeImage.setImageResource(R.drawable.msgsel);
			placeText.setTextColor(Color.rgb(5, 171, 5));
			if (placeFragment == null) {
				placeFragment = new PlaceFragment();
				transaction.add(R.id.content, placeFragment);
			} else {
				transaction.show(placeFragment);
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
		barcodeImage.setImageResource(R.drawable.placeunsel);
		barcodeText.setTextColor(Color.parseColor("#999999"));
		placeImage.setImageResource(R.drawable.msgunsel);
		placeText.setTextColor(Color.parseColor("#999999"));
		setImage.setImageResource(R.drawable.setunsel);
		setText.setTextColor(Color.parseColor("#999999"));
	}

	private void hideFragments(FragmentTransaction transaction) {
		if (barcodeFragment != null) {
			transaction.hide(barcodeFragment);
		}
		if (placeFragment != null) {
			transaction.hide(placeFragment);
		}

		if (setFragment != null) {
			transaction.hide(setFragment);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
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
