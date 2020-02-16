package com.vil.vil_bot.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import android.util.Log;

public class TextReciever extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getExtras() != null) {
            String result = intent.getStringExtra("text");
            Log.e("CHECK_INTENT", intent.getStringExtra("text"));

            Intent in = new Intent("textrecieved");
            Bundle extras = new Bundle();

            extras.putString("text", result);
            in.putExtras(extras);
            context.sendBroadcast(in);
        }
    }
}