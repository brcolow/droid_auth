package net.cryptodirect.authenticator;

import android.util.AndroidRuntimeException;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ProtocolException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

/**
 * Allows for sending HTTP reqeusts to a real Centurion instance (i.e.
 * one that is running locally if this is a debug build or remotely if
 * this is a release build).
 */
public class RealCenturion extends Centurion
{
    private static final long serialVersionUID = 1942L;

    private final String baseUrl;
    private static final String USER_AGENT = "Cryptodash Authenticator (Android) " +
            BuildConfig.BUILD_TIME + "-" + BuildConfig.GIT_SHA;

    public RealCenturion()
    {
        baseUrl = BuildConfig.DEBUG ? "https://10.0.2.2:4463/" : "https://cryptodash.net:4463/";
    }

    @Override
    public JSONObject get(String path)
    {
        try
        {
            URL url = new URL(baseUrl + path);
            HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
            urlConnection.setRequestProperty("User-Agent", USER_AGENT);
            String response;
            try
            {
                BufferedReader in = new BufferedReader(new InputStreamReader(
                        urlConnection.getInputStream()));

                String inputLine;
                StringBuilder stringBuilder = new StringBuilder();
                while ((inputLine = in.readLine()) != null)
                {
                    stringBuilder.append(inputLine);
                }

                response = stringBuilder.toString();
            }
            finally
            {
                urlConnection.disconnect();
            }

            JSONObject responseJsonObject;
            try
            {
                responseJsonObject = new JSONObject(response);
            }
            catch (JSONException e)
            {
                // server returned bad Json
                throw new IllegalStateException(e);
            }

            return responseJsonObject;
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public JSONObject post(String path, String payload)
    {
        try
        {
            URL url = new URL(baseUrl + path);
            HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
            try
            {
                urlConnection.setRequestMethod("POST");
            }
            catch (ProtocolException e)
            {
                throw new AndroidRuntimeException(e);
            }
            urlConnection.setRequestProperty("User-Agent", USER_AGENT);
            urlConnection.setRequestProperty("Accept", "application/json");
            urlConnection.setRequestProperty("Content-Type", "text/plain");
            urlConnection.setUseCaches(false);
            urlConnection.setDoOutput(true);
            urlConnection.setDoOutput(true);
            urlConnection.setConnectTimeout(3000);
            urlConnection.connect();
            DataOutputStream dataOutputStream = new DataOutputStream(urlConnection.getOutputStream());
            dataOutputStream.writeBytes(payload);
            dataOutputStream.flush();
            dataOutputStream.close();
            int responseCode = urlConnection.getResponseCode();
            BufferedReader in;
            if (responseCode >= 400)
            {
                in = new BufferedReader(new InputStreamReader(
                        urlConnection.getErrorStream()));
            }
            else
            {
                in = new BufferedReader(new InputStreamReader(
                        urlConnection.getInputStream()));
            }

            String inputLine;
            StringBuilder stringBuilder = new StringBuilder();
            while ((inputLine = in.readLine()) != null)
            {
                stringBuilder.append(inputLine);
            }

            String response = stringBuilder.toString();
            JSONObject responseJsonObject;
            responseJsonObject = new JSONObject(response);
            responseJsonObject.put("httpResponseCode", responseCode);
            return responseJsonObject;
        }
        catch (IOException e)
        {
            try
            {
                return new JSONObject("{\"local_error\": " + JSONObject.quote(e.getMessage()) + "}");
            }
            catch (JSONException e2)
            {
                // really shouldn't happen (server returned invalid JSON)
                throw new AndroidRuntimeException(e2);
            }
        }
        catch (JSONException e)
        {
            // really shouldn't happen
            throw new AndroidRuntimeException(e);
        }
    }
}
