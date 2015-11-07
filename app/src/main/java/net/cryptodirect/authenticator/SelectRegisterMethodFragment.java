package net.cryptodirect.authenticator;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Allows the user to select either the Scan QR Code or
 * Manual Entry methods of registering an account.
 */
public class SelectRegisterMethodFragment extends Fragment
{

    public SelectRegisterMethodFragment()
    {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle state)
    {
        return inflater.inflate(R.layout.fragment_select_register_method, container, false);
    }

}
