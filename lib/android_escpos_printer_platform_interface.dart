import 'package:plugin_platform_interface/plugin_platform_interface.dart';

import 'android_escpos_printer_method_channel.dart';

abstract class AndroidEscposPrinterPlatform extends PlatformInterface {
  /// Constructs a AndroidEscposPrinterPlatform.
  AndroidEscposPrinterPlatform() : super(token: _token);

  static final Object _token = Object();

  static AndroidEscposPrinterPlatform _instance = MethodChannelAndroidEscposPrinter();

  /// The default instance of [AndroidEscposPrinterPlatform] to use.
  ///
  /// Defaults to [MethodChannelAndroidEscposPrinter].
  static AndroidEscposPrinterPlatform get instance => _instance;

  /// Platform-specific implementations should set this with their own
  /// platform-specific class that extends [AndroidEscposPrinterPlatform] when
  /// they register themselves.
  static set instance(AndroidEscposPrinterPlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  Future<String?> getPlatformVersion() {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }
}
