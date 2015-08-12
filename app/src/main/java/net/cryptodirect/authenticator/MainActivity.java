package net.cryptodirect.authenticator;

import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import org.acra.ACRA;
import org.json.JSONException;

import java.io.IOException;

public class MainActivity extends AppCompatActivity
{
    public static Context BASE_CONTEXT;
    private static final String PREFS_FILE = "PrefsFile";
    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle state)
    {
        super.onCreate(state);
        BASE_CONTEXT = getBaseContext();
        setContentView(R.layout.activity_main);
        if (findViewById(R.id.main_fragment_container) != null && state != null)
        {
            return;
        }

        try
        {
            AccountManager.getInstance().initAccountManager();
        }
        catch (IOException | JSONException e)
        {
            ACRA.getErrorReporter().handleException(e);
            throw new RuntimeException(e);
        }

        SharedPreferences settings = getSharedPreferences(PREFS_FILE, 0);
        if (settings.contains("default_account"))
        {
            // the user has saved a default account preference - so try to go to its' authenticator
            // widget immediately.
            String defaultAccount = settings.getString("default_account", "");
            if (AccountManager.getInstance().accountExists(defaultAccount))
            {
                // go to authenticator for default account preference
                addAuthenticatorFragment(defaultAccount, 30);
            }
            else
            {
                // we have no data for the stored default account preference
                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(getBaseContext());
                alertBuilder.setMessage("We found that you set your default account to: "
                        + defaultAccount + " but there is no stored data for that account. " +
                        "Sorry about that - please register the account again.");
                alertBuilder.setCancelable(true);
                alertBuilder.setPositiveButton("Register Account",
                    new DialogInterface.OnClickListener()
                    {
                        public void onClick(DialogInterface dialog, int id)
                        {
                            goToRegisterAccountActivity();
                        }
                    });
                alertBuilder.setNegativeButton("Not Now",
                    new DialogInterface.OnClickListener()
                    {
                        public void onClick(DialogInterface dialog, int id)
                        {
                            dialog.cancel();
                        }
                    });
                AlertDialog alertDialog = alertBuilder.create();
                alertDialog.show();
            }
        }
        else
        {
            Log.i(TAG, "There was no default account setting, so going by number of accounts");
            Log.i(TAG, "Number of accounts: " + AccountManager.getInstance().getNumAccounts());
            // there is no default account setting
            if (AccountManager.getInstance().getNumAccounts() > 0)
            {
                // we have data for at least one account
                if (AccountManager.getInstance().getNumAccounts() > 1)
                {
                    // we have data for multiple accounts and no default account
                    AccountChooserFragment accountChooserFragment = new AccountChooserFragment();
                    FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                    fragmentTransaction.add(R.id.main_fragment_container,
                            accountChooserFragment, "choose-account")
                            .commit();
                }
                else
                {
                    // we have data for only one account
                    addAuthenticatorFragment(AccountManager.getInstance().getOnlyAccount().getEmail(), 30);
                }
            }
            else
            {
                // we have no stored account data
                goToRegisterAccountActivity();
            }
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId())
        {
            case R.id.register_account_fragment_container:
                goToRegisterAccountActivity();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void addAuthenticatorFragment(String email, int ts)
    {
        Bundle bundle = new Bundle();
        bundle.putString("email", email);
        bundle.putString("key", AccountManager.getInstance().getAccount(email).getSecretKey());
        bundle.putInt("ts", ts);
        AuthenticatorFragment authenticatorFragment = new AuthenticatorFragment();
        authenticatorFragment.setArguments(bundle);
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.main_fragment_container,
                authenticatorFragment, "authenticator")
                .commit();
    }

    private void goToRegisterAccountActivity()
    {
        Intent intent = new Intent(this, RegisterAccountActivity.class);
        startActivity(intent);
    }
}
