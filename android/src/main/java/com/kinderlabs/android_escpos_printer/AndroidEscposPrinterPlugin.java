package com.kinderlabs.android_escpos_printer;

import android.Manifest;
import android.app.Activity;
import android.app.Application;
import android.content.pm.PackageManager;
import android.hardware.usb.UsbDevice;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry;

/** AndroidEscposPrinterPlugin */
public class AndroidEscposPrinterPlugin implements FlutterPlugin, MethodCallHandler, ActivityAware, PluginRegistry.RequestPermissionsResultListener {
  private final String TAG = AndroidEscposPrinterPlugin.class.getSimpleName();
  private static final int REQUEST_COARSE_LOCATION_PERMISSIONS = 1451;
  private MethodChannel channel;
  private Activity activity;
  private UsbAndUsbSerialPrinterAdapter usbAndUsbSerialPrinterAdapter;
  private SerialPrinterAdapter serialPrinterAdapter;
  private BluetoothClassicPrinterAdapter bluetoothClassicPrinterAdapter;
  private Result pendingResult;

  @Override
  public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
    channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), "android_escpos_printer");
    channel.setMethodCallHandler(this);
    usbAndUsbSerialPrinterAdapter = new UsbAndUsbSerialPrinterAdapter(flutterPluginBinding.getApplicationContext(), flutterPluginBinding.getBinaryMessenger());
    serialPrinterAdapter = new SerialPrinterAdapter();
    bluetoothClassicPrinterAdapter = new BluetoothClassicPrinterAdapter((Application) flutterPluginBinding.getApplicationContext());
  }

  @Override
  public void onAttachedToActivity(@NonNull ActivityPluginBinding binding) {
    activity = binding.getActivity();
  }

  @Override
  public void onDetachedFromActivityForConfigChanges() {

  }

  @Override
  public void onReattachedToActivityForConfigChanges(@NonNull ActivityPluginBinding binding) {
    onAttachedToActivity(binding);
  }

  @Override
  public void onDetachedFromActivity() {

  }

  @Override
  public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
    switch (call.method) {
      case "getPlatformVersion": {
        result.success("Android " + android.os.Build.VERSION.RELEASE);
        break;
      }

      case "getUsbAndUsbSerialDevicesList": {
        result.success(usbAndUsbSerialPrinterAdapter.getUsbAndUsbSerialDevicesList());
        break;
      }

      case "getSerialPortsList": {
        result.success(serialPrinterAdapter.getSerialPortsList());
        break;
      }

      case "getBClassicBondedDevicesList": {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
          if (ContextCompat.checkSelfPermission(activity,
                  Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED ||
                  ContextCompat.checkSelfPermission(activity,
                          Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED ||
                  ContextCompat.checkSelfPermission(activity,
                          Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(activity,new String[]{
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.ACCESS_FINE_LOCATION,
            }, 1);

            pendingResult = result;
            break;
          }
        } else {
          if (ContextCompat.checkSelfPermission(activity,
                  Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED||ContextCompat.checkSelfPermission(activity,
                  Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(activity,
                    new String[] { Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION }, REQUEST_COARSE_LOCATION_PERMISSIONS);

            pendingResult = result;
            break;
          }
        }
        bluetoothClassicPrinterAdapter.getBClassicBondedDevicesList(result);
      }

      case "connectUsbDevice": {
        Integer vendorId = call.argument("vendorId");
        Integer productId = call.argument("productId");
        Integer deviceId = call.argument("deviceId");
        String alias = call.argument("alias");
        if (vendorId !=null && productId !=null && deviceId != null) {
          UsbDevice usbDevice = usbAndUsbSerialPrinterAdapter.getUsbDevice(vendorId, productId, deviceId);
          usbAndUsbSerialPrinterAdapter.openUsbDevice(usbDevice, result, alias, true);
        }
        break;
      }

      case "connectUsbSerialDevice": {
        String type = call.argument("type");
        Integer vendorId = call.argument("vendorId");
        Integer productId = call.argument("productId");
        Integer deviceId = call.argument("deviceId");
        Integer interfaceId = call.argument("interface");
        String alias = call.argument("alias");
        if (type !=null && vendorId !=null && productId !=null && deviceId != null && interfaceId != null ) {
          UsbDevice usbDevice = usbAndUsbSerialPrinterAdapter.getUsbDevice(vendorId, productId, deviceId);
          if (usbDevice != null) {
            usbAndUsbSerialPrinterAdapter.openUsbSerialDevice(type, usbDevice, interfaceId, result, alias, true);
          } else {
            result.error(TAG, "No such device", null);
          }
        }
        break;
      }

      case "connectSerialDevice": {
        String device = call.argument("device");
        Integer baudRate = call.argument("baudRate");
        String alias = call.argument("alias");
        serialPrinterAdapter.openSerialDevice(device, baudRate, alias, result);
        result.error(TAG, "Fail to open serial device.", null);
        break;
      }

      case "connectBClassicDevice": {
        String address = call.argument("address");
        String alias = call.argument("alias");
        bluetoothClassicPrinterAdapter.openBluetoothClassicDevice(address, alias, result);
      }

      case "isConnected": {
        String vendorId = call.argument("vendorId");
        String productId = call.argument("productId");
        result.success(usbAndUsbSerialPrinterAdapter.isConnected(vendorId, productId));
        break;
      }

      default:
        result.notImplemented();
        break;
    }
  }

  @Override
  public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
    channel.setMethodCallHandler(null);
  }

  @Override
  public boolean onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    if (requestCode == REQUEST_COARSE_LOCATION_PERMISSIONS) {
      if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
        bluetoothClassicPrinterAdapter.getBClassicBondedDevicesList(pendingResult);
      } else {
        pendingResult.error("no_permissions", "this plugin requires location permissions for scanning", null);
        pendingResult = null;
      }
      return true;
    }
    return false;
  }
}
