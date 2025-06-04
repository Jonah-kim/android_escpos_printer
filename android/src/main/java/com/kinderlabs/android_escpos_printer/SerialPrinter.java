package com.kinderlabs.android_escpos_printer;

import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbManager;
import android.nfc.Tag;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android_serialport_api.SerialPort;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;

public class SerialPrinter implements MethodCallHandler {

    private final String TAG = SerialPrinter.class.getSimpleName();

    private final SerialPort m_serialPort;
    private final OutputStream m_outputStream;
    private final String m_MethodChannelName;

    SerialPrinter(String device, String devicePath, int baudRate, String alias) throws IOException {
        m_serialPort = new SerialPort(new File(devicePath), baudRate, 0);
        m_outputStream = m_serialPort.getOutputStream();
        m_MethodChannelName = "android_escpos_printer/" + alias;
    }

    String getMethodChannelName() {
        return m_MethodChannelName;
    }

    private Boolean close() {
        if (m_serialPort != null) {
            try {
                m_outputStream.flush();
                m_outputStream.close();
            } catch (IOException e) {

            } finally {
                m_serialPort.close();
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