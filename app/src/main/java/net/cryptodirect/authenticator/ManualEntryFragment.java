package net.cryptodirect.authenticator;

import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Allows to the user to register an account by manually
 * entering their email and key. Users would probably only
 * use this if they are uncomfortable with QR codes (which
 * should be alleviated by proper documentation) or if
 * they have a broken camera.
 */
public class ManualEntryFragment extends Fragment
{
    public ManualEntryFragment()
    {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle state)
    {
        View view = inflater.inflate(R.layout.fragment_manual_entry, container, false);

        final TextView emailField = (TextView) view.findViewById(R.id.email_field);
        // we set the email field to anonymous pro for consistency with key text field
        emailField.setTypeface(FontManager.getInstance().getTypeface("ANONYMOUS_PRO"));

        final TextView keyTextField = (TextView) view.findViewById(R.id.key_text_field);
        keyTextField.setTypeface(FontManager.getInstance().getTypeface("ANONYMOUS_PRO"));

        // input validation
        emailField.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {
            }

            @Override
            public void afterTextChanged(Editable s)
            {
                if (TextUtils.isEmpty(emailField.getText())
                        || !android.util.Patterns.EMAIL_ADDRESS.matcher(emailField.getText()).matches())
                {
                    // validation failed - add red checkmark
                    Drawable errorDrawable = ContextCompat.getDrawable(getActivity().getBaseContext(), R.drawable.ic_close_white_24dp);
                    errorDrawable.setBounds(0, 0, errorDrawable.getIntrinsicWidth(), errorDrawable.getIntrinsicHeight());
                    errorDrawable.setColorFilter(Utils.getIntFromRGB(229, 57, 53), PorterDuff.Mode.SRC_IN);
                    emailField.setError("Invalid Email", errorDrawable);
                    emailField.setError(getResources().getString(R.string.invalid_email));
                    emailField.setCompoundDrawables(null, null, null, null);
                }
                else
                {
                    // validation successful - add green checkmark
                    emailField.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_check_white_24dp, 0);
                    emailField.getCompoundDrawables()[2].setColorFilter(Utils.getIntFromRGB(76, 175, 80), PorterDuff.Mode.SRC_IN);
                }
            }
        });

        final Pattern keyPattern = Pattern.compile("^[a-zA-Z0-9+\\/]+=$");

        keyTextField.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (TextUtils.isEmpty(keyTextField.getText())
                        || !keyPattern.matcher(keyTextField.getText()).matches())
                {
                    // validation failed - add red checkmark
                    Drawable errorDrawable = ContextCompat.getDrawable(getActivity().getBaseContext(), R.drawable.ic_close_white_24dp);
                    errorDrawable.setBounds(0, 0, errorDrawable.getIntrinsicWidth(), errorDrawable.getIntrinsicHeight());
                    errorDrawable.setColorFilter(Utils.getIntFromRGB(229, 57, 53), PorterDuff.Mode.SRC_IN);
                    keyTextField.setError("Invalid Key", errorDrawable);
                    keyTextField.setError(getResources().getString(R.string.invalid_key));
                    keyTextField.setCompoundDrawables(null, null, null, null);
                }
                else
                {
                    // validation successful - add green checkmark
                    keyTextField.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_check_white_24dp, 0);
                    keyTextField.getCompoundDrawables()[2].setColorFilter(Utils.getIntFromRGB(76, 175, 80), PorterDuff.Mode.SRC_IN);
                }
            }
        });
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        Button okayButton = (Button) view.findViewById(R.id.okay_button);
        okayButton.getCompoundDrawables()[0].setColorFilter(Utils.getIntFromRGB(46, 125, 50), PorterDuff.Mode.SRC_IN);
    }
}
