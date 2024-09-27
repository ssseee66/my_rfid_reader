import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:my_rfid_reader/my_rfid_reader_util.dart';

mixin MyRfidReaderMixin<T extends StatefulWidget> on State<T> {
  late StreamSubscription streamSubscription;
  final MyRfidReaderUtil util = MyRfidReaderUtil();

  @override
  void initState() {
    super.initState();
    util.flutterChannel.setMessageHandler(listenerAndroidHandle);
  }

  Future<void> listenerAndroidHandle(dynamic message);

}