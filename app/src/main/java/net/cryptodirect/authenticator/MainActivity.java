package net.cryptodirect.authenticator;

import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import org.acra.ACRA;
import org.json.JSONException;

import java.io.IOException;
import java.lang.reflect.Method;

public class MainActivity extends AppCompatActivity implements AccountChooserFragment.OnAccountChosenListener
{
    public static Context BASE_CONTEXT;
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
            AccountManager.getInstance().init();
        }
        catch (IOException | JSONException e)
        {
            ACRA.getErrorReporter().handleException(e);
            throw new RuntimeException(e);
        }

        FontManager.getInstance().init(getApplicationContext());
        SoundPoolManager.getInstance().init(getApplicationContext());

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);

        if (!settings.getString("default_account", "").isEmpty() && settings.getBoolean("skip_choose", true))
        {
            // the user has saved a default account preference and skip choose is set to true
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
                            startRegisterAccountActivity();
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
                startRegisterAccountActivity();
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
    public boolean onPrepareOptionsMenu(Menu menu)
    {
        for (int  i =0; i < menu.size(); i++)
        {
            MenuItem mi = menu.getItem(i);
            String title = mi.getTitle().toString();
            Spannable newTitle = new SpannableString(title);
            newTitle.setSpan(new ForegroundColorSpan(Color.WHITE), 0, newTitle.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            mi.setTitle(newTitle);
        }
        return true;
    }

    /**
     * We override this method to add a nasty hack that forces
     * action bar overflow dropdown menu list items to always
     * display their icons.
     *
     * @see {@literal http://stackoverflow.com/a/30337653/3634630}
     */
    @Override
    protected boolean onPrepareOptionsPanel(View view, Menu menu)
    {
        if (menu != null)
        {
            if (menu.getClass().getSimpleName().equals("MenuBuilder"))
            {
                try
                {
                    Method m = menu.getClass().getDeclaredMethod("setOptionalIconsVisible", Boolean.TYPE);
                    m.setAccessible(true);
                    m.invoke(menu, true);
                }
                catch (Exception e)
                {
                    Log.e(getClass().getSimpleName(), "onMenuOpened...unable to set icons for overflow menu", e);
                }
            }
        }
        return super.onPrepareOptionsPanel(view, menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
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
            case R.id.action_register_new_account:
                startRegisterAccountActivity();
                return true;
            case R.id.action_settings:
                showSettingsFragment();
                return true;
            case R.id.action_about:
                showAboutFragment();
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
                .addToBackStack("authenticator")
                .commit();
    }

    private void showSettingsFragment()
    {
        SettingsFragment settingsFragment = new SettingsFragment();
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.main_fragment_container,
                settingsFragment, "settings")
                .addToBackStack("settings")
                .commit();
    }

    private void showAboutFragment()
    {
        AboutFragment aboutFragment = new AboutFragment();
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.main_fragment_container,
                aboutFragment, "about")
                .addToBackStack("about")
                .commit();
    }

    private void startRegisterAccountActivity()
    {
        Intent intent = new Intent(this, RegisterAccountActivity.class);
        startActivity(intent);
    }

    @Override
    public void onAccountChosen(String chosenAccount)
    {
        addAuthenticatorFragment(chosenAccount, 30);
    }
}
