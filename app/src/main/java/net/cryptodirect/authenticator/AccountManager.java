package net.cryptodirect.authenticator;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.JsonWriter;

import net.cryptodirect.authenticator.crypto.Algorithm;
import net.cryptodirect.authenticator.crypto.Base;
import net.cryptodirect.authenticator.crypto.Base64;
import net.cryptodirect.authenticator.crypto.CodeParams;
import net.cryptodirect.authenticator.crypto.CodeType;

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
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static net.cryptodirect.authenticator.StandardCharsets.UTF_8;

public class AccountManager
{
    private Context baseContext;
    private static final AccountManager INSTANCE = new AccountManager();
    private static final String ACCOUNTS_FILE = "accounts.json";
    private final Map<Issuer, Account> accounts = new ConcurrentHashMap<>();

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

    public synchronized void init(int numAccounts, boolean verify) throws IOException, JSONException
    {
        File file = baseContext.getFileStreamPath(ACCOUNTS_FILE);

        if (file.length() == 0)
        {
            // create accounts.json skeleton
            FileOutputStream accountsFileOutputStream = baseContext.openFileOutput(
                    ACCOUNTS_FILE, Context.MODE_PRIVATE);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(
                    accountsFileOutputStream);
            String jsonSkeleton =
                    "{ " +
                    "  \"accounts\" : [] " +
                    "}";
            outputStreamWriter.write(jsonSkeleton);
            outputStreamWriter.flush();
            outputStreamWriter.close();
            return;
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
            JSONObject codeParamsJsonObject = accountJsonObject.getJSONObject("codeParams");
            int base = codeParamsJsonObject.getInt("base");
            Account account = new Account(accountJsonObject.getString("label"),
                    Issuer.getIssuer(accountJsonObject.getInt("issuer")),
                    Base64.getDecoder().decode(accountJsonObject.getString("key")),
                    new CodeParams.Builder(
                            CodeType.getCodeType(codeParamsJsonObject.getString("codeType")))
                            .algorithm(Algorithm.getAlgorithm(codeParamsJsonObject
                                    .getString("algorithm")))
                            .digits(codeParamsJsonObject.getInt("digits"))
                            .hotpCounter(codeParamsJsonObject.getInt("hotpCounter"))
                            .totpPeriod(codeParamsJsonObject.getInt("totpPeriod"))
                            .base(base)
                            .build());

            accounts.put(account.getIssuer(), account);
        }

        bufferedReader.close();
        inputStreamReader.close();
        accountsFileInputStream.close();

        if (verify && accounts.size() != numAccounts)
        {
            // data corruption
            throw new DataMismatchException("SharedPreferences said we have " + numAccounts +
                    " accounts, but accounts.json only contained " + accounts.size() + " accounts!",
                    Math.abs(numAccounts - accounts.size()));
        }
    }

    public Account getAccount(int accountId)
    {
        return accounts.get(Issuer.getIssuer(accountId));
    }

    public Account getFirstAccount()
    {
        return accounts.values().iterator().next();
    }

    public boolean accountExists(int accountId)
    {
        return accounts.containsKey(Issuer.getIssuer(accountId));
    }

    public int getNumAccounts()
    {
        return accounts.size();
    }

    public boolean registerAccount(Account account, boolean overwriteExisting, boolean setAsDefault)
    {
        if (account == null)
        {
            throw new IllegalArgumentException("account must not be null");
        }

        if (accounts.containsKey(account.getIssuer()) && !overwriteExisting)
        {
            return false;
        }

        try
        {
            accounts.put(account.getIssuer(), account);
            FileOutputStream accountsFileOutputStream = baseContext.openFileOutput(
                    ACCOUNTS_FILE, Context.MODE_PRIVATE);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(
                    accountsFileOutputStream, UTF_8);
            JsonWriter jsonWriter = new JsonWriter(outputStreamWriter);
            jsonWriter.beginObject();
            jsonWriter.name("accounts");
            writeAccountsArray(jsonWriter);
            jsonWriter.endObject();
            outputStreamWriter.flush();
            jsonWriter.close();
            outputStreamWriter.close();

            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(baseContext);
            SharedPreferences.Editor prefsEditor = settings.edit();
            prefsEditor.putInt("numAccounts", accounts.size());
            if (setAsDefault)
            {
                prefsEditor.putString("default_account", String.valueOf(account.getIssuer().getId()));
            }
            prefsEditor.apply();

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
        writer.name("label");
        writer.value(account.getLabel());
        writer.name("issuer");
        writer.value(account.getIssuer().getId());
        writer.name("key");
        writer.value(account.getBase64EncodedSecretKey());
        writer.name("codeParams");
        writer.beginObject();
        writer.name("codeType");
        writer.value(account.getCodeParams().getCodeType().name());
        writer.name("algorithm");
        writer.value(account.getCodeParams().getAlgorithm().name());
        writer.name("digits");
        writer.value(account.getCodeParams().getDigits());
        writer.name("hotpCounter");
        writer.value(account.getCodeParams().getHotpCounter());
        writer.name("totpPeriod");
        writer.value(account.getCodeParams().getTotpPeriod());
        writer.name("base");
        writer.value(account.getCodeParams().getBase().equals(Base.BASE32) ? 32 : 64);
        writer.endObject();
        writer.endObject();
    }

    /**
     * Allows to easily use the account data in a ListPreference, by returning
     * the pair of entries, and entry values.
     */
    public CharSequence[] getAccountLabels()
    {
        CharSequence[] result = new CharSequence[accounts.size()];
        int i = 0;
        for (Issuer issuer : accounts.keySet())
        {
            result[i] = issuer.toString();
            i++;
        }
        return result;
    }

    public Collection<Account> getAccounts()
    {
        return accounts.values();
    }

    public static class DataMismatchException extends JSONException
    {
        private final int numMissingAccounts;
        static final long serialVersionUID = 1L;

        public DataMismatchException(String s, int numMissingAccounts)
        {
            super(s);
            this.numMissingAccounts = numMissingAccounts;
        }

        public int getNumMissingAccounts()
        {
            return numMissingAccounts;
        }
    }
}
