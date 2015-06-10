package com.itrustoor.boby.snail;

import com.itrustoor.boby.snail.R;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;

public class PlaceFragment extends Fragment {
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View placeLayout = inflater.inflate(R.layout.place, container, false);
		return placeLayout;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Button startservice = (Button) getActivity().findViewById(
				R.id.btn_startservices);
		Button stopservice = (Button) getActivity().findViewById(
				R.id.stop_services);

		startservice.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent startIntent = new Intent(getActivity(),
						ScanWifiService.class);
				getActivity().startService(startIntent);
			}
		});

		stopservice.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent stopIntent = new Intent(getActivity(),
						ScanWifiService.class);
				getActivity().stopService(stopIntent);
			}
		});
	}

}
