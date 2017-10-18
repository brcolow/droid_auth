package net.cryptodirect.authenticator.acra;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import org.acra.ReportField;
import org.acra.collector.CrashReportData;
import org.acra.model.Element;
import org.acra.sender.ReportSender;
import org.acra.sender.ReportSenderException;

import java.util.Map;

class DebugSender implements ReportSender
{
    @Override
    public void send(@NonNull Context context, @NonNull CrashReportData report) throws ReportSenderException
    {
        // Iterate over the CrashReportData instance and do whatever
        // you need with each pair of ReportField key / String value
        for (Map.Entry<ReportField, Element> entry : report.entrySet())
        {
            Log.e("DebugSender", entry.getKey() + " = " + entry.getValue().value());
        }
    }
}
