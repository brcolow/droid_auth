package net.cryptodirect.authenticator;

import android.app.Activity;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;

import java.util.Collections;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

/**
 * A simple {@link Fragment} subclass.
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
        // Required empty public constructor
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
        MenuItem menuItem;

        if (flash)
        {
            menuItem = menu.add(Menu.NONE, R.id.menu_flash, 0, R.string.flash_on);
        }
        else
        {
            menuItem = menu.add(Menu.NONE, R.id.menu_flash, 0, R.string.flash_off);
        }
        MenuItemCompat.setShowAsAction(menuItem, MenuItem.SHOW_AS_ACTION_ALWAYS);


        if (autoFocus)
        {
            menuItem = menu.add(Menu.NONE, R.id.menu_auto_focus, 0, R.string.auto_focus_on);
        }
        else
        {
            menuItem = menu.add(Menu.NONE, R.id.menu_auto_focus, 0, R.string.auto_focus_off);
        }
        MenuItemCompat.setShowAsAction(menuItem, MenuItem.SHOW_AS_ACTION_ALWAYS);
    }

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        try
        {
            listener = (OnQRCodeScannedListener) activity;
        } catch (ClassCastException e)
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
        Toast.makeText(getActivity(), "Contents = " + rawResult.getText() +
                ", Format = " + rawResult.getBarcodeFormat().toString(), Toast.LENGTH_SHORT).show();
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