package net.cryptodirect.authenticator.camera;

import android.app.Activity;
import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;

import org.acra.ACRA;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class NewCamera implements CameraSupport
{
    private final Activity activity;
    private final CameraManager manager;
    private final Context context;
    private CameraDevice camera;

    public NewCamera(final Activity activity, final Context context)
    {
        this.activity = activity;
        this.context = context;
        this.manager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
    }

    @Override
    public CameraSupport open(int cameraId)
    {
        try
        {
            String[] cameraIds = manager.getCameraIdList();
            manager.openCamera(cameraIds[cameraId], new CameraDevice.StateCallback()
            {
                @Override
                public void onOpened(@NonNull CameraDevice camera)
                {
                    NewCamera.this.camera = camera;
                }

                @Override
                public void onDisconnected(@NonNull CameraDevice camera)
                {
                    NewCamera.this.camera = camera;
                    // TODO handle
                }

                @Override
                public void onError(@NonNull CameraDevice camera, int error)
                {
                    NewCamera.this.camera = camera;
                    // TODO handle
                }
            }, null);
        }
        catch (Exception e)
        {
            ACRA.getErrorReporter().handleException(e);
        }
        return this;
    }

    @Override
    public int getNumberOfCameras()
    {
        try
        {
            return manager.getCameraIdList().length;
        }
        catch (CameraAccessException e)
        {
            ACRA.getErrorReporter().handleException(e);
            return 0;
        }
    }

    @Override
    public String[] getCameras()
    {
        String[] cameras = new String[getNumberOfCameras()];

        try
        {
            int cameraIndex = 0;
            for (String cameraId : manager.getCameraIdList())
            {
                CameraCharacteristics cameraCharacteristics = manager.getCameraCharacteristics(cameraId);
                Integer facing = cameraCharacteristics.get(CameraCharacteristics.LENS_FACING);
                if (facing == null)
                {
                    ACRA.getErrorReporter().handleException(new RuntimeException("camera with id: " + cameraId + " returned null for CameraCharacteristics.LENS_FACING"));
                    return new String[]{};
                }

                switch (facing)
                {
                    case CameraMetadata.LENS_FACING_BACK:
                        cameras[cameraIndex] = "Front Camera";
                    case CameraMetadata.LENS_FACING_FRONT:
                        cameras[cameraIndex] = "Back Camera";
                    case CameraMetadata.LENS_FACING_EXTERNAL:
                        cameras[cameraIndex] = "External Camera ID: " + cameraIndex;
                }

                cameraIndex++;
            }
            return cameras;
        }
        catch (CameraAccessException e)
        {
            ACRA.getErrorReporter().handleException(e);
            return new String[]{};
        }
    }
}
