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

        // on-the -fly key input validation
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
                if (TextUtils.isEmpty(keyTextField.getText()))
                {
                    keyTextField.setError(null);
                    keyTextField.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
                    keyTextField.forceLayout();
                }
                else
                {
                    String errorMessage = null;
                    if (!Utils.BASE64_VALID_REGEX.matcher(keyTextField.getText()).matches())
                    {
                        errorMessage = getResources().getString(R.string.invalid_char_key);
                    }
                    else
                    {
                        if (keyTextField.length() >= 2 && keyTextField.getText().toString().substring(0, keyTextField.length() - 1).contains("="))
                            {
                            errorMessage = getResources().getString(R.string.key_equals_misplaced);
                        }
                    }

                    if (errorMessage != null)
                    {
                        // whats been entered for the key so far is invalid
                        Drawable errorDrawable = ContextCompat.getDrawable(getActivity().getBaseContext(), R.drawable.ic_close_white_24dp);
                        errorDrawable.setBounds(0, 0, errorDrawable.getIntrinsicWidth(), errorDrawable.getIntrinsicHeight());
                        errorDrawable.setColorFilter(Utils.MaterialDesignColors.MD_RED_600.getColor(), PorterDuff.Mode.SRC_IN);
                        keyTextField.setError(errorMessage, errorDrawable);
                        keyTextField.forceLayout();
                    }
                    else
                    {
                        // key is so-far valid
                        keyTextField.setError(null);
                        keyTextField.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
                        keyTextField.forceLayout();
                    }
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
        okayButton.getCompoundDrawables()[0].setColorFilter(Utils.MaterialDesignColors.MD_GREEN_300.getColor(), PorterDuff.Mode.SRC_IN);
    }
}