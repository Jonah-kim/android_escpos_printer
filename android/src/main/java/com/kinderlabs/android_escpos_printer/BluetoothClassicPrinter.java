package com.kinderlabs.android_escpos_printer;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import androidx.annotation.NonNull;


import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;

public class BluetoothClassicPrinter implements MethodCallHandler {

    private final String TAG = BluetoothClassicPrinter.class.getSimpleName();
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private BluetoothDevice m_Device;
    private BluetoothSocket m_socket;
    private final OutputStream m_outputStream;
    private final String m_MethodChannelName;

    BluetoothClassicPrinter(BluetoothDevice device, String alias) throws IOException {
        m_Device = device;
        m_socket = device.createRfcommSocketToServiceRecord(MY_UUID);
        m_outputStream = m_socket.getOutputStream();
        m_MethodChannelName = "android_escpos_printer/" + alias;
    }

    String getMethodChannelName() {
        return m_MethodChannelName;
    }

    private Boolean close() {
        if (m_socket != null) {
            try {
                m_outputStream.flush();
                m_outputStream.close();
                m_socket.close();
            } catch (IOException e) {

            }
        }
        return true;
    }

    private void write(byte[] data) {
        if (m_outputStream == null) {
            Log.e(TAG, "Fail to open outputStream.");
        }
        try {
            m_outputStream.write(data);
        } catch (IOException e) {
            Log.e(TAG, "Fail to write outputStream");
        }
    }

    @Override
    public void onMethodCall(MethodCall call, @NonNull Result result) {

        switch (call.method) {
            case "close":
                result.success(close());
                break;
            case "write":
                write((byte[])call.argument("data"));
                result.success(true);
                break;

            default:
                result.notImplemented();
        }
    }
}