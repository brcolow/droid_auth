package net.cryptodirect.authenticator.camera;

import android.app.Activity;
import android.content.Context;
import android.os.Build;

public interface CameraSupport
{
    CameraSupport open(int cameraId);
    int getNumberOfCameras();
    String[] getCameras();

    static CameraSupport getCamera(Activity activity, Context context)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            return new NewCamera(activity, context);
        }
        else
        {
            return new OldCamera();
        }
    }

}