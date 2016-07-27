package net.cryptodirect.authenticator;

import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import net.cryptodirect.authenticator.crypto.TOTP;

/**
 * The authenticator widget which displays the current TOTP code
 * and how much remaining time it is valid for indicated by a circular time
 * wheel {@code TimestepIntervalWheel}.
 */
public class AuthenticatorFragment extends Fragment
{
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle state)
    {
        View rootView = inflater.inflate(R.layout.fragment_authenticator, container, false);
        if (getArguments().getSerializable("account") == null)
        {
            throw new IllegalArgumentException("bundle: " + state + " did not have serializable " +
                    "\"account\"");
        }

        Account account = (Account) getArguments().getSerializable("account");
        TextView authenticatorBlurb = (TextView) rootView.findViewById(R.id.authenticator_blurb);
        authenticatorBlurb.setText(getResources().getString(R.string.authenticator_description,
                account.getIssuer().toString()));
        ((TextView) rootView.findViewById(R.id.account_label)).setText(account.getLabel());

        // Set up TimestepIntervalWheel with current time
        int ts = account.getCodeParams().getTotpPeriod();
        double tc = TOTP.getTC(ts);
        WindowManager windowManager = getActivity().getWindowManager();
        Rect displayRect = new Rect();
        windowManager.getDefaultDisplay().getRectSize(displayRect);

        RelativeLayout.LayoutParams intervalWheelLayoutParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        intervalWheelLayoutParams.addRule(RelativeLayout.BELOW, R.id.code_box);
        intervalWheelLayoutParams.setMargins(0, 10, 0, 0); // left, top, right, bottom
        intervalWheelLayoutParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        intervalWheelLayoutParams.width = displayRect.width();

        // Set initial codebox to initial TOTP token
        EditText codeBox = ((EditText) rootView.findViewById(R.id.code_box));
        codeBox.setText(TOTP.generateTOTP(account.getSecretKey(), (long) tc,
                account.getCodeParams().getDigits(), account.getCodeParams().getAlgorithm()));

        TimestepIntervalWheel timestepIntervalWheel = new TimestepIntervalWheel(getActivity(),
                account, codeBox, displayRect.width());
        ((RelativeLayout) rootView.findViewById(R.id.main_layout)).addView(timestepIntervalWheel,
                intervalWheelLayoutParams);
        return rootView;
    }
}
