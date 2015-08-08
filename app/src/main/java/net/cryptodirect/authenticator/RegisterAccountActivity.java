package net.cryptodirect.authenticator;

import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

public class RegisterAccountActivity extends AppCompatActivity
{
    private SelectRegisterMethodFragment selectRegisterMethodFragment;
    private static final int CONTENT_VIEW_ID = 10101010;
    private static final String TAG = RegisterAccountActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle state)
    {
        super.onCreate(state);
        RelativeLayout relativeLayout = new RelativeLayout(this);
        relativeLayout.setId(CONTENT_VIEW_ID);
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        selectRegisterMethodFragment = new SelectRegisterMethodFragment();
        fragmentTransaction.add(CONTENT_VIEW_ID, selectRegisterMethodFragment, "select-method").commit();
        setContentView(relativeLayout, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        addClickListeners();
    }

    private void addClickListeners()
    {
        selectRegisterMethodFragment.getScanRCodeButton().setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                Log.i(TAG, "View: " + v.toString());
                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                ScanQRCodeFragment scanQRCodeFragment = new ScanQRCodeFragment();
                fragmentTransaction.replace(CONTENT_VIEW_ID, scanQRCodeFragment, "qr-code").addToBackStack("select-to-qr").commit();
            }
        });

        selectRegisterMethodFragment.getManualEntryButton().setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                Log.i(TAG, "View: " + v.toString());
                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                ManualEntryFragment manualEntryFragment = new ManualEntryFragment();
                fragmentTransaction.replace(CONTENT_VIEW_ID , manualEntryFragment, "manual-entry").addToBackStack("select-to-manual").commit();
            }
        });
    }

    @Override
    public void onBackPressed()
    {
        if (getFragmentManager().getBackStackEntryCount() > 0)
        {
            getFragmentManager().popBackStack();
        }
        else
        {
            super.onBackPressed();
        }
    }
}
