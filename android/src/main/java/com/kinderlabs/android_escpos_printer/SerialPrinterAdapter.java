package com.kinderlabs.android_escpos_printer;

import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android_serialport_api.SerialPortFinder;
import io.flutter.plugin.common.MethodChannel.Result;

public class SerialPrinterAdapter {
    private final String TAG = SerialPrinterAdapter.class.getSimpleName();
    public List<HashMap<String, Object>> getSerialPortsList() {
        SerialPortFinder serialPortFinder = new SerialPortFinder();
        List<HashMap<String, Object>> data = new ArrayList<>();
        String[] devices = serialPortFinder.getAllDevices();
        String[] devicesPath = serialPortFinder.getAllDevicesPath();

        for (int i = 0; i < devices.length; i++) {
            HashMap<String, Object> dev = new HashMap<>();
            dev.put("device", devices[i]);
            dev.put("devicePath", devicesPath[i]);
            data.add(dev);
        }

        return data;
    }

    public void openSerialDevice(String device, int baudRate, String alias, Result result ) {
        SerialPortFinder serialPortFinder = new SerialPortFinder();
        List<HashMap<String, Object>> data = new ArrayList<>();
        String[] devices = serialPortFinder.getAllDevices();
        String[] devicesPath = serialPortFinder.getAllDevicesPath();

        for (int i = 0; i < devices.length; i++) {
            String d = devices[i];
            if (d.contains(device)) {
                try {
                    SerialPrinter serialPrinter = new SerialPrinter(devices[i], devicesPath[i], baudRate, alias);
                    result.success(serialPrinter.getMethodChannelName());
                    Log.d(TAG, "success.");
                    break;
                } catch (IOException e) {
                    Log.e(TAG, "Fail to open serial port");
                }
            }
        }

    }
}
