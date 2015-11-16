package net.cryptodirect.authenticator;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class Centurion
{
    private static final String BASE_URL = BuildConfig.DEBUG ? "https://10.0.2.2:4463/" : "https://cryptodash.net:4463/";
    private static final String USER_AGENT = "[Android]: Cryptodash Authenticator" +
            BuildConfig.BUILD_TIME + "-" + BuildConfig.GIT_SHA;

    private Centurion()
    {
    }

    private static class SingletonHolder
    {
        private static final Centurion INSTANCE = new Centurion();
    }

    public static Centurion getInstance()
    {
        return SingletonHolder.INSTANCE;
    }

    public JSONObject get(String path) throws IOException
    {
        URL url = new URL(BASE_URL + path);
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

    public JSONObject post(String path, String payload) throws IOException, JSONException
    {
        URL url = new URL(BASE_URL + path);
        HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
        urlConnection.setRequestMethod("POST");
        urlConnection.setRequestProperty("User-Agent", USER_AGENT);
        urlConnection.setRequestProperty("Accept", "application/json");
        urlConnection.setRequestProperty("Content-Type", "text/plain");
        urlConnection.setUseCaches(false);
        urlConnection.setDoOutput(true);
        urlConnection.setDoOutput(true);
        urlConnection.setConnectTimeout(3000);

        try
        {
            urlConnection.connect();
        }
        catch (SocketTimeoutException | SocketException e)
        {
            return new JSONObject("{\"local_error\": \""+ e.getMessage() +"\"}");
        }

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
}
