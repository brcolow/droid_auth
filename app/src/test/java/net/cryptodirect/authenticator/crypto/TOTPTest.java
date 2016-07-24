package net.cryptodirect.authenticator.crypto;

import org.junit.Test;

import static net.cryptodirect.authenticator.StandardCharsets.US_ASCII;
import static net.cryptodirect.authenticator.crypto.Algorithm.SHA1;
import static net.cryptodirect.authenticator.crypto.Algorithm.SHA256;
import static net.cryptodirect.authenticator.crypto.Algorithm.SHA512;
import static net.cryptodirect.authenticator.crypto.TOTP.generateTOTP;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Based on test vectors provided in Table 1: TOTP Table of RFC 6238.
 *
 * @see <a href="https://tools.ietf.org/html/rfc6238">RFC 6238</a>
 */
public class TOTPTest
{
    private static final byte[] KEY_20_BYTES = "12345678901234567890".getBytes(US_ASCII);
    private static final byte[] KEY_32_BYTES = "12345678901234567890123456789012".getBytes(US_ASCII);
    private static final byte[] KEY_64_BYTES = "1234567890123456789012345678901234567890123456789012345678901234".getBytes(US_ASCII);

    @Test
    public void testTOTPSha1()
    {
        assertThat(generateTOTP(KEY_20_BYTES, TOTP.getTC(59, 30), 8, SHA1), is("94287082"));
        assertThat(generateTOTP(KEY_20_BYTES, TOTP.getTC(1111111109L, 30), 8, SHA1), is("07081804"));
        assertThat(generateTOTP(KEY_20_BYTES, TOTP.getTC(1111111111L, 30), 8, SHA1), is("14050471"));
        assertThat(generateTOTP(KEY_20_BYTES, TOTP.getTC(1234567890L, 30), 8, SHA1), is("89005924"));
        assertThat(generateTOTP(KEY_20_BYTES, TOTP.getTC(2000000000L, 30), 8, SHA1), is("69279037"));
        assertThat(generateTOTP(KEY_20_BYTES, TOTP.getTC(20000000000L, 30), 8, SHA1), is("65353130"));
    }

    @Test
    public void testTOTPSha256()
    {
        assertThat(generateTOTP(KEY_32_BYTES, TOTP.getTC(59, 30), 8, SHA256), is("46119246"));
        assertThat(generateTOTP(KEY_32_BYTES, TOTP.getTC(1111111109L, 30), 8, SHA256), is("68084774"));
        assertThat(generateTOTP(KEY_32_BYTES, TOTP.getTC(1111111111L, 30), 8, SHA256), is("67062674"));
        assertThat(generateTOTP(KEY_32_BYTES, TOTP.getTC(1234567890L, 30), 8, SHA256), is("91819424"));
        assertThat(generateTOTP(KEY_32_BYTES, TOTP.getTC(2000000000L, 30), 8, SHA256), is("90698825"));
        assertThat(generateTOTP(KEY_32_BYTES, TOTP.getTC(20000000000L, 30), 8, SHA256), is("77737706"));
    }

    @Test
    public void testTOTPSha512()
    {
        assertThat(generateTOTP(KEY_64_BYTES, TOTP.getTC(59, 30), 8, SHA512), is("90693936"));
        assertThat(generateTOTP(KEY_64_BYTES, TOTP.getTC(1111111109L, 30), 8, SHA512), is("25091201"));
        assertThat(generateTOTP(KEY_64_BYTES, TOTP.getTC(1111111111L, 30), 8, SHA512), is("99943326"));
        assertThat(generateTOTP(KEY_64_BYTES, TOTP.getTC(1234567890L, 30), 8, SHA512), is("93441116"));
        assertThat(generateTOTP(KEY_64_BYTES, TOTP.getTC(2000000000L, 30), 8, SHA512), is("38618901"));
        assertThat(generateTOTP(KEY_64_BYTES, TOTP.getTC(20000000000L, 30), 8, SHA512), is("47863826"));
    }
}
