package com.khgkjg12.flashlight;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;

import java.io.IOException;
import java.security.Permission;
import java.security.Permissions;

public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback {

    private Camera mCamera;
    private Switch mFlashSwitch;
    private SurfaceHolder mSurfaceHolder;
    private static final int REQUEST_FOR_START_FLASH_SERVICE = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mFlashSwitch = findViewById(R.id.flash_switch);

        mFlashSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    turnOnFlash();
                }else{
                    turnOffFlash();
                }
            }
        });
        SurfaceView surfaceView = findViewById(R.id.surface_view);
        mSurfaceHolder = surfaceView.getHolder();
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        mSurfaceHolder.addCallback(this);
    }

    private boolean checkFlashPermission(){

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if (checkSelfPermission(Manifest.permission.CAMERA)!=PackageManager.PERMISSION_GRANTED){
                return false;
            }
        }
        return true;
    }

    private void requestPermissionAndStartFlashService(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)){
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
                alertDialog.setMessage(R.string.explain_camera_service_permission_for_flash_service);
                alertDialog.setNeutralButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            requestPermissions(new String[]{Manifest.permission.CAMERA},REQUEST_FOR_START_FLASH_SERVICE);
                        }
                    }
                }).create().show();
            }else{
                requestPermissions(new String[]{Manifest.permission.CAMERA},REQUEST_FOR_START_FLASH_SERVICE);
            }
        }
    }

    private void turnOnFlash() {
        if(checkCameraHardware(this)) {
            if (checkFlashPermission()) {
                mCamera = Camera.open();
                if (mCamera != null) {
                    if (checkFlash()) {
                        Camera.Parameters parameters = mCamera.getParameters();
                        parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                        mCamera.setParameters(parameters);
                        try {
                            mCamera.setPreviewDisplay(mSurfaceHolder);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        mCamera.startPreview();
                        //syncSwitchWithCamera();
                    } else {
                        onNoFlash(this);
                    }
                }else{
                    onNoFlash(this);
                }
            } else {
                requestPermissionAndStartFlashService();
            }
        }else{
            onNoFlash(this);
        }
    }

    private void turnOffFlash(){
        if(checkCameraHardware(this)) {
            if (checkFlashPermission()) {
                if (mCamera != null) {
                    if (checkFlash()) {
                        Camera.Parameters parameters = mCamera.getParameters();
                        parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                        mCamera.setParameters(parameters);
                        mCamera.stopPreview();
                        mCamera.release();
                        mCamera = null;
                    } else {
                        onNoFlash(this);
                    }
                }else{
                    onNoFlash(this);
                }
            } else {
                requestPermissionAndStartFlashService();
            }
        }else{
            onNoFlash(this);
        }
    }

    private boolean checkFlash(){
        if(mCamera!=null){
            String flashMode = mCamera.getParameters().getFlashMode();
            if(flashMode!=null){
                return true;
            }else{
                return false;
            }
        }else{
            return false;
        }
    }

    /** Check if this device has a camera */
    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }

    private void onNoFlash(Context context){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(R.string.no_flash).setNeutralButton(R.string.confirm, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseCamera();
    }

    private void releaseCamera(){
        if(mCamera != null){
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == REQUEST_FOR_START_FLASH_SERVICE){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                mFlashSwitch.toggle();
            }
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if(mCamera!=null){
            try {
                mCamera.setPreviewDisplay(holder);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }
}