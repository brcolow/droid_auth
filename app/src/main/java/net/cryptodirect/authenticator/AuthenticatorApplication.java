package net.cryptodirect.authenticator;

import android.app.Application;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;
import org.acra.sender.HttpSender;

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
@ReportsCrashes(
        formUri = "https://cryptodash.net:4463/acra/report",
        mode = ReportingInteractionMode.DIALOG,
        resToastText = R.string.crash_toast_text,
        resDialogText = R.string.crash_dialog_text,
        resDialogIcon = android.R.drawable.ic_dialog_info,
        resDialogTitle = R.string.crash_dialog_title,
        resDialogOkToast = R.string.crash_dialog_ok_toast,
        httpMethod = HttpSender.Method.PUT,
        reportType = HttpSender.Type.JSON
)
public class AuthenticatorApplication extends Application
{
    private static final String TAG = AuthenticatorApplication.class.getSimpleName();

    @Override
    public void onCreate()
    {
        super.onCreate();
        ACRA.init(this);
    }
}
