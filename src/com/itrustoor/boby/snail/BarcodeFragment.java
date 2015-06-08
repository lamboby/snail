
package com.itrustoor.boby.snail;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.itrustoor.boby.snail.HttpUtil.HttpCallbackListener;
import com.itrustoor.boby.snial.R;
import com.zxing.activity.CaptureActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class BarcodeFragment extends Fragment{
	
	private Dialog dialog;
	@Override
	public View onCreateView(LayoutInflater inflater,ViewGroup container,Bundle savedInstanceState){
		View barcodeLayout=inflater.inflate(R.layout.barcode, container,false);
		return barcodeLayout;
	}

	
	@Override  
    public void onActivityCreated(Bundle savedInstanceState) {  
        super.onActivityCreated(savedInstanceState);  
        Button       scanBarCodeButton = (Button) getActivity().findViewById(R.id.btn_scanbarcode);  
      
        scanBarCodeButton.setOnClickListener(new OnClickListener() {  
            @Override  
            public void onClick(View v) {  
            	//打开扫描界面扫描条形码或二维码
				Intent openCameraIntent = new Intent(getActivity(),CaptureActivity.class);
				startActivityForResult(openCameraIntent, 0);
            }  
        });  
    }  
	

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
	//	super.onActivityResult(requestCode, resultCode, data);
		//处理扫描结果（在界面上显示）
		if (resultCode == Activity.RESULT_OK) {
			Bundle bundle = data.getExtras();
			String scanResult = bundle.getString("result");				
				EditText tschoolfullname=(EditText)getView().findViewById(R.id.Tschoolfullname);
				EditText tshoolshrotname=(EditText)getView().findViewById(R.id.Tschoolshortname);
				EditText listwifi=(EditText)getView().findViewById(R.id.Listwifi);				
				if ( scanResult.substring(0,4).equals("<fn>") & scanResult.substring(scanResult.length()-1).equals(">")
						& (scanResult.indexOf("</fn>")!=-1)& (scanResult.indexOf("<sid>")!=-1)& (scanResult.indexOf("</sid>")!=-1)
							& (scanResult.indexOf("mnum>")!=-1)) {				
								tschoolfullname.setText(scanResult.substring(scanResult.indexOf("<fn>")+4,scanResult.indexOf("</fn>")));
								String schoolid=scanResult.substring(scanResult.indexOf("<sid>")+5,scanResult.indexOf("</sid>"));
								tshoolshrotname.setText(schoolid);				
								Integer macnum=Integer.parseInt(scanResult.substring(scanResult.indexOf("<mnum>")+6,scanResult.indexOf("</mnum>")));
								String strmac="";
								if (macnum>0){					
									for(int i=0;i<macnum;i++){							  
										strmac=strmac+(scanResult.substring(scanResult.indexOf("<m"+i+">")+4,scanResult.indexOf("</m"+i+">")))+"\n";
								}
								listwifi.setText(strmac);
							}	
								
								SharedPreferences pref=getActivity().getSharedPreferences("snial",getActivity().MODE_PRIVATE);
								String sfamilyid=pref.getString("familyid", "");
								String sstudentid=pref.getString("id", "");
								//绑定二维码
								bindcard("sch_id="+schoolid+"&stu_id="+sstudentid+"&fml_id="+sfamilyid+"&card="+sstudentid+schoolid);
								
								
				}
		else
		{
			tschoolfullname.setText("地址校验错误");}		
		}	
	}
	
	
	 //绑定二维码
	 public void bindcard(String HttpString){
		 	dialog = ProgressDialog.show(getActivity(), "", "数据绑定上传..."); 		 
		 	
	        HttpUtil.sendHttpPostRequest("http://121.41.49.137:8080/api/snail/bindCard", HttpString,new HttpCallbackListener(){
	   				 @Override
	   				 public void onFinish(String response){
	   						dialog.dismiss();
	   						Message message=new Message();
	   						message.what=1;
	   						message.obj=response.toString();
	   						handler.sendMessage(message);						 
	   				 }
	   				 
	   				 @Override
	   				 public void onError(Exception e)
	   				 {
	   					
	   					 Looper.prepare();
	   					 dialog.dismiss();							
						 new AlertDialog.Builder(getActivity())
		                  .setTitle("退出")
		                  .setMessage("登录失败,请检查网络设置或联系客服!")
		                  .setPositiveButton("确定",null)
		                  . show();	 	  		 
						Looper.loop();				
	   					 
	   				 }
	   			 });
	        	};
	        	
	        	//接收程序消息
	   		 private Handler handler=new Handler(){
	   				public void handleMessage(Message msg){		
	   					
	   					String response=(String)msg.obj;			
	   					JSONObject resObject;
	   					try {
	   						 
	   						resObject = new JSONObject(response);				
	   						if (resObject.getInt("Code")==0){
	   											
	   							switch( msg.what){							
	   							case 1:     
	   								//同步完成.														 
	   								Toast.makeText(getActivity(), "完成二维码绑定", 0).show();  
	   															 
	   								 break;
	   						
	   								 
	   						default:
	   							break;
	   							}
	   					}
	   					else
	   					{
	   						 new AlertDialog.Builder(getActivity())
	   		                  .setTitle("服务器返回码:"+resObject.getString("Code"))
	   		                  .setMessage(resObject.getString("Msg"))
	   		                  .setPositiveButton("确定",null)
	   		                  . show();	 	  		 //如果返回码不等于0
	   					}
	   		}
	   		catch (Exception e) {  
	   		}  	 	
	   					}
	   			
	   		 }; 
}
