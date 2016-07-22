package net.cryptodirect.authenticator;

import static net.cryptodirect.authenticator.Utils.NEXUS_6_HEIGHT_PIXELS;
import static net.cryptodirect.authenticator.Utils.NEXUS_6_DPI;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import net.cryptodirect.authenticator.crypto.Algorithm;
import net.cryptodirect.authenticator.crypto.TOTP;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Allows the user to test their device by running TOTP
 * test vectors and testing clock drift between Centurion
 * authentication servers and their device. This will help
 * provide us with more information when bug reports arise.
 */
public class TestDeviceFragment extends Fragment
{
    private static final byte[] KEY_20_BYTES = "12345678901234567890"
            .getBytes(StandardCharsets.US_ASCII);
    private static final byte[] KEY_32_BYTES = "12345678901234567890123456789012"
            .getBytes(StandardCharsets.US_ASCII);
    private static final byte[] KEY_64_BYTES = ("123456789012345678901234567890123456789012345678" +
            "9012345678901234").getBytes(StandardCharsets.US_ASCII);

    private static final long[] testTimeParams = new long[]{59, 1111111109L, 1111111111L,
            1234567890L, 2000000000L, 20000000000L};

    private static final String[] testCorrectResults = new String[]{"94287082", "07081804",
            "14050471", "89005924", "69279037", "65353130", "46119246", "68084774", "67062674",
            "91819424", "90698825", "77737706", "90693936", "25091201", "99943326", "93441116",
            "38618901", "47863826"};

    private static final int PASSED_COLOR = Color.parseColor("#4CAF50");
    private static final int FAILED_COLOR = Color.parseColor("#E53935");

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle state)
    {
        Centurion centurion = state.getParcelable("centurion");
        if (centurion == null)
        {
            throw new IllegalArgumentException("bundle: " + state + " did not contain parcelable " +
                    "\"centurion\"");
        }
        View view = inflater.inflate(R.layout.fragment_test_device, container, false);
        final Button button = (Button) view.findViewById(R.id.begin_tests_button);
        final LinearLayout resultsContainer = (LinearLayout) view.findViewById(R.id.results_container);
        final TableLayout resultsTable = (TableLayout) view.findViewById(R.id.results_table);

        button.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
            /*
            FetchTimeTask fetchTimeTask = new FetchTimeTask(centurion);
            long serverTimeMillis;
            try
            {
                serverTimeMillis = fetchTimeTask.execute().get(5000, TimeUnit.MILLISECONDS);
            }
            catch (InterruptedException | ExecutionException | TimeoutException e)
            {
                serverTimeMillis = System.currentTimeMillis();
            }
            */
                boolean[] testSuccessful = new boolean[18];
                for (int i = 0; i < testSuccessful.length; i++)
                {
                    if (i < 6)
                    {
                        testSuccessful[i] = TOTP.generateTOTP(KEY_20_BYTES,
                                TOTP.getTC(testTimeParams[i], 30), 8, Algorithm.SHA1)
                                .equals(testCorrectResults[i]);
                    }
                    else if (i < 12)
                    {
                        testSuccessful[i] = TOTP.generateTOTP(KEY_32_BYTES,
                                TOTP.getTC(testTimeParams[i % 6], 30), 8, Algorithm.SHA256)
                                .equals(testCorrectResults[i]);
                    }
                    else
                    {
                        testSuccessful[i] = TOTP.generateTOTP(KEY_64_BYTES,
                                TOTP.getTC(testTimeParams[i % 6], 30), 8, Algorithm.SHA512)
                                .equals(testCorrectResults[i]);
                    }
                }
                TableRow.LayoutParams layoutParams = new TableRow.LayoutParams(
                        TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
                layoutParams.setMargins(0, 0, 0, 0);

                LinearLayout testNumberHeaderCell = new LinearLayout(TestDeviceFragment.this.getActivity());
                testNumberHeaderCell.setLayoutParams(layoutParams);
                TextView testNumber = new TextView(TestDeviceFragment.this.getActivity());
                testNumber.setText(R.string.test_number_sign);
                testNumber.setTextColor(Color.parseColor("#212121"));
                testNumber.setPadding(0, 0, 0, 0);
                testNumberHeaderCell.addView(testNumber);

                LinearLayout testResultHeaderCell = new LinearLayout(TestDeviceFragment.this.getActivity());
                testResultHeaderCell.setLayoutParams(layoutParams);
                TextView testResult = new TextView(TestDeviceFragment.this.getActivity());
                testResult.setText(R.string.result);
                testResult.setTextColor(Color.parseColor("#212121"));
                testNumber.setPadding(0, 0, 0, 0);
                testResultHeaderCell.addView(testResult);

                TableRow resultsTableHeaderRow = new TableRow(TestDeviceFragment.this.getActivity());
                resultsTableHeaderRow.addView(testNumberHeaderCell);
                resultsTableHeaderRow.addView(testResultHeaderCell);
                resultsTableHeaderRow.setBackgroundColor(Color.WHITE);
                resultsTableHeaderRow.setPadding(0, 0, 0, 1); // Border between rows

                resultsTable.addView(resultsTableHeaderRow);
                layoutParams.setMargins(2, 0, 2, 0);

                for (int i = 0; i < testSuccessful.length; i++)
                {
                    TableRow testResultRow = new TableRow(TestDeviceFragment.this.getActivity());

                    testResultRow.setBackgroundColor(Color.WHITE);
                    testResultRow.setPadding(0, 0, 0, 2); // Border between rows

                    LinearLayout cell = new LinearLayout(TestDeviceFragment.this.getActivity());
                    cell.setBackgroundColor(Color.parseColor("#212121"));
                    cell.setLayoutParams(layoutParams);
                    testNumber = new TextView(TestDeviceFragment.this.getActivity());
                    testNumber.setText(String.format("Test %d", i + 1));
                    testNumber.setTextColor(Color.WHITE);
                    testNumber.setPadding(0, 0, 4, 3);
                    cell.addView(testNumber);

                    LinearLayout cell2 = new LinearLayout(TestDeviceFragment.this.getActivity());
                    cell2.setBackgroundColor(Color.parseColor("#212121"));
                    cell2.setLayoutParams(layoutParams);
                    testResult = new TextView(TestDeviceFragment.this.getActivity());
                    testResult.setText(testSuccessful[i] ? "Passed" : "Failed");
                    testResult.setTextColor(testSuccessful[i] ? PASSED_COLOR : FAILED_COLOR);
                    testNumber.setPadding(0, 0, 4, 3);
                    cell2.addView(testResult);

                    testResultRow.addView(cell);
                    testResultRow.addView(cell2);
                    resultsTable.addView(testResultRow);
                }
                button.setVisibility(View.GONE);
                DisplayMetrics metrics = new DisplayMetrics();
                TestDeviceFragment.this.getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
                int height = metrics.heightPixels;
                // The -200f value centers the table on Nexus 6, so we use that as the reference
                resultsContainer.setTranslationY((-200f) * ((height * metrics.densityDpi) / (float) (NEXUS_6_HEIGHT_PIXELS * NEXUS_6_DPI)));
            }
        });
        return view;
    }

    private class FetchTimeTask extends AsyncTask<String, Void, Long>
    {
        private final Centurion centurion;

        FetchTimeTask(Centurion centurion)
        {
            this.centurion = centurion;
        }

        @Override
        protected Long doInBackground(String... args)
        {
            try
            {
                JSONObject timeJsonObject = centurion.get("time");
                return timeJsonObject.getLong("time");
            }
            catch (JSONException e)
            {
                throw new RuntimeException(e);
            }
        }
    }
}
