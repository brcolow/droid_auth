package net.cryptodirect.authenticator;

import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

/**
 * A simple {@link Fragment} subclass.
 */
public class SelectRegisterMethodFragment extends Fragment
{
    private Button scanQRCodeButton;
    private Button manualEntryButton;

    public SelectRegisterMethodFragment()
    {
        // Required empty public constructor
    }

    public Button getScanRCodeButton()
    {
        return scanQRCodeButton;
    }

    public Button getManualEntryButton()
    {
        return manualEntryButton;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_select_register_method, container, false);
        scanQRCodeButton = (Button) view.findViewById(R.id.scan_qr_code_button);
        manualEntryButton = (Button) view.findViewById(R.id.manual_entry_button);
        return view;
    }

}
