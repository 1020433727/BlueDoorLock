package com.FFX.bluedoorlock;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.format.Time;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TimePicker;
import android.widget.Toast;

public class TimeSet extends Activity {	
	private Button bt_settime;				//У׼ʱ�䰴ť
	private Button bt_setrmtime;			//���ù�������ʱ�䰴ť
	private Button bt_setaltime;			//���ÿ��ű���ʱ�䰴ť
	private static String settime;			//����У׼ʱ��
	private static String setrmtime;		//��������ʱ��
	private static String setaltime;		//���汨��ʱ��
	private static Time t=new Time();	//ʱ��
	
	protected void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        //�����Զ��������
	  //      requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
	        setContentView(R.layout.timeset);
	//        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.titlebar);     	        
	        //��ʼ���ؼ�
	        init();
}
	private void init(){
		bt_settime=(Button) findViewById(R.id.button_settime);
        bt_settime.setOnClickListener(listener);
        bt_setrmtime=(Button) findViewById(R.id.button_setmindtime);
        bt_setrmtime.setOnClickListener(listener);
        bt_setaltime=(Button) findViewById(R.id.button_setalarmtime);
        bt_setaltime.setOnClickListener(listener);      
	}
	private OnClickListener listener =new OnClickListener() {
	public void onClick(View v) {
		
		switch(v.getId()){
		case R.id.button_settime:	
			settimeOnclick();
			break;
		case R.id.button_setmindtime:
			setremindtimeOnclick();
			break;
		case R.id.button_setalarmtime:
			setrealarmtimeOnclick();
			break;
		}
	}
};	
	//У׼ʱ�䰴ť�����¼�
	private void settimeOnclick(){
		String hour ="00" ;
		String minu ="00";
		String sec = "00";
		t.setToNow();
		if(t.hour<10){hour="0"+String.valueOf(t.hour);}else{hour=String.valueOf(t.hour);}
		if(t.minute<10){minu="0"+String.valueOf(t.minute);}else{minu=String.valueOf(t.minute);}
		if(t.second<10){sec="0"+String.valueOf(t.second);}else{sec=String.valueOf(t.second);}
		settime= "*settime "+hour+"-"+minu+"-"+sec+"#";
		System.out.println(settime);
		BluetoothService.newTask(new BluetoothService(mHandler, BluetoothService.TASK_SEND_MSG, new Object[]{settime}));
		Toast.makeText(TimeSet.this, "У׼ʱ��Ϊ:"+hour+":"+minu+":"+sec, 
				Toast.LENGTH_SHORT).show();
	}
	//��������ʱ�䰴ť�����¼�
	private void setremindtimeOnclick(){
	
		View view = View.inflate(getApplicationContext(), R.layout.remindtimeset, null);  
	    final TimePicker timePicker= (TimePicker)view.findViewById(R.id.timePicker_remind);  
	    // ��ʼ�� TimePicker  
	    int hour;  
	    int minute;  
	     // ʹ�õ�ǰʱ���ʼ��TimePickerʱ��
	    t.setToNow();
	    hour = t.hour;  
	    minute = t.minute;  	 
	    timePicker.setIs24HourView(true);  
	    timePicker.setCurrentHour(hour);  
	    timePicker.setCurrentMinute(minute);    
	    //����AlertDialog.Builder
	    AlertDialog.Builder builder = new AlertDialog.Builder(TimeSet.this);  
	    builder.setView(view);  
	    builder.setTitle("��������ʱ��");  
	    builder.setIcon(R.drawable.bluedoorlock);
	    builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener(){
			public void onClick(DialogInterface arg0, int arg1) {			
			}});
	    builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {  
	        public void onClick(DialogInterface dialog, int which) {
	        	String hour="00";
	        	String minute="00";
	        	if(timePicker.getCurrentHour()<10){
	        		hour="0"+String.valueOf(timePicker.getCurrentHour());
	        	}else{
	        		hour=String.valueOf(timePicker.getCurrentHour());
	        	}
	        	if(timePicker.getCurrentMinute()<10){
	        		minute="0"+String.valueOf(timePicker.getCurrentMinute());
	        	}else{
	        		minute=String.valueOf(timePicker.getCurrentMinute());
	        		}
	        	setrmtime= "*setremindtime "+hour+"-"+minute+"-00#";
				BluetoothService.newTask(new BluetoothService(mHandler, BluetoothService.TASK_SEND_MSG, new Object[]{setrmtime}));
				Toast.makeText(TimeSet.this,"����ʱ��Ϊ:"+hour+":"+minute, 
					  Toast.LENGTH_SHORT).show();
	        }  
	    });   
	    builder.show();  	
	}
	//���ű���ʱ�䰴ť�����¼�
	private void setrealarmtimeOnclick(){
		View view = View.inflate(getApplicationContext(), R.layout.alarmtimeset, null);  
	    final TimePicker timePicker_start = (TimePicker)view.findViewById(R.id.timePicker_start);  
	    final TimePicker timePicker_stop= (TimePicker)view.findViewById(R.id.timePicker_stop);       
	    // ��ʼ�� TimePicker  
	    int hour;  
	    int minute;  
	     // ʹ�õ�ǰʱ���ʼ��TimePickerʱ��
	    t.setToNow();
	    hour = t.hour;  
	    minute = t.minute;  	 
	    timePicker_start.setIs24HourView(true);  
	    timePicker_start.setCurrentHour(hour);  
	    timePicker_start.setCurrentMinute(minute);  
	    timePicker_stop.setIs24HourView(true);  
	    timePicker_stop.setCurrentHour(hour);  
	    timePicker_stop.setCurrentMinute(minute);  
	    //����AlertDialog.Builder
	    AlertDialog.Builder builder = new AlertDialog.Builder(TimeSet.this);  
	    builder.setView(view);  
	    builder.setTitle("���ñ���ʱ��");  
	    builder.setIcon(R.drawable.bluedoorlock);
	    builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener(){
			public void onClick(DialogInterface arg0, int arg1) {			
			}});
	    builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {  
	        public void onClick(DialogInterface dialog, int which) {
	        	String starthour="00";
	        	String startminute="00";
	        	String stophour="00";
	        	String stopminute="00";
	        	if(timePicker_start.getCurrentHour()<10){
	        		starthour="0"+String.valueOf(timePicker_start.getCurrentHour());
	        	}else{
	        		starthour=String.valueOf(timePicker_start.getCurrentHour());
	        	}
	        	if(timePicker_start.getCurrentMinute()<10){
	        		startminute="0"+String.valueOf(timePicker_start.getCurrentMinute());
	        	}else{
	        		startminute=String.valueOf(timePicker_start.getCurrentMinute());
	        		}
	        	if(timePicker_stop.getCurrentHour()<10){
	        		stophour="0"+String.valueOf(timePicker_stop.getCurrentHour());
	        	}else{
	        		stophour=String.valueOf(timePicker_stop.getCurrentHour());
	        		}
	        	if(timePicker_stop.getCurrentMinute()<10){
	        		stopminute="0"+String.valueOf(timePicker_stop.getCurrentMinute());
	        	}else{
	        		stopminute=String.valueOf(timePicker_stop.getCurrentMinute());
	        	}
	        	String starttime=starthour+"-"+startminute+"-00";
	        	String stoptime=stophour+"-"+stopminute+"-00";
	        	setaltime="*setalarmtime "+starttime+","+stoptime+"#";
	        	BluetoothService.newTask(new BluetoothService(mHandler, BluetoothService.TASK_SEND_MSG, new Object[]{setaltime}));
	        	Toast.makeText(TimeSet.this, "����ʱ��Ϊ:"+starthour+":"+startminute+"����"+stophour+":"+stopminute, 
	    				Toast.LENGTH_SHORT).show();
	        }  
	    });   
	    builder.show();  	
	}

	private Handler mHandler = new Handler(){
	@Override
	public void handleMessage(Message msg) {
		switch (msg.what) {
		case BluetoothService.TASK_SEND_MSG:
			Toast.makeText(TimeSet.this, msg.obj.toString(), Toast.LENGTH_SHORT).show();
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