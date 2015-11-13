package net.cryptodirect.authenticator;

import android.app.Activity;
import android.content.Context;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
public class LinkAccountDataFragment extends Fragment
{
    // we display the key in Base64 - so we need a good font for this purpose
    // i.e. O and 0 are distinguished from each other

    public LinkAccountDataFragment()
    {

    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        getActivity().invalidateOptionsMenu();
        setHasOptionsMenu(false);
    }

    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);

        if (context instanceof Activity)
        {
            ((Activity) context).invalidateOptionsMenu();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle state)
    {
        getActivity().invalidateOptionsMenu();
        setHasOptionsMenu(false);
        View view = inflater.inflate(R.layout.fragment_register_account_data, container, false);

        if (!getArguments().containsKey("new_email"))
        {
            throw new IllegalArgumentException("LinkAccountDataFragment was not given decoded email");
        }

        if (!getArguments().containsKey("new_key"))
        {
            throw new IllegalArgumentException("LinkAccountDataFragment was not given decoded key");
        }
        TextView emailTextField = (TextView) view.findViewById(R.id.email_field);
        // we set the email field to anonymous pro for consistency with key text field
        emailTextField.setTypeface(FontManager.getInstance().getTypeface("ANONYMOUS_PRO"));
        emailTextField.setText(getArguments().getString("new_email"));

        TextView keyTextField = (TextView) view.findViewById(R.id.key_text_field);
        keyTextField.setTypeface(FontManager.getInstance().getTypeface("ANONYMOUS_PRO"));
        keyTextField.setText(getArguments().getString("new_key"));

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        Button correctButton = (Button) view.findViewById(R.id.correct_button);
        Button incorrectButton = (Button) view.findViewById(R.id.incorrect_button);

        correctButton.getCompoundDrawables()[0].setColorFilter(Utils.MaterialDesignColors.MD_GREEN_800.getColor(), PorterDuff.Mode.SRC_IN);
        incorrectButton.getCompoundDrawables()[0].setColorFilter(Utils.MaterialDesignColors.MD_RED_600.getColor(), PorterDuff.Mode.SRC_IN);
    }
}
