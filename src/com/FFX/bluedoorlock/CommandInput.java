package com.FFX.bluedoorlock;

import java.util.ArrayList;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

public class CommandInput extends Activity{
	private ListView listview;
	private EditText edit;
	private Button btn_title;
	private ArrayAdapter<String>  listAdapter;
	// �������ݱ������ 
	private ArrayList<String>  chatContent;
	private BluetoothAdapter mBluetoothAdapter;
			
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		
		//�����Զ��������
		 requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
	     setContentView(R.layout.command_input);
	     getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.titlebar);
		 getWindow().setSoftInputMode(WindowManager.
				LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		 
		//��ʼ���ؼ�
		init();
	}
	private void init(){
		listview=(ListView) findViewById(R.id.listView_command);
		edit=(EditText) findViewById(R.id.editText_command);
		btn_title=(Button) findViewById(R.id.button_title);
	    btn_title.setOnClickListener(new android.view.View.OnClickListener() {		
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub		
				mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter(); 
				mBluetoothAdapter.disable();
			}
		});
		chatContent = new ArrayList<String>();
		// ListView��Adapter
		listAdapter = new ArrayAdapter<String>(this, 
				// ����ʾ����
				android.R.layout.simple_list_item_1, 
				// ��ʾ������Դ
				chatContent);
		listview.setAdapter(listAdapter);
	}
	//���Ͱ�ť�����¼�
	public void onSendClick(View v){
		String msg = edit.getText().toString().trim();
		if(msg.length() <= 0){
			Toast.makeText(this, "��Ϣ����Ϊ��", Toast.LENGTH_SHORT).show();
			return;
		}
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter(); 
		// �����豸����֧��
		if (mBluetoothAdapter == null) {
			Toast.makeText(this, "���豸û�������豸", Toast.LENGTH_LONG).show();
			return ;
		}
		// ���û��������Ϣ��ӵ�ListView��ȥ��ʾ
		chatContent.add(mBluetoothAdapter.getName() + ":" + msg);
		// ����ListView��ʾ
		listAdapter.notifyDataSetChanged();
		// ��������Ϣ�����ύ����̨����
		BluetoothService.newTask(new BluetoothService(mHandler, BluetoothService.TASK_SEND_MSG, new Object[]{msg}));
		// ��������
		edit.setText("");
	}
	private Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case BluetoothService.TASK_SEND_MSG:
				Toast.makeText(CommandInput.this, msg.obj.toString(), Toast.LENGTH_SHORT).show();
				break;
			case BluetoothService.TASK_RECV_MSG:
				// ���Զ���豸���͵���Ϣ
				chatContent.add(msg.obj.toString());
				listAdapter.notifyDataSetChanged();
				break;
			case BluetoothService.TASK_GET_REMOTE_STATE:
				setTitle(msg.obj.toString());
				break;
			default:
				break;
			}
		}
	};
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.commandinput, menu);
		return true;
	}
	//�˵�����¼�
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		//��ȡ����״̬
		case R.id.button_opstate:
			String msg="*getstate#";
			BluetoothService.newTask(new BluetoothService(mHandler, 
					BluetoothService.TASK_SEND_MSG, new Object[]{msg}));
			break;
		//��ȡ��������״̬
		case R.id.button_restate:
			String msg2="*getremind#";
			BluetoothService.newTask(new BluetoothService(mHandler, 
					BluetoothService.TASK_SEND_MSG, new Object[]{msg2}));
			break;
		//��ȡ���ű���״̬
		case R.id.button_alstate:
			String msg3="*getalarm#";
			BluetoothService.newTask(new BluetoothService(mHandler, 
					BluetoothService.TASK_SEND_MSG, new Object[]{msg3}));
			break;
		//�����û���
		case R.id.change_name:
			AlertDialog.Builder dlg = new AlertDialog.Builder(this);
			final EditText devNameEdit = new EditText(this);
			dlg.setView(devNameEdit);
			dlg.setTitle("�������û���");
			dlg.setPositiveButton("����", new OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
				if(devNameEdit.getText().toString().length() != 0)
					// ���������豸��
					mBluetoothAdapter.setName(devNameEdit.getText().toString());
				}
			});
			dlg.create();
			dlg.show();
			break;
		}
		return true;
	}
}
