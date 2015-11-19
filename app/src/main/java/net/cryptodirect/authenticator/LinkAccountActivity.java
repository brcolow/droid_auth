package net.cryptodirect.authenticator;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;

import net.cryptodirect.authenticator.crypto.CodeParams;
import net.cryptodirect.authenticator.crypto.CodeType;

import org.acra.ACRA;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class LinkAccountActivity
        extends AppCompatActivity
        implements ScanQRCodeFragment.OnQRCodeScannedListener,
        FragmentManager.OnBackStackChangedListener
{
    private Account scannedAccount = null;
    private String enteredEmail = null;
    private String enteredKey = null;
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
        showQRCodeFragment();
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

        try
        {
            scannedAccount = Account.parse(scannedCode);
        }
        catch (URISyntaxException e)
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
                            showQRCodeFragment();
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
            bundle.putString("new_email", scannedAccount.getEmail());
            bundle.putString("new_issuer", scannedAccount.getIssuer());
            bundle.putString("new_key", Base64.encodeToString(scannedAccount.getSecretKey(), Base64.NO_WRAP));
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            LinkAccountDataFragment linkAccountDataFragment = new LinkAccountDataFragment();
            linkAccountDataFragment.setArguments(bundle);
            fragmentTransaction.add(R.id.register_account_fragment_container,
                    linkAccountDataFragment, "register-account-data")
                    .addToBackStack("register-account-data")
                    .commit();
        }
    }

    private void showQRCodeFragment()
    {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        Bundle bundle = new Bundle();
        bundle.putBoolean("play_scan_sound", sharedPreferences.getBoolean("play_scan_sound", true));
        ScanQRCodeFragment scanQRCodeFragment = new ScanQRCodeFragment();
        scanQRCodeFragment.setArguments(bundle);
        fragmentTransaction.add(R.id.register_account_fragment_container,
                scanQRCodeFragment, "qr-code")
                .addToBackStack("qr-code")
                .commit();
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
                if (response.has("local_error"))
                {
                    final String error = response.getString("local_error");
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
                    Toast toast = Toast.makeText(getApplicationContext(), "You have successfully linked your account!", Toast.LENGTH_SHORT);
                    toast.show();
                    // TODO instead of showing the above toast, maybe we could animate in a "Successfully linked"
                    // text/image and then fade to time wheel widget
                    if (scannedAccount != null)
                    {
                        AccountManager.getInstance().registerAccount(scannedAccount, true, setAsDefaultAccountCheckBox.isChecked());
                    }
                    if (enteredEmail != null && enteredKey != null)
                    {
                        Account newAccount = new Account(enteredEmail, "Cryptodash",
                                Base64.decode(enteredKey, Base64.DEFAULT),
                                new CodeParams.Builder(CodeType.TOTP).build());
                        AccountManager.getInstance().registerAccount(newAccount, true, setAsDefaultAccountCheckBox.isChecked());
                    }
                    else
                    {
                        throw new IllegalStateException("At linked account but scannedAccount, " +
                                "enteredEmail, and enteredKey were all null!");
                    }

                    scannedAccount = null;
                    enteredEmail = null;
                    enteredKey = null;
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
        // resume camera
        if (getSupportFragmentManager().getBackStackEntryCount() >= 2)
        {
            FragmentManager.BackStackEntry backEntry = getSupportFragmentManager().
                    getBackStackEntryAt(getSupportFragmentManager().getBackStackEntryCount() - 2);
            if (backEntry.getName().equals("qr-code"))
            {
                Fragment fragment = getSupportFragmentManager().findFragmentByTag("qr-code");
                fragment.onResume();
            }
        }
        getSupportFragmentManager().popBackStack();
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
        enteredEmail = emailTextField.getText().toString();
        enteredKey = keyTextField.getText().toString();
        // TODO need to implement issuer for manual entry for non-Cryptodash accounts
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
                try
                {
                    result = new JSONObject("{\"local_error\": \"" + e.getMessage() + "\"}");
                }
                catch (JSONException e2)
                {
                    throw new RuntimeException(e2);
                }
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
