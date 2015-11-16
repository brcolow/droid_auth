package net.cryptodirect.authenticator;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;

import org.acra.ACRA;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.register_account_fragment_container);
        currentFragmentIsScanQRCode = fragment instanceof ScanQRCodeFragment;
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
        // FIXME this handles invisible buttons from the previous fragment
        if (getSupportFragmentManager().getBackStackEntryCount() > 1)
        {
            return;
        }
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
        if (getSupportFragmentManager().getBackStackEntryCount() > 1)
        {
            return;
        }
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
        TextView emailTextField = (TextView) findViewById(R.id.email_edit_text);
        TextView keyTextField = (TextView) findViewById(R.id.key_edit_text);
        CheckBox setAsDefaultAccountCheckBox = (CheckBox) findViewById(R.id.set_as_default_account_box);

        if (keyTextField.getError() != null)
        {
            Toast toast = Toast.makeText(getApplicationContext(), keyTextField.getError(), Toast.LENGTH_SHORT);
            toast.show();
            return;
        }

        ProgressDialog progressDialog = new ProgressDialog(LinkAccountActivity.this);
        progressDialog.setMessage("Verifying credentials...");
        progressDialog.show();

        NotifyAccountLinkedTask notifyAccountLinkedTask = new NotifyAccountLinkedTask(progressDialog, emailTextField.getText(), keyTextField.getText());
        JSONObject response = null;
        try
        {
            response = notifyAccountLinkedTask.execute().get(5000, TimeUnit.MILLISECONDS);
        }
        catch (InterruptedException | ExecutionException | TimeoutException e)
        {
            // shouldn't happen
            ACRA.getErrorReporter().handleException(e);
        }

        if (response != null)
        {
            try
            {
                if (response.has("error"))
                {
                    final String error = response.getString("error");
                    Toast toast = Toast.makeText(getApplicationContext(), "Looks like we are having some server trouble. This incident has been reported. Sorry about that!", Toast.LENGTH_LONG);
                    toast.show();
                    ACRA.getErrorReporter().handleSilentException(new IOException(error));
                    return;
                }

                int responseCode = response.getInt("httpResponseCode");
                if (responseCode != 200)
                {
                    AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this, R.style.Theme_Dialog);
                    alertBuilder.setMessage(R.string.centurion_invalid_args);
                    alertBuilder.setCancelable(false);
                    alertBuilder.setPositiveButton(getString(R.string.okay),
                            new DialogInterface.OnClickListener()
                            {
                                public void onClick(DialogInterface dialog, int id)
                                {
                                    // TODO maybe we should allow the user to go to
                                    // manual entry with the current text already entered
                                    dialog.dismiss();
                                }
                            });
                    AlertDialog alertDialog = alertBuilder.create();
                    alertDialog.show();
                }
                else
                {
                    // valid credentials
                    AccountManager.getInstance().registerAccount(new Account(emailTextField.getText().toString(),
                            keyTextField.getText().toString()), true, setAsDefaultAccountCheckBox.isChecked());
                    Intent intent = new Intent(this, MainActivity.class);
                    startActivity(intent);
                }
            }
            catch (JSONException e)
            {
                ACRA.getErrorReporter().handleException(e);
            }
        }

    }

    /**
     * The "Incorrect" button is in LinkAccountDataFragment
     * @param view
     */
    public void handleIncorrectButtonClicked(View view)
    {
        getSupportFragmentManager().popBackStackImmediate();
    }

    /**
     * The "Okay" button is in ManualEntryFragment
     * @param view
     */
    public void handleOkayButtonClicked(View view)
    {
        EditText emailTextField = (EditText) findViewById(R.id.email_edit_text);
        EditText keyTextField = (EditText) findViewById(R.id.key_edit_text);

        String errorMessage = isEnteredKeyValid(keyTextField);

        if (errorMessage != null)
        {
            // key is invalid
            Drawable errorDrawable = ContextCompat.getDrawable(getBaseContext(), R.drawable.ic_close_white_24dp);
            errorDrawable.setBounds(0, 0, errorDrawable.getIntrinsicWidth(), errorDrawable.getIntrinsicHeight());
            errorDrawable.setColorFilter(Utils.MaterialDesignColors.MD_RED_600.getColor(), PorterDuff.Mode.SRC_IN);
            keyTextField.setError(errorMessage, errorDrawable);
            keyTextField.forceLayout();
            return;
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

    private class NotifyAccountLinkedTask extends AsyncTask<String, Void, JSONObject>
    {
        private final CharSequence email;
        private final CharSequence key;
        private final ProgressDialog progressDialog;

        public NotifyAccountLinkedTask(ProgressDialog progressDialog, CharSequence email, CharSequence key)
        {
            this.progressDialog = progressDialog;
            this.email = email;
            this.key = key;
        }

        @Override
        protected void onPreExecute()
        {
            progressDialog.show();
        }

        @Override
        protected void onPostExecute(JSONObject result)
        {
            if (progressDialog.isShowing())
            {
                progressDialog.dismiss();
            }
        }

        @Override
        protected JSONObject doInBackground(String... args)
        {
            JSONObject result;
            try
            {
                String payload = "{\"email\": \"" + email + "\", \"totpKeyBase64\": \"" + key + "\"}";
                result = Centurion.getInstance().post("twofactor/notify-linked", payload);
            }
            catch (IOException | JSONException e)
            {
                result = new JSONObject();
            }

            return result;
        }
    }

    private String isEnteredKeyValid(EditText keyTextField)
    {
        // do full validation of key
        String errorMessage = null;
        if (keyTextField.getError() != null)
        {
            return keyTextField.getError().toString();
        }

        if (keyTextField.getText().length() != 44)
        {
            if (keyTextField.getText().length() == 0)
            {
                errorMessage = getResources().getString(R.string.empty_key);
            }
            else
            {
                errorMessage = getResources().getString(R.string.key_too_short);
            }
        }
        else if (keyTextField.getText().toString().charAt(keyTextField.getText().length() - 1) != '=')
        {
            errorMessage = getResources().getString(R.string.key_equals_missing);
        }
        return errorMessage;
    }
}
