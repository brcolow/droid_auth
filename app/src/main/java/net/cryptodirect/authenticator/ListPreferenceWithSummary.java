package net.cryptodirect.authenticator;

import android.content.Context;
import android.preference.ListPreference;
import android.util.AttributeSet;
import android.util.Log;

/**
 * Work-around for buggy %s implementation.
 * <p/>
 *
 * Based on Stackoverflow answer:
 * http://stackoverflow.com/questions/7017082/change-the-summary-of-a-listpreference-with-the-new-value-android
 */
public class ListPreferenceWithSummary extends ListPreference
{
    private static final String TAG = ListPreferenceWithSummary.class.getSimpleName();

    public ListPreferenceWithSummary(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public ListPreferenceWithSummary(Context context)
    {
        super(context);
    }

    @Override
    public void setValue(String value)
    {
        Log.e(TAG, "SET VALUE CALLED WITH VALUE: " + value);
        super.setValue(value);
        setSummary(value);
    }

    @Override
    public void setSummary(CharSequence summary)
    {
        Log.e(TAG, "SET SUMMARY CALLED WITH VALUE: " + summary);
        super.setSummary(getEntry());
    }
}