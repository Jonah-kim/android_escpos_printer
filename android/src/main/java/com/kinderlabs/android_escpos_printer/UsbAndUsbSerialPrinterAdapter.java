package com.kinderlabs.android_escpos_printer;

import static android.content.Context.USB_SERVICE;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.util.Log;

import com.felhr.usbserial.UsbSerialDevice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.MethodChannel.Result;

public class UsbAndUsbSerialPrinterAdapter implements EventChannel.StreamHandler {
    private final String TAG = UsbAndUsbSerialPrinterAdapter.class.getSimpleName();
    @SuppressLint("StaticFieldLeak")
    private static Context context;
    private static BinaryMessenger messenger;
    private EventChannel.EventSink events;
    private final UsbManager usbManager;
    private static final String ACTION_USB_PERMISSION = "com.example.flutter_thermal_printer.USB_PERMISSION";
    private static final String ACTION_USB_ATTACHED = "android.hardware.usb.action.USB_DEVICE_ATTACHED";
    private static final String ACTION_USB_DETACHED = "android.hardware.usb.action.USB_DEVICE_DETACHED";

    UsbAndUsbSerialPrinterAdapter(Context context, BinaryMessenger messenger) {
        UsbAndUsbSerialPrinterAdapter.context = context;
        UsbAndUsbSerialPrinterAdapter.messenger = messenger;
        EventChannel eventChannel = new EventChannel(messenger, "android_escpos_printer/usb_and_usb_serial_events");
        eventChannel.setStreamHandler(this);

        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_USB_DETACHED);
        filter.addAction(ACTION_USB_ATTACHED);
        BroadcastReceiver usbStateChangeReceiver = new BroadcastReceiver() {
            private UsbDevice getUsbDeviceFromIntent(Intent intent) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    return intent.getParcelableExtra(UsbManager.EXTRA_DEVICE, UsbDevice.class);
                } else {
                    @SuppressWarnings("deprecation")
                    UsbDevice ret = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    return ret;
                }
            }

            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action == null) {
                    return;
                }
                if (action.equals(ACTION_USB_ATTACHED)) {
                    Log.d(TAG, "ACTION_USB_ATTACHED");
                    if (events != null) {
                        UsbDevice device = getUsbDeviceFromIntent(intent);
                        if (device != null) {
                            HashMap<String, Object> msg = serializeDevice(device);
                            msg.put("event", ACTION_USB_ATTACHED);
                            events.success(msg);
                        } else {
                            Log.e(TAG, "ACTION_USB_ATTACHED but no EXTRA_DEVICE");
                        }
                    }
                } else if (action.equals(ACTION_USB_DETACHED)) {
                    Log.d(TAG, "ACTION_USB_DETACHED");
                    if (events != null) {
                        UsbDevice device = getUsbDeviceFromIntent(intent);
                        if (device != null) {
                            HashMap<String, Object> msg = serializeDevice(device);
                            msg.put("event", ACTION_USB_DETACHED);
                            events.success(msg);
                        } else {
                            Log.e(TAG, "ACTION_USB_DETACHED but no EXTRA_DEVICE");
                        }
                    }
                }
            }
        };
        UsbAndUsbSerialPrinterAdapter.context.registerReceiver(usbStateChangeReceiver, filter);

        usbManager = (UsbManager) context.getSystemService(USB_SERVICE);
    }

    @Override
    public void onListen(Object arguments, EventChannel.EventSink events) {
        this.events = events;
    }

    @Override
    public void onCancel(Object arguments) {
        this.events = null;
    }

    public List<HashMap<String, Object>> getUsbAndUsbSerialDevicesList() {
        HashMap<String, UsbDevice> usbAndUsbSerialDevices = usbManager.getDeviceList();
        List<HashMap<String, Object>> data = new ArrayList<>();
        if (usbAndUsbSerialDevices != null) {
            for (UsbDevice usbAndUsbSerialDevice : usbAndUsbSerialDevices.values()) {
                data.add(serializeDevice(usbAndUsbSerialDevice));
            }
        }
        return data;
    }

    private HashMap<String, Object> serializeDevice(UsbDevice device) {
        HashMap<String, Object> dev = new HashMap<>();
        dev.put("deviceName", device.getDeviceName());
        dev.put("vendorId", device.getVendorId());
        dev.put("productId", device.getProductId());
        dev.put("manufacturerName", device.getManufacturerName());
        dev.put("productName", device.getProductName());
        dev.put("interfaceCount", device.getInterfaceCount());
        try {
            dev.put("serialNumber", device.getSerialNumber());
        } catch  ( SecurityException e ) {
            Log.e(TAG, e.toString());
        }
        dev.put("deviceId", device.getDeviceId());
        return dev;
    }

    public boolean isConnected(String vendorId, String productId) {
        HashMap<String, UsbDevice> usbDevices = usbManager.getDeviceList();
        UsbDevice device = null;
        for (HashMap.Entry<String, UsbDevice> entry : usbDevices.entrySet()) {
            if (String.valueOf(entry.getValue().getVendorId()).equals(vendorId) && String.valueOf(entry.getValue().getProductId()).equals(productId)) {
                device = entry.getValue();
                break;
            }
        }
        if (device == null) {
            return false;
        }
        return usbManager.hasPermission(device);
    }

    public UsbDevice getUsbDevice(int vendorId, int productId, int deviceId) {
        HashMap<String, UsbDevice> devices = usbManager.getDeviceList();
        for (UsbDevice device : devices.values()) {
            if ( deviceId == device.getDeviceId() || (device.getVendorId() == vendorId && device.getProductId() == productId) ) {
                return device;
            }
        }
        return null;
    }


    private interface AcquirePermissionCallback {
        void onSuccess(UsbDevice device);
        void onFailed(UsbDevice device);
    }

    @SuppressLint("PrivateApi")
    private void acquirePermissions(UsbDevice device, AcquirePermissionCallback cb) {

        class BRC2 extends BroadcastReceiver {

            private final UsbDevice m_Device;
            private final AcquirePermissionCallback m_CB;

            BRC2(UsbDevice device, AcquirePermissionCallback cb) {
                m_Device = device;
                m_CB = cb;
            }

            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (ACTION_USB_PERMISSION.equals(action)) {
                    Log.e(TAG, "BroadcastReceiver intent arrived, entering sync...");
                    context.unregisterReceiver(this);
                    synchronized (this) {
                        Log.e(TAG, "BroadcastReceiver in sync");
                        /* UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE); */
                        if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                            // createPort(m_DriverIndex, m_PortIndex, m_Result, false);
                            m_CB.onSuccess(m_Device);
                        } else {
                            Log.d(TAG, "permission denied for device ");
                            m_CB.onFailed(m_Device);
                        }
                    }
                }
            }
        }
    }

    public void openUsbSerialDevice(String type, UsbDevice device, int iface, Result result, String alias, boolean allowAcquirePermission) {

        final AcquirePermissionCallback cb = new AcquirePermissionCallback() {
            @Override
            public void onSuccess(UsbDevice device) {
                openUsbSerialDevice(type, device, iface, result, alias,false);
            }
            @Override
            public void onFailed(UsbDevice device) {
                result.error(TAG, "Failed to acquire permissions.", null);
            }
        };

        try {
            UsbDeviceConnection connection = usbManager.openDevice(device);
            if ( connection == null && allowAcquirePermission ) {
                acquirePermissions(device, cb);
                return;
            }

            UsbSerialDevice usbSerialDevice;
            if ( type.equals("") ) {
                usbSerialDevice = UsbSerialDevice.createUsbSerialDevice(device, connection, iface);
            } else {
                usbSerialDevice = UsbSerialDevice.createUsbSerialDevice(type, device, connection, iface);
            }

            if (usbSerialDevice != null) {
                UsbSerialPrinter adapter = new UsbSerialPrinter(alias, usbSerialDevice);
                result.success(adapter.getMethodChannelName());
                Log.d(TAG, "success.");
                return;
            }
            result.error(TAG, "Not an Serial device.", null);

        } catch ( java.lang.SecurityException e ) {
            if ( allowAcquirePermission ) {
                acquirePermissions(device, cb);
            } else {
                result.error(TAG, "Failed to acquire USB permission.", null);
            }
        } catch ( java.lang.Exception e ) {
            result.error(TAG, "Failed to acquire USB device.", null);
        }
    }

    public void openUsbDevice(UsbDevice device, Result result, String alias, boolean allowAcquirePermission) {

        final AcquirePermissionCallback cb = new AcquirePermissionCallback() {
            @Override
            public void onSuccess(UsbDevice device) {
                openUsbDevice(device, result, alias,false);
            }
            @Override
            public void onFailed(UsbDevice device) {
                result.error(TAG, "Failed to acquire permissions.", null);
            }
        };

        try {
            if ( !usbManager.hasPermission(device) && allowAcquirePermission ) {
                acquirePermissions(device, cb);
                return;
            }

            UsbPrinter adapter = new UsbPrinter(alias, device, usbManager);
            result.success(adapter.getMethodChannelName());
            Log.d(TAG, "success.");

        } catch ( java.lang.SecurityException e ) {
            if ( allowAcquirePermission ) {
                acquirePermissions(device, cb);
            } else {
                result.error(TAG, "Failed to acquire USB permission.", null);
            }
        } catch ( java.lang.Exception e ) {
            result.error(TAG, "Failed to acquire USB device.", null);
        }
    }
}
