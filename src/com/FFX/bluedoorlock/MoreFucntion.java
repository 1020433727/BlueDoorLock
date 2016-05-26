package com.FFX.bluedoorlock;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class MoreFucntion extends Activity{
	
	private Button bt_lighton;
	private Button bt_lightflash;
	private Button bt_bee;
	private Button bt_isclose;
	private Button bt_safemode;
	private Button btn_title;
	private static boolean islighton = false;
	private static boolean issafemodeon = false;
	private static boolean isremindon = false;
	private BluetoothAdapter mBluetoothAdapter;		//����������

	 protected void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        //�����Զ��������
	        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
	        setContentView(R.layout.fuction);
	        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.titlebar);        
	        //��ʼ���ؼ�
	       init();
	 }
	 private void init(){
		 //��ʼ���ؼ�
	        bt_lighton=(Button) findViewById(R.id.button_lighton);
	        bt_lightflash=(Button) findViewById(R.id.button_lightflash);
	        bt_bee=(Button) findViewById(R.id.button_beebee);
	        bt_isclose=(Button) findViewById(R.id.button_remindclose);
	        bt_safemode=(Button) findViewById(R.id.button_savemode);
	        btn_title=(Button) findViewById(R.id.button_title);
	        btn_title.setOnClickListener(listener);
	        bt_lighton.setOnClickListener(listener);
	        bt_lightflash.setOnClickListener(listener);
	        bt_bee.setOnClickListener(listener);
	        bt_isclose.setOnClickListener(listener);
	        bt_safemode.setOnClickListener(listener);
	 }
	 //��ť����¼�
	 private OnClickListener listener=new OnClickListener() {	
		@Override
		public void onClick(View v) {
			String msg=null;
			switch (v.getId()){
			case R.id.button_lighton:
				if(!islighton){
					 msg="*lighton#";
					 islighton=true;
					 bt_lighton.setText("�ص�");
				BluetoothService.newTask(new BluetoothService(mHandler, BluetoothService.TASK_SEND_MSG, new Object[]{msg}));
				}else{
					msg="*lightoff#";
					BluetoothService.newTask(new BluetoothService(mHandler, BluetoothService.TASK_SEND_MSG, new Object[]{msg}));
					bt_lighton.setText("����");
					 islighton=false;
				}break;
			case R.id.button_lightflash:
				msg="*lightflash#";
				BluetoothService.newTask(new BluetoothService(mHandler, BluetoothService.TASK_SEND_MSG, new Object[]{msg}));break;
			case R.id.button_beebee:
			msg="*beepon#";
			BluetoothService.newTask(new BluetoothService(mHandler, BluetoothService.TASK_SEND_MSG, new Object[]{msg}));break;
			case R.id.button_remindclose:
				if(!isremindon){
			msg="*remindon#";
			 bt_isclose.setText("�رչ�������");
			isremindon=true;
			BluetoothService.newTask(new BluetoothService(mHandler, BluetoothService.TASK_SEND_MSG, new Object[]{msg}));
			}else{
				msg="*remindoff#";
				 bt_isclose.setText("������������");
				isremindon=false;
				BluetoothService.newTask(new BluetoothService(mHandler, BluetoothService.TASK_SEND_MSG, new Object[]{msg}));
			}
				break;
			case R.id.button_savemode:
				if(!issafemodeon){
					msg="*alarmon#";
					 bt_safemode.setText("�رտ��ű���");
					issafemodeon=true;
					BluetoothService.newTask(new BluetoothService(mHandler, BluetoothService.TASK_SEND_MSG, new Object[]{msg}));
				}else{
					msg="*alarmoff#";
					 bt_safemode.setText("�������ű���");
					issafemodeon=false;
					BluetoothService.newTask(new BluetoothService(mHandler, BluetoothService.TASK_SEND_MSG, new Object[]{msg}));
				}break;
			case R.id.button_title: 
				mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter(); 
				mBluetoothAdapter.disable();
				break;
			}		
		}
	};
	private Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case BluetoothService.TASK_SEND_MSG:
				Toast.makeText(MoreFucntion.this, msg.obj.toString(), Toast.LENGTH_SHORT).show();
				break;
			case BluetoothService.TASK_GET_REMOTE_STATE:
				setTitle(msg.obj.toString());
				break;
			default:
				break;
			}
		}
	};
}
