package net.cryptodirect.authenticator.crypto;

import android.util.AndroidRuntimeException;
import android.util.Log;

import org.joda.time.DateTime;

import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class TOTP
{
    //private static final DateTime UNIX_EPOCH = new DateTime(0);

    private static final int[] DIGITS_POWER
            = { 1, 10, 100, 1000, 10000, 100000, 1000000, 10000000, 100000000 };

    private static final String TAG = TOTP.class.getSimpleName();

    private TOTP()
    {
    }

    /**
     * Returns the counter value - the number of durations {@code TS} seconds
     * long that have occurred from now since the Unix epoch.
     *
     * @param TS the duration in seconds
     * @return the corresponding TC counter value
     */
    public static long getTC(int TS)
    {
        return getTC((DateTime.now().getMillis() / 1000L), TS, 0);
    }

    /**
     * Returns the counter value - the number of durations {@code TS} seconds
     * long that have occurred from the given {@code time} since the Unix epoch.
     *
     * E.g. if the given time is 1111111109 and the given {@code TS}
     * is 30, then the returned TC will be 37037037
     * @param time the number of seconds since the Unix epoch
     * @param TS the duration in seconds
     * @return the corresponding TC counter value
     */
    public static long getTC(long time, int TS)
    {
        return getTC(time, TS, 0);
    }

    /**
     * Returns the counter value - the number of durations {@code TS} seconds
     * long that have occurred from the given {@code time} since the given
     * epoch {@code T0}.
     *
     * @param time the number of seconds since T0
     * @param TS the duration in seconds
     * @param T0 the initial time epoch
     * @return the corresponding TC counter value
     */
    public static long getTC(long time, int TS, int T0)
    {
        return (time - T0) / TS;
    }
    /**
     * <p>Generates an RFC 6238 compliant TOTP value using SHA1. Argument names
     * are taken directly from RFC 6238.</p>
     *
     * @see <a href="https://tools.ietf.org/html/rfc6238">RFC 6238</a>
     * @param K the agreed upon key
     * @param TC the current time counter
     * @param N the number of digits the returned token should have
     * @return the TOTP token in base 10 that includes {@code N} digits
     */
    public static String generateTOTPSha1(byte[] K, long TC, int N)
    {
        return generateTOTP(K, TC, N, "HmacSHA1");
    }

    /**
     * <p>Generates an RFC 6238 compliant TOTP value using SHA-256. Argument names
     * are taken directly from RFC 6238.</p>
     *
     * @see <a href="https://tools.ietf.org/html/rfc6238">RFC 6238</a>
     * @param K the agreed upon key
     * @param TC the current time counter
     * @param N the number of digits the returned token should have
     * @return the TOTP token in base 10 that includes {@code N} digits
     */
    public static String generateTOTPSha256(byte[] K, long TC, int N)
    {
        return generateTOTP(K, TC, N, "HmacSHA256");
    }

    /**
     * <p>Generates an RFC 6238 compliant TOTP value using SHA-512. Argument names
     * are taken directly from RFC 6238.</p>
     *
     * @see <a href="https://tools.ietf.org/html/rfc6238">RFC 6238</a>
     * @param K the agreed upon key
     * @param TC the current time counter
     * @param N the number of digits the returned token should have
     * @return the TOTP token in base 10 that includes {@code N} digits
     */
    public static String generateTOTPSha512(byte[] K, long TC, int N)
    {
        return generateTOTP(K, TC, N, "HmacSHA512");
    }

    private static String generateTOTP(byte[] K, long TC, int N, String sha)
    {
        byte[] rawTC = ByteBuffer.allocate(8).putLong(TC).array();
        byte[] hash = hmacSha(K, rawTC, sha);

        // put selected bytes into result int
        int offset = hash[hash.length - 1] & 0xf;

        int binary = ((hash[offset] & 0x7f) << 24)
                | ((hash[offset + 1] & 0xff) << 16)
                | ((hash[offset + 2] & 0xff) << 8)
                | (hash[offset + 3] & 0xff);

        int otp = binary % DIGITS_POWER[N];
        // prepend zeros to result to get N digits
        String result = Integer.toString(otp);
        while (result.length() < N) {
            result = "0" + result;
        }
        return result;
    }

    private static byte[] hmacSha(byte[] key, byte[] value, String sha)
    {
        try
        {
            SecretKeySpec secret = new SecretKeySpec(key, "RAW");
            Mac mac = Mac.getInstance(sha);
            mac.init(secret);
            return mac.doFinal(value);
        }
        catch (GeneralSecurityException e)
        {
            Log.e(TAG, "Could not execute HMAC-SHA1", e);
            throw new AndroidRuntimeException(e);
        }
    }

}
