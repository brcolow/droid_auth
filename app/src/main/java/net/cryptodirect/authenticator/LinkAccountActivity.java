package net.cryptodirect.authenticator;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
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
        if (getSupportFragmentManager().getBackStackEntryCount() > 0)
        {
            getSupportFragmentManager().popBackStack();
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
        String reasonInvalid = null;

        if (!barcodeFormat.equals(BarcodeFormat.QR_CODE))
        {
            reasonInvalid = "The scanned barcode must be a QR code. Instead, it was in format: " +
                    barcodeFormat.name();
        }

        String[] scannedCodeSplit = scannedCode.split("\\|");
        if (!scannedCode.contains("|") || scannedCodeSplit.length != 2)
        {
            reasonInvalid = "The scanned QR code was not formatted correctly - make sure you are " +
                    "scanning a Cryptodash provided QR code.";
        }

        if (reasonInvalid != null)
        {
            AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this, R.style.Theme_Dialog);
            alertBuilder.setMessage(reasonInvalid);
            alertBuilder.setCancelable(false);
            alertBuilder.setPositiveButton(getString(R.string.rescan),
                    new DialogInterface.OnClickListener()
                    {
                        public void onClick(DialogInterface dialog, int id)
                        {
                            getSupportFragmentManager().popBackStack();
                            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                            ScanQRCodeFragment scanQRCodeFragment = new ScanQRCodeFragment();
                            fragmentTransaction.add(R.id.register_account_fragment_container,
                                    scanQRCodeFragment, "qr-code")
                                    .addToBackStack("qr-code")
                                    .commit();
                        }
                    });
            alertBuilder.setNegativeButton(getString(R.string.enter_manually),
                    new DialogInterface.OnClickListener()
                    {
                        public void onClick(DialogInterface dialog, int id)
                        {
                            getSupportFragmentManager().popBackStack();
                            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                            ManualEntryFragment manualEntryFragment = new ManualEntryFragment();
                            fragmentTransaction.add(R.id.register_account_fragment_container,
                                    manualEntryFragment, "manual-entry")
                                    .addToBackStack("manual-entry")
                                    .commit();
                        }
                    });
            AlertDialog alertDialog = alertBuilder.create();
            alertDialog.show();
        }
        else
        {
            Bundle bundle = new Bundle();
            bundle.putString("new_email", scannedCode.split("\\|")[0]);
            bundle.putString("new_key", scannedCode.split("\\|")[1]);
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            LinkAccountDataFragment linkAccountDataFragment = new LinkAccountDataFragment();
            linkAccountDataFragment.setArguments(bundle);
            fragmentTransaction.add(R.id.register_account_fragment_container,
                    linkAccountDataFragment, "register-account-data")
                    .addToBackStack("register-account-data")
                    .commit();
        }
    }

    /**
     * The "Correct" button is in LinkAccountDataFragment
     * @param view
     */
    public void handleCorrectButtonClicked(View view)
    {
        EditText emailTextField = (EditText) findViewById(R.id.email_field);
        EditText keyTextField = (EditText) findViewById(R.id.key_text_field);
        if (emailTextField == null || keyTextField == null)
        {
            throw new IllegalStateException("email EditText or key EditText controls were null: ["
                    + emailTextField + ", " + keyTextField + "]");
        }
        AccountManager.getInstance().registerAccount(new Account(emailTextField.getText().toString(),
                keyTextField.getText().toString()), true);
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
        getSupportFragmentManager().popBackStackImmediate();
        /*
        getSupportFragmentManager().popBackStack("select-method", FragmentManager.POP_BACK_STACK_INCLUSIVE);
        SelectRegisterMethodFragment selectMethodFragment = new SelectRegisterMethodFragment();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.register_account_fragment_container,
                selectMethodFragment, "select-method")
                .addToBackStack("select-method")
                .commit();
                */
    }

    /**
     * The "Okay" button is in ManualEntryFragment
     * @param view
     */
    public void handleOkayButtonClicked(View view)
    {
        EditText emailTextField = (EditText) findViewById(R.id.email_field);
        EditText keyTextField = (EditText) findViewById(R.id.key_text_field);
        if (emailTextField == null || keyTextField == null)
        {
            throw new IllegalStateException("email EditText or key EditText controls were null: ["
                    + emailTextField + ", " + keyTextField + "]");
        }

        Bundle bundle = new Bundle();
        bundle.putString("new_email", emailTextField.getText().toString());
        bundle.putString("new_key", keyTextField.getText().toString());
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        LinkAccountDataFragment linkAccountDataFragment = new LinkAccountDataFragment();
        linkAccountDataFragment.setArguments(bundle);
        fragmentTransaction.add(R.id.register_account_fragment_container,
                linkAccountDataFragment, "register-account-data")
                .addToBackStack("register-account-data")
                .commit();
    }

}
