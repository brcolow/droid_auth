package net.cryptodirect.authenticator;

import android.content.Intent;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.filters.MediumTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import net.cryptodirect.authenticator.crypto.Base32;
import net.cryptodirect.authenticator.crypto.Base64;
import net.cryptodirect.authenticator.crypto.CodeParams;
import net.cryptodirect.authenticator.crypto.CodeType;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

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
import static net.cryptodirect.authenticator.CustomMatchers.withScaledCompoundDrawable;
import static net.cryptodirect.authenticator.crypto.Algorithm.SHA1;
import static net.cryptodirect.authenticator.crypto.Algorithm.SHA256;
import static net.cryptodirect.authenticator.crypto.TOTP.generateTOTP;
import static net.cryptodirect.authenticator.crypto.TOTP.getTC;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.not;

@RunWith(AndroidJUnit4.class)
@MediumTest
public class LinkAccountBehaviorTest
{
    @Rule
    public ActivityTestRule<MainActivity> rule = new IntentsTestRule<>(MainActivity.class,
            true, false);

    @Before
    public void beforeEach()
    {
        TestUtils.clearSharedPreferences();
        TestUtils.clearStorage();
        TestUtils.clearCache();
    }

    @Test
    public void shouldLinkValidManuallyEnteredCryptodashAccount() throws Exception
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
        intent.putExtra("verifyAccountsFile", false);

        rule.launchActivity(intent);

        // when
        onView(withText("Link Account")).perform(click());
        onView(withId(R.id.manual_entry_button)).perform(click());
        onView(withId(R.id.account_label_edittext)).perform(typeText(email), closeSoftKeyboard());
        onView(withId(R.id.key_edit_text)).perform(typeText(key), closeSoftKeyboard());
        onView(withText("Okay")).perform(click());

        onView(allOf(withId(R.id.key_edit_text), not(isClickable()))).check(matches(
                withText(key)));
        onView(allOf(withId(R.id.key_edit_text), not(isClickable()))).check(matches(
                withCompoundDrawable(R.drawable.ic_check_white_24dp)));

        onView(withId(R.id.correct_button)).perform(click());

        // then
        assertThat(AccountManager.getInstance().getAccount(Issuer.CRYPTODASH.getId()),
                is(new Account(email, Issuer.CRYPTODASH, Base64.getDecoder().decode(key),
                        new CodeParams.Builder(CodeType.TOTP).base(64).algorithm(SHA256).build())));

