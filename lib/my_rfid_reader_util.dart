import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

class MyRfidReaderUtil {
  MyRfidReaderUtil._();

  factory MyRfidReaderUtil() => _instance;
  static final MyRfidReaderUtil _instance = MyRfidReaderUtil._();

  BasicMessageChannel flutterChannel = const BasicMessageChannel("flutter_rfid_android", StandardMessageCodec());

  void sendMessageToAndroid(String methodName, dynamic arg) async {
    flutterChannel.send({methodName: arg});
  }

}