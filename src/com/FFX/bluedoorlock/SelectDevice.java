package com.FFX.bluedoorlock;

import java.util.ArrayList;
import java.util.Set;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

public class SelectDevice extends Activity implements OnItemClickListener{
	private final String TAG="SelectDevice";
	private static final int REQUEST_ENABLE_CODE = 0x1003;
	private BluetoothAdapter mBluetoothAdapter;
	private Button btn_scan;
	private ListView list_dev;
	private ArrayAdapter<String>adapter;
	private ArrayList<String> mArrayAdapter= new ArrayList<String>();
	private ArrayList<BluetoothDevice> mDeviceList = new ArrayList<BluetoothDevice>();
	
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		// ������ʾ������
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.scan_device);
		//��ʼ���ؼ�
		init();	
		// �򿪲����������豸
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter(); 
		if (mBluetoothAdapter == null) {     
			Log.e(TAG, "Your device is not support Bluetooth!");
			return;
		}
		if (!mBluetoothAdapter.isEnabled()) {   
			Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableBtIntent, REQUEST_ENABLE_CODE); 
		}else{
			findBTDevice();
		}
		// �������յ��豸���ҵ��Ĺ㲥��ɨ����ɵĹ㲥
		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		// ��̬ע��㲥������
		// ��������ɨ�赽���豸��Ϣ
		registerReceiver(mReceiver, filter); 
	}
	private void init(){
		//��ʼ���ؼ�
		btn_scan = (Button) findViewById(R.id.imageButton_scan);
		btn_scan.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if(!mBluetoothAdapter.isDiscovering()){
					mBluetoothAdapter.startDiscovery();
					setProgressBarIndeterminateVisibility(true);
				}
			 }
		});
		list_dev = (ListView) findViewById(R.id.listView_dev);
		list_dev.setOnItemClickListener(this);
		adapter = new ArrayAdapter<String>(this, 
					android.R.layout.simple_list_item_1, mArrayAdapter);
		list_dev.setAdapter(adapter);	
	}
	private void findBTDevice(){
		// ���������Ѿ���Ե������豸����
		Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices(); 
		if (pairedDevices.size() > 0) {
			for (BluetoothDevice device : pairedDevices) { 
				// ���Ѿ�����豸��Ϣ��ӵ�ListView��
				mArrayAdapter.add(device.getName() + "\n" + device.getAddress());
				mDeviceList.add(device);
			} 
		}
		adapter.notifyDataSetChanged();
	}
	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) { 
			String action = intent.getAction();
			// ɨ�赽�µ������豸
			if (BluetoothDevice.ACTION_FOUND.equals(action)) {
				// ��������豸����
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				//��ֹ�豸��������ظ�
				if(mDeviceList.contains(device)){
					return;
				}
				mArrayAdapter.add(device.getName() + "\n" + device.getAddress());
				System.out.println(device.getName() + "\n" + device.getAddress());
				mDeviceList.add(device);
				adapter.notifyDataSetChanged();
			}else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){
				// ɨ����ɣ��ر���ʾ������
				setProgressBarIndeterminateVisibility(false);
			}
		} 
	}; 
	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		String targetDev = mArrayAdapter.get(arg2);
		System.out.println(targetDev);
		Intent data = new Intent();
		data.putExtra("DEVICE", mDeviceList.get(arg2));
		// �����ڵ��ĳ���豸ʱ�������豸���󷵻ظ������ߣ�MainActivity��
		setResult(RESULT_OK, data);
		this.finish();
	}
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(requestCode == REQUEST_ENABLE_CODE){
			if(resultCode ==  RESULT_OK){
				System.out.println("�豸�򿪳ɹ�");
				findBTDevice();
			}else{
				System.out.println("�豸��ʧ��");
			}
		}
	}
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(mReceiver);
	}
}
