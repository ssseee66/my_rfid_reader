
import 'dart:async';

import 'package:flutter/services.dart';

class MyBluetooth {
  static const MethodChannel _channel =
  MethodChannel('my_rfid_reader');

  static Future<String?> Init() async {
    final String? code = await _channel.invokeMethod('init');
    return code;
  }
}