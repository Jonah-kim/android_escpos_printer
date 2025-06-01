
import 'android_escpos_printer_platform_interface.dart';

class AndroidEscposPrinter {
  Future<String?> getPlatformVersion() {
    return AndroidEscposPrinterPlatform.instance.getPlatformVersion();
  }
}
