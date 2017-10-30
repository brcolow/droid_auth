package net.cryptodirect.authenticator;

import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v13.app.FragmentPagerAdapter;

public class HowItWorksPagerAdapter extends FragmentPagerAdapter
{
    public HowItWorksPagerAdapter(FragmentManager fragmentManager)
    {
        super(fragmentManager);
    }

    @Override
    public Fragment getItem(int position)
    {
        return MainActivity.getHelpPages().get(position);
    }

    @Override
    public int getCount()
    {
        return 3;
    }
}
