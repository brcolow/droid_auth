package net.cryptodirect.authenticator;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.acra.ACRA;

import java.util.ArrayList;
import java.util.Locale;

import static android.graphics.Bitmap.createScaledBitmap;
import static android.support.v4.content.ContextCompat.getDrawable;

/**
 * If the user has more than one account registered, this Fragment
 * allows the user to select which account to display the authenticator
 * widget for. This can always be bypassed by setting a default account.
 */
public class AccountChooserFragment extends Fragment
{
    private OnAccountChosenListener listener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle state)
    {
        CharSequence[] accountLabels = getArguments().getCharSequenceArray("accountLabels");
        if (accountLabels == null)
        {
            throw new IllegalArgumentException("bundle: " + state + " did not have CharSequence " +
                    "array \"accountLabels\"");
        }
        View view = inflater.inflate(R.layout.fragment_account_chooser, container, false);
        final ListView accountsListView = view.findViewById(R.id.accounts_list);

        final ArrayAdapter<Account> adapter = new ArrayAdapter<Account>(getActivity().getApplicationContext(),
                android.R.layout.simple_list_item_1, android.R.id.text1,
                new ArrayList<>(AccountManager.getInstance().getAccounts()))
        {
            @Override
            public View getView(int position, View convertView, ViewGroup parent)
            {
                Account account = getItem(position);
                RelativeLayout listItem = new RelativeLayout(getContext());
                listItem.setPadding(0, 10, 0, 10);
                LinearLayout.LayoutParams listItemLayoutParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT);
                listItem.setLayoutParams(listItemLayoutParams);

                TextView issuerTextView = new TextView(getActivity());
                issuerTextView.setId(Utils.generateViewId());
                issuerTextView.setText(account.getIssuer().toString());
                issuerTextView.setTextSize(17);
                issuerTextView.setGravity(Gravity.CENTER_VERTICAL);
                Drawable providerDrawable = getDrawable(getContext(), Issuer.valueOf(
                        issuerTextView.getText().toString().toUpperCase(Locale.US)).getDrawable());
                Bitmap providerBitmap = ((BitmapDrawable) providerDrawable).getBitmap();
                Drawable scaledDrawable = new BitmapDrawable(getResources(), createScaledBitmap(
                        providerBitmap, 100, 100, true));
                issuerTextView.setCompoundDrawablePadding(25);
                issuerTextView.setPadding(0, 0, 100, 0);
                issuerTextView.setCompoundDrawablesWithIntrinsicBounds(scaledDrawable, null, null, null);
                issuerTextView.setTextColor(Color.argb(255, 242, 242, 242));
                RelativeLayout.LayoutParams issuerLayoutParams = new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                issuerLayoutParams.setMargins(0, 0, 50, 0);
                issuerLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                issuerTextView.setLayoutParams(issuerLayoutParams);

                TextView codeTextView = new TextView(getActivity());
                codeTextView.setId(Utils.generateViewId());
                codeTextView.setText(account.getOneTimePasscode());
                codeTextView.setTextSize(17);
                codeTextView.setGravity(Gravity.CENTER);
                codeTextView.setTextColor(Color.argb(255, 242, 242, 242));
                RelativeLayout.LayoutParams codeLayoutParams = new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                codeTextView.setPadding(100, 0, 0, 0);
                codeLayoutParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
                codeTextView.setLayoutParams(codeLayoutParams);

                TimestepIntervalWheel timestepIntervalWheel = new TimestepIntervalWheel(
                        getActivity(), account, codeTextView, 120);

                timestepIntervalWheel.setPadding(20, 0, 0, 0);
                RelativeLayout.LayoutParams wheelLayoutParams = new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                wheelLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                wheelLayoutParams.width = 124;
                wheelLayoutParams.height = 124;
                wheelLayoutParams.setMargins(30, 0, 30, 0);
                timestepIntervalWheel.setLayoutParams(wheelLayoutParams);

                listItem.addView(issuerTextView, issuerLayoutParams);
                listItem.addView(codeTextView, codeLayoutParams);
                listItem.addView(timestepIntervalWheel, wheelLayoutParams);
                return listItem;
            }
        };

        accountsListView.setAdapter(adapter);
        accountsListView.setOnItemClickListener((parent, view1, position, id) ->
                listener.onAccountChosen(((Account) accountsListView.getItemAtPosition(position)).getIssuer().name()));

        accountsListView.setEmptyView(view.findViewById(R.id.empty_list_view_layout));
        return view;
    }

    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);
        try
        {
            listener = (OnAccountChosenListener) context;
        }
        catch (ClassCastException e)
        {
            ACRA.getErrorReporter().handleException(e);
        }
    }

    public interface OnAccountChosenListener
    {
        void onAccountChosen(String chosenAccount);
    }
}
