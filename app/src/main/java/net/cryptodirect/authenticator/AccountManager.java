package net.cryptodirect.authenticator;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AccountManager
{
    private static final AccountManager INSTANCE = new AccountManager();
    private static final String ACCOUNTS_FILE = "accounts.json";
    private final Map<String, Account> accounts = new ConcurrentHashMap<>();
    private AccountManager() {}

    public static AccountManager getInstance()
    {
        return INSTANCE;
    }

    public void initAccountManager() throws IOException, JSONException
    {
        File file = MainActivity.BASE_CONTEXT.getFileStreamPath(ACCOUNTS_FILE);
        JSONObject accountsJsonObject;

        if (!file.exists())
        {
            // create accounts.json skeleton
            FileOutputStream fos = MainActivity.BASE_CONTEXT.openFileOutput(ACCOUNTS_FILE, Context.MODE_PRIVATE);
            OutputStreamWriter osw = new OutputStreamWriter(fos);
            String jsonSkeleton = "{ \"accounts\" : [] }";
            osw.write(jsonSkeleton);
        }

        FileInputStream fis = MainActivity.BASE_CONTEXT.openFileInput(ACCOUNTS_FILE);
        InputStreamReader isr = new InputStreamReader(fis);
        BufferedReader bufferedReader = new BufferedReader(isr);
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = bufferedReader.readLine()) != null)
        {
            sb.append(line);
        }

        accountsJsonObject = new JSONObject(sb.toString());
        JSONArray accountsJsonArray = accountsJsonObject.getJSONArray("accounts");
        for (int i = 0; i < accountsJsonArray.length(); i++)
        {
            JSONObject accountJsonObject = accountsJsonArray.getJSONObject(i);
            Account account = new Account(
                    accountJsonObject.getString("email"),
                    accountJsonObject.getString("key"));
            accounts.put(accountJsonObject.getString("email"), account);
        }
    }

    public Account getAccount(String email)
    {
        return accounts.get(email);
    }

    public Account getOnlyAccount()
    {
       return accounts.values().iterator().next();
    }

    public boolean accountExists(String email)
    {
        return accounts.containsKey(email);
    }

    public int getNumAccounts()
    {
        return accounts.size();
    }
}
