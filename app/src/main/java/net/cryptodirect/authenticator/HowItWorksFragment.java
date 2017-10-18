package net.cryptodirect.authenticator;

import android.app.Fragment;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class HowItWorksFragment extends Fragment
{
    private static ViewPagerCustomDuration pager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle state)
    {
        return inflater.inflate(R.layout.fragment_how_it_works, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        pager = view.findViewById(R.id.view_pager);

        if (android.os.Build.VERSION.SDK_INT >= 17)
        {
            // using child fragment manager here makes it so that when one
            // clicks on "How it Works", then clicks back button, then clicks
            // "How it Works" again, the how it works view pager works correctly.
            // Without child fragment manager, the view pager only works the first
            // time it is launched from the button click.
            pager.setAdapter(new HowItWorksPagerAdapter(getChildFragmentManager()));
        }
        else
        {
            pager.setAdapter(new HowItWorksPagerAdapter(getFragmentManager()));
        }

        pager.setScrollDurationFactor(10);
    }

    public ViewPager getViewPager()
    {
        return pager;
    }
}
