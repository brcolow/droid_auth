package net.cryptodirect.authenticator;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;

import java.util.Collections;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

/**
 * Allows the user to scan an externally provided QR code to
 * register an account. That QR code was most likely generated
 * by Centurion, and it encodes the email concatenated with
 * the key (encoded in Base 64) separated by the '|' character.
 * <p/>
 * Example:
 * <p/>
 * tom@gmail.com|OEbnsWQidFxYp3TUcAqzREIuywzD7Gz3wFZhLz8qXEI=
 */
public class ScanQRCodeFragment extends Fragment implements ZXingScannerView.ResultHandler
{
    private boolean flash;
    private boolean autoFocus;
    private ZXingScannerView scannerView;
    private OnQRCodeScannedListener listener;
    private static final String FLASH_STATE = "FLASH_STATE";
    private static final String AUTO_FOCUS_STATE = "AUTO_FOCUS_STATE";

    public ScanQRCodeFragment()
    {
    }

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
        }
        else
        {
            flash = false;
            autoFocus = true;
        }
        scannerView.setFormats(Collections.singletonList(BarcodeFormat.QR_CODE));

        return scannerView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        super.onCreateOptionsMenu(menu, inflater);
        MenuItemCompat.setShowAsAction(menu.add(Menu.NONE, R.id.menu_flash, 0,
                flash ? R.string.flash_on : R.string.flash_off), MenuItem.SHOW_AS_ACTION_IF_ROOM);
        MenuItemCompat.setShowAsAction(menu.add(Menu.NONE, R.id.menu_auto_focus, 0,
                autoFocus ? R.string.auto_focus_on : R.string.auto_focus_off), MenuItem.SHOW_AS_ACTION_IF_ROOM);
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
        scannerView.startCamera();
        scannerView.setFlash(flash);
        scannerView.setAutoFocus(autoFocus);
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putBoolean(FLASH_STATE, flash);
        outState.putBoolean(AUTO_FOCUS_STATE, autoFocus);
    }

    @Override
    public void handleResult(Result rawResult)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        if (prefs.getBoolean("play_scan_sound", true))
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

    public interface OnQRCodeScannedListener
    {
        void onQRCodeScanned(BarcodeFormat barcodeFormat, String scannedCode);
    }
}
