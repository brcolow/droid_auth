package net.cryptodirect.authenticator;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.AndroidRuntimeException;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;

import net.cryptodirect.authenticator.crypto.Algorithm;
import net.cryptodirect.authenticator.crypto.Base;
import net.cryptodirect.authenticator.crypto.Base64;
import net.cryptodirect.authenticator.crypto.CodeParams;
import net.cryptodirect.authenticator.crypto.CodeType;

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
    private static final int REQUEST_CAMERA = 0;

    private Centurion centurion;
    private Account scannedAccount = null;
    private String enteredEmail = null;
    private String enteredKey = null;
    private volatile boolean currentFragmentIsScanQRCode = false;

    @Override
    protected void onCreate(Bundle state)
    {
        super.onCreate(state);
        Intent intent = getIntent();
        if (!intent.hasExtra("centurion"))
        {
            throw new IllegalArgumentException("intent: " + intent + " did not contain extra " +
                    "\"centurion\"");
        }
        centurion = (Centurion) intent.getSerializableExtra("centurion");
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
            fragmentTransaction.replace(R.id.register_account_fragment_container,
                    selectMethodFragment, "select-method")
                    .addToBackStack("select-method")
                    .commit();
        }
    }
    @Override
    public void onBackStackChanged()
    {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0)
        {
            String fragmentTag = getSupportFragmentManager().getBackStackEntryAt(getSupportFragmentManager().getBackStackEntryCount() - 1).getName();
            Log.e(LinkAccountActivity.class.getName(), "Fragment back stack changed to: " + fragmentTag);
        }
        Log.e(LinkAccountActivity.class.getName(), "Fragment stack count: " + getSupportFragmentManager().getBackStackEntryCount());
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
        if (getSupportFragmentManager().getBackStackEntryCount() == 0)
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
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, new String[]{ Manifest.permission.CAMERA }, REQUEST_CAMERA);
        }

        showQRCodeFragment();
    }

    /**
     * The "Manual Entry" button is in the SelectRegisterMethodFragment
     * @param view
     */
    public void handleManualEntryClicked(View view)
    {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        ManualEntryFragment manualEntryFragment = new ManualEntryFragment();
        fragmentTransaction.replace(R.id.register_account_fragment_container,
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
        catch (InvalidOptAuthUriException e)
        {
            reasonInvalid = "The scanned QR code was not formatted correctly - make sure you are " +
                    "scanning a Cryptodash provided QR code.";
        }

        if (reasonInvalid != null)
        {
            AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this, R.style.Theme_Dialog);
            alertBuilder.setMessage(reasonInvalid);
            alertBuilder.setCancelable(false);
            alertBuilder.setPositiveButton(getString(R.string.rescan), new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int id)
                {
                    LinkAccountActivity.this.getSupportFragmentManager().popBackStack();
                    LinkAccountActivity.this.showQRCodeFragment();
                }
            });
            alertBuilder.setNegativeButton(getString(R.string.enter_manually), new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int id)
                {
                    LinkAccountActivity.this.getSupportFragmentManager().popBackStack();
                    FragmentTransaction fragmentTransaction = LinkAccountActivity.this
                            .getSupportFragmentManager().beginTransaction();
                    ManualEntryFragment manualEntryFragment = new ManualEntryFragment();
                    fragmentTransaction.replace(R.id.register_account_fragment_container,
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
            bundle.putString("method", LinkMethod.QRCODE.name());
            bundle.putString("new_label", scannedAccount.getLabel());
            bundle.putInt("new_issuer", scannedAccount.getIssuer().getId());
            bundle.putByteArray("new_key", scannedAccount.getSecretKey());
            bundle.putInt("new_base", scannedAccount.getCodeParams().getBase() == Base.BASE32 ? 32 : 64);
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            LinkAccountDataFragment linkAccountDataFragment = new LinkAccountDataFragment();
            linkAccountDataFragment.setArguments(bundle);
            fragmentTransaction.replace(R.id.register_account_fragment_container,
                    linkAccountDataFragment, "register-account-data")
                    .addToBackStack("register-account-data")
                    .commit();
        }
    }

    private void showQRCodeFragment()
    {
        Intent intent = getIntent();
        if (intent.hasExtra("mockScannedCode"))
        {
            onQRCodeScanned(BarcodeFormat.QR_CODE, intent.getStringExtra("mockScannedCode"));
        }
        else
        {
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            Bundle bundle = new Bundle();
            bundle.putBoolean("play_scan_sound", sharedPreferences.getBoolean("play_scan_sound", true));
            ScanQRCodeFragment scanQRCodeFragment = new ScanQRCodeFragment();
            scanQRCodeFragment.setArguments(bundle);
            fragmentTransaction.replace(R.id.register_account_fragment_container,
                    scanQRCodeFragment, "qr-code")
                    .addToBackStack("qr-code")
                    .commit();
        }
    }

    /**
     * The "Correct" button is in LinkAccountDataFragment.
     *
     * @param view
     */
    public void handleCorrectButtonClicked(View view)
    {
        TextView emailTextField = findViewById(R.id.account_label_edittext);
        TextView keyTextField = findViewById(R.id.key_edit_text);
        CheckBox setAsDefaultAccountCheckBox = findViewById(R.id.set_as_default_account_box);

        if (keyTextField.getError() != null)
        {
            Toast toast = Toast.makeText(getApplicationContext(), keyTextField.getError(),
                    Toast.LENGTH_SHORT);
            toast.show();
            return;
        }

        boolean wasManualEntry;
        FragmentManager.BackStackEntry backEntry = getSupportFragmentManager().getBackStackEntryAt(
                getSupportFragmentManager().getBackStackEntryCount() - 2);
        wasManualEntry = backEntry.getName().equals("manual-entry");

        if (wasManualEntry || scannedAccount.getIssuer() == Issuer.CRYPTODASH)
        {
            ProgressDialog progressDialog = new ProgressDialog(LinkAccountActivity.this);
            ProgressBar progressBar = new ProgressBar(LinkAccountActivity.this, null,
                    android.R.attr.progressBarStyleHorizontal);

            progressDialog.setTitle(getResources().getString(R.string.verifying_credentials_title));
            progressDialog.setMessage(getResources().getString(R.string.verifying_credentials));
            progressDialog.show();
            NotifyAccountLinkedTask notifyAccountLinkedTask = new NotifyAccountLinkedTask(
                    centurion, progressDialog, emailTextField.getText(), keyTextField.getText());
            JSONObject response;

            // if this is a code provided by us, verify entered credentials via Centurion
            try
            {
                response = notifyAccountLinkedTask.execute().get(4000, TimeUnit.MILLISECONDS);
            }
            catch (InterruptedException | ExecutionException | TimeoutException e)
            {
                try
                {
                    response = new JSONObject("{\"local_error\": " + JSONObject.quote(e.getMessage()) + "}");
                }
                catch (JSONException e1)
                {
                    throw new AndroidRuntimeException(e1);
                }
            }

            if (response != null)
            {
                if (response.has("local_error"))
                {
                    final String error = response.optString("local_error");
                    Toast toast = Toast.makeText(getApplicationContext(), "Looks like we have a " +
                            "problem on our end. Sorry about that! We have notified our developers " +
                            "of the problem.", Toast.LENGTH_LONG);
                    toast.show();
                    ACRA.getErrorReporter().handleSilentException(new IOException(error));
                    return;
                }

                int responseCode = Integer.valueOf(response.optString("httpResponseCode", "-1"));
                if (responseCode != 200)
                {
                    AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this, R.style.Theme_Dialog);
                    alertBuilder.setMessage(R.string.centurion_invalid_args);
                    alertBuilder.setCancelable(false);
                    alertBuilder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int id)
                        {
                            // TODO maybe we should allow the user to go to
                            // manual entry with the current text already entered
                            dialog.dismiss();
                        }
                    });
                    AlertDialog alertDialog = alertBuilder.create();
                    alertDialog.show();
                    return;
                }
            }
        }

        Toast toast = Toast.makeText(getApplicationContext(), "You have successfully linked " +
                "your account!", Toast.LENGTH_SHORT);
        toast.show();

        // TODO instead of showing the above toast, maybe we could animate in a "Successfully linked"
        // text/image and then fade to time wheel widget

        if (wasManualEntry)
        {
            if (enteredEmail != null && enteredKey != null)
            {
                // TODO currently we default to a Cryptodash provided code if manual entry was used
                Account newAccount = new Account(enteredEmail, Issuer.CRYPTODASH,
                        Base64.getDecoder().decode(enteredKey),
                        new CodeParams.Builder(CodeType.TOTP).base(64).algorithm(Algorithm.SHA256).build());
                AccountManager.getInstance().registerAccount(newAccount, true,
                        setAsDefaultAccountCheckBox.isChecked());
            }
            else
            {
                throw new IllegalStateException("enteredEmail or enteredKey were null!");
            }
        }
        else
        {
            if (scannedAccount != null)
            {
                AccountManager.getInstance().registerAccount(scannedAccount, true,
                        setAsDefaultAccountCheckBox.isChecked());
            }
            else
            {
                throw new IllegalStateException("scannedAccount was null!");
            }
        }

        scannedAccount = null;
        enteredEmail = null;
        enteredKey = null;
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        switch (requestCode)
        {
            case REQUEST_CAMERA:
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED)
                {
                    Toast.makeText(this, "Cannot run application because camera service permission has not been granted", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
                break;
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
        EditText emailTextField = (EditText) findViewById(R.id.account_label_edittext);
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

        // TODO need to implement issuer for manual entry for non-Cryptodash accounts
        Bundle bundle = new Bundle();
        bundle.putString("method", LinkMethod.MANUAL_ENTRY.name());
        bundle.putString("new_label", emailTextField.getText().toString());
        bundle.putByteArray("new_key", Base64.getDecoder().decode(keyTextField.getText().toString()));
        bundle.putInt("new_issuer", Issuer.CRYPTODASH.getId());
        bundle.putInt("new_base", 64);
        enteredEmail = emailTextField.getText().toString();
        enteredKey = keyTextField.getText().toString();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        LinkAccountDataFragment linkAccountDataFragment = new LinkAccountDataFragment();
        linkAccountDataFragment.setArguments(bundle);
        fragmentTransaction.replace(R.id.register_account_fragment_container,
                linkAccountDataFragment, "register-account-data")
                .addToBackStack("register-account-data")
                .commit();
    }

    private static class NotifyAccountLinkedTask extends AsyncTask<String, Void, JSONObject>
    {
        private final Centurion centurion;
        private final CharSequence email;
        private final CharSequence key;
        private final ProgressDialog progressDialog;

        public NotifyAccountLinkedTask(Centurion centurion, ProgressDialog progressDialog,
                                       CharSequence email, CharSequence key)
        {
            this.centurion = centurion;
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
            String payload = "{\"email\": \"" + email + "\", \"totpKeyBase64\": \"" + key + "\"}";
            JSONObject response = centurion.post("twofactor/notify-linked", payload);
            Log.e("Blah", "Response: " + response.toString());
            return response;
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
