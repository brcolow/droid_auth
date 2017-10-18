package net.cryptodirect.authenticator.camera;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import net.cryptodirect.authenticator.R;

public class CameraSelectorDialogFragment extends DialogFragment
{
    public interface CameraSelectorDialogListener
    {
        void onCameraSelected(int cameraId);
    }

    private int cameraId;
    private CameraSelectorDialogListener listener;

    public void onCreate(Bundle state)
    {
        super.onCreate(state);
        setRetainInstance(true);
    }

    public static CameraSelectorDialogFragment newInstance(CameraSelectorDialogListener listener, int cameraId)
    {
        CameraSelectorDialogFragment fragment = new CameraSelectorDialogFragment();
        fragment.cameraId = cameraId;
        fragment.listener = listener;
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        if (listener == null)
        {
            dismiss();
            return new Dialog(getContext());
        }

        CameraSupport camera = CameraSupport.getCamera(getActivity(), getContext());
        int checkedIndex = 0;

        for (int i = 0; i < camera.getNumberOfCameras(); i++)
        {
            if (i == cameraId)
            {
                checkedIndex = i;
            }
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Set the dialog title
        builder.setTitle(R.string.select_camera)
                // Specify the list array, the items to be selected by default (null for none),
                // and the listener through which to receive callbacks when items are selected
                .setSingleChoiceItems(camera.getCameras(), checkedIndex,
                        new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                cameraId = which;
                            }
                        })
                // Set the action buttons
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int id)
                    {
                        // User clicked OK, so save the mSelectedIndices results somewhere
                        // or return them to the component that opened the dialog
                        if (listener != null)
                        {
                            listener.onCameraSelected(cameraId);
                        }
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int id)
                    {
                    }
                });

        return builder.create();
    }
}