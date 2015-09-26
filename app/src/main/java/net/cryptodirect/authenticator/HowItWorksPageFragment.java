package net.cryptodirect.authenticator;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import me.grantland.widget.AutofitTextView;

public class HowItWorksPageFragment extends Fragment
{
    private int backgroundColor;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_how_it_works_page, container, false);

        view.setId(getArguments().getInt("id"));

        backgroundColor = Color.parseColor(getArguments().getString("backgroundColor"));
        view.setBackgroundColor(backgroundColor);

        ImageView imageView = (ImageView) view.findViewById(R.id.how_it_works_page_image);
        imageView.setImageDrawable(ContextCompat.getDrawable(getActivity().getBaseContext(),
                getArguments().getInt("drawable", R.drawable.ic_keyboard_white_36dp)));

        AutofitTextView textView = (AutofitTextView) view.findViewById(R.id.how_it_works_page_text);
        textView.setText(getArguments().getString("blurb"));

        return view;
    }

    public static HowItWorksPageFragment newInstance(int id, String backgroundColorString,
                                                     int drawable, String text)
    {

        HowItWorksPageFragment howItWorksPageFragment = new HowItWorksPageFragment();
        Bundle bundle = new Bundle();

        bundle.putInt("id", id);
        bundle.putString("backgroundColor", backgroundColorString);
        bundle.putInt("drawable", drawable);
        bundle.putString("blurb", text);

        howItWorksPageFragment.setArguments(bundle);

        return howItWorksPageFragment;
    }

    public int getBackgroundColor()
    {
        return backgroundColor;
    }
}