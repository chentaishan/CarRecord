package com.example.carrecord;


import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.BatteryManager;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.WindowManager;

import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class Utils {

    private static final String TAG = "Utils";
    static String rootPath = Environment.getExternalStorageDirectory() + File.separator + "car_record";


    public static boolean  isForeground(Context context, String className) {
        if (context == null || TextUtils.isEmpty(className)) {
            return false;
        }

        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> list = am.getRunningTasks(1);
        if (list != null && list.size() > 0) {
            ComponentName cpn = list.get(0).topActivity;
            if (className.equals(cpn.getClassName())) {
                return true;
            }
        }

        return false;

    }

    public static boolean phoneHas1024MB() {
        long maxSize = Environment.getExternalStorageDirectory().getFreeSpace();
        int freeMB = (int) (maxSize / (1024 * 1024));
        Log.d(TAG, "checkSDSize: freeMB=" + freeMB);
        return freeMB>1024 ? true:false;
    }

    public static void checkSDSize() {

        if (!phoneHas1024MB()) {
            // 删除

            final File file = new File(rootPath);
            final File[] files = file.listFiles();
            if (files.length > 0 ) {

                final boolean delete = files[0].delete();
                if (delete) {

                    checkSDSize();
                }
            }
        }

    }

    public static String getOutputMediaFile() {
        String rootPath = Environment.getExternalStorageDirectory() + File.separator + "car_record";
        final File file = new File(rootPath);
        if (!file.exists()) {
            file.mkdirs();
        }
        final Date date = new Date();

        String filename = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);


        return file.getAbsolutePath() + File.separator + filename + ".mp4";

    }




    public static int[] getScreenWidth(Context context){
        WindowManager wm = (WindowManager) context  .getSystemService(Context.WINDOW_SERVICE);
        int width = wm.getDefaultDisplay().getWidth();
        int height = wm.getDefaultDisplay().getHeight();
        return new int[]{width,height};
    }

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public static int dp2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * 是否充电中
     * @param context
     * @return
     */
    public static boolean isCharging(Context context) {
        Intent batteryBroadcast = context.registerReceiver(null,
                new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        // 0 means we are discharging, anything else means charging
        boolean isCharging = batteryBroadcast.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1) != 0;
        Log.d(TAG,"isCharging = " + isCharging );
        return isCharging;
    }

}

