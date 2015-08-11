package net.cryptodirect.authenticator;

import android.app.Application;
import android.content.Intent;

import net.danlew.android.joda.JodaTimeAndroid;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

@ReportsCrashes(
        formUri = "https://www.cryptodash.net/acraproxyphp",
        mode = ReportingInteractionMode.DIALOG,
        resToastText = R.string.crash_toast_text,
        resDialogText = R.string.crash_dialog_text,
        resDialogIcon = android.R.drawable.ic_dialog_info,
        resDialogTitle = R.string.crash_dialog_title,
        resDialogOkToast = R.string.crash_dialog_ok_toast
)
public class AuthenticatorApplication extends Application
{
    @Override
    public void onCreate()
    {
        super.onCreate();
        ACRA.init(this);
        JodaTimeAndroid.init(this);
        Intent mainActivityIntent = new Intent(this, MainActivity.class);
        startActivity(mainActivityIntent);
    }
}
