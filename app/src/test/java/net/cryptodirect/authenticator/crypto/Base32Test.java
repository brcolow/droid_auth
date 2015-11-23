package net.cryptodirect.authenticator.crypto;

import net.cryptodirect.authenticator.StandardCharsets;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;

/**
 * Base32 (RFC 4648) tests
 *
 * @see <a href="https://tools.ietf.org/html/rfc4648#section-10">RFC 4648 Test Vectors</a>
 */
public class Base32Test
{
    @Test
    public void testBase32DecodingRFC4648Vectors()
    {
        assertArrayEquals(Base32.getDecoder().decode(""), "".getBytes(StandardCharsets.UTF_8));
        assertArrayEquals(Base32.getDecoder().decode("MY======"), "f".getBytes(StandardCharsets.UTF_8));
        assertArrayEquals(Base32.getDecoder().decode("MZXQ===="), "fo".getBytes(StandardCharsets.UTF_8));
        assertArrayEquals(Base32.getDecoder().decode("MZXW6==="), "foo".getBytes(StandardCharsets.UTF_8));
        assertArrayEquals(Base32.getDecoder().decode("MZXW6YQ="), "foob".getBytes(StandardCharsets.UTF_8));
        assertArrayEquals(Base32.getDecoder().decode("MZXW6YTB"), "fooba".getBytes(StandardCharsets.UTF_8));
        assertArrayEquals(Base32.getDecoder().decode("MZXW6YTBOI======"), "foobar".getBytes(StandardCharsets.UTF_8));
    }
}
