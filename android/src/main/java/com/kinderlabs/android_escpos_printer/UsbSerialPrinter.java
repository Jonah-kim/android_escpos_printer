package com.kinderlabs.android_escpos_printer;

import android.util.Log;

import androidx.annotation.NonNull;

import com.felhr.usbserial.UsbSerialDevice;

import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;

public class UsbSerialPrinter implements MethodCallHandler {

    private final String TAG = UsbSerialPrinter.class.getSimpleName();

    private final UsbSerialDevice m_SerialDevice;
    private final String m_MethodChannelName;

    UsbSerialPrinter(String alias, UsbSerialDevice serialDevice) {
        m_SerialDevice = serialDevice;
        m_MethodChannelName = "android_escpos_printer/" + alias;
    }

    String getMethodChannelName() {
        return m_MethodChannelName;
    }

    private void setPortParameters(int baudRate, int dataBits, int stopBits, int parity) {
        m_SerialDevice.setBaudRate(baudRate);
        m_SerialDevice.setDataBits(dataBits);
        m_SerialDevice.setStopBits(stopBits);
        m_SerialDevice.setParity(parity);
    }

    private void setFlowControl( int flowControl ) {
        m_SerialDevice.setFlowControl(flowControl);
    }

    private Boolean open() {
        return m_SerialDevice.open();
    }

    private Boolean close() {
        m_SerialDevice.close();
        return true;
    }

    private void write( byte[] data ) {
        m_SerialDevice.write(data);
    }

    @Override
    public void onMethodCall(MethodCall call, @NonNull Result result) {

        switch (call.method) {
            case "close":
                result.success(close());
                break;
            case "open":
                result.success(open());
                break;
            case "write":
                write((byte[])call.argument("data"));
                result.success(true);
                break;

            case "setPortParameters":
                setPortParameters((int) call.argument("baudRate"), (int) call.argument("dataBits"),
                        (int) call.argument("stopBits"), (int) call.argument("parity"));
                result.success(null);
                break;

            case "setFlowControl":
                setFlowControl((int) call.argument("flowControl"));
                result.success(null);
                break;

            case "setDTR": {
                boolean v = call.argument("value");
                m_SerialDevice.setDTR(v);
                if (v == true) {
                    Log.e(TAG, "set DTR to true");
                } else {
                    Log.e(TAG, "set DTR to false");
                }
                result.success(null);
                break;
            }
            case "setRTS": {
                boolean v = call.argument("value");
                m_SerialDevice.setRTS(v);
                result.success(null);
                break;
            }

            default:
                result.notImplemented();
        }
    }
}