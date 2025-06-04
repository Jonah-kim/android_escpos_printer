import 'dart:typed_data';

import 'package:android_escpos_printer/src/models/printer_connect_info.dart';

abstract class PrinterAdapter {
  Future<void> connect(PrinterConnectInfo info);
  Future<void> disconnect();
  Future<void> write(Uint8List data);
  Stream<Uint8List> onRead();
  bool get isConnected;
}
