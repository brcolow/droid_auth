package net.cryptodirect.authenticator;

import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import net.cryptodirect.authenticator.crypto.TOTP;

import java.util.Timer;
import java.util.TimerTask;

/**
 * The authenticator widget which displays the current TOTP code
 * and how much remaining time it is valid for indicated by a circular time
 * wheel {@code TimestepIntervalWheel}.
 */
public class AuthenticatorFragment extends Fragment
{
    // time step (default is 30 seconds)
    private int ts;
    // String whose bytes (in ASCII) are used for the hmac
    private String key;

    // the number of digits the final TOTP password should have
    private static final int NUM_DIGITS = 6;
    private static final Handler handler = new Handler();
    private static SharedPreferences sharedPreferences;
    private static TimestepIntervalWheel timestepIntervalWheel;
    private volatile boolean tickingSoundPlaying = false;
    private static EditText codeBox;
    private static final String TAG = AuthenticatorFragment.class.getSimpleName();

    public AuthenticatorFragment()
    {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle state)
    {
        View rootView = inflater.inflate(R.layout.fragment_authenticator, container, false);
        String email = getArguments().getString("email");
        key = getArguments().getString("key");
        ts = getArguments().getInt("ts");

        if (key == null || email == null)
        {
            throw new IllegalStateException("AuthenticatorFragment was not passed email," +
                    " key data arguments, was: [" + email + ", " + key + "]");
        }
        if (ts == 0)
        {
            Log.e(TAG, "AuthenticatorFragment was not passed ts argument, using default of 30!");
            ts = 30;
        }
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        ((TextView) rootView.findViewById(R.id.email_label)).setText(email);

        // Set up TimestepIntervalWheel with current time
        double tc = TOTP.getTC(ts);
        // how far along we are percentage-wise of the cyclical range [0, TS], e.g. 0.25
        double fractionalPart = tc - (long) tc;
        // what second of the cyclical range [0, TS] we are in, e.g. TS * 0.25
        int currentSpotInTSInterval = (int) ((double) ts * fractionalPart);
        timestepIntervalWheel = new TimestepIntervalWheel(this.getActivity(), ts, ts - currentSpotInTSInterval);
        RelativeLayout.LayoutParams intervalWheelLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        intervalWheelLayoutParams.width = 500;
        intervalWheelLayoutParams.addRule(RelativeLayout.BELOW, R.id.code_box);
        intervalWheelLayoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
        ((RelativeLayout) rootView.findViewById(R.id.main_layout)).addView(timestepIntervalWheel, intervalWheelLayoutParams);
        codeBox = ((EditText) rootView.findViewById(R.id.code_box));

        // Set initial TOTP token value
        codeBox.setText(TOTP.generateTOTPSha1(key.getBytes(StandardCharsets.US_ASCII), (long) tc, NUM_DIGITS));
        Timer timer = new Timer();
        timer.schedule(new WheelTask(), 0, 1000);
        return rootView;
    }

    private class WheelTask extends TimerTask
    {
        @Override
        public void run()
        {
            if (timestepIntervalWheel.decrementSecondsRemaining())
            {
                handler.post(new SetNewCodeTask(TOTP.generateTOTPSha1(key.getBytes(StandardCharsets.US_ASCII), TOTP.getTC(ts), NUM_DIGITS)));
                // SoundPoolManager.getInstance().stopSound("TICKTOCK");
                tickingSoundPlaying = false;
            }
            else
            {
                if (!tickingSoundPlaying)
                {
                    // TODO might need to cache this value and implement a prefchangedlistener
                    if (sharedPreferences.getBoolean("play_time_running_out_sound", true))
                    {
                        if (timestepIntervalWheel.getSecondsRemainingInInterval() <= sharedPreferences.getInt("time_running_out_start", 5))
                        {
                            tickingSoundPlaying = true;
                            // SoundPoolManager.getInstance().playSound("TICKTOCK", false);
                        }
                    }
                }
            }
        }
    }

    private class SetNewCodeTask implements Runnable
    {
        private final String newCode;

        SetNewCodeTask(String newCode)
        {
            this.newCode = newCode;
        }

        @Override
        public void run()
        {
            codeBox.setText(newCode);
        }
    }
}
