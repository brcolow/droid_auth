package net.cryptodirect.authenticator.crypto;

import java.io.Serializable;

public class CodeParams implements Serializable
{
    private final CodeType codeType;
    private final Algorithm algorithm;
    private final int digits;
    private final int hotpCounter;
    private final int totpPeriod;
    private final Base base;

    private CodeParams(Builder builder)
    {
        this.codeType = builder.codeType;
        this.algorithm = builder.algorithm;
        this.digits = builder.digits;
        this.hotpCounter = builder.hotpCounter;
        this.totpPeriod = builder.totpPeriod;
        this.base = builder.base;
    }

    public CodeType getCodeType()
    {
        return codeType;
    }

    public Algorithm getAlgorithm()
    {
        return algorithm;
    }

    public int getDigits()
    {
        return digits;
    }

    public int getHotpCounter()
    {
        return hotpCounter;
    }

    public int getTotpPeriod()
    {
        return totpPeriod;
    }

    public Base getBase()
    {
        return base;
    }

    public static class Builder
    {
        private final CodeType codeType;
        private Algorithm algorithm = Defaults.ALGORITHM;
        private int digits = Defaults.DIGITS;
        private int hotpCounter = -1;
        private int totpPeriod = Defaults.TOTP_PERIOD;
        private Base base = Base.BASE32;

        public Builder(CodeType codeType)
        {
            this.codeType = codeType;
        }

        public Builder algorithm(Algorithm algorithm)
        {
            this.algorithm = algorithm;
            return this;
        }

        public Builder digits(int digits)
        {
            this.digits = digits;
            return this;
        }

        public Builder hotpCounter(int hotpCounter)
        {
            if (codeType != CodeType.HOTP && hotpCounter != -1)
            {
                throw new IllegalArgumentException("Code type must be HOTP when specifying hotpCounter");
            }
            this.hotpCounter = hotpCounter;
            return this;
        }

        public Builder totpPeriod(int totpPeriod)
        {
            if (codeType != CodeType.TOTP && totpPeriod != -1)
            {
                throw new IllegalArgumentException("Code type must be TOTP when specifying totpPeriod");
            }

            this.totpPeriod = totpPeriod;
            return this;
        }

        public Builder base(int base)
        {
            switch (base)
            {
                case 32:
                    this.base = Base.BASE32;
                    break;
                case 64:
                    this.base = Base.BASE64;
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported base: " + base + " must be 32 or 64");
            }

            return this;
        }

        public CodeParams build()
        {
            return new CodeParams(this);
        }
    }

    /**
     * Code parameter default values when not specified explicitly in the otpauth URI
     */
    public static class Defaults
    {
        public static final Algorithm ALGORITHM = Algorithm.SHA1;
        public static final int DIGITS = 6;
        public static final int TOTP_PERIOD = 30;
    }
}
