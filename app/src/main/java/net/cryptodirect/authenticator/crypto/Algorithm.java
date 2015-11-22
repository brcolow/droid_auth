package net.cryptodirect.authenticator.crypto;

import java.io.Serializable;

public enum Algorithm implements Serializable
{
    SHA1,
    SHA256,
    SHA512;

    public static Algorithm getAlgorithm(String algorithm)
    {
        switch (algorithm)
        {
            case "SHA1":
                return SHA1;
            case "SHA256":
                return SHA256;
            case "SHA512":
                return SHA512;
            default:
                throw new IllegalArgumentException("Unknown algorithm: " + algorithm);
        }
    }
}
