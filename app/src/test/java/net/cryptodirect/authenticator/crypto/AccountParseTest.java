package net.cryptodirect.authenticator.crypto;

import net.cryptodirect.authenticator.Account;

import org.junit.Test;

import static net.cryptodirect.authenticator.Issuer.BITFINEX;
import static net.cryptodirect.authenticator.Issuer.BITSTAMP;
import static net.cryptodirect.authenticator.Issuer.COINBASE;
import static net.cryptodirect.authenticator.Issuer.CRYPTODASH;
import static net.cryptodirect.authenticator.crypto.Algorithm.SHA1;
import static net.cryptodirect.authenticator.crypto.Algorithm.SHA256;
import static net.cryptodirect.authenticator.crypto.Base.BASE32;
import static net.cryptodirect.authenticator.crypto.Base.BASE64;
import static net.cryptodirect.authenticator.crypto.CodeType.TOTP;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Tests that the various optauth URIs provided by the Issuers we support
 * are correctly parsed by {@link Account#parse(String)}.
 */
public class AccountParseTest
{
    @Test
    public void shouldCorrectlyParseCryptodashStyleUri() throws Exception
    {
        Account account = Account.parse("otpauth://totp/test@gmail.com?secret=Ne2WwwXegWzl" +
                "HuD0eriSXx1EaxmQiEW8QMCRcn5RJ78=&issuer=Cryptodash&base=64&algorithm=SHA256");
        assertThat(account.getLabel(), is("test@gmail.com"));
        assertThat(account.getIssuer(), is(CRYPTODASH));
        assertThat(account.getSecretKey(), is(new byte[]{53, -19, -106, -61, 5, -34, -127, 108,
                -27, 30, -32, -12, 122, -72, -110, 95, 29, 68, 107, 25, -112, -120, 69, -68, 64,
                -64, -111, 114, 126, 81, 39, -65}));
        assertThat(account.getBase64EncodedSecretKey(),
                is("Ne2WwwXegWzlHuD0eriSXx1EaxmQiEW8QMCRcn5RJ78="));
        assertThat(account.getCodeParams().getAlgorithm(), is(SHA256));
        assertThat(account.getCodeParams().getBase(), is(BASE64));
        assertThat(account.getCodeParams().getCodeType(), is(TOTP));
        assertThat(account.getCodeParams().getDigits(), is(6));
        assertThat(account.getCodeParams().getTotpPeriod(), is(30));
    }

    @Test
    public void shouldCorrectlyParseCoinbaseStyleUri() throws Exception
    {
        Account account = Account.parse("otpauth://totp/test@gmail.com?secret=5NIWJET3BG6N" +
                "UA3U&issuer=Coinbase");
        assertThat(account.getLabel(), is("test@gmail.com"));
        assertThat(account.getIssuer(), is(COINBASE));
        assertThat(account.getSecretKey(), is(new byte[]{-21, 81, 100, -110, 123, 9, -68, -38, 3,
                116}));
        assertThat(account.getCodeParams().getAlgorithm(), is(SHA1));
        assertThat(account.getCodeParams().getBase(), is(BASE32));
        assertThat(account.getCodeParams().getCodeType(), is(TOTP));
        assertThat(account.getCodeParams().getDigits(), is(6));
        assertThat(account.getCodeParams().getTotpPeriod(), is(30));
    }

    @Test
    public void shouldCorrectlyParseBitstampStyleUri() throws Exception
    {
        // otpauth://totp/872472@Bitstamp?secret=KJCDKRSZKNKFOWSU
        Account account = Account.parse("otpauth://totp/872472@Bitstamp?secret=KJCDKRSZKNKFOWSU");
        assertThat(account.getLabel(), is("872472@Bitstamp"));
        assertThat(account.getIssuer(), is(BITSTAMP));
        assertThat(account.getSecretKey(), is(new byte[]{82, 68, 53, 70, 89, 83, 84, 87, 90, 84}));
        assertThat(account.getCodeParams().getAlgorithm(), is(SHA1));
        assertThat(account.getCodeParams().getBase(), is(BASE32));
        assertThat(account.getCodeParams().getCodeType(), is(TOTP));
        assertThat(account.getCodeParams().getDigits(), is(6));
        assertThat(account.getCodeParams().getTotpPeriod(), is(30));
    }

    @Test
    public void shouldCorrectlyParseBitfinexStyleUri() throws Exception
    {
        // otpauth://totp/Bitfinex-Jul-23-2016?secret=qpni5ijdl54kd7xv
        Account account = Account.parse("otpauth://totp/Bitfinex-Jul-23-2016?secret=qpni5ijdl54kd7xv");
        assertThat(account.getLabel(), is("Bitfinex-Jul-23-2016"));
        assertThat(account.getIssuer(), is(BITFINEX));
        assertThat(account.getSecretKey(), is(new byte[]{-125, -38, -114, -95, 35, 95, 120, -95,
                -2, -11}));
        assertThat(account.getCodeParams().getAlgorithm(), is(SHA1));
        assertThat(account.getCodeParams().getBase(), is(BASE32));
        assertThat(account.getCodeParams().getCodeType(), is(TOTP));
        assertThat(account.getCodeParams().getDigits(), is(6));
        assertThat(account.getCodeParams().getTotpPeriod(), is(30));
    }
}
