package net.cryptodirect.authenticator;

import android.app.Fragment;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

/**
 * Takes the account key data that was obtained from either
 * scanning a QR code (in ScanQRCodeFragment) or manually
 * entering a key (in ManualEntryFragment) and saves that
 * account key with an associated email address in internal
 * storage so that the account data is persisted.
 */
public class RegisterAccountDataFragment extends Fragment
{
    // we display the key in Base64 - so we need a good font for this purpose
    // i.e. O and 0 are distinguished from each other
    private static final String KEY_FONT_PATH = "fonts/Anonymous_Pro.ttf";

    public RegisterAccountDataFragment()
    {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle state)
    {
        View view = inflater.inflate(R.layout.fragment_register_account_data, container, false);

        if (!getArguments().containsKey("decoded_key"))
        {
            throw new IllegalArgumentException("RegisterAccountDataFragment was not given decoded key");
        }
        if (!getArguments().containsKey("decoded_email"))
        {
            throw new IllegalArgumentException("RegisterAccountDataFragment was not given decoded email");
        }

        Typeface anonymousProTypeface = Typeface.createFromAsset(getActivity().getAssets(), KEY_FONT_PATH);
        TextView emailField = (TextView) view.findViewById(R.id.email_field);
        // we set the email field to anonymous pro for consistency with key text field
        emailField.setTypeface(anonymousProTypeface);
        emailField.setText(getArguments().getString("decoded_email"));

        TextView keyTextField = (TextView) view.findViewById(R.id.key_text_field);
        keyTextField.setTypeface(anonymousProTypeface);
        keyTextField.setText(getArguments().getString("decoded_key"));

        Button correctButton = (Button) view.findViewById(R.id.correct_button);
        Button incorrectButton = (Button) view.findViewById(R.id.incorrect_button);


        return view;
    }
}
