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
import android.widget.EditText;

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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle state)
    {
        View view = inflater.inflate(R.layout.fragment_manual_entry, container, false);

        final EditText emailTextField = (EditText) view.findViewById(R.id.email_field);
        // we set the email field to anonymous pro for consistency with key text field
        emailTextField.setTypeface(FontManager.getInstance().getTypeface("ANONYMOUS_PRO"));

        final EditText keyTextField = (EditText) view.findViewById(R.id.key_text_field);
        keyTextField.setTypeface(FontManager.getInstance().getTypeface("ANONYMOUS_PRO"));

        // input validation
        emailTextField.addTextChangedListener(new TextWatcher()
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
                if (TextUtils.isEmpty(emailTextField.getText())
                        || !android.util.Patterns.EMAIL_ADDRESS.matcher(emailTextField.getText()).matches())
                {
                    // validation failed - add red checkmark
                    Drawable errorDrawable = ContextCompat.getDrawable(getActivity().getBaseContext(), R.drawable.ic_close_white_24dp);
                    errorDrawable.setBounds(0, 0, errorDrawable.getIntrinsicWidth(), errorDrawable.getIntrinsicHeight());
                    errorDrawable.setColorFilter(Utils.MaterialDesignColors.MD_RED_600.getColor(), PorterDuff.Mode.SRC_IN);
                    emailTextField.setError(getResources().getString(R.string.invalid_email), errorDrawable);
                    emailTextField.forceLayout();
                }
                else
                {
                    // validation successful - add green checkmark
                    emailTextField.setError(null);
                    Drawable validDrawable = ContextCompat.getDrawable(getActivity().getBaseContext(), R.drawable.ic_check_white_24dp);
                    validDrawable.setBounds(0, 0, validDrawable.getIntrinsicWidth(), validDrawable.getIntrinsicHeight());
                    validDrawable.setColorFilter(Utils.MaterialDesignColors.MD_GREEN_500.getColor(), PorterDuff.Mode.SRC_IN);
                    emailTextField.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
                    emailTextField.setCompoundDrawablesWithIntrinsicBounds(null, null, validDrawable, null);
                    emailTextField.forceLayout();
                }
            }
        });

        final Pattern keyPattern = Pattern.compile("^[a-zA-Z0-9+\\/]+=$");

        keyTextField.addTextChangedListener(new TextWatcher()
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
                if (TextUtils.isEmpty(keyTextField.getText())
                        || !keyPattern.matcher(keyTextField.getText()).matches()
                        || keyTextField.getText().length() != 44)
                {
                    // validation failed - add red checkmark
                    Drawable errorDrawable = ContextCompat.getDrawable(getActivity().getBaseContext(), R.drawable.ic_close_white_24dp);
                    errorDrawable.setBounds(0, 0, errorDrawable.getIntrinsicWidth(), errorDrawable.getIntrinsicHeight());
                    errorDrawable.setColorFilter(Utils.MaterialDesignColors.MD_RED_600.getColor(), PorterDuff.Mode.SRC_IN);
                    keyTextField.setError(getResources().getString(R.string.invalid_key), errorDrawable);
                    keyTextField.forceLayout();
                }
                else
                {
                    // validation successful - add green checkmark
                    keyTextField.setError(null);
                    Drawable validDrawable = ContextCompat.getDrawable(getActivity().getBaseContext(), R.drawable.ic_check_white_24dp);
                    validDrawable.setBounds(0, 0, validDrawable.getIntrinsicWidth(), validDrawable.getIntrinsicHeight());
                    validDrawable.setColorFilter(Utils.MaterialDesignColors.MD_GREEN_500.getColor(), PorterDuff.Mode.SRC_IN);
                    keyTextField.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
                    keyTextField.setCompoundDrawablesWithIntrinsicBounds(null, null, validDrawable, null);
                    keyTextField.forceLayout();
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
        okayButton.getCompoundDrawables()[0].setColorFilter(Utils.MaterialDesignColors.MD_GREEN_800.getColor(), PorterDuff.Mode.SRC_IN);
    }

}