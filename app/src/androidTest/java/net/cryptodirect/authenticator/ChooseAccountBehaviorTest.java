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

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static net.cryptodirect.authenticator.crypto.Algorithm.SHA1;
import static net.cryptodirect.authenticator.crypto.Algorithm.SHA256;
import static net.cryptodirect.authenticator.crypto.TOTP.generateTOTP;
import static net.cryptodirect.authenticator.crypto.TOTP.getTC;

@RunWith(AndroidJUnit4.class)
@MediumTest
public class ChooseAccountBehaviorTest
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
    public void shouldBeAbleToChooseBetweenLinkedAccounts()
    {
        // given
        Intent intent = new Intent();
        rule.launchActivity(intent);

        // The user has a linked account with Cryptodash, Bitfinex, Bitstamp, and Coinbase
        Account cryptodashAccount = new Account("test@gmail.com", Issuer.CRYPTODASH,
                Base64.getDecoder().decode("Ne2WwwXegWzlHuD0eriSXx1EaxmQiEW8QMCRcn5RJ78="),
                new CodeParams.Builder(CodeType.TOTP).base(64).algorithm(SHA256).build());
        Account bitfinexAccount = new Account("Bitfinex-Jul-23-2016",
                Issuer.BITFINEX, Base32.getDecoder().decode("qpni5ijdl54kd7xv"),
                new CodeParams.Builder(CodeType.TOTP).base(32).algorithm(SHA1).build());
        Account bitstampAccount = new Account("872472@Bitstamp",
                Issuer.BITSTAMP, Base32.getDecoder().decode("KJCDKRSZKNKFOWSU"),
                new CodeParams.Builder(CodeType.TOTP).base(32).algorithm(SHA1).build());
        Account coinbaseAccount = new Account("test@gmail.com",
                Issuer.COINBASE, Base32.getDecoder().decode("5NIWJET3BG6NUA3U"),
                new CodeParams.Builder(CodeType.TOTP).base(32).algorithm(SHA1).build());
        AccountManager.getInstance().registerAccount(cryptodashAccount, false, false);
        AccountManager.getInstance().registerAccount(bitfinexAccount, false, false);
        AccountManager.getInstance().registerAccount(bitstampAccount, false, false);
        AccountManager.getInstance().registerAccount(coinbaseAccount, false, false);

        // when
        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());
        onView(withText("Accounts")).perform(click());
        onView(withText("Coinbase")).perform(click());

        // The current TOTP code and time-wheel should be displayed for the newly linked account
        onView(withId(R.id.code_box)).check(matches(isDisplayed()));
        onView(withId(R.id.code_box)).check(matches(withText(generateTOTP(
                coinbaseAccount.getSecretKey(), (long) getTC(30), 6, SHA1))));
    }
}
