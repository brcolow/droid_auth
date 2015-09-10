package net.cryptodirect.authenticator;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

/**
 * Allows the user to test their device by running TOTP
 * test vectors and testing clock drift between Centurion
 * authentication servers and their device. This will help
 * provide us with more information when bug reports arise.
 */
public class TestDeviceFragment extends Fragment
{
    private static final String TAG = TestDeviceFragment.class.getSimpleName();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle state)
    {
        View view = inflater.inflate(R.layout.fragment_test_device, container, false);
        final Button button = (Button) view.findViewById(R.id.begin_tests_button);
        button.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                String timeResponse = null;
                try
                {
                    URL url = new URL("https://cryptodash.net:4463/time");
                    HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
                    try
                    {
                        BufferedReader in = new BufferedReader(new InputStreamReader(
                                        urlConnection.getInputStream()));

                        String inputLine;
                        StringBuilder timeResponseBuilder = new StringBuilder();
                        while ((inputLine = in.readLine()) != null)
                        {
                            timeResponseBuilder.append(inputLine);
                        }

                        timeResponse = timeResponseBuilder.toString();
                    }
                    finally
                    {
                        urlConnection.disconnect();
                    }
                }
                catch (IOException e)
                {
                    throw new IllegalStateException(e);
                }

                try
                {
                    JSONObject timeJsonObject = new JSONObject(timeResponse);
                    long serverTimeMillis = timeJsonObject.getLong("time");
                    Log.i(TAG, "server time json object:" + timeJsonObject.toString());
                    Log.i(TAG, "server time millis: " + serverTimeMillis);
                }
                catch (JSONException e)
                {
                    throw new IllegalStateException(e);
                }

            }
        });
        return view;
    }
}
