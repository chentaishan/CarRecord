package com.example.carrecord;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.jakewharton.disklrucache.Util;

public class DamonService extends Service {
    public DamonService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    int delay = 1000*5;

    @Override
    public void onCreate() {
        super.onCreate();
        new Thread(){

            @Override
            public void run() {
                super.run();

                while (true){


                    try {
                        Thread.sleep(delay);
                        final boolean charging = Utils.isCharging(DamonService.this);

                        Log.d(TAG, "running--"+charging+"---"+Contants.isStarted);
                        if (charging&&!Contants.isStarted){
//                            com.service_start_activity

                            Contants.isStarted = true;
                            sendBroadcast(new Intent("com.service_start_activity"));

                        }


                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }

            }
        }.start();

    }

    private static final String TAG = "DamonService";
}
