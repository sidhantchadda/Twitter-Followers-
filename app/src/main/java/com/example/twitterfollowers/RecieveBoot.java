package com.example.twitterfollowers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class RecieveBoot extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)){
            SharedPreferences sp = context.getSharedPreferences("MyPref", 0);
            SharedPreferences.Editor edit = sp.edit();
            edit.putBoolean("alarmOn", false);
            edit.commit();
        }
    }
}
