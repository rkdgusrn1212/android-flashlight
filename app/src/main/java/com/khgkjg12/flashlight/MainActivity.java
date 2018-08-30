package com.khgkjg12.flashlight;

import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Switch flashSwitch = findViewById(R.id.flash_switch);

        if(checkCameraHardware(this)){
            Camera camera = null;
            try {
                camera = Camera.open(); // attempt to get a Camera instance
            }
            catch (Exception e){
                onNoCamera(this);
            }
            if(camera!=null){
                final Camera finalCamera = camera;
                flashSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if(isChecked){
                            finalCamera.getParameters().setFlashMode(Camera.Parameters.FLASH_MODE_ON);
                        }else{
                            finalCamera.getParameters().setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                        }
                    }
                });
            }else {
                onNoCamera(this);
            }
        }else{
            onNoCamera(this);
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

    private void onNoCamera(Context context){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(R.string.no_camera).setNeutralButton(R.string.confirm, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
    }
}
