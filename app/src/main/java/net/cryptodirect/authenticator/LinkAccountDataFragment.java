package net.cryptodirect.authenticator;

import android.app.Activity;
import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
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
        View view = inflater.inflate(R.layout.fragment_link_account_data, container, false);

        final String email = getArguments().getString("new_email");
        final String key = getArguments().getString("new_key");

        if (email == null)
        {
            throw new IllegalArgumentException("LinkAccountDataFragment was not given decoded email");
        }
        if (key == null)
        {
            throw new IllegalArgumentException("LinkAccountDataFragment was not given decoded key");
        }

        TextView emailTextField = (TextView) view.findViewById(R.id.email_edit_text);
        // we set the email field to anonymous pro for consistency with key text field
        emailTextField.setTypeface(FontManager.getInstance().getTypeface("ANONYMOUS_PRO"));
        emailTextField.setText(email);

        TextView keyTextField = (TextView) view.findViewById(R.id.key_edit_text);
        keyTextField.setTypeface(FontManager.getInstance().getTypeface("ANONYMOUS_PRO"));
        keyTextField.setText(key);

        if (TextUtils.isEmpty(key) || !Utils.KEY_REGEX.matcher(key).matches() || key.length() != 44)
        {
            // key is invalid
            Drawable errorDrawable = ContextCompat.getDrawable(getActivity().getBaseContext(), R.drawable.ic_close_white_24dp);
            errorDrawable.setBounds(0, 0, errorDrawable.getIntrinsicWidth(), errorDrawable.getIntrinsicHeight());
            errorDrawable.setColorFilter(Utils.MaterialDesignColors.MD_RED_600.getColor(), PorterDuff.Mode.SRC_IN);
            keyTextField.setError(getResources().getString(R.string.invalid_key), errorDrawable);
        }
        else
        {
            Drawable validDrawable = ContextCompat.getDrawable(getActivity().getBaseContext(), R.drawable.ic_check_white_24dp);
            validDrawable.setBounds(0, 0, validDrawable.getIntrinsicWidth(), validDrawable.getIntrinsicHeight());
            validDrawable.setColorFilter(Utils.MaterialDesignColors.MD_GREEN_500.getColor(), PorterDuff.Mode.SRC_IN);
            keyTextField.setCompoundDrawablesWithIntrinsicBounds(null, null, validDrawable, null);
        }
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        Button correctButton = (Button) view.findViewById(R.id.correct_button);
        Button incorrectButton = (Button) view.findViewById(R.id.incorrect_button);

        correctButton.getCompoundDrawables()[0].setColorFilter(Utils.MaterialDesignColors.MD_GREEN_300.getColor(), PorterDuff.Mode.SRC_IN);
        incorrectButton.getCompoundDrawables()[0].setColorFilter(Utils.MaterialDesignColors.MD_RED_300.getColor(), PorterDuff.Mode.SRC_IN);
    }
}
