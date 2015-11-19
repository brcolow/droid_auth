package net.cryptodirect.authenticator;

import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
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
public class AuthenticatorFragment extends Fragment implements SharedPreferences.OnSharedPreferenceChangeListener
{
    // time step (default is 30 seconds)
    private int ts;
    // String whose bytes (in ASCII) are used for the hmac
    private byte[] key;
    private boolean playTimeRunningOutSound;
    private int timeRunningOutStart;

    // the number of digits the final TOTP password should have
    private static final int NUM_DIGITS = 6;
    private final Handler handler = new Handler();
    private TimestepIntervalWheel timestepIntervalWheel;
    private volatile boolean tickingSoundPlaying = false;
    private final Timer timer = new Timer("Wheel Timer", true);
    private final WheelTask currentTask = new WheelTask();
    private EditText codeBox;
    private static final String TAG = AuthenticatorFragment.class.getSimpleName();

    public AuthenticatorFragment()
    {
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        timer.schedule(currentTask, 0, 1000);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle state)
    {
        View rootView = inflater.inflate(R.layout.fragment_authenticator, container, false);
        String email = getArguments().getString("email");
        key = getArguments().getByteArray("key");
        ts = getArguments().getInt("ts");
        playTimeRunningOutSound = getArguments().getBoolean("play_time_running_out_sound");
        timeRunningOutStart = getArguments().getInt("time_running_out_start");

        if (ts == 0)
        {
            Log.e(TAG, "AuthenticatorFragment was not passed ts argument, using default of 30!");
            ts = 30;
        }

        ((TextView) rootView.findViewById(R.id.email_label)).setText(email);

        // Set up TimestepIntervalWheel with current time
        double tc = TOTP.getTC(ts);
        // how far along we are percentage-wise of the cyclical range [0, TS], e.g. 0.25
        double fractionalPart = tc - (long) tc;
        // what second of the cyclical range [0, TS] we are in, e.g. TS * 0.25
        int currentSpotInTSInterval = (int) ((double) ts * fractionalPart);
        WindowManager windowManager = getActivity().getWindowManager();
        Rect displayRect = new Rect();
        windowManager.getDefaultDisplay().getRectSize(displayRect);

        RelativeLayout.LayoutParams intervalWheelLayoutParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        intervalWheelLayoutParams.addRule(RelativeLayout.BELOW, R.id.code_box);
        intervalWheelLayoutParams.setMargins(0, 10, 0, 0); // left, top, right, bottom
        intervalWheelLayoutParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        intervalWheelLayoutParams.width = displayRect.width();
        timestepIntervalWheel = new TimestepIntervalWheel(this.getActivity(), ts, ts - currentSpotInTSInterval, displayRect.width());
        ((RelativeLayout) rootView.findViewById(R.id.main_layout)).addView(timestepIntervalWheel, intervalWheelLayoutParams);

        // Set initial codebox to initial TOTP token
        codeBox = ((EditText) rootView.findViewById(R.id.code_box));
        codeBox.setText(TOTP.generateTOTPSha1(key, (long) tc, NUM_DIGITS));

        return rootView;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
    {
        if (key.equals("play_time_running_out_sound"))
        {
            playTimeRunningOutSound = sharedPreferences.getBoolean("play_time_running_out_sound", true);
        }
        else if (key.equals("time_running_out_start"))
        {
            timeRunningOutStart = sharedPreferences.getInt("time_running_out_start", 5);
        }
    }

    private class WheelTask extends TimerTask
    {
        @Override
        public void run()
        {
            if (timestepIntervalWheel == null)
            {
                return;
            }

            if (timestepIntervalWheel.decrementSecondsRemaining())
            {
                handler.post(new SetNewCodeTask(TOTP.generateTOTPSha1(key, TOTP.getTC(ts), NUM_DIGITS)));
                // SoundPoolManager.getInstance().stopSound("TICKTOCK");
                tickingSoundPlaying = false;
            }
            else
            {
                if (!tickingSoundPlaying)
                {
                    // TODO might need to cache this value and implement a prefchangedlistener
                    if (playTimeRunningOutSound)
                    {
                        if (timestepIntervalWheel.getSecondsRemainingInInterval() <= timeRunningOutStart)
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
