import 'dart:async';

import 'package:flutter/services.dart';

class Ussd {
  static const MethodChannel _channel = const MethodChannel('ussd');

  static Future<String> runUssd(String ussdCode) async {
    final String launch = await _channel.invokeMethod('runUssd', <String, dynamic>{
        'ussdCode': ussdCode,
      });
    return launch;
  }
  
}

