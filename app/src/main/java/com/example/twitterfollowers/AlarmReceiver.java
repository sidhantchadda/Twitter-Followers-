package com.example.twitterfollowers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

public class AlarmReceiver extends BroadcastReceiver{

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		Log.d("Broadcaster recived", "Cpu has been awoken");
		// Loads Get_Followers class whenever this Broadcast Receiver has been called.
		SharedPreferences sp = context.getSharedPreferences("MyPref", 0);
		Editor edit = sp.edit();
		edit.putBoolean("alarmOn", true);
		edit.commit();
		Intent intentone = new Intent(context.getApplicationContext(), Get_Followers.class);
		intentone.putExtra("fromAlarm", true);
		intentone.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(intentone);
	}
}
