package net.cryptodirect.authenticator.crypto;

import static net.cryptodirect.authenticator.crypto.Algorithm.*;

import static org.junit.Assert.assertEquals;

import net.cryptodirect.authenticator.StandardCharsets;

import org.junit.Test;

/**
 * Based on test vectors provided in Table 1: TOTP Table of RFC 6238.
 *
 * @see <a href="https://tools.ietf.org/html/rfc6238">RFC 6238</a>
 */
public class TOTPTest
{
    private static final byte[] KEY_20_BYTES = "12345678901234567890".getBytes(StandardCharsets.US_ASCII);
    private static final byte[] KEY_32_BYTES = "12345678901234567890123456789012".getBytes(StandardCharsets.US_ASCII);
    private static final byte[] KEY_64_BYTES = "1234567890123456789012345678901234567890123456789012345678901234".getBytes(StandardCharsets.US_ASCII);

    @Test
    public void testTOTPSha1()
    {
        assertEquals(TOTP.generateTOTP(KEY_20_BYTES, TOTP.getTC(59, 30), 8, SHA1), "94287082");
        assertEquals(TOTP.generateTOTP(KEY_20_BYTES, TOTP.getTC(1111111109L, 30), 8, SHA1), "07081804");
        assertEquals(TOTP.generateTOTP(KEY_20_BYTES, TOTP.getTC(1111111111L, 30), 8, SHA1), "14050471");
        assertEquals(TOTP.generateTOTP(KEY_20_BYTES, TOTP.getTC(1234567890L, 30), 8, SHA1), "89005924");
        assertEquals(TOTP.generateTOTP(KEY_20_BYTES, TOTP.getTC(2000000000L, 30), 8, SHA1), "69279037");
        assertEquals(TOTP.generateTOTP(KEY_20_BYTES, TOTP.getTC(20000000000L, 30), 8, SHA1), "65353130");
    }

    @Test
    public void testTOTPSha256()
    {
        assertEquals(TOTP.generateTOTP(KEY_32_BYTES, TOTP.getTC(59, 30), 8, SHA256), "46119246");
        assertEquals(TOTP.generateTOTP(KEY_32_BYTES, TOTP.getTC(1111111109L, 30), 8, SHA256), "68084774");
        assertEquals(TOTP.generateTOTP(KEY_32_BYTES, TOTP.getTC(1111111111L, 30), 8, SHA256), "67062674");
        assertEquals(TOTP.generateTOTP(KEY_32_BYTES, TOTP.getTC(1234567890L, 30), 8, SHA256), "91819424");
        assertEquals(TOTP.generateTOTP(KEY_32_BYTES, TOTP.getTC(2000000000L, 30), 8, SHA256), "90698825");
        assertEquals(TOTP.generateTOTP(KEY_32_BYTES, TOTP.getTC(20000000000L, 30), 8, SHA256), "77737706");
    }

    @Test
    public void testTOTPSha512()
    {
        assertEquals(TOTP.generateTOTP(KEY_64_BYTES, TOTP.getTC(59, 30), 8, SHA512), "90693936");
        assertEquals(TOTP.generateTOTP(KEY_64_BYTES, TOTP.getTC(1111111109L, 30), 8, SHA512), "25091201");
        assertEquals(TOTP.generateTOTP(KEY_64_BYTES, TOTP.getTC(1111111111L, 30), 8, SHA512), "99943326");
        assertEquals(TOTP.generateTOTP(KEY_64_BYTES, TOTP.getTC(1234567890L, 30), 8, SHA512), "93441116");
        assertEquals(TOTP.generateTOTP(KEY_64_BYTES, TOTP.getTC(2000000000L, 30), 8, SHA512), "38618901");
        assertEquals(TOTP.generateTOTP(KEY_64_BYTES, TOTP.getTC(20000000000L, 30), 8, SHA512), "47863826");
    }
}
