package net.cryptodirect.authenticator;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;

import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;

import com.google.zxing.BarcodeFormat;

public class LinkAccountActivity
        extends AppCompatActivity
        implements ScanQRCodeFragment.OnQRCodeScannedListener,
        FragmentManager.OnBackStackChangedListener
{
    private volatile boolean currentFragmentIsScanQRCode = false;
    private static final String TAG = LinkAccountActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle state)
    {
        super.onCreate(state);
        setContentView(R.layout.activity_register_account);
        getSupportFragmentManager().addOnBackStackChangedListener(this);
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
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.add(R.id.register_account_fragment_container,
                    selectMethodFragment, "select-method")
                    .addToBackStack("select-method")
                    .commit();
        }
    }
    @Override
    public void onBackStackChanged()
    {
        // this is probably wrong but it works - most likely for the wrong reasons
        Fragment f = getSupportFragmentManager().findFragmentById(R.id.register_account_fragment_container);
        currentFragmentIsScanQRCode = f instanceof ScanQRCodeFragment;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu)
    {
        if (!currentFragmentIsScanQRCode)
        {
            menu.clear();
        }
        super.onPrepareOptionsMenu(menu);
        return true;
    }

    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
        if (getSupportFragmentManager().getBackStackEntryCount() >= 1)
        {
            getFragmentManager().popBackStack();
        }
        else
        {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }
    }

    /**
     * The "QR Code" button is in the SelectRegisterMethodFragment
     * @param view
     */
    public void handleScanQRCodeClicked(View view)
    {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        ScanQRCodeFragment scanQRCodeFragment = new ScanQRCodeFragment();
        fragmentTransaction.add(R.id.register_account_fragment_container,
                scanQRCodeFragment, "qr-code")
                .addToBackStack("qr-code")
                .commit();
    }

    /**
     * The "Manual Entry" button is in the SelectRegisterMethodFragment
     * @param view
     */
    public void handleManualEntryClicked(View view)
    {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        ManualEntryFragment manualEntryFragment = new ManualEntryFragment();
        fragmentTransaction.add(R.id.register_account_fragment_container,
                manualEntryFragment, "manual-entry")
                .addToBackStack("manual-entry")
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
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        LinkAccountDataFragment linkAccountDataFragment = new LinkAccountDataFragment();
        linkAccountDataFragment.setArguments(bundle);
        fragmentTransaction.add(R.id.register_account_fragment_container,
                linkAccountDataFragment, "register-account-data")
                .addToBackStack("register-account-data")
                .commit();
    }

    /**
     * The "Correct" button is in LinkAccountDataFragment
     * @param view
     */
    public void handleCorrectButtonClicked(View view)
    {
        EditText emailField = (EditText) findViewById(R.id.email_field);
        EditText keyTextField = (EditText) findViewById(R.id.key_text_field);
        if (emailField == null || keyTextField == null)
        {
            throw new IllegalStateException("email EditText or key EditText controls were null: ["
                    + emailField + ", " + keyTextField + "]");
        }
        AccountManager.getInstance().registerAccount(new Account(emailField.getText().toString(), keyTextField.getText().toString()), true);
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    /**
     * The "Incorrect" button is in LinkAccountDataFragment
     * @param view
     */
    public void handleIncorrectButtonClicked(View view)
    {
        // information was incorrect, so go back to select method fragment
        getFragmentManager().popBackStack("select-method", FragmentManager.POP_BACK_STACK_INCLUSIVE);
        SelectRegisterMethodFragment selectMethodFragment = new SelectRegisterMethodFragment();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.register_account_fragment_container,
                selectMethodFragment, "select-method")
                .addToBackStack("select-method")
                .commit();
    }
}
