package com.FFX.bluedoorlock;


import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	
	private static final int REQUES_SELECT_BT_CODE = 0x1001; 	//ѡ���豸��
//	private static final int REQUES_BT_ENABLE_CODE = 0x1002;	//ʹ��������
	private BluetoothAdapter mBluetoothAdapter;							//����������
	private BluetoothDevice mRemoteDevice;									//�û�ѡ�����ӵ������豸
	private Button btn_scan;
	private Button btn_send;
	private Button btn_time;
	private Button btn_more;
	private Button btn_title;
	private TextView tv_title;
	private ImageButton imbtn_open;
	private static boolean isopen = false;	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.activity_main);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.titlebar);
        //��ʼ���ؼ������ü�����
        init();
       // �������ƥ����
    	mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter(); 
    	// �����豸����֧��
    	if (mBluetoothAdapter == null) {
    		Toast.makeText(this, "���豸û�������豸", Toast.LENGTH_LONG).show();	
    	}
    	// ʹ�������豸
    	mBluetoothAdapter.enable();
  //  	if (!mBluetoothAdapter.isEnabled()) {
  //  			Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
 //   			startActivityForResult(enableBtIntent, REQUES_BT_ENABLE_CODE); 
  //  	}
       while (!mBluetoothAdapter.isEnabled()){
        	 try {  
                 Thread.currentThread();  
                 Thread.sleep(100);  
             } catch (InterruptedException e) {  
                 e.printStackTrace();  
             }     
       }
	    	String address="98:D3:32:20:54:0A";
	    	BluetoothDevice AutoDevice=mBluetoothAdapter.getRemoteDevice (address);
	    	BluetoothService.newTask(new BluetoothService(mHandler, BluetoothService.TASK_START_CONN_THREAD, 
	    			new Object[]{AutoDevice}));
        }
    private void init(){
    	 //��ʼ���ؼ�
        btn_scan=(Button) findViewById(R.id.button_scan);
        btn_send=(Button) findViewById(R.id.button_send);
        btn_time=(Button) findViewById(R.id.button_time);
        btn_more=(Button) findViewById(R.id.button_more);
        btn_title=(Button) findViewById(R.id.button_title);
        tv_title=(TextView) findViewById(R.id.textView_title);
        imbtn_open=(ImageButton) findViewById(R.id.imageButton_open);
        //���ü�����
        btn_scan.setOnClickListener(listener);
        btn_send .setOnClickListener(listener);
        btn_time.setOnClickListener(listener);
        btn_more.setOnClickListener(listener);
        btn_title.setOnClickListener(listener);
       imbtn_open.setOnClickListener(listener);
    }
    private OnClickListener listener =new OnClickListener() {
		@Override
		public void onClick(View v) {
			switch(v.getId()){
			case R.id.button_scan: 
				// ɨ����Χ�����豸
				startActivityForResult(new Intent(MainActivity.this, SelectDevice.class), REQUES_SELECT_BT_CODE);break;
			case R.id.button_send: 
				startActivity(new Intent(MainActivity.this, CommandInput.class));break;
			case R.id.button_time: 
				startActivity(new Intent(MainActivity.this, TimeSet.class));break;
			case R.id.button_more: 
				startActivity(new Intent(MainActivity.this, MoreFucntion.class));break;
			case R.id.button_title: 
				mBluetoothAdapter.disable();
				break;
			case R.id.imageButton_open: 
				// ��������Ϣ�����ύ����̨����
				if(!isopen){
					String msg="*unlock#";
					isopen=true;
					imbtn_open.setImageDrawable(getResources().getDrawable(R.drawable.unlock));
					BluetoothService.newTask(new BluetoothService(mHandler, BluetoothService.TASK_SEND_MSG,
							new Object[]{msg}));
					break;
				}else{
					String msg="*lock#";
					isopen=false;
					imbtn_open.setImageDrawable(getResources().getDrawable(R.drawable.lock));
					BluetoothService.newTask(new BluetoothService(mHandler, BluetoothService.TASK_SEND_MSG, new Object[]{msg}));break;
				}
		   }
		}
	};
	// ��startActivityForResult������ ���������ʱ�򣬸÷������ص�
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {	
			 if(requestCode == REQUES_SELECT_BT_CODE && resultCode == RESULT_OK){
				mRemoteDevice = data.getParcelableExtra("DEVICE");
				if(mRemoteDevice == null)
					return;
				// �ύ�����û�ѡ����豸�����Լ���Ϊ�ͻ���
				BluetoothService.newTask(new BluetoothService(mHandler, BluetoothService.TASK_START_CONN_THREAD, new Object[]{mRemoteDevice}));
				BluetoothService.start(this, mHandler);
			}
		}
	private Handler mHandler = new Handler(){
   @Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case BluetoothService.TASK_SEND_MSG:
				Toast.makeText(MainActivity.this, msg.obj.toString(), Toast.LENGTH_SHORT).show();
				break;
			case BluetoothService.TASK_RECV_MSG:
				if(msg.obj.toString().equals("on")){
					imbtn_open.setImageDrawable(getResources().getDrawable(R.drawable.unlock));
				}
				else if(msg.obj.toString().equals("off")){
					imbtn_open.setImageDrawable(getResources().getDrawable(R.drawable.lock));
				}
				break;
			case BluetoothService.TASK_GET_REMOTE_STATE:
				tv_title.setText(msg.obj.toString());
				break;
			default:
				break;
			}
		}
	};  
    protected void onDestroy() {
		// ֹͣ����
    	BluetoothService.stop(this);
    	mBluetoothAdapter.disable();
		super.onDestroy();
	}
 }
