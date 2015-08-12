package net.cryptodirect.authenticator;

/**
 * Represents a registered Cryptodash account, consisting of
 * an email and secret key bytes to generate the TOTP codes.
 */
public class Account
{
    private final String email;
    private final String secretKey;

    public Account(String email, String secretKey)
    {
        this.email = email;
        this.secretKey = secretKey;
    }

    public String getEmail()
    {
        return email;
    }

    public String getSecretKey()
    {
        return secretKey;
    }
}
