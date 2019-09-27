import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:ussd/ussd.dart';

void main() {
  const MethodChannel channel = MethodChannel('ussd');

  setUp(() {
    channel.setMockMethodCallHandler((MethodCall methodCall) async {
      return '42';
    });
  });

  tearDown(() {
    channel.setMockMethodCallHandler(null);
  });

  test('runUssdCode', () async {
    expect(await Ussd.runUssd("*436*2*1*1*0330297426*0330297426#"), '42');
  });
}
