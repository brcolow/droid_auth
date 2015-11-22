package net.cryptodirect.authenticator.crypto;

import java.io.Serializable;

public enum CodeType implements Serializable
{
    HOTP,
    TOTP;

    public static CodeType getCodeType(String codeType)
    {
        switch (codeType)
        {
            case "HOTP":
                return HOTP;
            case "TOTP":
                return TOTP;
            default:
                throw new IllegalArgumentException("Unknown code type: " + codeType);
        }
    }
}
