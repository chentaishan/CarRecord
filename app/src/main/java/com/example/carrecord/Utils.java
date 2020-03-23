package com.example.carrecord;


import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.BatteryManager;
import android.os.Environment;
import android.util.Log;
import android.view.WindowManager;

import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Utils {

    private static final String TAG = "Utils";

    public static boolean phoneHas1024MB() {
        long maxSize = Environment.getExternalStorageDirectory().getFreeSpace();
        int freeMB = (int) (maxSize / (1024 * 1024));
        return freeMB>1024 ? true:false;
    }

    public static void checkSDSize() {
        long maxSize = Environment.getExternalStorageDirectory().getFreeSpace();
        int freeMB = (int) (maxSize / (1024 * 1024));
        Log.d(TAG, "checkSDSize: freeMB=" + freeMB);
        if (freeMB < 1000) {
            // 删除
            String rootPath = Environment.getExternalStorageDirectory() + File.separator + "car_record";
            final File file = new File(rootPath);
            final File[] files = file.listFiles();
            if (files.length > 0 && file.getName().endsWith(".mp4")) {

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

    /**
     * 获取缓存文件夹，这里优先选择SD卡下面的android/data/packageName/cache/路径，若没有SD卡，就选择data/data/packageName/cache
     *
     * @param context    上下文环境
     * @param uniqueName 缓存文件夹名称
     * @return 返回缓存文件
     */
    public static File getDiskCacheDir(Context context, String uniqueName) {
        String cachePath;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                || !Environment.isExternalStorageRemovable()) {
            cachePath = context.getExternalCacheDir().getPath();
        } else {
            cachePath = context.getCacheDir().getPath();
        }
        File file = new File(cachePath + File.separator + uniqueName);
        Log.d(TAG, "getDiskCacheDir: file=" + file.getAbsolutePath());
        return file;
    }


    /**
     * 获取本App的版本号
     *
     * @param context context上下文
     * @return 返回版本号
     */
    public static int getAppVersion(Context context) {
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return info.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return 1;
    }

    /**
     * 给字符串来个md5加密，
     *
     * @param key 需要加密的string
     * @return 返回加密后的string ，或者加密失败，就返回string的哈希值
     */
    public static String hashKeyForDisk(String key) {
        String cacheKey;
        try {
            //md5加密
            MessageDigest mDigest = MessageDigest.getInstance("MD5");
            mDigest.update(key.getBytes());
            cacheKey = bytesToHexString(mDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            //若md5加密失败，就用哈希值
            cacheKey = String.valueOf(key.hashCode());
        }
        return cacheKey;
    }

    /**
     * 字节数组转为十六进制字符串
     *
     * @param bytes 字节数组
     * @return 返回十六进制字符串
     */
    private static String bytesToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xFF & b);
            if (hex.length() == 1) {
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
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

