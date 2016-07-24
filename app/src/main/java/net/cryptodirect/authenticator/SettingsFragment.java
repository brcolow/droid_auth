package net.cryptodirect.authenticator;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.view.View;

/**
 * Allows the user to configure various application settings.
 */
public class SettingsFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener
{
    private String initialMessage;

    @Override
    public void onCreate(Bundle state)
    {
        super.onCreate(state);
        initialMessage = getActivity().getApplicationContext().getString(R.string.time_running_out_start_summ);
        addPreferencesFromResource(R.xml.preferences);
        ListPreference defaultAccountPref = (ListPreference) findPreference("default_account");
        CharSequence[] accountEmailsArray = AccountManager.getInstance().getAccountEmails();
        defaultAccountPref.setEntries(accountEmailsArray);
        defaultAccountPref.setEntryValues(accountEmailsArray);
        if (accountEmailsArray.length == 1)
        {
            defaultAccountPref.setDefaultValue(accountEmailsArray[0]);
        }
        Preference timeRunningOutStartPref = findPreference("time_running_out_start");
        timeRunningOutStartPref.setOnPreferenceChangeListener(this);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String message = initialMessage.split("X")[0] + prefs.getInt("time_running_out_start", 5) +
                initialMessage.split("X")[1];
        timeRunningOutStartPref.setSummary(message);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState)
    {
        view.setBackgroundColor(Color.WHITE);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue)
    {
        preference.setSummary(initialMessage.split("X")[0] + newValue +
                initialMessage.split("X")[1]);
        return true;
    }
}
