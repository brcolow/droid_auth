package net.cryptodirect.authenticator;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import static net.cryptodirect.authenticator.Utils.MaterialDesignColors.MD_GREEN_300;
import static net.cryptodirect.authenticator.Utils.MaterialDesignColors.MD_RED_600;

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

        final EditText emailEditText = (EditText) view.findViewById(R.id.email_edit_text);
        // we set the email field to anonymous pro for consistency with key text field
        emailEditText.setTypeface(FontManager.getInstance().getTypeface("ANONYMOUS_PRO"));

        final EditText keyEditText = (EditText) view.findViewById(R.id.key_edit_text);
        keyEditText.setTypeface(FontManager.getInstance().getTypeface("ANONYMOUS_PRO"));

        // allow enter to be clicked when inside key edit text, and it hides keyboard
        emailEditText.setOnKeyListener(new View.OnKeyListener()
        {
            @Override
            public boolean onKey(View view, int keyCode, KeyEvent event)
            {
                if (keyCode == KeyEvent.KEYCODE_ENTER)
                {
                    keyEditText.requestFocus(View.FOCUS_DOWN);
                }
                return false;
            }
        });

        emailEditText.setOnEditorActionListener(new TextView.OnEditorActionListener()
        {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent event)
            {
                if (actionId == EditorInfo.IME_ACTION_DONE)
                {
                    keyEditText.requestFocus(View.FOCUS_DOWN);
                }
                return false;
            }
        });


        keyEditText.setOnEditorActionListener(new TextView.OnEditorActionListener()
        {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent event)
            {
                if (actionId == EditorInfo.IME_ACTION_DONE)
                {
                    InputMethodManager inputMethodManager = (InputMethodManager) ManualEntryFragment.this.getActivity()
                            .getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputMethodManager.hideSoftInputFromWindow(keyEditText.getWindowToken(), 0);
                }
                return false;
            }
        });

        keyEditText.setOnKeyListener(new View.OnKeyListener()
        {
            @Override
            public boolean onKey(View view, int keyCode, KeyEvent event)
            {
                if (keyCode == KeyEvent.KEYCODE_ENTER)
                {
                    InputMethodManager inputMethodManager =
                            (InputMethodManager) ManualEntryFragment.this.getActivity()
                            .getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputMethodManager.hideSoftInputFromWindow(keyEditText.getWindowToken(), 0);
                }
                return false;
            }
        });

        // on-the-fly key input validation
        keyEditText.addTextChangedListener(new TextWatcher()
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
                if (TextUtils.isEmpty(keyEditText.getText()))
                {
                    keyEditText.setError(null);
                    keyEditText.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
                    keyEditText.forceLayout();
                }
                else
                {
                    String errorMessage = null;
                    if (!Utils.VALID_BASE64_CHARS_REGEX.matcher(keyEditText.getText()).matches())
                    {
                        errorMessage = getResources().getString(R.string.invalid_char_key);
                    }
                    else
                    {
                        if (keyEditText.length() >= 2 && keyEditText.getText().toString().substring(
                                0, keyEditText.length() - 1).contains("="))
                        {
                            errorMessage = getResources().getString(R.string.key_equals_misplaced);
                        }
                    }

                    if (errorMessage != null)
                    {
                        // whats been entered for the key so far is invalid
                        Drawable errorDrawable = ContextCompat.getDrawable(
                                getActivity().getBaseContext(), R.drawable.ic_close_white_24dp);
                        errorDrawable.setBounds(0, 0, errorDrawable.getIntrinsicWidth(),
                                errorDrawable.getIntrinsicHeight());
                        errorDrawable.setColorFilter(MD_RED_600.getColor(), PorterDuff.Mode.SRC_IN);
                        keyEditText.setError(errorMessage, errorDrawable);
                        keyEditText.forceLayout();
                    }
                    else
                    {
                        // key is so-far valid
                        keyEditText.setError(null);
                        keyEditText.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
                        keyEditText.forceLayout();
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
        okayButton.getCompoundDrawables()[0].setColorFilter(MD_GREEN_300.getColor(),
                PorterDuff.Mode.SRC_IN);
    }
}
