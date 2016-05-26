package com.FFX.bluedoorlock;


import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.UUID;
import android.annotation.SuppressLint;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;
import com.FFX.bluedoorlock.CommandInput;

public class BluetoothService extends Service {
	
	private final String TAG = "TaskService";
	public static final int TASK_START_CONN_THREAD = 1;  //��������Զ�������豸����Ϊ�ͻ��ˣ�
	public static final int TASK_SEND_MSG = 2;	//������Ϣ����
	public static final int TASK_GET_REMOTE_STATE = 3; //�����������״̬ 
	public static final int TASK_RECV_MSG = 4;//���յ�������Ϣ
	private final String UUID_STR = "00001101-0000-1000-8000-00805F9B34FB";
    private int mTaskID;   				// ����ID
	public Object[] mParams;   		// ��������б�			
	private static boolean istaskThread_alive=false;
	private TaskThread mThread;
	//private GetstateThread mGetstateThread;
	private static ArrayList<BluetoothService> mTaskList = new ArrayList<BluetoothService>();   // �������
	private BluetoothAdapter mBluetoothAdapter;
	private ConnectThread mConnectThread;
	private static Handler mActivityHandler;
	private static String getstate;
	
//�������캯��	
	public BluetoothService(Handler handler, int taskID, Object[] params){
		mActivityHandler = handler;
		this.mTaskID = taskID;
		this.mParams = params;
		if(!istaskThread_alive){
			// ������������߳�
			mThread = new TaskThread();
			mThread.start();
			istaskThread_alive=true;		
		}
	}
	public BluetoothService(){	

		if(!istaskThread_alive){
			// ������������߳�
			mThread = new TaskThread();
			mThread.start();
			istaskThread_alive=true;
		}
	}
	
	public void onCreate() {
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (mBluetoothAdapter == null) {
			Log.e(TAG, "Your device is not support Bluetooth!");
			return;
		}
		super.onCreate();
	}	
	
	public static void newTask(BluetoothService target) {
		synchronized (mTaskList) {
			// ��������ӵ����������
			mTaskList.add(target);
		}
	}
	
	private Handler mServiceHandler = new Handler() {
		@Override
		public void handleMessage(android.os.Message msg) {
				switch (msg.what) {		
			case TASK_GET_REMOTE_STATE:	
				android.os.Message activityMsg = mActivityHandler
						.obtainMessage();
				activityMsg.what = msg.what;
				if (mCommThread != null && mCommThread.isAlive()) {
					activityMsg.obj = mCommThread.getRemoteName() + "[������]";
			     } else if (mConnectThread != null && mConnectThread.isAlive()) {
			    	 activityMsg.obj = "�������ӣ�"
							+ mConnectThread.getDevice().getName();
				} else {
					activityMsg.obj = "[δ����]";
					// ���µȴ�����
				}
				mActivityHandler.sendMessage(activityMsg);
				break;			
			default:
				break;
			}
			super.handleMessage(msg);			
		}
	};

	public static void start(Context c, Handler handler) {
		mActivityHandler = handler;
		// ��ʽ��������
		Intent intent = new Intent(c, BluetoothService.class);
		c.startService(intent);
	}
	
