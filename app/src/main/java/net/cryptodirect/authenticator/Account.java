package net.cryptodirect.authenticator;

/**
 * Created by brcolow on 8/9/2015.
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
