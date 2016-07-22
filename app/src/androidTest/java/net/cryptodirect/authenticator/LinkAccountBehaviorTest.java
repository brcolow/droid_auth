package net.cryptodirect.authenticator;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.MediumTest;

import net.cryptodirect.authenticator.crypto.Base64;
import net.cryptodirect.authenticator.crypto.CodeParams;
import net.cryptodirect.authenticator.crypto.CodeType;
import net.cryptodirect.authenticator.crypto.TOTP;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isClickable;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static net.cryptodirect.authenticator.CustomMatchers.withCompoundDrawable;
import static net.cryptodirect.authenticator.crypto.Algorithm.SHA256;
import static net.cryptodirect.authenticator.crypto.TOTP.generateTOTP;
import static net.cryptodirect.authenticator.crypto.TOTP.getTC;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasValue;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
@MediumTest
public class LinkAccountBehaviorTest
{
    @Rule
    public ActivityTestRule<MainActivity> rule = new ActivityTestRule<>(MainActivity.class,
            true, false);

    @SuppressLint("CommitPrefEdits")
    @Before
    public void beforeEach()
    {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(
                getInstrumentation().getTargetContext());
        preferences.edit().clear().commit();
        File file = getInstrumentation().getTargetContext().getFileStreamPath(
                AccountManager.ACCOUNTS_FILE);
        file.delete();
    }

    @Test
    public void shouldLinkValidManuallyEnteredAccount() throws Exception
    {
        // given
        String email = "test@gmail.com";
        String key = "Ne2WwwXegWzlHuD0eriSXx1EaxmQiEW8QMCRcn5RJ78=";
        MockCenturion centurion = new MockCenturion();
        centurion.setPostResponse("twofactor/notify-linked",
                String.format("{\"email\": \"%s\", \"totpKeyBase64\": \"%s\"}", email, key),
                "{\"success\": true, \"httpResponseCode\": 200}");

        Intent intent = new Intent();
        intent.putExtra("net.cryptodirect.authenticator.Centurion", centurion);
        rule.launchActivity(intent);

        // when
        onView(withText("Link Account")).perform(click());
        onView(withId(R.id.manual_entry_button)).perform(click());
        onView(withId(R.id.email_edit_text)).perform(typeText(email), closeSoftKeyboard());
        onView(withId(R.id.key_edit_text)).perform(typeText(key), closeSoftKeyboard());
        onView(withText("Okay")).perform(click());

        // Key EditText should have a check-mark drawable indicating it is valid
        onView(allOf(withId(R.id.key_edit_text), not(isClickable()))).check(matches(
                withCompoundDrawable(R.drawable.ic_check_white_24dp)));

        onView(withId(R.id.correct_button)).perform(click());

        // then
        // manually entered account should be added to AccountManager
        assertEquals(AccountManager.getInstance().getAccount(email),
                new Account(email, "Cryptodash", Base64.getDecoder().decode(key),
                        new CodeParams.Builder(CodeType.TOTP).base(64).algorithm(SHA256).build()));

        // The current TOTP code and time-wheel should be displayed for the newly linked account
        onView(withId(R.id.code_box)).check(matches(isDisplayed()));
        onView(withId(R.id.code_box)).check(matches(withText(generateTOTP(
                Base64.getDecoder().decode(key), getTC(30), 6, SHA256))));
    }

    @Test
    public void shouldLinkAccountUponScanningValidQRCode()
    {
        // when
        onView(withText("Scan QR Code")).perform(click());
        onView(withId(R.id.manual_entry_button)).perform(click());
    }
}