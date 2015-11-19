package net.cryptodirect.authenticator.crypto;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;


public class AccountParseTest
{
    @Test
    public void testUriParse() throws URISyntaxException
    {
        URI uri = new URI("otpauth://totp/test@gmail.com?secret=OEbnsWQidFxYp3TUcAqzREIuywzD7Gz3wFZhLz8qXEI=&issuer=Cryptodash&base=64");
        // TODO
        // test parsing the above URI with Account.parse
        System.out.println("Scheme: " + uri.getScheme());
        System.out.println("Host: " + uri.getHost());
        System.out.println("Path: " + uri.getPath());
        System.out.println("Query: " + uri.getQuery());
    }
}
