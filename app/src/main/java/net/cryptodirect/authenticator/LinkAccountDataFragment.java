package net.cryptodirect.authenticator;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import net.cryptodirect.authenticator.crypto.Base32;
import net.cryptodirect.authenticator.crypto.Base64;

import static android.support.v4.content.ContextCompat.getDrawable;
import static net.cryptodirect.authenticator.Utils.MaterialDesignColors.MD_GREEN_300;
import static net.cryptodirect.authenticator.Utils.MaterialDesignColors.MD_RED_300;

/**
 * Takes the account key data that was obtained from either
 * scanning a QR code (in ScanQRCodeFragment) or manually
 * entering a key (in ManualEntryFragment) and saves that
 * account key with its associated account data.
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

        String linkMethodString = getArguments().getString("method");
        if (linkMethodString == null)
        {
            throw new IllegalArgumentException("bundle: " + state + " does not contain " +
                    "String \"method\"");
        }

        final LinkMethod linkMethod = LinkMethod.valueOf(linkMethodString);
        final String label = getArguments().getString("new_label");
        final byte[] rawKey = getArguments().getByteArray("new_key");

        int issuerId = getArguments().getInt("new_issuer", -999);
        if (issuerId == -999)
        {
            throw new IllegalArgumentException("bundle: " + state + " does not contain " +
                    "int \"new_issuer\"");
        }

        final Issuer issuer = Issuer.getIssuer(issuerId);
        final int base = getArguments().getInt("new_base");

        if (label == null)
        {
            throw new IllegalArgumentException("bundle: " + state + " does not contain " +
                    "String \"new_email\"");
        }
        if (rawKey == null)
        {
            throw new IllegalArgumentException("bundle: " + state + " does not contain " +
                    "byte-array \"new_key\"");
        }
        if (issuer == null)
        {
            throw new IllegalArgumentException("bundle: " + state + " does not contain " +
                    "int \"new_issuer\"");
        }
        if (base == 0)
        {
            throw new IllegalArgumentException("bundle: " + state + " does not contain " +
                    "int \"new_base\"");
        }

        TextView verifyBlurb = (TextView) view.findViewById(R.id.verify_blurb);
        switch (linkMethod)
        {
            case MANUAL_ENTRY:
                verifyBlurb.setText(R.string.verify_manual_blurb);
                break;
            case QRCODE:
                verifyBlurb.setText(R.string.verify_qrcode_blurb);
                break;
        }

        EditText providerEditText = (EditText) view.findViewById(R.id.account_provider_edittext);
        providerEditText.setTypeface(FontManager.getInstance().getTypeface("ANONYMOUS_PRO"));
        Drawable providerDrawable = getDrawable(getContext(), issuer.getDrawable());
        Bitmap providerBitmap = ((BitmapDrawable) providerDrawable).getBitmap();
        Drawable scaledDrawable = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(
                providerBitmap, 100, 100, true));
        providerEditText.setText(issuer.toString());
        providerEditText.setCompoundDrawablesWithIntrinsicBounds(scaledDrawable, null, null, null);

        TextView accountLabel = (TextView) view.findViewById(R.id.account_label_textview);
        accountLabel.setText(issuer.getLabel());
        EditText accountLabelTextField = (EditText) view.findViewById(R.id.account_label_edittext);
        accountLabelTextField.setTypeface(FontManager.getInstance().getTypeface("ANONYMOUS_PRO"));
        accountLabelTextField.setText(issuer.getDisplayableLabel(label));

        TextView keyTextField = (TextView) view.findViewById(R.id.key_edit_text);
        keyTextField.setTypeface(FontManager.getInstance().getTypeface("ANONYMOUS_PRO"));
        final String key;

        switch (base)
        {
            case 32:
                key = Base32.getEncoder().encode(rawKey);
                keyTextField.setMinLines(1);
                keyTextField.setMaxLines(1);
                break;
            case 64:
                key = Base64.getEncoder().encodeToString(rawKey);
                keyTextField.setMaxLines(2);
                break;
            default:
                throw new IllegalArgumentException("unsupported base: " + base);
        }

        keyTextField.setText(key);

        boolean keyValid = true;
        if (base == 64)
        {
            if (TextUtils.isEmpty(key) || !Utils.BASE64_KEY_REGEX.matcher(key).matches() ||
                    key.length() != 44)
            {
                // base64 key is invalid
                keyValid = false;
            }
        }
        else
        {
            if (TextUtils.isEmpty(key) || !Utils.BASE32_KEY_REGEX.matcher(key).matches())
            {
                // base32 key is invalid
                keyValid = false;
            }
        }

        if (!keyValid)
        {
            Drawable errorDrawable = ContextCompat.getDrawable(getActivity().getBaseContext(),
                    R.drawable.ic_close_white_24dp);
            errorDrawable.setBounds(0, 0, errorDrawable.getIntrinsicWidth(),
                    errorDrawable.getIntrinsicHeight());
            errorDrawable.setColorFilter(Utils.MaterialDesignColors.MD_RED_600.getColor(),
                    PorterDuff.Mode.SRC_IN);
            keyTextField.setError(getResources().getString(R.string.invalid_key), errorDrawable);
        }
        else
        {
            Drawable validDrawable = ContextCompat.getDrawable(getActivity().getBaseContext(),
                    R.drawable.ic_check_white_24dp);
            validDrawable.setBounds(0, 0, validDrawable.getIntrinsicWidth(),
                    validDrawable.getIntrinsicHeight());
            validDrawable.setColorFilter(Utils.MaterialDesignColors.MD_GREEN_500.getColor(),
                    PorterDuff.Mode.SRC_IN);
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

        correctButton.getCompoundDrawables()[0].setColorFilter(MD_GREEN_300.getColor(),
                PorterDuff.Mode.SRC_IN);
        incorrectButton.getCompoundDrawables()[0].setColorFilter(MD_RED_300.getColor(),
                PorterDuff.Mode.SRC_IN);
    }
}
