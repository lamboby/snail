
package com.boby.snail.itrustoor;



import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.boby.snail.itrustoor.R;

import android.support.v4.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


public class PlaceFragment extends Fragment{	
	 
	
	@Override
	public View onCreateView(LayoutInflater inflater,ViewGroup container,Bundle savedInstanceState){
		View barcodeLayout=inflater.inflate(R.layout.place, container,false);
		return barcodeLayout;		
	}	
	@Override  
    public void onActivityCreated(Bundle savedInstanceState) {  
        super.onActivityCreated(savedInstanceState); 
        final Data myconfig = (Data)getActivity().getApplication();
     	TextView scanfrequency = (TextView) getActivity()
				.findViewById(
						R.id.scanfrequency);
     	TextView bindbarcode=(TextView)getActivity().findViewById(R.id.bindbarcode);
     	TextView viewSchoolname = (TextView) getActivity().findViewById(
				R.id.atschoolname);
		TextView viewTime = (TextView) getActivity().findViewById(
				R.id.atschooltime);
		viewSchoolname.setText(myconfig.getatplace());
		viewTime.setText(myconfig.getatplacetime());
		
		Log.v("debug","位置页面建立");
     	switch (myconfig.getfrequency()){
     	case 0:
     		scanfrequency.setText("快(15秒)");
     		break;
     	case 1:
     		scanfrequency.setText("中(30秒)");
     		break;
     	case 2:
     		scanfrequency.setText("慢(60秒)");
     		break;
     	default:
     		break;
     	}
     	List<Wifilist> schlist = new ArrayList<Wifilist>();
     	String strschool="";
     	String savestring = myconfig.getschool();// 保存的MAC地址与学校对应信息
		
		JSONArray jsonArray;
		try {
			jsonArray = new JSONArray(savestring);
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject jsonObject;
				jsonObject = jsonArray.getJSONObject(i);				
				strschool =strschool+"["+jsonObject.getString("schoolname")+"]";			 
			}

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		bindbarcode.setText(strschool);

	}
  
}
	
	
	
