package com.inthinc.vbuscontrol;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.inthinc.vbuscontrol.Communication.BluetoothConnect;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.inthinc.vbuscontrol.MainActivity.TAG;

/**
 * Created by ajalal on 5/8/18.
 */

public class BluetoothPair extends Activity implements ListView.OnItemClickListener {

    private ListView listView;
    private ListView listView2;
    private ArrayAdapter<String> mNewDevicesList;
    private ArrayAdapter<String> mPairedDevicesList;
    ReceiverManager mReceiverManager;
    IntentFilter filter1, filter2, filter3, filter4, filter5;

    final BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    BluetoothConnect mBluetoothConnect;
    ProgressDialog pageLoading;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bluetooth_pair);

        Log.i(TAG, "onCreated called");


        pageLoading = new ProgressDialog(this);
        pageLoading.setIndeterminate(true);
        pageLoading.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        pageLoading.setCancelable(true);

        mReceiverManager = ReceiverManager.init(this.getApplicationContext());
        listView = (ListView) findViewById(R.id.list);
        listView2 = (ListView) findViewById(R.id.list2);



        filter1 = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        filter2 = new IntentFilter(android.bluetooth.BluetoothDevice.ACTION_ACL_DISCONNECTED);
        filter3 = new IntentFilter(android.bluetooth.BluetoothDevice.ACTION_ACL_CONNECTED);
        filter4 = new IntentFilter(android.bluetooth.BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
        filter5 = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);

        if (!mReceiverManager.isReceiverRegistered(mReceiver)){
            mReceiverManager.registerReceiver(mReceiver, filter1);
            mReceiverManager.registerReceiver(mReceiver, filter2);
            mReceiverManager.registerReceiver(mReceiver, filter3);
            mReceiverManager.registerReceiver(mReceiver, filter4);
            mReceiverManager.registerReceiver(mReceiver, filter5);
        }else{
            Log.i(TAG, "Receiver already registered");
        }

        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
        }

        mBluetoothAdapter.startDiscovery();

        mNewDevicesList = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_2, android.R.id.text1);
        mPairedDevicesList = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_2, android.R.id.text1);

        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

        if (pairedDevices.size() > 0) {
            findViewById(R.id.paired_devices).setVisibility(View.VISIBLE);
            for (BluetoothDevice device : pairedDevices) {
                mPairedDevicesList.add(device.getName() + "\n" + device.getAddress());
            }
        }
        listView.setAdapter(mNewDevicesList);
        listView2.setAdapter(mPairedDevicesList);
        listView.setOnItemClickListener(this);

    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.i(TAG,"Onresume called");

        if (!mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.startDiscovery();
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG,"OnPause called");
        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        Log.i(TAG,"OnStop called");
        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
        }
        mNewDevicesList.clear();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG,"Ondestroy called");

        if(mBluetoothConnect != null && mBluetoothConnect.connected()) {
            mBluetoothConnect.cancel();
        }
        mBluetoothAdapter.cancelDiscovery();
        mNewDevicesList.clear();
        mReceiverManager.unregisterReceiver(mReceiver);
    }

    @Override
    public void onItemClick(AdapterView<?> listView, View view, int position, long id) {
        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
        }

        Object item = listView.getItemAtPosition(position);
        String info = item.toString();
        String address = info.substring(info.length() - 17);
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        Log.i(TAG, "Bluetooth MAC address selected: "+ address);

        pageLoading.setMessage("Connecting to " + device.getName() + " - " + device.getAddress());
        pageLoading.show();

        if(BluetoothConnect.mState == BluetoothConnect.STATE_CONNECTED){
            mBluetoothConnect.cancel();
        }

        mBluetoothConnect = new BluetoothConnect(device, mHandler);
        mBluetoothConnect.start();

    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

            switch (action){
                case BluetoothDevice.ACTION_FOUND: {
                    if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                        mNewDevicesList.add(device.getName() + "\n" + device.getAddress());
                        mNewDevicesList.notifyDataSetChanged();
                    }
                }
                break;

                case BluetoothDevice.ACTION_ACL_CONNECTED: {
//                    Log.i(TAG, "Device connected");
                }
                break;

                case BluetoothAdapter.ACTION_DISCOVERY_FINISHED: {
                    Log.i(TAG, "Finished discovery");
                    if (mNewDevicesList.getCount() == 0) {
                        Toast.makeText(getApplicationContext(), "No devices found", Toast.LENGTH_LONG).show();
                    }
                }
                break;

                case BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED:
                    break;

                case BluetoothDevice.ACTION_ACL_DISCONNECTED:
                    Toast.makeText(getApplicationContext(), "Bluetooth is disconnecting", Toast.LENGTH_LONG).show();
                    pageLoading.dismiss();
                    break;

                case BluetoothDevice.ACTION_BOND_STATE_CHANGED: {
                    Log.i(TAG, "bond state changed");
                }
                    break;

            }
        }
    };


    public static class ReceiverManager {

        private static List<BroadcastReceiver> receivers = new ArrayList<BroadcastReceiver>();
        private static ReceiverManager ref;
        private Context context;

        private ReceiverManager(Context context){
            this.context = context;
        }

        public static synchronized ReceiverManager init(Context context) {
            if (ref == null) ref = new ReceiverManager(context);
            return ref;
        }

        public Intent registerReceiver(BroadcastReceiver receiver, IntentFilter intentFilter){
            if(!receivers.contains(receiver)) {
                receivers.add(receiver);
            }

            Intent intent = context.registerReceiver(receiver, intentFilter);
            return intent;
        }

        public boolean isReceiverRegistered(BroadcastReceiver receiver){
            boolean registered = receivers.contains(receiver);
            return registered;
        }

        public synchronized void  unregisterReceiver(BroadcastReceiver receiver){
            if (isReceiverRegistered(receiver)){
                receivers.remove(receiver);
                context.unregisterReceiver(receiver);
            }
        }
    }

    public Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what){
                case BluetoothConnect.STATE_CONNECTED:{
                    pageLoading.dismiss();
                    Toast.makeText(getApplicationContext(), "Device connected successfully", Toast.LENGTH_LONG).show();
                    finish();
                    Intent serverIntent = new Intent(getBaseContext(), VehicleSelection.class);
                    startActivity(serverIntent);
                }
                break;

                case BluetoothConnect.STATE_CONNECTING:{
                    //pageLoading.setMessage("Connecting...");
                }
                break;

                case BluetoothConnect.STATE_FAILED:{
                    pageLoading.dismiss();
                    Toast.makeText(getApplicationContext(), "Failed to connect!", Toast.LENGTH_LONG).show();
                }
                break;
            }

        }
    };
}
