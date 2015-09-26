package net.cryptodirect.authenticator;

import android.animation.ArgbEvaluator;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_USE_LOGO | ActionBar.DISPLAY_SHOW_TITLE);

        getSupportActionBar().setIcon(R.drawable.ic_qrcode_white_36dp);
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
                WelcomeFragment welcomeFragment = new WelcomeFragment();
                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                fragmentTransaction.add(R.id.main_fragment_container,
                        welcomeFragment, "welcome")
                        .addToBackStack("welcome")
                        .commit();
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
        for (int i = 0; i < menu.size(); i++)
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

    /**
     * The "Register Account" button is in WelcomeFragment
     *
     * @param view
     */
    public void handleRegisterAccountClicked(View view)
    {
        startRegisterAccountActivity();
    }

    /**
     * The "How it Works" button is in WelcomeFragment
     *
     * @param view
     */
    public void handleHowItWorksClicked(View view)
    {
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        HowItWorksFragment howItWorksFragment = new HowItWorksFragment();
        fragmentTransaction.add(R.id.main_fragment_container,
                howItWorksFragment, "how-it-works")
                .addToBackStack("how-it-works")
                .commit();

        getFragmentManager().executePendingTransactions();

        howItWorksFragment.getViewPager().setAdapter(new HowItWorksPagerAdapter(getSupportFragmentManager()));
        howItWorksFragment.getViewPager().setPageTransformer(false, new HowItWorksPageTransformer());
    }

    private static final Map<Integer, HowItWorksPageFragment> pageMap = new ConcurrentHashMap<>();

    static
    {
        pageMap.put(0 , HowItWorksPageFragment.newInstance(0,
                "#EC407A",
                R.drawable.ic_qrcode_white_36dp,
                "In order to use this authenticator app, you have to link your Cryptodash account " +
                "with this device. To do so just click the \"Register Account\" button which will " +
                "help you to scan a QR code. After that, you're linked. Simple." +
                "Now that your account is linked, when you log in to Cryptodash or perform " +
                "certain actions (this is configurable) you will be prompted to enter a verification code."));

        pageMap.put(1 , HowItWorksPageFragment.newInstance(1,
                "#5C6BC0",
                R.drawable.ic_keyboard_white_36dp,
                "Every 30 seconds this app automatically generates a new, 6-digit " +
                 "random code. Requiring this code adds an extra layer " +
                 "of security to your account in that even if your username and password became " +
                 "compromised, an attacker would still need your mobile phone to even attempt to " +
                 "gain access to your account."));

        pageMap.put(2, HowItWorksPageFragment.newInstance(2,
                "#43A047",
                R.drawable.ic_restore_white_48dp,
                "Codes are generated using a secret key that is stored securely on your " +
                "device. Codes are generated by a cryptographically secure standard " +
                "called TOTP which uses the same cryptographic hash function as Bitcoin! " +
                "You can run this app against the set of tests listed in the TOTP "  +
                "specification via the \"Test Device\" option in the main menu."));
    }

    private class HowItWorksPagerAdapter extends FragmentPagerAdapter
    {
        public HowItWorksPagerAdapter(FragmentManager fragmentManager)
        {
            super(fragmentManager);
        }

        @Override
        public Fragment getItem(int position)
        {
            return pageMap.get(position);
        }

        @Override
        public int getCount()
        {
            return pageMap.size();
        }
    }

    private class HowItWorksPageTransformer implements ViewPager.PageTransformer
    {
        public void transformPage(View view, float position)
        {
            // Log.e(TAG, String.valueOf("View: " + view.getId() + ": " + position));
            //ImageView imageView = (ImageView) view.findViewById(R.id.how_it_works_page_image);
            if (position < -1) // [-Infinity,-1)
            {
                // This page is way off-screen to the left.
                view.setAlpha(0);
            }
            else if (position <= 1) // [-1,1]
            {
                view.setRotationY(position * -10);

                if (position != 0f)
                {
                    if (position < 0)
                    {
                        if (pageMap.get(view.getId() + 1) != null)
                        {
                            int color = (Integer) new ArgbEvaluator().evaluate(Math.abs(position),
                                    pageMap.get(view.getId()).getBackgroundColor(),
                                    pageMap.get(view.getId() + 1).getBackgroundColor());
                            view.setBackgroundColor(color);
                        }
                    }
                    else
                    {
                        if (pageMap.get(view.getId() - 1) != null)
                        {
                            int color = (Integer) new ArgbEvaluator().evaluate(Math.abs(position),
                                    pageMap.get(view.getId()).getBackgroundColor(),
                                    pageMap.get(view.getId() - 1).getBackgroundColor());
                            view.setBackgroundColor(color);
                        }
                    }
                }
            }
            else // (1,+Infinity]
            {
                // This page is way off-screen to the right.
                view.setAlpha(0);
            }
        }
    }
}
