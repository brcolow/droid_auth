package net.cryptodirect.authenticator;

import android.content.Context;
import android.util.JsonWriter;

import org.acra.ACRA;
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
    private static FileOutputStream accountsFileOutputStream;
    private static FileInputStream accountsFileInputStream;
    private static final String TAG = AccountManager.class.getSimpleName();

    private AccountManager()
    {

    }

    public static AccountManager getInstance()
    {
        return INSTANCE;
    }

    public synchronized void init() throws IOException, JSONException
    {
        File file = MainActivity.BASE_CONTEXT.getFileStreamPath(ACCOUNTS_FILE);

        if (file.length() == 0)
        {
            // create accounts.json skeleton
            accountsFileOutputStream = MainActivity.BASE_CONTEXT.openFileOutput(ACCOUNTS_FILE, Context.MODE_PRIVATE);
            OutputStreamWriter osw = new OutputStreamWriter(accountsFileOutputStream);
            String jsonSkeleton = "{ \"accounts\" : [] }";
            osw.write(jsonSkeleton);
            osw.flush();
        }

        accountsFileInputStream = MainActivity.BASE_CONTEXT.openFileInput(ACCOUNTS_FILE);
        InputStreamReader isr = new InputStreamReader(accountsFileInputStream);
        BufferedReader bufferedReader = new BufferedReader(isr);
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = bufferedReader.readLine()) != null)
        {
            sb.append(line);
        }

        JSONObject accountsJsonObject = new JSONObject(sb.toString());
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

    public boolean registerAccount(Account account, boolean overwriteExisting)
    {
        if (accounts.containsKey(account.getEmail()) && !overwriteExisting)
        {
            return false;
        }
        try
        {
            accounts.put(account.getEmail(), account);
            if (accountsFileOutputStream == null)
            {
                accountsFileOutputStream = MainActivity.BASE_CONTEXT.openFileOutput(ACCOUNTS_FILE, Context.MODE_PRIVATE);
            }
            OutputStreamWriter osw = new OutputStreamWriter(accountsFileOutputStream, StandardCharsets.UTF_8);
            JsonWriter jsonWriter = new JsonWriter(osw);
            jsonWriter.beginObject();
            jsonWriter.name("accounts");
            writeAccountsArray(jsonWriter);
            jsonWriter.endObject();
            osw.flush();
            jsonWriter.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
            ACRA.getErrorReporter().handleException(e);
        }

        return true;
    }

    private void writeAccountsArray(JsonWriter writer) throws IOException
    {
        writer.beginArray();
        for (Account account : accounts.values())
        {
            writeAccount(writer, account);
        }
        writer.endArray();
    }

    private void writeAccount(JsonWriter writer, Account account) throws IOException
    {
        writer.beginObject();
        writer.name("email");
        writer.value(account.getEmail());
        writer.name("key");
        writer.value(account.getSecretKey());
        writer.endObject();
    }
}
