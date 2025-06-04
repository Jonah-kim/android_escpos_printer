package com.kinderlabs.android_escpos_printer;

import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.flutter.plugin.common.MethodChannel.Result;

public class BluetoothClassicPrinterAdapter {

    BluetoothManager m_BluetoothManager;
    BluetoothAdapter m_BluetoothAdapter;
    private final String TAG = BluetoothClassicPrinterAdapter.class.getSimpleName();

    BluetoothClassicPrinterAdapter(Application application) {
        m_BluetoothManager = (BluetoothManager) application.getSystemService(Context.BLUETOOTH_SERVICE);
        m_BluetoothAdapter = m_BluetoothManager.getAdapter();

    }

    public void getBClassicBondedDevicesList(Result result) {
        List<HashMap<String, Object>> data = new ArrayList<>();

        for (BluetoothDevice device : m_BluetoothAdapter.getBondedDevices()) {
            HashMap<String, Object> ret = new HashMap<>();
            ret.put("address", device.getAddress());
            ret.put("name", device.getName());
            ret.put("type", device.getType());
            data.add(ret);
        }

        result.success(data);
    }

    public void openBluetoothClassicDevice(String address, String alias, Result result) {
        BluetoothDevice device = m_BluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            result.error(TAG, "Failed to get bclassic device", null);
            return;
        }
        try {
            BluetoothClassicPrinter bluetoothClassicPrinter = new BluetoothClassicPrinter(device, alias);
            result.success(bluetoothClassicPrinter.getMethodChannelName());
        } catch (IOException e) {
            Log.e(TAG, "Fail to open bluetooth classic device");
        }
    }
}
