import 'package:flutter_test/flutter_test.dart';
import 'package:my_rfid_reader/my_rfid_reader.dart';
import 'package:my_rfid_reader/my_rfid_reader_platform_interface.dart';
import 'package:my_rfid_reader/my_rfid_reader_method_channel.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';

class MockMyRfidReaderPlatform
    with MockPlatformInterfaceMixin
    implements MyRfidReaderPlatform {

  @override
  Future<String?> getPlatformVersion() => Future.value('42');
}

void main() {
  final MyRfidReaderPlatform initialPlatform = MyRfidReaderPlatform.instance;

  test('$MethodChannelMyRfidReader is the default instance', () {
    expect(initialPlatform, isInstanceOf<MethodChannelMyRfidReader>());
  });

  test('getPlatformVersion', () async {
    MyRfidReader myRfidReaderPlugin = MyRfidReader();
    MockMyRfidReaderPlatform fakePlatform = MockMyRfidReaderPlatform();
    MyRfidReaderPlatform.instance = fakePlatform;

    expect(await myRfidReaderPlugin.getPlatformVersion(), '42');
  });
}
