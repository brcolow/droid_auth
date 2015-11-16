package net.cryptodirect.authenticator;

import android.app.Application;

import org.acra.ACRA;
import org.acra.ACRAConfiguration;
import org.acra.ACRAConfigurationException;
import org.acra.ReportingInteractionMode;
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
        ACRAConfiguration acraConfiguration = new ACRAConfiguration();
        acraConfiguration.setFormUri(BuildConfig.DEBUG ? "https://10.0.2.2:4463/acra/report"
                : "https://cryptodash.net:4463/acra/report");
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

            try
            {
                KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
                InputStream keyStream = getResources().openRawResource(R.raw.keystore);
                keyStore.load(keyStream, "123456".toCharArray());
                keyStream.close();
                acraConfiguration.setKeyStore(keyStore);
            }
            catch (KeyStoreException | CertificateException | NoSuchAlgorithmException | IOException e)
            {
                e.printStackTrace();
            }
        }
        acraConfiguration.setResToastText(R.string.crash_toast_text);
        acraConfiguration.setResDialogText(R.string.crash_dialog_text);
        acraConfiguration.setResDialogIcon(android.R.drawable.ic_dialog_info);
        acraConfiguration.setResDialogTitle(R.string.crash_dialog_title);
        acraConfiguration.setResDialogOkToast(R.string.crash_dialog_ok_toast);
        acraConfiguration.setResDialogPositiveButtonText(R.string.okay);
        acraConfiguration.setResDialogNegativeButtonText(R.string.no);

        acraConfiguration.setHttpMethod(HttpSender.Method.PUT);
        acraConfiguration.setReportType(HttpSender.Type.JSON);
        try
        {
            acraConfiguration.setMode(ReportingInteractionMode.DIALOG);
        }
        catch (ACRAConfigurationException e)
        {
            throw new RuntimeException(e);
        }
        ACRA.init(this, acraConfiguration);
    }
}
