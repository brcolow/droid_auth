package net.cryptodirect.authenticator;

import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.google.zxing.BarcodeFormat;

public class RegisterAccountActivity
        extends AppCompatActivity
        implements ScanQRCodeFragment.OnQRCodeScannedListener
{
    private static final String TAG = RegisterAccountActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle state)
    {
        super.onCreate(state);
        setContentView(R.layout.activity_register_account);
        if (findViewById(R.id.register_account_fragment_container) != null)
        {
            // However, if we're being restored from a previous state,
            // then we don't need to do anything and should return or else
            // we could end up with overlapping fragments.
            if (state != null)
            {
                return;
            }
            SelectRegisterMethodFragment selectMethodFragment = new SelectRegisterMethodFragment();
            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            fragmentTransaction.add(R.id.register_account_fragment_container,
                    selectMethodFragment, "select-method")
                    .commit();
        }
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

    /**
     * The "QR Code" button is in the SelectRegisterMethodFragment
     * @param view
     */
    public void handleScanQRCodeClicked(View view)
    {
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        ScanQRCodeFragment scanQRCodeFragment = new ScanQRCodeFragment();
        fragmentTransaction.replace(R.id.register_account_fragment_container,
                scanQRCodeFragment, "qr-code")
                .addToBackStack("select-to-qr")
                .commit();
    }

    /**
     * The "Manual Entry" button is in the SelectRegisterMethodFragment
     * @param view
     */
    public void handleManualEntryClicked(View view)
    {
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        ManualEntryFragment manualEntryFragment = new ManualEntryFragment();
        fragmentTransaction.replace(R.id.register_account_fragment_container,
                manualEntryFragment, "manual-entry")
                .addToBackStack("select-to-manual")
                .commit();
    }

    @Override
    public void onQRCodeScanned(BarcodeFormat barcodeFormat, String scannedCode)
    {
        if (!barcodeFormat.equals(BarcodeFormat.QR_CODE))
        {
            throw new IllegalArgumentException("decoded barcode was not in QR code format");
        }
        if (!scannedCode.contains("|"))
        {
            throw new IllegalArgumentException("decoded QR code was not formatted correctly");
        }

        Bundle bundle = new Bundle();
        bundle.putString("decoded_email", scannedCode.split("\\|")[0]);
        bundle.putString("decoded_key", scannedCode.split("\\|")[1]);
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        RegisterAccountDataFragment registerAccountDataFragment = new RegisterAccountDataFragment();
        registerAccountDataFragment.setArguments(bundle);
        fragmentTransaction.replace(R.id.register_account_fragment_container,
                registerAccountDataFragment, "register-account-data")
                .addToBackStack("select-to-manual")
                .commit();
    }

    /**
     * The "Correct" button is in RegisterAccountDataFragment
     * @param view
     */
    public void handleCorrectButtonClicked(View view)
    {
        /*
        if (more than one account)
        {
            setContentView(account_chooser_fragment) // should have set default button
        }
        else
        {
            // start MainActivity
        }
         */
    }

    /**
     * The "Incorrect" button is in RegisterAccountDataFragment
     * @param view
     */
    public void handleIncorrectButtonClicked(View view)
    {
        // information was incorrect, so go back to select method fragment
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.register_account_fragment_container,
                getFragmentManager().findFragmentByTag("select-method"))
                .addToBackStack("incorrect-to-select")
                .commit();
    }
}
