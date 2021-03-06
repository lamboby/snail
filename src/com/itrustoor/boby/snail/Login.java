package com.itrustoor.boby.snail;

import java.util.ArrayList;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.json.JSONArray;
import org.json.JSONObject;

import com.itrustoor.boby.snail.HttpUtil.HttpCallbackListener;
import com.itrustoor.boby.snial.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class Login  extends Activity{
private Dialog dialog;
	
	/*
	 * 学生唯一编号
	 * 用户名
	 * 密码
	 * 学校唯一编号
	 * */
    private String student_id;
	private String card_id;
	private String user_name;
	private String user_password;
	private String sch_id;
	private String fam_id;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.login);
	Button btnLogin =(Button)findViewById(R.id.btn_login);	
	
	SharedPreferences pref=getSharedPreferences("snial",MODE_PRIVATE);
	String susername=pref.getString("username", "");
	String spassword=pref.getString("userpassword", "");
	EditText  edtPhone=(EditText)findViewById(R.id.edtphone);
	EditText edtPassword=(EditText)findViewById(R.id.edtpassword);
	edtPhone.setText(susername);
	edtPassword.setText(spassword);
	 
	//密码，长度要是8的倍数 
	final String password ="itrustor"; 
	
	//用户登录
	btnLogin.setOnClickListener(new OnClickListener(){
		@Override
		public void onClick(View v){			//登录

	       	dialog = ProgressDialog.show(Login.this, "", "正在登录云端服务器,请稍候");
	        String strlogin;
	        EditText  edtPhone=(EditText)findViewById(R.id.edtphone);
	    	EditText edtPassword=(EditText)findViewById(R.id.edtpassword);
	        user_name=edtPhone.getText().toString().trim();
	        user_password=edtPassword.getText().toString().trim();
	 
	        strlogin="phone="+user_name+"&password="+encrypt(user_password,password);
	        HttpUtil.sendHttpPostRequest("http://121.41.49.137:8080/api/snail/login", strlogin,new HttpCallbackListener(){
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
						 new AlertDialog.Builder(Login.this)
		                  .setTitle("退出")
		                  .setMessage("登录失败,请检查网络设置或联系客服!")
		                  .setPositiveButton("确定",null)
		                  . show();	 	  		 
						 	Looper.loop();					   					 
	   				 }
	   			 });
	        	}
	        });
			
		
	}
	
	//接收程序消息
		 private Handler handler=new Handler(){
				public void handleMessage(Message msg){		
					
					String response=(String)msg.obj;			
					JSONObject resObject;
					try {
						final List<MyFamily> list = new ArrayList<MyFamily>();
						resObject = new JSONObject(response);				
						if (resObject.getInt("Code")==0){
							JSONArray jsonArray;							
							switch( msg.what){							
							case 1:     
								//登录完成,开始同步数据.														 
								 jsonArray= new JSONObject(response).getJSONArray("Data");   
								 student_id=jsonArray.getJSONObject(0).getString("stu_id");
							 
								 jsonArray=jsonArray.getJSONObject(0) .getJSONArray("families");
								 
								 for ( int i=0;i<jsonArray.length();i++){	
					    		    	JSONObject jsonObject=jsonArray.getJSONObject(i);
					    		    	String id=jsonObject.getString("id");
					    				String name=jsonObject.getString("name");
					    		    MyFamily myListItem=new MyFamily();
					    		  myListItem.setName(name);
					    		    myListItem.setid(id);
					    		   	list.add(myListItem);
					    		     }	 
								
								 if (list.size()>1) {
									 //家庭数目超过1个,选择家庭,绑定家庭
									    AlertDialog.Builder builder = new AlertDialog.Builder(Login.this);
						                builder.setIcon(R.drawable.ic_launcher2);
						                builder.setTitle("选择一个家庭");
						                //    指定下拉列表的显示数据\
						                
						                 final String[] cities ;
						                 cities=new String[list.size()];
						                 for (int i=0;i<list.size();i++)
						                 {
						                	 cities[i]=list.get(i).getName();
						                 }
						                //    设置一个下拉的列表选择项
						                builder.setItems(cities, new DialogInterface.OnClickListener()
						                {
						                    @Override
						                    public void onClick(DialogInterface dialog, int which)
						                    {
						                        fam_id=list.get(which).getid();
						                    }
						                });
						                builder.show();
						                syncstu("stu_id="+student_id);
								 }
								 if(list.size()==1){
									 //家庭唯一,直接同步信息
									
									 fam_id=list.get(0).getid();
								     syncstu("stu_id="+student_id);
								 }
								 if(list.size()<1){
									 //没有家庭,通知用户必须选注册家庭才能正常使用小蜗牛
									 					
									 new AlertDialog.Builder(Login.this)
					                   .setTitle("提示")
					                   .setMessage("家庭信息为空,请先使用小叮当APP设置家庭信息.")
					                   .setPositiveButton("确定",null)
					                   . show();	 	  	
									 
								 }
															 
								 break;
							case 2:
								//同步数据
								List<Student> stulist = new ArrayList<Student>();
							   
								 jsonArray= new JSONObject(response).getJSONArray("Data");   
								 jsonArray=jsonArray.getJSONObject(0) .getJSONArray("schools");
					
								 for (int  i=0;i<jsonArray.length();i++){	
									 
									 if ( (!  jsonArray.getJSONObject(i).getString("macs").equals("null")) & (!jsonArray.getJSONObject(i).getString("macs").equals("")) )
									 {
										 Student stu=new Student();
										 JSONObject jsonObject=jsonArray.getJSONObject(i);
										 	String schid=jsonObject.getString("id");//学校ID
										 	stu.setschid(schid);
										 	
										 	String stuid=jsonObject.getJSONArray("cards").getJSONObject(0).getString("card");//学号ID
										  	stu.setcard(stuid);
										 	String  maclist="";    
									        for (int j=0;j<jsonObject.getJSONArray("macs").length();j++){
										          String mac=jsonObject.getJSONArray("macs").getJSONObject(j).getString("mac");
										        
										 	      maclist=maclist+" "+mac;							 	
										 	}
								         stu.setschoollist(maclist);
									
									 stulist.add(stu);
									 }
					    		     }	 
								 
								 //保存数据
								 SharedPreferences.Editor editor=getSharedPreferences("snial",MODE_PRIVATE).edit();
								 editor.putString("username", user_name);
								 editor.putString("userpassword", user_password);
								 editor.putString("familyid",fam_id);
								 editor.putString("id",student_id);
								 String school="[";
								 for(int i=0;i<stulist.size();i++){
									 school=school+"{\"schid\":"+ "\"" +stulist.get(i).getschid()+"\""
											 +",\"stuid\":"+"\""+stulist.get(i).getcard()+"\""
											 +",\"mac\":"+"\""+stulist.get(i).getschool().toString()+"\"}";
									 if (i!=stulist.size()-1) school=school+",";
								 }
								 school=school+"]";
								 editor.putString("school", school);
								 editor.commit();
								Intent intent=new Intent(Login.this,MainActivity.class);
								startActivity(intent);
								 overridePendingTransition(R.anim.slide_in_right,  R.anim.slide_out_left);  
								finish();
								 
								 break;
								 
						default:
							break;
							}
					}
					else
					{
						 new AlertDialog.Builder(Login.this)
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
		 
		 //同步数据
		 public void syncstu(String HttpString){
			 	dialog = ProgressDialog.show(Login.this, "", "正在同步数据..."); 		 

		        HttpUtil.sendHttpPostRequest("http://121.41.49.137:8080/api/snail/sync", HttpString,new HttpCallbackListener(){
		   				 @Override
		   				 public void onFinish(String response){
		   						dialog.dismiss();
		   						Message message=new Message();
		   						message.what=2;
		   						message.obj=response.toString();
		   						handler.sendMessage(message);						 
		   				 }
		   				 
		   				 @Override
		   				 public void onError(Exception e)
		   				 {
		   					
		   					 Looper.prepare();
		   					 dialog.dismiss();							
							 new AlertDialog.Builder(Login.this)
			                  .setTitle("退出")
			                  .setMessage("登录失败,请检查网络设置或联系客服!")
			                  .setPositiveButton("确定",null)
			                  . show();	 	  		 
							Looper.loop();				
		   					 
		   				 }
		   			 });
		        	};
		 //DES加密
		 public String encrypt(String data,String key)  {
			 try {
		       		           
		            Cipher cipher = Cipher.getInstance("DES/ECB/NoPadding");
		            int blockSize = cipher.getBlockSize();
		            byte[] dataBytes = data.getBytes();
		            int plaintextLength = dataBytes.length;
		            if (plaintextLength % blockSize != 0) {
		                plaintextLength = plaintextLength + (blockSize - (plaintextLength % blockSize));
		            }
		            byte[] plaintext = new byte[plaintextLength];
		            System.arraycopy(dataBytes, 0, plaintext, 0, dataBytes.length);
		             
		            SecretKeySpec keyspec = new SecretKeySpec(key.getBytes(), "DES");
		           
		            cipher.init(Cipher.ENCRYPT_MODE, keyspec);
		            byte[] encrypted = cipher.doFinal(plaintext);
		            return new String(Base64.encode(encrypted, Base64.DEFAULT),"UTF-8") ;
		        } catch (Exception e) {
		            e.printStackTrace();
		            return null;
		        }
		    }
}
