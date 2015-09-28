package net.cryptodirect.authenticator;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.concurrent.ExecutionException;

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
            public void onClick(View view)
            {
                FetchTimeTask fetchTimeTask = new FetchTimeTask();
                try
                {
                    long serverTimeMillis = fetchTimeTask.execute().get();
                    Log.i(TAG, "server time millis: " + serverTimeMillis);
                }
                catch (InterruptedException | ExecutionException e)
                {
                    e.printStackTrace();
                }

                LinearLayout resultsContainer = (LinearLayout) view.findViewById(R.id.results_container);
                // TODO put test results as children of resultsContainer
                // also.. we may want to move the above code into an Async task, that is scheduled
                // for immediate execution when the button is clicked. Before that, the button
                // could be replaced by a progress indicator. once the async task is done, the progress
                // container is removed and the results are drawn on the main thread
            }
        });
        return view;
    }

    private class FetchTimeTask extends AsyncTask<String, Void, Long>
    {
        @Override
        protected Long doInBackground(String... args)
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
                Log.e(TAG, "Could not fetch current time from Centurion: ", e);
            }

            long serverTimeMillis;
            if (timeResponse == null)
            {
                // couldn't get time from server - just continue with device's current time
                serverTimeMillis = System.currentTimeMillis();
            }
            else
            {
                try
                {
                    JSONObject timeJsonObject = new JSONObject(timeResponse);
                    serverTimeMillis = timeJsonObject.getLong("time");
                }
                catch (JSONException e)
                {
                    throw new IllegalStateException(e);
                }
            }
            return serverTimeMillis;
        }
    }
}
