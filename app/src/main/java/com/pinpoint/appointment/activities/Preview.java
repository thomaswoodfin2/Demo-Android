package com.pinpoint.appointment.activities;

/**
 * Created by HEX15 on 07-06-2016.
 */

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.pinpoint.appointment.R;
import com.pinpoint.appointment.customviews.CustomDialog;
import com.pinpoint.appointment.models.LeadDetails;
import com.pinpoint.appointment.utils.Util;

import java.io.IOException;
import java.util.List;

public class Preview extends SurfaceView implements SurfaceHolder.Callback {
    private static final String TAG = "Preview";

    SurfaceHolder mHolder;
    public Camera camera;
    Context mContext;

    public Preview(Context context) {
        super(context);
        try
        {
            String message="FROM:Preview constructor";
//            LeadDetails.setMessage(context,message);
            mContext = context;
            // Install a SurfaceHolder.Callback so we get notified when the
            // underlying surface is created and destroyed.
            mHolder = getHolder();
            mHolder.addCallback(this);
//            mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        } catch (Exception e)
        {
            CustomDialog.getInstance().hide();
            String message=e.toString();
//            LeadDetails.setMessage(context,message);
//            CustomDialog.getInstance().hide();
//            CustomDialog.getInstance().showAlert(mContext, e.toString(), e.toString(), Util.getAppKeyValue(mContext, R.string.lblOk), Util.getAppKeyValue(mContext, R.string.str_dismiss), false);
            e.printStackTrace();
        }
    }

    public void surfaceCreated(SurfaceHolder holder) {
        // The Surface has been created, acquire the camera and tell it where
        // to draw.
        String message="FROM:surfaceCreated method";
//        LeadDetails.setMessage(mContext,message);
        try
        {
            releaseCamera();
            camera = Camera.open(getFrontCameraId());
            camera.setPreviewDisplay(holder);
            camera.setPreviewCallback(new PreviewCallback() {

                public void onPreviewFrame(byte[] data, Camera arg1)
                {//
                    Preview.this.invalidate();
                }
            });
        } catch (Exception e) {
            CustomDialog.getInstance().hide();
            String message1=e.toString();
//            LeadDetails.setMessage(mContext,message1);
//            CustomDialog.getInstance().hide();
//            CustomDialog.getInstance().showAlert(mContext, e.toString(), e.toString(), Util.getAppKeyValue(mContext, R.string.lblOk), Util.getAppKeyValue(mContext, R.string.str_dismiss), false);
            e.printStackTrace();
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        // Surface will be destroyed when we return, so stop the preview.
        // Because the CameraDevice object is not a shared resource, it's very
        // important to release it when the activity is paused.
        String message="FROM:surfaceDestroyed Destroyed";
//        LeadDetails.setMessage(mContext,message);
        camera.stopPreview();
        releaseCamera();
        camera = null;
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {

        /*Camera.Parameters parameters = camera.getParameters();
        parameters.setPreviewSize(w, h);
        camera.setParameters(parameters);
        camera.startPreview();*/
        String message="FROM:surfaceChanged method";
//        LeadDetails.setMessage(mContext,message);
        try {
            Camera.Parameters parameters = camera.getParameters();
            List<Camera.Size> previewSizes = parameters.getSupportedPreviewSizes();

            // You need to choose the most appropriate previewSize for your app
            //  Camera.Size previewSize = // .... select one of previewSizes here
            System.out.println("YUIO " + previewSizes.get(0).width + " " + previewSizes.get(0).height);
            parameters.setPreviewSize(previewSizes.get(0).width, previewSizes.get(0).height);
            camera.setParameters(parameters);
            camera.startPreview();
        }catch (Exception ex)
        {
            CustomDialog.getInstance().hide();
            String message1=ex.toString();
//            LeadDetails.setMessage(mContext,message1);

//            CustomDialog.getInstance().hide();
//            CustomDialog.getInstance().showAlert(mContext, ex.toString(), ex.toString(), Util.getAppKeyValue(mContext, R.string.lblOk), Util.getAppKeyValue(mContext, R.string.str_dismiss), false);
        }
    }
    private void releaseCamera() {
        if (camera != null) {
            camera.lock();
            camera.setPreviewCallback(null);
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }
    private int getFrontCameraId(){
        String message="FROM:getFrontCameraId";
//        LeadDetails.setMessage(mContext,message);
        int camId = 0;
        int numberOfCameras = Camera.getNumberOfCameras();
        Camera.CameraInfo ci = new Camera.CameraInfo();

        for(int i = 0;i < numberOfCameras;i++){
            Camera.getCameraInfo(i,ci);
            if(ci.facing == Camera.CameraInfo.CAMERA_FACING_FRONT){
                camId = i;
            }
        }

        return camId;
    }
}