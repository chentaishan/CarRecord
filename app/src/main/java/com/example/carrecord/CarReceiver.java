package com.example.carrecord;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;


public class CarReceiver extends BroadcastReceiver {

    private static final String TAG = "CarReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {

       Intent  intent1=new Intent(context,MainActivity.class);
        intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent1);

        Toast.makeText(context, ""+intent.getAction(), Toast.LENGTH_LONG).show();

        Log.d(TAG, "onReceive: "+intent.getAction());
    }
}
