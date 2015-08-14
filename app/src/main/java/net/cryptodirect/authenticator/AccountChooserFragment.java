package net.cryptodirect.authenticator;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

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
        View view = inflater.inflate(R.layout.fragment_account_chooser, container, false);
        final ListView accountsListView = (ListView) view.findViewById(R.id.accounts_list);

        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<>(getActivity().getApplicationContext(),
                android.R.layout.simple_list_item_1, android.R.id.text1, AccountManager.getInstance().getAccountEmails());

        accountsListView.setAdapter(adapter);

        accountsListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                CharSequence accountEmail = (CharSequence) accountsListView.getItemAtPosition(position);
                listener.onAccountChosen(accountEmail.toString());
            }
        });

        return view;
    }

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        try
        {
            listener = (OnAccountChosenListener) activity;
        }
        catch (ClassCastException e)
        {
            throw new ClassCastException(activity.toString() + " must implement OnAccountChosenListener");
        }
    }

    public interface OnAccountChosenListener
    {
        void onAccountChosen(String chosenAccount);
    }
}
