package net.cryptodirect.authenticator;

import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

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

        TextView emailField = (TextView) view.findViewById(R.id.email_field);
        // we set the email field to anonymous pro for consistency with key text field
        emailField.setTypeface(FontManager.getInstance().getTypeface("ANONYMOUS_PRO"));

        TextView keyTextField = (TextView) view.findViewById(R.id.key_text_field);
        keyTextField.setTypeface(FontManager.getInstance().getTypeface("ANONYMOUS_PRO"));

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
