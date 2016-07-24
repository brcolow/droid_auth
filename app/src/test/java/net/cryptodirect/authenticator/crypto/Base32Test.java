package net.cryptodirect.authenticator.crypto;

import org.junit.Test;

import static net.cryptodirect.authenticator.StandardCharsets.UTF_8;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * Base32 (RFC 4648) decoding tests. Tests use the official test vectors from the
 * RFC as well as some test's from tcl's test suite.
 *
 * @see <a href="https://tools.ietf.org/html/rfc4648#section-10">RFC 4648 Test Vectors</a>
 * @see <a href="http://opensource.apple.com//source/tcl/tcl-87/tcl_ext/tcllib/tcllib/modules/base32/base32.testsuite">Tcl Test Suite</a>
 */
public class Base32Test
{
    @Test
    public void testBase32DecodingRFC4648Vectors()
    {
        assertThat(Base32.getDecoder().decode(""), is("".getBytes(UTF_8)));
        assertThat(Base32.getDecoder().decode("MY======"), is("f".getBytes(UTF_8)));
        assertThat(Base32.getDecoder().decode("MZXQ===="), is("fo".getBytes(UTF_8)));
        assertThat(Base32.getDecoder().decode("MZXW6==="), is("foo".getBytes(UTF_8)));
        assertThat(Base32.getDecoder().decode("MZXW6YQ="), is("foob".getBytes(UTF_8)));
        assertThat(Base32.getDecoder().decode("MZXW6YTB"), is("fooba".getBytes(UTF_8)));
        assertThat(Base32.getDecoder().decode("MZXW6YTBOI======"), is("foobar".getBytes(UTF_8)));
    }

    @Test
    public void testBase32DecodingForConformance()
    {
        assertThat(Base32.getDecoder().decode("EA======"), is(" ".getBytes(UTF_8)));
        assertThat(Base32.getDecoder().decode("EAQA===="), is("  ".getBytes(UTF_8)));
        assertThat(Base32.getDecoder().decode("EAQCA==="), is("   ".getBytes(UTF_8)));
        assertThat(Base32.getDecoder().decode("EAQCAIA="), is("    ".getBytes(UTF_8)));
        assertThat(Base32.getDecoder().decode("EAQCAIBA"), is("     ".getBytes(UTF_8)));
        assertThat(Base32.getDecoder().decode("EAQCAIBAEA======"), is("      ".getBytes(UTF_8)));
    }

    @Test
    public void testBase32DecodingBadInput()
    {
        try
        {
            Base32.getDecoder().decode("abcde0aa");
            fail("Expected an IllegalArgumentException to be thrown");
        }
        catch (IllegalArgumentException exception)
        {
            assertThat(exception.getMessage(), is("Invalid Base32 character 0 at index 5"));
        }

        try
        {
            Base32.getDecoder().decode("A");
            fail("Expected an IllegalArgumentException to be thrown");
        }
        catch (IllegalArgumentException exception)
        {
            assertThat(exception.getMessage(), is("Length of encoded string was not a multiple " +
                    "of 8 but was 1"));
        }

        try
        {
            Base32.getDecoder().decode("ABCDEFG");
            fail("Expected an IllegalArgumentException to be thrown");
        }
        catch (IllegalArgumentException exception)
        {
            assertThat(exception.getMessage(), is("Length of encoded string was not a multiple " +
                    "of 8 but was 7"));
        }

        try
        {
            Base32.getDecoder().decode("A=======");
            fail("Expected an IllegalArgumentException to be thrown");
        }
        catch (IllegalArgumentException exception)
        {
            assertThat(exception.getMessage(), is("Invalid padding of length 7"));
        }

        try
        {
            Base32.getDecoder().decode("ACA=====");
            fail("Expected an IllegalArgumentException to be thrown");
        }
        catch (IllegalArgumentException exception)
        {
            assertThat(exception.getMessage(), is("Invalid padding of length 5"));
        }
    }

}
