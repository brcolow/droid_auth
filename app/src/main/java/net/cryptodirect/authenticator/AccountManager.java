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
    private Context baseContext;
    private static final AccountManager INSTANCE = new AccountManager();
    private static final String ACCOUNTS_FILE = "accounts.json";
    private final Map<String, Account> accounts = new ConcurrentHashMap<>();
    private static FileOutputStream accountsFileOutputStream;

    private AccountManager()
    {

    }

    public void setBaseContext(Context baseContext)
    {
        this.baseContext = baseContext;
    }

    public static AccountManager getInstance()
    {
        return INSTANCE;
    }

    public synchronized void init() throws IOException, JSONException
    {
        File file = baseContext.getFileStreamPath(ACCOUNTS_FILE);

        if (file.length() == 0)
        {
            // create accounts.json skeleton
            accountsFileOutputStream = baseContext.openFileOutput(ACCOUNTS_FILE, Context.MODE_PRIVATE);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(accountsFileOutputStream);
            String jsonSkeleton = "{ \"accounts\" : [] }";
            outputStreamWriter.write(jsonSkeleton);
            outputStreamWriter.flush();
        }

        FileInputStream accountsFileInputStream = baseContext.openFileInput(ACCOUNTS_FILE);
        InputStreamReader inputStreamReader = new InputStreamReader(accountsFileInputStream);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        StringBuilder stringBuilder = new StringBuilder();
        String line;
        while ((line = bufferedReader.readLine()) != null)
        {
            stringBuilder.append(line);
        }

        JSONObject accountsJsonObject = new JSONObject(stringBuilder.toString());
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

    public boolean registerAccount(Account account, boolean overwriteExisting, boolean setAsDefault)
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
                accountsFileOutputStream = baseContext.openFileOutput(ACCOUNTS_FILE, Context.MODE_PRIVATE);
            }
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(accountsFileOutputStream, StandardCharsets.UTF_8);
            JsonWriter jsonWriter = new JsonWriter(outputStreamWriter);
            jsonWriter.beginObject();
            jsonWriter.name("accounts");
            writeAccountsArray(jsonWriter);
            jsonWriter.endObject();
            outputStreamWriter.flush();
            jsonWriter.close();
        }
        catch (IOException e)
        {
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

    /**
     * Allows to easily use the account data in a ListPreference, by returning
     * the pair of entries, and entry values.
     */
    public CharSequence[] getAccountEmails()
    {
        return accounts.keySet().toArray(new CharSequence[accounts.size()]);
    }
}