        // The current TOTP code and time-wheel should be displayed for the newly linked account
        onView(withId(R.id.code_box)).check(matches(isDisplayed()));
        onView(withId(R.id.code_box)).check(matches(withText(generateTOTP(
                Base64.getDecoder().decode(key), (long) getTC(30), 6, SHA256))));
    }

    @Test
    public void shouldLinkAccountUponScanningValidCryptodashQRCode() throws Exception
    {
        // given
        String email = "test@gmail.com";
        String key = "Ne2WwwXegWzlHuD0eriSXx1EaxmQiEW8QMCRcn5RJ78=";
        String urlEncodedKey = "Ne2WwwXegWzlHuD0eriSXx1EaxmQiEW8QMCRcn5RJ78%3D";

        Intent intent = new Intent();
        intent.putExtra("net.cryptodirect.authenticator.MockScannedCode", String.format(
                "otpauth://totp/%s?secret=%s&issuer=Cryptodash&base=64&algorithm=SHA256",
                email, urlEncodedKey));
        MockCenturion centurion = new MockCenturion();
        centurion.setPostResponse("twofactor/notify-linked",
                String.format("{\"email\": \"%s\", \"totpKeyBase64\": \"%s\"}", email, key),
                "{\"success\": true, \"httpResponseCode\": 200}");
        intent.putExtra("net.cryptodirect.authenticator.Centurion", centurion);
        intent.putExtra("verifyAccountsFile", false);
        rule.launchActivity(intent);

        // when
        onView(withText("Link Account")).perform(click());
        onView(withId(R.id.scan_qr_code_button)).perform(click());

        onView(withId(R.id.account_provider_edittext)).check(matches(withText("Cryptodash")));
        onView(withId(R.id.account_provider_edittext)).check(matches(
                withScaledCompoundDrawable(R.drawable.cryptodash, 100, 100)));

        onView(allOf(withId(R.id.key_edit_text), not(isClickable()))).check(matches(withText(key)));
        onView(allOf(withId(R.id.key_edit_text), not(isClickable()))).check(matches(
                withCompoundDrawable(R.drawable.ic_check_white_24dp)));

        onView(withId(R.id.correct_button)).perform(click());

        // then
        assertThat(AccountManager.getInstance().getAccount(Issuer.CRYPTODASH.getId()),
                is(new Account(email, Issuer.CRYPTODASH, Base64.getDecoder().decode(key),
                        new CodeParams.Builder(CodeType.TOTP).base(64).algorithm(SHA256).build())));

        // The current TOTP code and time-wheel should be displayed for the newly linked account
        onView(withId(R.id.code_box)).check(matches(isDisplayed()));
        onView(withId(R.id.code_box)).check(matches(withText(generateTOTP(
                Base64.getDecoder().decode(key), (long) getTC(30), 6, SHA256))));
    }

    @Test
    public void shouldLinkAccountUponScanningValidBitstampQRCode() throws Exception
    {
        // given
        String accountId = "872472";
        String key = "KJCDKRSZKNKFOWSU";
        Intent intent = new Intent();
        intent.putExtra("net.cryptodirect.authenticator.MockScannedCode", String.format(
                "otpauth://totp/%s@Bitstamp?secret=%s", accountId, key));

        rule.launchActivity(intent);

        // when
        onView(withText("Link Account")).perform(click());
        onView(withId(R.id.scan_qr_code_button)).perform(click());

        onView(withId(R.id.account_provider_edittext)).check(matches(withText("Bitstamp")));
        onView(withId(R.id.account_provider_edittext)).check(matches(
                withScaledCompoundDrawable(R.drawable.bitstamp, 100, 100)));

        onView(withId(R.id.account_label_textview)).check(matches(withText("Bitstamp Account Id:")));
        onView(withId(R.id.account_label_edittext)).check(matches(withText(accountId)));

        onView(allOf(withId(R.id.key_edit_text), not(isClickable()))).check(matches(
                withText(key)));
        onView(allOf(withId(R.id.key_edit_text), not(isClickable()))).check(matches(
                withCompoundDrawable(R.drawable.ic_check_white_24dp)));

        onView(withId(R.id.correct_button)).perform(click());

        // then
        assertThat(AccountManager.getInstance().getAccount(Issuer.BITSTAMP.getId()),
                is(new Account(accountId + "@Bitstamp", Issuer.BITSTAMP, Base32.getDecoder().decode(key),
                        new CodeParams.Builder(CodeType.TOTP).base(32).algorithm(SHA1).build())));

        // The current TOTP code and time-wheel should be displayed for the newly linked account
        onView(withId(R.id.code_box)).check(matches(isDisplayed()));
        onView(withId(R.id.code_box)).check(matches(withText(generateTOTP(
                Base32.getDecoder().decode(key), (long) getTC(30), 6, SHA1))));
    }

    @Test
    public void shouldLinkAccountUponScanningValidBitfinexQRCode() throws Exception
    {
        // given
        String label = "Bitfinex-Jul-23-2016";
        String key = "qpni5ijdl54kd7xv";
        Intent intent = new Intent();
        intent.putExtra("net.cryptodirect.authenticator.MockScannedCode", String.format(
                "otpauth://totp/%s?secret=%s", label, key));
        rule.launchActivity(intent);

        // when
        onView(withText("Link Account")).perform(click());
        onView(withId(R.id.scan_qr_code_button)).perform(click());

        onView(withId(R.id.account_provider_edittext)).check(matches(withText("Bitfinex")));
        onView(withId(R.id.account_provider_edittext)).check(matches(
                withScaledCompoundDrawable(R.drawable.bitfinex, 100, 100)));

        onView(withId(R.id.account_label_textview)).check(matches(withText("QR Code Generated On:")));
        onView(withId(R.id.account_label_edittext)).check(matches(withText("Jul-23-2016")));

        onView(allOf(withId(R.id.key_edit_text), not(isClickable()))).check(matches(
                withText(key)));
        onView(allOf(withId(R.id.key_edit_text), not(isClickable()))).check(matches(
                withCompoundDrawable(R.drawable.ic_check_white_24dp)));

        onView(withId(R.id.correct_button)).perform(click());

        // then
        assertThat(AccountManager.getInstance().getAccount(Issuer.BITFINEX.getId()),
                is(new Account(label, Issuer.BITFINEX, Base32.getDecoder().decode(key),
                        new CodeParams.Builder(CodeType.TOTP).base(32).algorithm(SHA1).build())));

        // The current TOTP code and time-wheel should be displayed for the newly linked account
        onView(withId(R.id.code_box)).check(matches(isDisplayed()));
        onView(withId(R.id.code_box)).check(matches(withText(generateTOTP(
                Base32.getDecoder().decode(key), (long) getTC(30), 6, SHA1))));
    }
}