	public static void stop(Context c) {
		Intent intent = new Intent(c, BluetoothService.class);
		c.stopService(intent);
	}
	//�������߳�
	private class TaskThread extends Thread {
		private boolean isRun = true;
		private int mCount = 0;
		//ֹͣ�߳�
		public void cancel() {
			isRun = false;
		}
		@Override
		public void run() {
			BluetoothService task;
			while (isRun) {
				// ��õ�һ������ʼִ��
				if (mTaskList.size() > 0) {
					synchronized (mTaskList) {
						task = mTaskList.get(0);
						doTask(task);
					}
				} else {	
					try {
						Thread.sleep(200);
						mCount++;
					} catch (InterruptedException e) {
					}
					if (mCount >= 20) {
						mCount = 0;
					android.os.Message handlerMsg = mServiceHandler
							.obtainMessage();
						handlerMsg.what = TASK_GET_REMOTE_STATE;
					mServiceHandler.sendMessage(handlerMsg);
					}
				}
			}
		}
	}
	//dotask
	private void doTask(BluetoothService task) {
		switch (task.getTaskID()) {
		case TASK_START_CONN_THREAD:
			if (task.mParams == null || task.mParams.length == 0) {
				break;
			}
			BluetoothDevice remote = (BluetoothDevice) task.mParams[0];
			// ��Ϊ�ͻ���ȥ����Զ�̷�����
			mConnectThread = new ConnectThread(remote);
			mConnectThread.start();
			//��ȡ��������״̬
			android.os.Message handlerMsg = mServiceHandler
					.obtainMessage();
				handlerMsg.what = TASK_GET_REMOTE_STATE;
			mServiceHandler.sendMessage(handlerMsg);
			break;
		case TASK_SEND_MSG:
			boolean sucess = false;
			if (mCommThread == null || !mCommThread.isAlive()
					|| task.mParams == null || task.mParams.length == 0) {
				Log.e(TAG, "mCommThread or task.mParams null");		
			} else {
				sucess = mCommThread.write((String) task.mParams[0]);
			}
			if (!sucess) {
				android.os.Message returnMsg = mActivityHandler.obtainMessage();
				returnMsg.what = TASK_SEND_MSG;
				returnMsg.obj = "��Ϣ����ʧ�ܣ��������豸";
				mActivityHandler.sendMessage(returnMsg);
			}
			break;
		}
		synchronized (mTaskList) {
			mTaskList.remove(task);
		}
	}
	@Override
	public void onDestroy() {
		super.onDestroy();
		mThread.cancel();
	}
	//���������豸�߳�
	private class ConnectThread extends Thread {
		private final BluetoothSocket mSocket;
		private final BluetoothDevice mDevice;
		public ConnectThread(BluetoothDevice device) {
			Log.d(TAG, "ConnectThread");
			// ����豸�Ѿ������̴߳��ڣ��������
			if (mCommThread != null && mCommThread.isAlive()) {
				mCommThread.cancel();
			}
			BluetoothSocket tmp = null;
			mDevice = device;
			try {
				tmp = device.createRfcommSocketToServiceRecord(UUID
						.fromString(UUID_STR));
			} catch (IOException e) {
				Log.d(TAG, "createRfcommSocketToServiceRecord error!");
			}
			mSocket = tmp;
		}
		public BluetoothDevice getDevice() {
			return mDevice;
		}
		@SuppressLint("NewApi")
		public void run() {
			try {
				// ����Զ�̷������豸
				mSocket.connect();
				
			} catch (IOException connectException) {
				Log.e(TAG, "Connect server failed");
				try {	
					mSocket.close();
					Log.e(TAG, "mSocket.close();");
				} catch (IOException closeException) {
					Log.e(TAG, "mSocket.close()  failed;");
				}	
			}
			manageConnectedSocket(mSocket);
		}
		public void cancel() {
			try {
				mSocket.close();
			} catch (IOException e) {
			}
			mConnectThread = null;
		}
	}
	private ConnectedThread mCommThread;
	private void manageConnectedSocket(BluetoothSocket socket) {
		// �������߳���ά������
		mCommThread = new ConnectedThread(socket);
		mCommThread.start();
	}

