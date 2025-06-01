import 'package:flutter_test/flutter_test.dart';
import 'package:android_escpos_printer/android_escpos_printer.dart';
import 'package:android_escpos_printer/android_escpos_printer_platform_interface.dart';
import 'package:android_escpos_printer/android_escpos_printer_method_channel.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';

class MockAndroidEscposPrinterPlatform
    with MockPlatformInterfaceMixin
    implements AndroidEscposPrinterPlatform {

  @override
  Future<String?> getPlatformVersion() => Future.value('42');
}

void main() {
  final AndroidEscposPrinterPlatform initialPlatform = AndroidEscposPrinterPlatform.instance;

  test('$MethodChannelAndroidEscposPrinter is the default instance', () {
    expect(initialPlatform, isInstanceOf<MethodChannelAndroidEscposPrinter>());
  });

  test('getPlatformVersion', () async {
    AndroidEscposPrinter androidEscposPrinterPlugin = AndroidEscposPrinter();
    MockAndroidEscposPrinterPlatform fakePlatform = MockAndroidEscposPrinterPlatform();
    AndroidEscposPrinterPlatform.instance = fakePlatform;

    expect(await androidEscposPrinterPlugin.getPlatformVersion(), '42');
  });
}
