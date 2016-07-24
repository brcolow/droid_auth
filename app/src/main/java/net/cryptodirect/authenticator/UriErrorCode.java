package net.cryptodirect.authenticator;

public enum UriErrorCode
{
    PARSE_ERROR(100),
    INVALID_PROTOCOL(101),
    INVALID_HOST(102),
    EMPTY_PATH(103),
    UNKNOWN_ISSUER(104),
    HOTP_MISSING_COUNTER(201),
    HOTP_MULTIPLE_COUNTERS(202),
    HOTP_NEGATIVE_COUNTER(203),
    MISSING_SECRET(301),
    MULTIPLE_SECRETS(302),
    MULTIPLE_BASES(401),
    UNSUPPORTED_BASE(402),
    MULTIPLE_ALGORITHMS(501),
    UNSUPPORTED_ALGORITHM(502),
    MULTIPLE_DIGITS(601),
    UNSUPPORTED_DIGITS(602),
    MULTIPLE_PERIODS(701),
    TOTP_NON_POSITIVE_PERIOD(702);

    private final int errorCode;

    private UriErrorCode(int errorCode)
    {
        this.errorCode = errorCode;
    }

    public int getErrorCode()
    {
        return errorCode;
    }
}
