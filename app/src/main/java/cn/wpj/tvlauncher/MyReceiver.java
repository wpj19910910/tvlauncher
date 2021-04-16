package cn.wpj.tvlauncher;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.greenrobot.eventbus.EventBus;

public class MyReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.PACKAGE_ADDED")) {
            EventBus.getDefault().post("app_list_update");
        }
        if (intent.getAction().equals("android.intent.action.PACKAGE_REMOVED")) {
            EventBus.getDefault().post("app_list_update");
        }
    }
}
