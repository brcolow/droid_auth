package net.cryptodirect.authenticator.crypto;

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
        assertEquals(TOTP.generateTOTPSha1(KEY_20_BYTES, TOTP.getTC(59, 30), 8), "94287082");
        assertEquals(TOTP.generateTOTPSha1(KEY_20_BYTES, TOTP.getTC(1111111109L, 30), 8), "07081804");
        assertEquals(TOTP.generateTOTPSha1(KEY_20_BYTES, TOTP.getTC(1111111111L, 30), 8), "14050471");
        assertEquals(TOTP.generateTOTPSha1(KEY_20_BYTES, TOTP.getTC(1234567890L, 30), 8), "89005924");
        assertEquals(TOTP.generateTOTPSha1(KEY_20_BYTES, TOTP.getTC(2000000000L, 30), 8), "69279037");
        assertEquals(TOTP.generateTOTPSha1(KEY_20_BYTES, TOTP.getTC(20000000000L, 30), 8), "65353130");
    }

    @Test
    public void testTOTPSha256()
    {
        assertEquals(TOTP.generateTOTPSha256(KEY_32_BYTES, TOTP.getTC(59, 30), 8), "46119246");
        assertEquals(TOTP.generateTOTPSha256(KEY_32_BYTES, TOTP.getTC(1111111109L, 30), 8), "68084774");
        assertEquals(TOTP.generateTOTPSha256(KEY_32_BYTES, TOTP.getTC(1111111111L, 30), 8), "67062674");
        assertEquals(TOTP.generateTOTPSha256(KEY_32_BYTES, TOTP.getTC(1234567890L, 30), 8), "91819424");
        assertEquals(TOTP.generateTOTPSha256(KEY_32_BYTES, TOTP.getTC(2000000000L, 30), 8), "90698825");
        assertEquals(TOTP.generateTOTPSha256(KEY_32_BYTES, TOTP.getTC(20000000000L, 30), 8), "77737706");
    }

    @Test
    public void testTOTPSha512()
    {
        assertEquals(TOTP.generateTOTPSha512(KEY_64_BYTES, TOTP.getTC(59, 30), 8), "90693936");
        assertEquals(TOTP.generateTOTPSha512(KEY_64_BYTES, TOTP.getTC(1111111109L, 30), 8), "25091201");
        assertEquals(TOTP.generateTOTPSha512(KEY_64_BYTES, TOTP.getTC(1111111111L, 30), 8), "99943326");
        assertEquals(TOTP.generateTOTPSha512(KEY_64_BYTES, TOTP.getTC(1234567890L, 30), 8), "93441116");
        assertEquals(TOTP.generateTOTPSha512(KEY_64_BYTES, TOTP.getTC(2000000000L, 30), 8), "38618901");
        assertEquals(TOTP.generateTOTPSha512(KEY_64_BYTES, TOTP.getTC(20000000000L, 30), 8), "47863826");
    }
}
