package com.inthinc.vbuscontrol.Communication;

/**
 * Created by ajalal on 5/18/18.
 */

import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.ByteArrayOutputStream;

import static com.inthinc.vbuscontrol.MainActivity.TAG;

/**
 * Created by User on 6/5/2015.
 */
public class BluetoothCommunication extends Thread {

    public BluetoothCommunication() {
    }

    public void sendData(BluetoothSocket socket, String data) throws IOException {


        Log.i(TAG, "trying data send: " + data);

        byte[] buffer = new byte[1024];
        byte[] send = data.getBytes();
        OutputStream outputStream = socket.getOutputStream();
        outputStream.write(send);
        Log.i(TAG, "data send: " + send);
    }

    public int receiveData(BluetoothSocket socket) throws IOException {
        byte[] buffer = new byte[4];
        ByteArrayInputStream input = new ByteArrayInputStream(buffer);
        InputStream inputStream = socket.getInputStream();
        inputStream.read(buffer);
        return input.read();
    }
}

