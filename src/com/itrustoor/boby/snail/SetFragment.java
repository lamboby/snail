package com.itrustoor.boby.snail;

import com.itrustoor.boby.snail.R;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class SetFragment extends Fragment {
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View setLayout = inflater.inflate(R.layout.set, container, false);
		return setLayout;
	}

}
