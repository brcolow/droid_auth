package net.cryptodirect.authenticator;

import android.animation.ArgbEvaluator;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
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
import android.view.Window;

import org.acra.ACRA;
import org.json.JSONException;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The main activity of the authenticator application. This activity is the first
 * activity that the application enters on startup. Currently this activity is used
 * for everything except account linking
 */
public class MainActivity
        extends AppCompatActivity
        implements AccountChooserFragment.OnAccountChosenListener,
        FragmentManager.OnBackStackChangedListener,
        DialogInterface.OnCancelListener
{
    private static final String TAG = MainActivity.class.getSimpleName();
    private ViewPager howItWorksPager;
    private int currSelectedPage = 0;
    private static final Map<Integer, HowItWorksPageFragment> pageMap = new LinkedHashMap<>(3);
    private static EntryPage entryPage;
    private Centurion centurion;
    private String mockScannedCode;

    static
    {
        initPageMap();
    }

    @Override
    protected void onCreate(Bundle state)
    {
        super.onCreate(state);
        Intent intent = getIntent();
        centurion = (Centurion) intent.getSerializableExtra(
                "net.cryptodirect.authenticator.Centurion");
        mockScannedCode = intent.getStringExtra("net.cryptodirect.authenticator.MockScannedCode");
        boolean verify = intent.getBooleanExtra("verifyAccountsFile", true);
        setContentView(R.layout.activity_main);
        if (findViewById(R.id.main_fragment_container) != null && state != null)
        {
            return;
        }

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        final int numAccounts = settings.getInt("numAccounts", 0);
        try
        {
            AccountManager.getInstance().setBaseContext(getBaseContext());
            AccountManager.getInstance().init(numAccounts, verify);
        }
        catch (AccountManager.DataMismatchException e)
        {
            // report data corruption
            ACRA.getErrorReporter().handleSilentException(e);
            AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this, R.style.Theme_Dialog);
            alertBuilder.setMessage("The stored account data is corrupted. We are missing data " +
                    "for " + e.getNumMissingAccounts() + " account " + (e.getNumMissingAccounts() > 1 ? "s" : "") +
                    ". Please check which accounts are still registered. This error has " +
                    "been reported.");
            alertBuilder.setCancelable(false);
            alertBuilder.setPositiveButton(getString(R.string.check_accounts), new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int id)
                {
                    MainActivity.this.showAccountChooserFragment();
                }
            });
            alertBuilder.setNegativeButton(getString(R.string.not_now), new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int id)
                {
                    dialog.cancel();
                }
            });
            AlertDialog alertDialog = alertBuilder.create();
            alertDialog.show();
        }
        catch (IOException | JSONException e)
        {
            ACRA.getErrorReporter().handleException(e);
            throw new RuntimeException(e);
        }

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        FontManager.getInstance().init(getApplicationContext());
        SoundPoolManager.getInstance().init(getApplicationContext());

        if (getSupportActionBar() != null)
        {
            getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_USE_LOGO |
                    ActionBar.DISPLAY_SHOW_TITLE);
        }

        getSupportFragmentManager().addOnBackStackChangedListener(this);
        if (!settings.getString("default_account", "").isEmpty() &&
                !settings.getString("default_account", "").equalsIgnoreCase("none") &&
                settings.getBoolean("skip_choose", true))
        {
            // the user has saved a default account preference and skip choose is set to true
            int defaultAccount = Integer.valueOf(settings.getString("default_account", ""));
            if (AccountManager.getInstance().accountExists(defaultAccount))
            {
                // go to authenticator for default account preference
                entryPage = EntryPage.DEFAULT_ACCOUNT_AUTHENTICATOR;
                showAuthenticatorFragment(defaultAccount);
            }
            else
            {
                // we have no data for the stored default account preference
                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this, R.style.Theme_Dialog);
                alertBuilder.setMessage("We found that your default account is set to: "
                        + defaultAccount + " but we have no data for that account. " +
                        "Sorry about that - please link the account again.");
                alertBuilder.setCancelable(true);
                alertBuilder.setPositiveButton(getString(R.string.register_new_account),
                        new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int id)
                            {
                                MainActivity.this.startLinkAccountActivity();
                            }
                        });
                alertBuilder.setNegativeButton(getString(R.string.not_now),
                        new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int id)
                            {
                                dialog.cancel();
                            }
                        });
                AlertDialog alertDialog = alertBuilder.create();
                alertDialog.setOnCancelListener(this);
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
                    entryPage = EntryPage.ACCOUNT_CHOOSER;
                    showAccountChooserFragment();
                }
                else
                {
                    // we have data for only one account
                    entryPage = EntryPage.ONE_ACCOUNT_AUTHENTICATOR;
                    showAuthenticatorFragment(AccountManager.getInstance().getFirstAccount()
                            .getIssuer().getId());
                }
            }
            else
            {
                // we have no stored account data
                entryPage = EntryPage.WELCOME;
                WelcomeFragment welcomeFragment = new WelcomeFragment();
                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
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
            android.app.FragmentManager.BackStackEntry backEntry = getFragmentManager().
                    getBackStackEntryAt(getFragmentManager().getBackStackEntryCount() - 1);
            String fragmentName = backEntry.getName();
            if (fragmentName.equals("settings") || fragmentName.equals("how-it-works"))
            {
                // the settings and how-it-works fragments are the only fragments
                // in the application that use the non-compat Fragment, and so are
                // managed by getFragmentManager() rather than getSupportFragmentManager()
                getFragmentManager().popBackStack();
                return;
            }
        }

        if (getSupportFragmentManager().getBackStackEntryCount() > 1)
        {
            getSupportFragmentManager().popBackStack();
            return;
        }

        if (getSupportFragmentManager().getBackStackEntryCount() == 1)
        {
            FragmentManager.BackStackEntry backEntry = getSupportFragmentManager().
                    getBackStackEntryAt(getSupportFragmentManager().getBackStackEntryCount() - 1);
            String fragmentName = backEntry.getName();

            if (!fragmentName.equals("welcome") && !fragmentName.equals("authenticator")
                    && !fragmentName.equals("choose-account"))
            {
                getSupportFragmentManager().popBackStack();
            }
            else if (fragmentName.equals("choose-account") && entryPage != EntryPage.ACCOUNT_CHOOSER)
            {
                getSupportFragmentManager().popBackStack();
            }
            else if (fragmentName.equals("authenticator") && (entryPage == EntryPage.WELCOME ||
                    entryPage == EntryPage.ACCOUNT_CHOOSER))
            {
                getSupportFragmentManager().popBackStack();
            }
            else
            {
                if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                {
                    this.finishAffinity();
                }
                else
                {
                    finish();
                }
            }
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu)
    {
        for (int i = 0; i < menu.size(); i++)
        {
            MenuItem menuItem = menu.getItem(i);
            String title = menuItem.getTitle().toString();
            Spannable newTitle = new SpannableString(title);
            newTitle.setSpan(new ForegroundColorSpan(Color.WHITE), 0, newTitle.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            menuItem.setTitle(newTitle);
        }
        return true;
    }

    @Override
    public boolean onMenuOpened(int featureId, Menu menu)
    {
        if (featureId == Window.FEATURE_ACTION_BAR && menu != null)
        {
            if (menu.getClass().getSimpleName().equals("MenuBuilder"))
            {
                try
                {
                    Method method = menu.getClass().getDeclaredMethod("setOptionalIconsVisible", Boolean.TYPE);
                    method.setAccessible(true);
                    method.invoke(menu, true);
                }
                catch (NoSuchMethodException e)
                {
                    Log.e(TAG, "setOptionalIconsVisible method not found: ", e);
                }
                catch (Exception e)
                {
                    throw new IllegalStateException(e);
                }
            }
        }
        return super.onMenuOpened(featureId, menu);
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
                    Log.e(TAG, "onMenuOpened...unable to set icons for overflow menu", e);
                    throw new IllegalStateException(e);
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
            case R.id.action_select_account:
                showAccountChooserFragment();
                return true;
            case R.id.action_register_new_account:
                startLinkAccountActivity();
                return true;
            case R.id.action_settings:
                showSettingsFragment();
                return true;
            case R.id.action_about:
                showAboutFragment();
                return true;
            case R.id.action_test_device:
                showTestDeviceFragment();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showAccountChooserFragment()
    {
        Bundle bundle = new Bundle();
        bundle.putCharSequenceArray("accountLabels", AccountManager.getInstance().getAccountLabels());
        AccountChooserFragment accountChooserFragment = new AccountChooserFragment();
        accountChooserFragment.setArguments(bundle);
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.main_fragment_container,
                accountChooserFragment, "choose-account")
                .addToBackStack("choose-account")
                .commit();
    }

    private void showAuthenticatorFragment(int accountId)
    {
        Bundle bundle = new Bundle();
        bundle.putSerializable("account", AccountManager.getInstance().getAccount(accountId));
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        bundle.putBoolean("play_time_running_out_sound", sharedPreferences.getBoolean(
                "play_time_running_out_sound", true));
        bundle.putInt("time_running_out_start", sharedPreferences.getInt(
                "time_running_out_start", 5));
        AuthenticatorFragment authenticatorFragment = new AuthenticatorFragment();
        //sharedPreferences.registerOnSharedPreferenceChangeListener(authenticatorFragment);
        authenticatorFragment.setArguments(bundle);
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.main_fragment_container,
                authenticatorFragment, "authenticator")
                .addToBackStack("authenticator")
                .commit();
    }

    private void showSettingsFragment()
    {
        setTheme(R.style.SettingsFragmentStyle);
        Bundle bundle = new Bundle();
        bundle.putCharSequenceArray("accountLabels", AccountManager.getInstance().getAccountLabels());
        SettingsFragment settingsFragment = new SettingsFragment();
        settingsFragment.setArguments(bundle);
        android.app.FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.main_fragment_container,
                settingsFragment, "settings")
                .addToBackStack("settings")
                .commit();
    }

    private void showAboutFragment()
    {
        AboutFragment aboutFragment = new AboutFragment();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.main_fragment_container,
                aboutFragment, "about")
                .addToBackStack("about")
                .commit();
    }

    private void showTestDeviceFragment()
    {
        TestDeviceFragment testDeviceFragment = new TestDeviceFragment();
        Bundle bundle = new Bundle();
        if (centurion == null)
        {
            centurion = new RealCenturion();
        }
        bundle.putSerializable("centurion", centurion);
        testDeviceFragment.setArguments(bundle);
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.main_fragment_container,
                testDeviceFragment, "test-device")
                .addToBackStack("test-device")
                .commit();
    }

    private void startLinkAccountActivity()
    {
        Intent intent = new Intent(this, LinkAccountActivity.class);
        if (centurion == null)
        {
            centurion = new RealCenturion();
        }
        intent.putExtra("centurion", centurion);
        if (mockScannedCode != null)
        {
            intent.putExtra("mockScannedCode", mockScannedCode);
        }
        startActivity(intent);
    }

    @Override
    public void onAccountChosen(String chosenAccount)
    {
        showAuthenticatorFragment(Issuer.valueOf(chosenAccount).getId());
    }

    /**
     * The "Register Account" button is in WelcomeFragment
     *
     * @param view
     */
    public void handleRegisterAccountClicked(View view)
    {
        startLinkAccountActivity();
    }

    /**
     * The "How it Works" button is in WelcomeFragment
     *
     * @param view
     */
    public void handleHowItWorksClicked(View view)
    {
        android.app.FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        HowItWorksFragment howItWorksFragment = new HowItWorksFragment();
        fragmentTransaction.add(R.id.main_fragment_container,
                howItWorksFragment, "how-it-works")
                .addToBackStack("how-it-works")
                .commit();

        // here we force (and block) the fragment transaction, otherwise getViewPager() returns null
        getFragmentManager().executePendingTransactions();

        howItWorksPager = howItWorksFragment.getViewPager();
        howItWorksPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener()
        {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels)
            {
            }

            @Override
            public void onPageSelected(int position)
            {
                currSelectedPage = position;
            }

            @Override
            public void onPageScrollStateChanged(int state)
            {
            }
        });

        howItWorksFragment.getViewPager().setPageTransformer(false, new HowItWorksPageTransformer());
    }

    @Override
    public void onBackStackChanged()
    {
        currSelectedPage = 0;
    }

    private static void initPageMap()
    {
        pageMap.put(0, HowItWorksPageFragment.newInstance(0,
                "#EC407A",
                R.drawable.ic_qrcode_white_36dp,
                "In order to use this authenticator app, you need to link your Cryptodash account " +
                        "with this device. To do so, just tap the \"Link Account\" button. The " +
                        "linking process involves one step - scanning a QR code. You can get the required " +
                        "QR code from either www.cryptodash.net or the Cryptodash application. When logging " +
                        "in without two-factor authentication enabled, a warning will appear suggesting you " +
                        "enable it. Follow the warning's instructions to retrieve your QR code and use this " +
                        "app to scan it. After that, you're successfully linked. " +
                        "Once your account is linked, when you log in to Cryptodash or perform " +
                        "certain actions (which is configurable) you will be prompted to enter a verification code."));

        pageMap.put(1, HowItWorksPageFragment.newInstance(1,
                "#5C6BC0",
                R.drawable.ic_keyboard_white_36dp,
                "Every 30 seconds, this app automatically generates a random 6-digit " +
                        "code. When two-factor authentication is enabled, this code is required when " +
                        "logging in to your account or performing sensitive actions (such as " +
                        "withdrawing funds or posting an order). Exactly which actions require " +
                        "entering a code is completely configurable and uses sensible defaults. " +
                        "Requiring a code adds an extra layer of security on to your account. In " +
                        "other words, even if your username and password were compromised, an " +
                        "attacker would still need your mobile phone to even attempt to gain " +
                        "access to your account."));

        pageMap.put(2, HowItWorksPageFragment.newInstance(2,
                "#43A047",
                R.drawable.ic_restore_white_48dp,
                "Codes are generated using a 32-byte shared secret key that is stored securely on your " +
                        "device. The process of generating a code uses a cryptographically secure standard " +
                        "called TOTP (Time-based One-time Password Algorithm) which internally uses the same " +
                        "cryptographic hash function as Bitcoin (SHA-256). In accordance with our " +
                        "transparent security practices, you can run this app against the set of " +
                        "tests listed in the TOTP specification via the \"Test Device\" option on the main menu."));
    }

    @Override
    public void onCancel(DialogInterface dialogInterface)
    {
        // the alert dialog is shown when a default account setting exists
        // but we don't have data for it
        // TODO not sure if this is the best thing to do if the dialog is cancelled
        WelcomeFragment welcomeFragment = new WelcomeFragment();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.main_fragment_container,
                welcomeFragment, "welcome")
                .addToBackStack("welcome")
                .commit();
    }

    private class HowItWorksPageTransformer implements ViewPager.PageTransformer
    {
        public void transformPage(View view, float position)
        {
            if (position >= -1 || position <= 1) // [-1,1]
            {
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
        }
    }

    /**
     * The "Next" button is in HowItWorksPageFragment
     */
    public void handleNextPageButtonClicked(View view)
    {
        howItWorksPager.setCurrentItem(++currSelectedPage, true);
    }

    static Map<Integer, HowItWorksPageFragment> getPageMap()
    {
        return pageMap;
    }
}
