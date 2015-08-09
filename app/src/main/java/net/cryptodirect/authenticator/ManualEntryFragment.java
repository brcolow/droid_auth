package net.cryptodirect.authenticator;

import android.graphics.Typeface;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * A simple {@link Fragment} subclass.
 */
public class ManualEntryFragment extends Fragment
{
    private static final String KEY_FONT_PATH = "fonts/Anonymous_Pro.ttf";

    public ManualEntryFragment()
    {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle state)
    {
        View view = inflater.inflate(R.layout.fragment_manual_entry, container, false);

        Typeface anonymousProTypeface = Typeface.createFromAsset(getActivity().getAssets(), KEY_FONT_PATH);

        TextView emailField = (TextView) view.findViewById(R.id.email_field);
        // we set the email field to anonymous pro for consistency with key text field
        emailField.setTypeface(anonymousProTypeface);

        TextView keyTextField = (TextView) view.findViewById(R.id.key_text_field);
        keyTextField.setTypeface(anonymousProTypeface);

        return view;
    }

}
