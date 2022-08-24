package com.isb.webplayer;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;


public class StartMyServiceAtBootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {


        Log.d(" BroadcastReceiver", "Starting webplayer");

        try {
            Util.scheduleJob(context, 60);
        } catch (Exception e) {
            Log.d(" BroadcastReceiver", e.getMessage());
        }


    }

}
