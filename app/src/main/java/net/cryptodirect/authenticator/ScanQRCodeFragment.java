package net.cryptodirect.authenticator;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;

import net.cryptodirect.authenticator.camera.CameraSelectorDialogFragment;

import java.util.Collections;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

/**
 * Allows the user to scan a QR code that encodes an otpauth
 * URL which contains the necessary information for providing
 * TOTP codes.
 */
public class ScanQRCodeFragment extends Fragment implements ZXingScannerView.ResultHandler,
        SharedPreferences.OnSharedPreferenceChangeListener,
        CameraSelectorDialogFragment.CameraSelectorDialogListener
{
    private boolean flash;
    private boolean autoFocus;
    private boolean playScanSound;
    private int cameraId = -1;

    private ZXingScannerView scannerView;
    private OnQRCodeScannedListener listener;

    private static final String FLASH_STATE = "FLASH_STATE";
    private static final String AUTO_FOCUS_STATE = "AUTO_FOCUS_STATE";
    private static final String CAMERA_ID = "CAMERA_ID";

    @Override
    public void onCreate(Bundle state)
    {
        super.onCreate(state);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle state)
    {
        scannerView = new ZXingScannerView(getActivity());
        if (state != null)
        {
            flash = state.getBoolean(FLASH_STATE, false);
            autoFocus = state.getBoolean(AUTO_FOCUS_STATE, true);
            playScanSound = state.getBoolean("play_scan_sound");
            cameraId = state.getInt(CAMERA_ID, -1);
        }
        else
        {
            flash = false;
            autoFocus = true;
            playScanSound = true;
            cameraId = -1;
        }

        scannerView.setFormats(Collections.singletonList(BarcodeFormat.QR_CODE));
        return scannerView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        super.onCreateOptionsMenu(menu, inflater);
        menu.add(Menu.NONE, R.id.menu_flash, 0, flash ? R.string.flash_on : R.string.flash_off)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        menu.add(Menu.NONE, R.id.menu_auto_focus, 0,
                autoFocus ? R.string.auto_focus_on : R.string.auto_focus_off)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        menu.add(Menu.NONE, R.id.menu_camera_selector, 0, R.string.select_camera)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.menu_flash:
                flash = !flash;
                if (flash)
                {
                    item.setTitle(R.string.flash_on);
                }
                else
                {
                    item.setTitle(R.string.flash_off);
                }
                scannerView.setFlash(flash);
                return true;
            case R.id.menu_auto_focus:
                autoFocus = !autoFocus;
                if (autoFocus)
                {
                    item.setTitle(R.string.auto_focus_on);
                }
                else
                {
                    item.setTitle(R.string.auto_focus_off);
                }
                scannerView.setAutoFocus(autoFocus);
                return true;
            case R.id.menu_camera_selector:
                scannerView.stopCamera();
                DialogFragment cFragment = CameraSelectorDialogFragment.newInstance(this, cameraId);
                cFragment.show(getActivity().getSupportFragmentManager(), "camera_selector");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onAttach(Context activity)
    {
        super.onAttach(activity);
        try
        {
            listener = (OnQRCodeScannedListener) activity;
        }
        catch (ClassCastException e)
        {
            throw new ClassCastException(activity.toString() + " must implement OnQRCodeScannedListener");
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();
        scannerView.setResultHandler(this);
        scannerView.startCamera(cameraId);
        scannerView.setFlash(flash);
        scannerView.setAutoFocus(autoFocus);
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putBoolean(FLASH_STATE, flash);
        outState.putBoolean(AUTO_FOCUS_STATE, autoFocus);
        outState.putBoolean("play_scan_sound", playScanSound);
        outState.putInt(CAMERA_ID, cameraId);
    }

    @Override
    public void handleResult(Result rawResult)
    {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        if (sharedPreferences.getBoolean("play_scan_sound", true))
        {
            SoundPoolManager.getInstance().playSound("SCAN", false);
        }
        listener.onQRCodeScanned(rawResult.getBarcodeFormat(), rawResult.getText());
    }

    @Override
    public void onPause()
    {
        super.onPause();
        scannerView.stopCamera();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
    {
        if (key.equals("play_scan_sound"))
        {
            playScanSound = sharedPreferences.getBoolean("play_scan_sound", true);
        }
    }

    @Override
    public void onCameraSelected(int cameraId)
    {
        this.cameraId = cameraId;
        scannerView.startCamera(cameraId);
        scannerView.setFlash(flash);
        scannerView.setAutoFocus(autoFocus);
    }

    public interface OnQRCodeScannedListener
    {
        void onQRCodeScanned(BarcodeFormat barcodeFormat, String scannedCode);
    }
}
