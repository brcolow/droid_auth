package net.cryptodirect.authenticator;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import net.danlew.android.joda.JodaTimeAndroid;

import java.io.File;

public class MainActivity extends AppCompatActivity
{
    private static final String PREFS_FILE = "PrefsFile";
    private static final String ACCOUNTS_FILE = "AccountsFile";
    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle state)
    {
        super.onCreate(state);
        if (state != null)
        {
            return;
        }
        JodaTimeAndroid.init(this);
        SharedPreferences settings = getSharedPreferences(PREFS_FILE, 0);
        File file = getBaseContext().getFileStreamPath(ACCOUNTS_FILE);
        // FIXME reverse this condition when we get it working
        if (!settings.contains("has_run_before"))
        {
            setContentView(R.layout.activity_main);
        }
        else
        {
            settings.edit().putBoolean("has_run_before", true).apply();
            Intent intent = new Intent(this, RegisterAccountActivity.class);
            startActivity(intent);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings)
        {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
