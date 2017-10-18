package net.cryptodirect.authenticator;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

/**
 * Displays information about this application in a web view.
 * The content corresponds to about.html in /assets.
 */
public class AboutFragment extends Fragment
{
    private WebView webView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle state)
    {
        View view = inflater.inflate(R.layout.fragment_about, container, false);
        webView = view.findViewById(R.id.aboutWebView);
        webView.loadUrl("file:///android_asset/about.html");
        return view;
    }

    public WebView getWebView()
    {
        return webView;
    }
}
