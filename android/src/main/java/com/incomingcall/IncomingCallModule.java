package com.incomingcall;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.view.WindowManager;
import android.content.Context;
import android.util.Log;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeMap;

public class IncomingCallModule extends ReactContextBaseJavaModule {

    public static ReactApplicationContext reactContext;
    public static Activity mainActivity;

    private static final String TAG = "RNIC:IncomingCallModule";
    private WritableMap headlessExtras;
    private String currentCall;

    public IncomingCallModule(ReactApplicationContext context) {
        super(context);
        reactContext = context;
        mainActivity = getCurrentActivity();
    }

    @Override
    public String getName() {
        return "IncomingCall";
    }

    @ReactMethod
    public void display(String uuid, String name, String avatar, String info, String payload, int timeout) {
        if (UnlockScreenActivity.active) {
            return;
        }
        if (reactContext != null) {
            currentCall = payload;
            Bundle bundle = new Bundle();
            bundle.putString("uuid", uuid);
            bundle.putString("name", name);
            bundle.putString("avatar", avatar);
            bundle.putString("info", info);
            bundle.putInt("timeout", timeout);
            Intent i = new Intent(reactContext, UnlockScreenActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            i.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED +
            WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD +
            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
            
            i.putExtras(bundle);
            reactContext.startActivity(i);
            
        }
    }

    private Context getAppContext() {
        return this.reactContext.getApplicationContext();
    }

    @ReactMethod
    public void backToForeground() {
        Context context = getAppContext();
        String packageName = context.getApplicationContext().getPackageName();
        Intent focusIntent = context.getPackageManager().getLaunchIntentForPackage(packageName).cloneFilter();
        Activity activity = getCurrentActivity();
        boolean isOpened = activity != null;
        Log.d(TAG, "backToForeground, app isOpened ?" + (isOpened ? "true" : "false"));

        if (isOpened) {
            focusIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            activity.startActivity(focusIntent);
        }
    }

    @ReactMethod
    public void openAppFromHeadlessMode(String uuid) {
        Context context = getAppContext();
        String packageName = context.getApplicationContext().getPackageName();
        Intent focusIntent = context.getPackageManager().getLaunchIntentForPackage(packageName).cloneFilter();
        Activity activity = getCurrentActivity();
        boolean isOpened = activity != null;

        if (!isOpened) {
            focusIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

            final WritableMap response = new WritableNativeMap();
            response.putBoolean("isHeadless", true);
            response.putString("uuid", uuid);
            if(currentCall != null){
                response.putString("payload", currentCall);
            }
            this.headlessExtras = response;

            getReactApplicationContext().startActivity(focusIntent);
        }
    }


    @ReactMethod
    public void dismiss() {
        Log.e("Incommingcallmodule", "start dismissing from native android: " + UnlockScreenActivity.active);
        if (UnlockScreenActivity.active) {
           UnlockScreenActivity.getInstance().dismissIncoming();
        }
        return;
    }

    @ReactMethod
    public void getExtrasFromHeadlessMode(Promise promise) {
        if (this.headlessExtras != null) {
            promise.resolve(this.headlessExtras);

            this.headlessExtras = null;
            this.currentCall = null;

            return;
        }

        promise.resolve(null);
    }
}
