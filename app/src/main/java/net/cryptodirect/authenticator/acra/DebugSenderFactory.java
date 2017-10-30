package net.cryptodirect.authenticator.acra;

import android.content.Context;
import android.support.annotation.NonNull;

import org.acra.config.ACRAConfiguration;
import org.acra.sender.ReportSender;
import org.acra.sender.ReportSenderFactory;

public class DebugSenderFactory implements ReportSenderFactory
{
    @NonNull
    public ReportSender create(@NonNull Context context, @NonNull ACRAConfiguration config)
    {
        return new DebugSender();
    }
}