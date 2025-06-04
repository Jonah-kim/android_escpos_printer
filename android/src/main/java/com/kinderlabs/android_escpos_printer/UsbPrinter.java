package com.kinderlabs.android_escpos_printer;

import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbManager;
import android.util.Log;

import androidx.annotation.NonNull;

import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;

public class UsbPrinter implements MethodCallHandler {

    private final String TAG = UsbPrinter.class.getSimpleName();

    private final UsbManager m_UsbManager;
    private final UsbDevice m_UsbDevice;
    private final String m_MethodChannelName;

    UsbPrinter(String alias, UsbDevice usbDevice, UsbManager usbManager) {
        m_UsbManager = usbManager;
        m_UsbDevice = usbDevice;
        m_MethodChannelName = "android_escpos_printer/" + alias;
    }

    String getMethodChannelName() {
        return m_MethodChannelName;
    }

    private Boolean close() {
        if (m_UsbDevice == null || !m_UsbManager.hasPermission(m_UsbDevice)) {
            return false;
        }

        UsbDeviceConnection connection = null;
        try {
            connection = m_UsbManager.openDevice(m_UsbDevice);
            if (connection == null) {
                Log.e(TAG, "Failed to open USB device for closing");
                return false;
            }

            try {
                connection.releaseInterface(m_UsbDevice.getInterface(0));
            } catch (Exception e) {
                Log.w(TAG, "Failed to release interface: " + e.getMessage());
            }

            connection.close();
            return true;

        } catch (Exception e) {
            Log.e(TAG, "USB close failed: " + e.getMessage());
            return false;
        }
    }


    private void write(byte[] data) {
        UsbDeviceConnection connection = null;
        try {
            connection = m_UsbManager.openDevice(m_UsbDevice);
            if (connection == null) {
                Log.e(TAG, "Failed to open device");
                return;
            }

            connection.claimInterface(m_UsbDevice.getInterface(0), true);

            UsbEndpoint mBulkEndOut = null;
            for (int i = 0; i < m_UsbDevice.getInterface(0).getEndpointCount(); i++) {
                UsbEndpoint ep = m_UsbDevice.getInterface(0).getEndpoint(i);
                if (ep.getType() == UsbConstants.USB_ENDPOINT_XFER_BULK &&
                        ep.getDirection() == UsbConstants.USB_DIR_OUT) {
                    mBulkEndOut = ep;
                    break;
                }
            }

            if (mBulkEndOut != null) {
                connection.bulkTransfer(mBulkEndOut, data, data.length, 5000);
            } else {
                Log.e(TAG, "No OUT endpoint found");
            }

        } catch (Exception e) {
            Log.e(TAG, "USB write failed: " + e.getMessage());
        } finally {
            if (connection != null) {
                try {
                    connection.releaseInterface(m_UsbDevice.getInterface(0));
                } catch (Exception ignored) {}
                connection.close();
            }
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