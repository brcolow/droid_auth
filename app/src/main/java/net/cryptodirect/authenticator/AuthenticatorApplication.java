package net.cryptodirect.authenticator;

import android.app.Application;

import net.danlew.android.joda.JodaTimeAndroid;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;
import org.acra.sender.HttpSender;

/**
 * Two-factor authentication application for Cryptodash users, based
 * on RFC 6238 - TOTP: Time-Based One-Time Password Algorithm.
 *
 * @see <a href="https://tools.ietf.org/html/rfc6238">RFC 6238</a>
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
        JodaTimeAndroid.init(this);
    }
}
