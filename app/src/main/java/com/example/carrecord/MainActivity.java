package com.example.carrecord;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private Camera mCamera;
    private CameraPreview mPreview;
    private MediaRecorder mMediaRecorder;
    private TextView mCaptureButton;
    long totalSize;
    //每段视频的时长5分钟
    int timePart = 60 * 5;
//    int timePart = 20;

    int delay = 5;
    private TextView mTimer;
    Handler handler = new Handler() {
        @SuppressLint("HandlerLeak")
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {

                case SEND_MSG:
                    if (delay > 0) {

                        delay--;
                        mTimer.setText(delay + "s");
                        final Message message = new Message();
                        message.what = SEND_MSG;
                        handler.sendMessageDelayed(message, 1000);
                    } else {
                        // 开始录制
                        mCaptureButton.setText("录制中..");

                        recordTimer();

                    }
                    break;

            }

        }
    };
    private FrameLayout preview;
    private CountDownTimer countDownTimer;

    /**
     * 开启定时器 5分钟录屏一个文件
     */
    private void recordTimer() {

        countDownTimer = new CountDownTimer(Long.MAX_VALUE, 1000) {

            @Override
            public void onTick(long millisUntilFinished) {

                if (totalSize == 0) {
                    startRecord();

                } else if (totalSize % timePart == 0) {

                    final boolean charging = Utils.isCharging(MainActivity.this);
                    Log.d(TAG, "onTick: 充电中=" + charging);
                    // 如果不是充电中，停止录制视频
                    if (!charging) {
                        goHome();
                        return;
                    }
                    // 开始录制

                    onPauseRecord();

                    startRecord();
                }
                // 一秒执行一次
                totalSize++;
                Log.d(TAG, "onTick: " + totalSize);
                mTimer.setText(totalSize + "s");
            }

            @Override
            public void onFinish() {

            }
        };
        countDownTimer.start();
    }

    private static final int SEND_MSG = 11;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        //去除标题栏
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //去除状态栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);


    }


    @Override
    protected void onResume() {
        super.onResume();

        initView();



        if (!Utils.phoneHas1024MB()) {
            Utils.checkSDSize();
            Toast.makeText(this, "手机SD卡空间不足！", Toast.LENGTH_LONG).show();

        }
        startService(new Intent(this, DamonService.class));

        // 创建Camera实例
        mCamera = getCameraInstance();


        // 创建预览视图，并作为Activity的内容
        mPreview = new CameraPreview(this, mCamera);



        preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(mPreview);


        //倒计时5秒开始录屏
        handler.removeMessages(SEND_MSG);
        final Message message = new Message();
        message.what = SEND_MSG;
        handler.sendMessageDelayed(message, 1000);

    }

    private void initView() {

        mCaptureButton = (TextView) findViewById(R.id.button_capture);
        mTimer = (TextView) findViewById(R.id.timer);
    }

    private boolean checkCameraHardware(Context context) {
        // 支持所有版本
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY);
        //  Android 2.3 (API Level 9) 及以上的
        // return  Camera.getNumberOfCameras() > 0;
    }

    public static Camera getCameraInstance() {
        Camera c = null;
        try {
            // 在多个摄像头时，默认打开后置摄像头
            c = Camera.open();


            // Android 2.3（API 9之后可指定cameraId摄像头id，可选值为后置（CAMERA_FACING_BACK）/前置（ CAMERA_FACING_FRONT）
            //  c = Camera.open(cameraId);
        } catch (Exception e) {
            // Camera被占用或者设备上没有相机时会崩溃。
            e.printStackTrace();

            c = Camera.open();
            Log.d(TAG, "getCameraInstance: " + e.getMessage());
        }
        return c;  // returns null if camera is unavailable
    }

    private boolean prepareVideoRecorder() {

        if (mCamera == null) {
            mCamera = getCameraInstance();
        }
        mMediaRecorder = new MediaRecorder();

        // Step 1: Unlock and set camera to MediaRecorder
        mCamera.unlock();


        mMediaRecorder.setCamera(mCamera);

        // Step 2: Set sources
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

        // Step 3: Set a CamcorderProfile (requires API Level 8 or higher)
        mMediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));

        // Step 4: Set output file
        mMediaRecorder.setOutputFile(Utils.getOutputMediaFile());

        // Step 5: Set the preview output
        mMediaRecorder.setPreviewDisplay(mPreview.getHolder().getSurface());
        mMediaRecorder.setVideoSize(960, 720);
        // 修正播放视频的方向
        mMediaRecorder.setOrientationHint(180);

        mMediaRecorder.setOnInfoListener(new MediaRecorder.OnInfoListener() {
            @Override
            public void onInfo(MediaRecorder mr, int what, int extra) {
                if (MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED == what) {
                    //到达最大时长
                    onPauseRecord();
                    startRecord();
                } else if (MediaRecorder.MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED == what) {
                    //到达最大尺寸

                    onPauseRecord();
                    startRecord();
                }


            }
        });
        // Step 6: Prepare configured MediaRecorder
        try {
            mMediaRecorder.prepare();
        } catch (IllegalStateException e) {
            Log.d(TAG, "IllegalStateException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        } catch (IOException e) {
            Log.d(TAG, "IOException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        }
        return true;
    }

    private static final String TAG = "MainActivity";


    public void stopRecord() {

        if (mMediaRecorder != null) {
            // stop recording and release camera
            mMediaRecorder.stop();  // stop the recording
            releaseMediaRecorder(); // release the MediaRecorder object
        }


    }

    private void startRecord() {


        // initialize video camera
        if (prepareVideoRecorder()) {
            // Camera is available and unlocked, MediaRecorder is prepared,
            // now you can start recording
            mMediaRecorder.start();

            // inform the user that recording has started


        } else {
            // prepare didn't work, release the camera
            releaseMediaRecorder();
            // inform user

        }
    }

    private void releaseMediaRecorder() {
        if (mMediaRecorder != null) {
            mMediaRecorder.reset();   // clear recorder configuration
            mMediaRecorder.release(); // release the recorder object
            mMediaRecorder = null;
            // lock camera for later use
        }
    }

    private void releaseCamera() {
        if (mCamera != null) {
            mCamera.release();        // release the camera for other applications

        }
    }


    public void onPauseRecord(){
        if (mMediaRecorder != null) {
            // stop recording and release camera
            mMediaRecorder.stop();  // stop the recording
            releaseMediaRecorder(); // release the MediaRecorder object
        }

    }

    @Override
    protected void onStop() {
        super.onStop();

        Log.d(TAG, "onStop: ");

        preview.removeAllViews();


        stopRecord();

        releaseCamera();
        if (countDownTimer != null) {

            countDownTimer.cancel();
            countDownTimer = null;
        }
        totalSize = 0;

    }


    public void goHome() {


        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addCategory(Intent.CATEGORY_HOME);
        startActivity(intent);
    }

}
