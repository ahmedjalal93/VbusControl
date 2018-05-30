package com.inthinc.vbuscontrol.Communication;

/**
 * Created by ajalal on 5/10/18.
 */

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import static com.inthinc.vbuscontrol.MainActivity.TAG;

public class BluetoothConnect extends Thread {

    private static final UUID MY_UUID_INSECURE = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private final BluetoothDevice mBluetoothDevice;
    private final BluetoothSocket mBluetoothSocket;
    private Context context;
    private Handler handler;
    private InputStream mInputStream;
    public static int mState;

    public static final int STATE_NONE = 0;
    public static final int STATE_FAILED = 1;
    public static final int STATE_CONNECTING = 2;
    public static final int STATE_CONNECTED = 3;
    public static final int STATE_CLOSED = 4;

    public BluetoothConnect(BluetoothDevice mBluetoothDevice, Handler handler) {

        BluetoothSocket tmp = null;
        mState = STATE_NONE;
        this.mBluetoothDevice = mBluetoothDevice;
        this.handler = handler;

        Log.i(TAG, "Bluetooth connection started");

        try {
            tmp = this.mBluetoothDevice.createInsecureRfcommSocketToServiceRecord(MY_UUID_INSECURE);
        } catch (IOException e) {
            mState = STATE_FAILED;
            Log.d(TAG, "Could not start listening for RFCOMM");
        }

        mBluetoothSocket = tmp;
        mState = STATE_CONNECTING;
        handler.obtainMessage(mState).sendToTarget();


    }

    public synchronized void run() {
        try {
            mBluetoothSocket.connect();


        } catch (IOException e) {
            mState = STATE_FAILED;
            Log.d(TAG, "Could not connect: " + e.toString());
            try {
                mBluetoothSocket.close();
            } catch (IOException close) {
                mState = STATE_FAILED;
                Log.d(TAG, "Could not close connection:" + e.toString());
            }
        } finally {
            if (mBluetoothSocket.isConnected()) {
                String sendddd = "hello";
                try {
                    Log.i(TAG, "sending data");

                    BluetoothCommunication mBluetoothCommunication = new BluetoothCommunication();
                    mBluetoothCommunication.sendData(mBluetoothSocket, sendddd);


                } catch (IOException e) {
                    e.printStackTrace();
                }

                Log.i(TAG, "Bluetooth successfully connected");
                mState = STATE_CONNECTED;
                handler.obtainMessage(mState).sendToTarget();
            } else {
                mState = STATE_FAILED;
                handler.obtainMessage(mState).sendToTarget();
                Log.i(TAG, "Bluetooth failed to successfully connect");
            }
        }
    }

    public synchronized void cancel() {
        try {
            mState = STATE_CLOSED;
            Log.i(TAG, "Closing bluetooth connection");
            handler.obtainMessage(mState).sendToTarget();
            mBluetoothSocket.close();
        } catch (IOException e) {
            Log.d(TAG, "Could cancel connection:" + e.toString());
        } finally {
            if (!mBluetoothSocket.isConnected()) {
                mState = STATE_CLOSED;
                handler.obtainMessage(mState).sendToTarget();
            }
        }
    }

    public synchronized boolean connected() {
        if (mState == STATE_CONNECTED) {
            return true;
        }
        return false;
    }

    public synchronized boolean connecting() {
        if (mState == STATE_CONNECTING) {
            return true;
        }
        return false;
    }
}