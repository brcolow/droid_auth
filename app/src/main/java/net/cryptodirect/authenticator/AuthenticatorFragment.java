package net.cryptodirect.authenticator;

import android.os.Handler;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RelativeLayout;

import net.cryptodirect.authenticator.crypto.TOTP;

import org.joda.time.DateTime;

/**
 * A placeholder fragment containing a simple view.
 */
public class AuthenticatorFragment extends Fragment
{
    private static final Handler handler = new Handler();
    private static TimestepIntervalWheel timestepIntervalWheel;

    // Time-based One-time Password Algorithm (TOTP) Variables:
    // TS - Time Step (default 30 seconds)
    // TC = (unixtime(now) - unixtime(T0)) / TS
    private static final int TS = 30;
    private static double TC;
    // the user's api secret key, which is a ECDSA secp256k1 private key
    private static String SECRET_KEY = "Ne2WwwXegWzlHuD0eriSXx1EaxmQiEW8QMCRcn5RJ78=";

    public AuthenticatorFragment()
    {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        // Set up TimestepIntervalWheel with current time
        TC = TOTP.getTC(TS);
        // how far along we are percentage-wise of the cyclical range [0, TS], e.g. 0.25
        double fractionalPart = TC - (long) TC;
        // what second of the cyclical range [0, TS] we are in, e.g. TS * 0.25
        int currentSpotInTSInterval = (int) ((double) TS * fractionalPart);
        timestepIntervalWheel = new TimestepIntervalWheel(this.getActivity(), TS, TS - currentSpotInTSInterval);
        RelativeLayout.LayoutParams intervalWheelLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        intervalWheelLayoutParams.width = 500;
        intervalWheelLayoutParams.addRule(RelativeLayout.BELOW, R.id.code_box);
        intervalWheelLayoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
        ((RelativeLayout) rootView.findViewById(R.id.main_layout)).addView(timestepIntervalWheel, intervalWheelLayoutParams);

        // Set initial TOTP token value
        ((EditText) rootView.findViewById(R.id.code_box)).setText(TOTP.generateTOTPSha1(SECRET_KEY.getBytes(), TOTP.getTC(TS), 6));

        handler.post(new UpdateProgressTask(rootView));
        return rootView;
    }

    private class UpdateProgressTask implements Runnable
    {
        private final View rootView;

        UpdateProgressTask(View rootView)
        {
            this.rootView = rootView;
        }

        @Override
        public void run()
        {
            if (timestepIntervalWheel.decrementSecondsRemaining())
            {
                // we just entered a new cycle, so find new TC
                TC = TOTP.getTC(TS);
                // Update token display
                ((EditText) rootView.findViewById(R.id.code_box)).setText(TOTP.generateTOTPSha1(SECRET_KEY.getBytes(), (long) TC, 6));
            }
            handler.postDelayed(this, 1000);
        }
    }
}
