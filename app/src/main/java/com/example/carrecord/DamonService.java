package com.example.carrecord;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;


public class DamonService extends Service {
    public DamonService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return null;
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

                        final boolean isVisible = Utils.isForeground(DamonService.this, "com.example.carrecord.MainActivity");



                        if (charging&&!isVisible){
//                            com.service_start_activity


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

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
    }
}
