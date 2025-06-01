import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

import 'android_escpos_printer_platform_interface.dart';

/// An implementation of [AndroidEscposPrinterPlatform] that uses method channels.
class MethodChannelAndroidEscposPrinter extends AndroidEscposPrinterPlatform {
  /// The method channel used to interact with the native platform.
  @visibleForTesting
  final methodChannel = const MethodChannel('android_escpos_printer');

  @override
  Future<String?> getPlatformVersion() async {
    final version = await methodChannel.invokeMethod<String>('getPlatformVersion');
    return version;
  }
}
