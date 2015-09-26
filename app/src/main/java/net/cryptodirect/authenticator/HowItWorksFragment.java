package net.cryptodirect.authenticator;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


public class HowItWorksFragment extends Fragment
{
    private static ViewPager pager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle state)
    {
        View view = inflater.inflate(R.layout.fragment_how_it_works, container, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        pager = (ViewPager) view.findViewById(R.id.view_pager);
    }

    public ViewPager getViewPager()
    {
        return pager;
    }
}
