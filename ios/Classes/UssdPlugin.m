#import "UssdPlugin.h"
#import <ussd/ussd-Swift.h>

@implementation UssdPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftUssdPlugin registerWithRegistrar:registrar];
}
@end
