package net.cryptodirect.authenticator.crypto;

import net.cryptodirect.authenticator.Account;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import java.net.URISyntaxException;

/**
 * Account optauth URI string parsing tests
 */
public class AccountParseTest
{
    @Test
    public void testCryptodashStyleUri() throws URISyntaxException
    {
        Account account = Account.parse("otpauth://totp/test@gmail.com?secret=Ne2WwwXegWzlHuD0eriSXx1EaxmQiEW8QMCRcn5RJ78=&issuer=Cryptodash&base=64&algorithm=SHA256");
        assertEquals(account.getEmail(), "test@gmail.com");
        assertEquals(account.getIssuer(), "Cryptodash");
        assertNotNull(account.getSecretKey());
        assertEquals(account.getSecretKey().length, 32);
        assertEquals(account.getBase64EncodedSecretKey(), "Ne2WwwXegWzlHuD0eriSXx1EaxmQiEW8QMCRcn5RJ78=");
        assertEquals(account.getCodeParams().getAlgorithm(), Algorithm.SHA256);
        assertEquals(account.getCodeParams().getBase(), Base.BASE64);
        assertEquals(account.getCodeParams().getCodeType(), CodeType.TOTP);
        assertEquals(account.getCodeParams().getDigits(), 6);
        assertEquals(account.getCodeParams().getTotpPeriod(), 30);
    }

    @Test
    public void testCoinbaseStyleUri() throws URISyntaxException
    {
        Account account = Account.parse("otpauth://totp/test@gmail.com?secret=5NIWJET3BG6NUA3U&issuer=Coinbase");
        assertEquals(account.getEmail(), "test@gmail.com");
        assertEquals(account.getIssuer(), "Coinbase");
        assertNotNull(account.getSecretKey());
        assertEquals(account.getSecretKey().length, 10);
        assertEquals(account.getCodeParams().getAlgorithm(), Algorithm.SHA1);
        assertEquals(account.getCodeParams().getBase(), Base.BASE32);
        assertEquals(account.getCodeParams().getCodeType(), CodeType.TOTP);
        assertEquals(account.getCodeParams().getDigits(), 6);
        assertEquals(account.getCodeParams().getTotpPeriod(), 30);
    }
}