	//ά�������߳�
	private BluetoothSocket mSocket;
	private  InputStream mInStream;
	private  OutputStream mOutStream;
	private BufferedWriter mBw;
	private class ConnectedThread extends Thread {
		@SuppressLint("NewApi") 
		public ConnectedThread(BluetoothSocket socket) {	
			Log.d(TAG, "ConnectedThread");
			mSocket = socket;
			if(mSocket.isConnected()){
				//��ȡ����״̬����ʾ������
				android.os.Message handlerMsg = mServiceHandler
				.obtainMessage();
				handlerMsg.what = TASK_GET_REMOTE_STATE;
				mServiceHandler.sendMessage(handlerMsg);
			}
			InputStream tmpIn = null;
			OutputStream tmpOut = null;
			try {
				tmpIn = socket.getInputStream();
				tmpOut = socket.getOutputStream();
			} catch (IOException e) {
			}
			mInStream = tmpIn;
			mOutStream = tmpOut;
			// ���Զ���豸����������ַ���
			mBw = new BufferedWriter(new PrintWriter(mOutStream));
		//	mGetstateThread=new GetstateThread();
		//	mGetstateThread.start();
		}
		public boolean write(String msg) {
			if (msg == null)
				return false;
			try {
				mBw.write(msg);
				mBw.flush();
				System.out.println("Write:" + msg);
			} catch (IOException e) {
				return false;
			}
			return true;
		}
		public String getRemoteName() {
			return mSocket.getRemoteDevice().getName();
		}
		public void cancel() {
			try {
				mSocket.close();
			} catch (IOException e) {
			}
			mCommThread = null;
		}
		public void run() {	
			Log.d(TAG, "ConnectedThread  run()");
			android.os.Message handlerMsg;
			//String buffer= null;
			byte []buffer=new byte[1024];
			int readBytes=0;  
			int len=buffer.length;  	  
			while (true) {
			try {		
			while (readBytes < len) {  	  
			    int read = mInStream.read(buffer, readBytes, len - readBytes);  		  
			    //�ж��ǲ��Ƕ�������������ĩβ ����ֹ������ѭ����  
			    if (read == -1) {  		  
			    	break;  	  
			     } 		               
			   readBytes += read;
			   if(readBytes==1){    
			        int state=buffer[0]& 0xff;
			         System.out.println(state);
			         if(state==85||state==170){
			               String str = "";
			                if(state==85){
			                	getstate="off";
			                	str=mSocket.getRemoteDevice().getName() + " : off";
			              //  	Toast.makeText(BluetoothService.this, "�ر�״̬", Toast.LENGTH_LONG).show();
			                	System.out.println(str);
			                }
			               else{
			            	   getstate="on";
			                	str=mSocket.getRemoteDevice().getName() + " : on";
			            //    	Toast.makeText(BluetoothService.this, "����״̬", Toast.LENGTH_LONG).show();
			                	System.out.println(str);
			                }
			       handlerMsg = mActivityHandler.obtainMessage();
				   handlerMsg.what = TASK_RECV_MSG;	
				   handlerMsg.obj = str;
				   buffer=null;  
				   buffer=new byte[1024];  
				   readBytes=0;
				   readBytes=0;
				   mActivityHandler.sendMessage(handlerMsg);
			       }
			   }
			   if(readBytes==2){
				   int high=buffer[0]& 0xff;
				   int low=buffer[1]& 0xff;
				   int cou=high*256+low;
			        System.out.println("�յ���" + cou);
			        String count= mSocket.getRemoteDevice().getName() + " : "+cou;
			        handlerMsg = mActivityHandler.obtainMessage();
			    	handlerMsg.what = TASK_RECV_MSG;	
			    	handlerMsg.obj = count;
			    	buffer=null;  
			    	buffer=new byte[1024];  
			    	readBytes=0; 	
			    	mActivityHandler.sendMessage(handlerMsg);
			    }	            
			    if(readBytes==8||readBytes==17){
			    	String time = new String(buffer);
			        System.out.println("�յ���" + time);
			        time=mSocket.getRemoteDevice().getName() + " : "+time;
			        handlerMsg = mActivityHandler.obtainMessage();
			    	handlerMsg.what = TASK_RECV_MSG;	
			    	handlerMsg.obj = time;
			    	buffer=null;  
			    	buffer=new byte[1024];  
			    	readBytes=0; 	
			    	mActivityHandler.sendMessage(handlerMsg);
			    }	                
		 }  		
				if (mActivityHandler == null) {
					return;
				}
				String string =new String (buffer);
				buffer=null;  
			    buffer=new byte[1024];  
				// ͨ��Activity���µ�UI��
				handlerMsg = mActivityHandler.obtainMessage();
				handlerMsg.what = TASK_RECV_MSG;	
				handlerMsg.obj = string;
				mActivityHandler.sendMessage(handlerMsg);
			} catch (IOException e) {
				try {
					mSocket.close();
				} catch (IOException e1) {
				}
				mCommThread = null;			
				break;
		}
	}  
		}
	}
	/*
	//��ȡ����״̬�߳�
	private class GetstateThread extends Thread {
		private boolean isRun = true;
		public void run() {	
			while(isRun){
				if(mCommThread!=null&&mCommThread.isAlive()){
					try {
						mBw.write("*getstate#");
						mBw.flush();			
						if(getstate=="off"||getstate=="on"){
							android.os.Message handlerMsg = mActivityHandler.obtainMessage();
					    	handlerMsg.what = TASK_RECV_MSG;	
					    	handlerMsg.obj = getstate;
						}
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
		public void cancel(){
			isRun = false;
		}
	}*/

	public int getTaskID(){
		return mTaskID;
	}
	
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
}
