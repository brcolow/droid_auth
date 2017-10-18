package net.cryptodirect.authenticator.camera;

import android.hardware.Camera;

@SuppressWarnings("deprecation")
public class OldCamera implements CameraSupport
{
    private Camera camera;

    @Override
    public CameraSupport open(final int cameraId)
    {
        this.camera = Camera.open(cameraId);
        return this;
    }

    @Override
    public int getNumberOfCameras()
    {
        return Camera.getNumberOfCameras();
    }

    @Override
    public String[] getCameras()
    {
        String[] cameras = new String[getNumberOfCameras()];
        for (int i = 0; i < getNumberOfCameras(); i++)
        {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT)
            {
                cameras[i] = "Front Camera";
            }
            else if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK)
            {
                cameras[i] = "Back Camera";
            }
            else
            {
                cameras[i] = "External Camera ID: " + i;
            }
        }
        return cameras;
    }

}
