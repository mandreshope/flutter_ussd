package com.mandreshope.ussd;

import android.Manifest;
import android.content.Context;
import android.app.Application;
import android.os.Bundle;
import android.os.Build;
import android.content.pm.PackageManager;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;


import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry;
import io.flutter.plugin.common.PluginRegistry.Registrar;

/** UssdPlugin */
public class UssdPlugin implements MethodCallHandler {
  /** Plugin registration. */
  private static final int PHONE_CALL_REQUEST_ID = 513469796;
  MethodChannel methodChannel;
  private Activity activity;
  private Registrar registrar;
  private Application.ActivityLifecycleCallbacks activityLifecycleCallbacks;
  // The code to run after requesting phone_call permissions.
  private Runnable phonecallPermissionContinuation;
  private boolean requestingPermission;
  
  public static void registerWith(Registrar registrar) {
    final MethodChannel channel = new MethodChannel(registrar.messenger(), "ussd");
    channel.setMethodCallHandler(new UssdPlugin(registrar, registrar.activity(), channel));
  }

  // constructor
  public UssdPlugin(Registrar registrar, Activity activity, MethodChannel methodChannel) {
    this.registrar = registrar;
    this.activity = activity;
    this.methodChannel = methodChannel;
    this.methodChannel.setMethodCallHandler(this);

    registrar.addRequestPermissionsResultListener(new PhoneCallRequestPermissionsListener());

    this.activityLifecycleCallbacks = new Application.ActivityLifecycleCallbacks() {
      @Override
      public void onActivityCreated(Activity activity, Bundle savedInstanceState) {}

      @Override
      public void onActivityStarted(Activity activity) {}

      @Override
      public void onActivityResumed(Activity activity) {
        boolean wasRequestingPermission = requestingPermission;
        if (requestingPermission) {
          requestingPermission = false;
        }
        if (activity != UssdPlugin.this.activity) {
          return;
        }
      }

      @Override
      public void onActivityPaused(Activity activity) {}

      @Override
      public void onActivityStopped(Activity activity) {}

      @Override
      public void onActivitySaveInstanceState(Activity activity, Bundle outState) {}

      @Override
      public void onActivityDestroyed(Activity activity) {}
    };
  }

  @Override
  public void onMethodCall(MethodCall call, Result result) {
    if (call.method.equals("runUssd")) {
      String ussdCode = call.argument("ussdCode");
      runUssd(ussdCode);
    } else {
      result.notImplemented();
    }
  }

  void runUssd(String ussdCode){
    if (phonecallPermissionContinuation != null) {
      // result.error("phoneCallPermission", "Phone_Call permission request ongoing", null);
    }
    phonecallPermissionContinuation =
        new Runnable() {
          @Override
          public void run() {
            phonecallPermissionContinuation = null;
            if (!hasPhoneCallPermission()) {
              // result.error("phoneCallPermission", "MediaRecorderCamera permission not granted", null);
              return;
            }
          }
        };
    requestingPermission = false;
    if (hasPhoneCallPermission()) {
      Intent intent = new Intent(Intent.ACTION_CALL);
      intent.setData(ussdToCallableUri(ussdCode));
      try{
        activity.startActivity(intent);
      } catch (SecurityException e){
        e.printStackTrace();
      }
      phonecallPermissionContinuation.run();
    } else {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        requestingPermission = true;
        registrar.activity().requestPermissions(new String[] {Manifest.permission.CALL_PHONE}, PHONE_CALL_REQUEST_ID);
      }
    }
  }

  private boolean hasPhoneCallPermission() {
    return Build.VERSION.SDK_INT < Build.VERSION_CODES.M
        || activity.checkSelfPermission(Manifest.permission.CALL_PHONE)
            == PackageManager.PERMISSION_GRANTED;
  }

  private class PhoneCallRequestPermissionsListener implements PluginRegistry.RequestPermissionsResultListener {
    @Override
    public boolean onRequestPermissionsResult(int id, String[] permissions, int[] grantResults) {
      if (id == PHONE_CALL_REQUEST_ID) {
        phonecallPermissionContinuation.run();
        return true;
      }
      return false;
    }
  }


  private Uri ussdToCallableUri(String ussd) {

    String uriString = "";

    if(!ussd.startsWith("tel:"))
        uriString += "tel:";

    for(char c : ussd.toCharArray()) {

        if(c == '#')
            uriString += Uri.encode("#");
        else
            uriString += c;
    }

    return Uri.parse(uriString);
  }
   
}
