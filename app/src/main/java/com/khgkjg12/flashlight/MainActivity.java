package com.khgkjg12.flashlight;

import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;

import java.security.Permissions;

public class MainActivity extends AppCompatActivity {

    private Camera mCamera;
    private Switch mFlashSwitch;
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
    }

    private boolean checkFlashPermission(){

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if (checkSelfPermission(CAMERA_SERVICE)!=PackageManager.PERMISSION_GRANTED){
                return false;
            }
        }
        return true;
    }

    private void requestPermissionAndStartFlashService(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(shouldShowRequestPermissionRationale(CAMERA_SERVICE)){
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
                alertDialog.setMessage(R.string.explain_camera_service_permission_for_flash_service);
                alertDialog.setNeutralButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            requestPermissions(new String[]{CAMERA_SERVICE},REQUEST_FOR_START_FLASH_SERVICE);
                        }
                    }
                }).create().show();
            }else{
                requestPermissions(new String[]{CAMERA_SERVICE},REQUEST_FOR_START_FLASH_SERVICE);
            }
        }
    }

    private void turnOnFlash() {
        if(checkFlashPermission()){
            if (checkFlash()) {
                mCamera.getParameters().setFlashMode(Camera.Parameters.FLASH_MODE_ON);
                syncSwitchWithCamera();
            } else {
                onNoFlash(this);
            }
        }else{
            requestPermissionAndStartFlashService();
        }
    }

    @Override
    protected void onResume() {
        if (checkCameraHardware(this)) {
            try {
                mCamera = Camera.open(); // attempt to get a Camera instance
                if(mCamera!=null){
                    if(checkFlash()){
                        syncSwitchWithCamera();
                    }else{
                        onNoFlash(this);
                    }
                }else{
                    onNoFlash(this);
                }
            } catch (Exception e) {
                onNoFlash(this);
            }
        } else {
            onNoFlash(this);
        }
        super.onResume();
    }

    private void turnOffFlash(){
        if(checkFlashPermission()) {
            if (checkFlash()) {
                mCamera.getParameters().setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                syncSwitchWithCamera();
            } else {
                onNoFlash(this);
            }
        }else{
            requestPermissionAndStartFlashService();
        }
    }

    private boolean checkFlash(){
        if(mCamera!=null){
            String flashMode = mCamera.getParameters().getFlashMode();
            if(flashMode!=null){
                mCamera.getParameters().setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                return true;
            }else{
                return false;
            }
        }else{
            return false;
        }
    }

    private void syncSwitchWithCamera(){
        if(checkFlash()){
            String flashMode = mCamera.getParameters().getFlashMode();
            if(flashMode.equals(Camera.Parameters.FLASH_MODE_ON)){
                if(!mFlashSwitch.isChecked()) {
                    mFlashSwitch.setChecked(true);
                }
            }else if(flashMode.equals(Camera.Parameters.FLASH_MODE_OFF)){
                if(mFlashSwitch.isChecked()) {
                    mFlashSwitch.setChecked(false);
                }
            }else{
                mCamera.getParameters().setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                if(mFlashSwitch.isChecked()) {
                    mFlashSwitch.setChecked(false);
                }
            }
        }else{
            onNoFlash(this);
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
}