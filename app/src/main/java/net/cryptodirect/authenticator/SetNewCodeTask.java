package net.cryptodirect.authenticator;

import android.widget.TextView;

public class SetNewCodeTask implements Runnable
{
    private final TextView codeTextView;
    private final String newCode;

    SetNewCodeTask(TextView codeTextView, String newCode)
    {
        this.codeTextView = codeTextView;
        this.newCode = newCode;
    }

    @Override
    public void run()
    {
        codeTextView.setText(newCode);
    }
}