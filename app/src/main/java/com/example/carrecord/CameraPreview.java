package com.example.carrecord;

import android.content.Context;

import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;
import java.util.List;

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    private SurfaceHolder mHolder;
    private Camera mCamera;
    private Context context;

    private static final String TAG = "CameraPreview";
    private boolean mIsPortrait;
    private Camera.Parameters parameters;
    private Camera.Size optionSize;

    public CameraPreview(Context context, Camera camera) {
        super(context);
        this.context = context;
        mCamera = camera;

        parameters = mCamera.getParameters();
        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = getHolder();
        mHolder.addCallback(this);
        // deprecated setting, but required on Android versions prior to 3.0
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    public void surfaceCreated(SurfaceHolder holder) {
        // 通知摄像头可以在这里绘制预览了
        try {


            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
            final int[] screenWidth = Utils.getScreenWidth(context);

            Log.d("jxd","optionSize : screenWidth "+screenWidth[0]+" * "+screenWidth[1]);


            List<Camera.Size> sizeList = parameters.getSupportedPreviewSizes();//获取所有支持的camera尺寸
            Log.d("jxd","optionSize : mSurfaceView "+getWidth()+" * "+getHeight());
            //获取一个最为适配的camera.size
            optionSize = getOptimalPreviewSize(sizeList,  getHeight(), getWidth());
            Log.d("jxd","optionSize : "+ optionSize.width+" * "+ optionSize.height);
            parameters.setPreviewSize(1280,720);//把camera.size赋值到parameters
            mCamera.setParameters(parameters);


            mCamera.setDisplayOrientation(180);
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
        } catch (IOException e) {
            Log.d(TAG, "Error setting camera preview: " + e.getMessage());
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        // 什么都不做，但是在Activity中Camera要正确地释放预览视图
        releaseCamera();
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        // 如果预览视图可变或者旋转，要在这里处理好这些事件
        // 在重置大小或格式化时，确保停止预览

        if (mHolder.getSurface() == null) {
            // preview surface does not exist
            return;
        }

        // 变更之前要停止预览
        try {
            mCamera.stopPreview();
        } catch (Exception e) {
            // ignore: tried to stop a non-existent preview
        }

        // 在这里重置预览视图的大小、旋转、格式化

        // 使用新设置启动预览视图
        try {
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();

        } catch (Exception e) {
            Log.d(TAG, "Error starting camera preview: " + e.getMessage());
        }
    }

    private void releaseCamera() {
        if (mCamera != null) {
            mCamera.release();        // release the camera for other applications
            mCamera = null;
        }
    }

    /**
     * 解决预览变形问题
     * @param sizes
     * @param w
     * @param h
     * @return
     */
    private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) w / h;
        if (sizes == null) return null;

        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = h;

        // Try to find an size match aspect ratio and size
        for (Camera.Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        // Cannot find the one match the aspect ratio, ignore the requirement
        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }


}
