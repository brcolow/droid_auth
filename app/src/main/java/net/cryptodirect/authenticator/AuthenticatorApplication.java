package net.cryptodirect.authenticator;

import android.app.Application;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.acra.ACRA;
import org.acra.config.ACRAConfiguration;
import org.acra.config.ACRAConfigurationException;
import org.acra.config.ConfigurationBuilder;
import org.acra.security.KeyStoreFactory;
import org.acra.sender.HttpSender;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

/**
 * Two-factor authentication application for Cryptodash users, based
 * on RFC 6238 - TOTP: Time-Based One-Time Password Algorithm.
 * <p/>
 * The TOTP code is currently generated using the user's key, which is
 * a byte array of 32 bytes where the first 20 bytes are used for SHA1,
 * and 32 bytes for SHA2. Because the key is only 32 bytes long, it is not
 * set up to use SHA512. However, from everything I can read, it seems that
 * the security of TOTP is <em>not</em> dependent on the security of the hash
 * function used.
 * <p/>
 * The user's key initially lives server-side in Centurion (in the user's
 * user table entry in our PostgreSQL database) and is transmitting to the
 * client when the account is linked (via a QR code scanning or manual entry).
 * <p/>
 * After the key is transmitted to the client, it is stored in "accounts.json"
 * which is stored using "internal storage", and is accessed using
 * {@code Context.MODE_PRIVATE}. It is currently stored in plain-text,
 * but due to Android's file permissions model - this seems sufficient.
 *
 * @see <a href="https://tools.ietf.org/html/rfc6238">RFC 6238</a>
 * @see <a href="http://developer.android.com/guide/topics/data/data-storage.html#filesInternal">Android: Internal Storage</a>
 */
public class AuthenticatorApplication extends Application
{
    @Override
    public void onCreate()
    {
        super.onCreate();
        final ACRAConfiguration config;
        try
        {
            config = new ConfigurationBuilder(this)
                    .setFormUri(BuildConfig.DEBUG ? "https://10.0.2.2:4463/acra/report" : "https://cryptodash.net:4463/acra/report")
                    .setDeleteOldUnsentReportsOnApplicationStart(BuildConfig.DEBUG)
                    .setKeyStoreFactoryClass(MyKeyStoreFactory.class)
                    .setResToastText(R.string.crash_toast_text)
                    .setResDialogText(R.string.crash_dialog_text)
                    .setResDialogIcon(android.R.drawable.ic_dialog_info)
                    .setResDialogTitle(R.string.crash_dialog_title)
                    .setResDialogOkToast(R.string.crash_dialog_ok_toast)
                    .setResDialogPositiveButtonText(R.string.okay)
                    .setResDialogNegativeButtonText(R.string.no)
                    .setHttpMethod(HttpSender.Method.PUT)
                    .setReportType(HttpSender.Type.JSON)
                    .build();
        }
        catch (ACRAConfigurationException e)
        {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        ACRA.init(this, config);

        if (BuildConfig.DEBUG)
        {
            javax.net.ssl.HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier()
            {
                @Override
                public boolean verify(String hostname, SSLSession session)
                {
                    return hostname.equals("10.0.2.2");
                }
            });
        }
    }

    public static class MyKeyStoreFactory implements KeyStoreFactory
    {
        @Nullable
        @Override
        public KeyStore create(@NonNull Context context)
        {
            try
            {
                KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
                InputStream keyStream = context.getResources().openRawResource(R.raw.keystore);
                keyStore.load(keyStream, "123456".toCharArray());
                keyStream.close();
                return keyStore;
            }
            catch (CertificateException | NoSuchAlgorithmException | KeyStoreException | IOException e)
            {
                throw new RuntimeException(e);
            }
        }
    }

}
