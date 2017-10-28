package net.cryptodirect.authenticator;

import net.cryptodirect.authenticator.crypto.Algorithm;
import net.cryptodirect.authenticator.crypto.Base32;
import net.cryptodirect.authenticator.crypto.Base64;
import net.cryptodirect.authenticator.crypto.CodeParams;
import net.cryptodirect.authenticator.crypto.CodeType;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static net.cryptodirect.authenticator.StandardCharsets.UTF_8;
import static net.cryptodirect.authenticator.crypto.TOTP.generateTOTP;
import static net.cryptodirect.authenticator.crypto.TOTP.getTC;

public class Account implements Serializable
{
    private final String label;
    private final Issuer issuer;
    private final byte[] secretKey;
    private final CodeParams codeParams;
    static final long serialVersionUID = 1L;

    Account(String label, Issuer issuer, byte[] secretKey, CodeParams codeParams)
    {
        this.label = label;
        this.issuer = issuer;
        this.secretKey = secretKey;
        this.codeParams = codeParams;
    }

    public String getLabel()
    {
        return label;
    }

    public Issuer getIssuer()
    {
        return issuer;
    }

    public byte[] getSecretKey()
    {
        return secretKey;
    }

    public String getBase64EncodedSecretKey()
    {
        return Base64.getEncoder().encodeToString(secretKey);
    }

    public CodeParams getCodeParams()
    {
        return codeParams;
    }

    public String getOneTimePasscode()
    {
        return generateTOTP(secretKey, getTC(getCodeParams().getTotpPeriod()),
                codeParams.getDigits(), codeParams.getAlgorithm());
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        Account account = (Account) o;

        return label.equals(account.label) &&
                issuer == account.issuer &&
                Arrays.equals(secretKey, account.secretKey) &&
                codeParams.equals(account.codeParams);
    }

    @Override
    public int hashCode()
    {
        int result = label.hashCode();
        result = 31 * result + issuer.hashCode();
        result = 31 * result + Arrays.hashCode(secretKey);
        result = 31 * result + codeParams.hashCode();
        return result;
    }

    @Override
    public String toString()
    {
        return "Account{" +
                "label='" + label + '\'' +
                ", issuer='" + issuer + '\'' +
                ", secretKey=" + Arrays.toString(secretKey) +
                ", codeParams=" + codeParams +
                '}';
    }

    /**
     * Returns an Account instance corresponding to an optauth
     * URI (used, for example, by Google Authenticator). The URI
     * has the form:
     * <p/>
     * otpauth://totp/account?secret=baseNEncodedSecret&issuer=issuerName&base=N
     * <p/>
     * Where the base parameter is <em>optional</em> and if not present in
     * the URI, defaults to Base32 (as this is what most Google Authenticator
     * compliant optauth URIs use and they don't use the base parameter)
     *
     * @param uriString the optauth URI String
     * @return the Account instance corresponding to the given uri
     * @throws InvalidOptAuthUriException if the given uriString is invalid
     */
    public static Account parse(String uriString) throws InvalidOptAuthUriException
    {
        URI uri;
        try
        {
            uri = new URI(uriString);
        }
        catch (URISyntaxException e)
        {
            throw new InvalidOptAuthUriException(UriErrorCode.PARSE_ERROR);
        }
        if (!uri.getScheme().equalsIgnoreCase("otpauth"))
        {
            throw new InvalidOptAuthUriException(UriErrorCode.INVALID_PROTOCOL)
                    .set("protocol", uri.getScheme());
        }

        CodeType codeType;
        if (!uri.getHost().equalsIgnoreCase("totp") && !uri.getHost().equalsIgnoreCase("hotp"))
        {
            throw new InvalidOptAuthUriException(UriErrorCode.INVALID_HOST)
                    .set("host", uri.getHost());
        }
        else
        {
            codeType = uri.getHost().equalsIgnoreCase("totp") ? CodeType.TOTP : CodeType.HOTP;
        }

        String accountLabel = uri.getPath();
        while (accountLabel.charAt(0) == '/')
        {
            accountLabel = accountLabel.substring(1);
        }

        if (accountLabel.isEmpty())
        {
            throw new InvalidOptAuthUriException(UriErrorCode.EMPTY_PATH)
                    .set("uri", uri.toString());
        }

        Map<String, List<String>> queryParams = splitQuery(uri);

        // Try and detect who issued this code
        Issuer issuer = Issuer.detect(accountLabel, queryParams);

        if (issuer == Issuer.UNKNOWN)
        {
            throw new InvalidOptAuthUriException(UriErrorCode.UNKNOWN_ISSUER);
        }

        int counter = -1;
        if (codeType == CodeType.HOTP)
        {
            if (!queryParams.containsKey("counter"))
            {
                throw new InvalidOptAuthUriException(UriErrorCode.HOTP_MISSING_COUNTER);
            }
            else if (queryParams.get("counter").size() != 1)
            {
                throw new InvalidOptAuthUriException(UriErrorCode.HOTP_MULTIPLE_COUNTERS)
                        .set("counters", queryParams.get("counter"));
            }
            else
            {
                counter = Integer.valueOf(queryParams.get("counter").get(0));
                if (counter < 0)
                {
                    throw new InvalidOptAuthUriException(UriErrorCode.HOTP_NEGATIVE_COUNTER)
                            .set("counter", counter);
                }
            }
        }

        if (!queryParams.containsKey("secret"))
        {
            throw new InvalidOptAuthUriException(UriErrorCode.MISSING_SECRET);
        }
        else if (queryParams.get("secret").size() != 1)
        {
            throw new InvalidOptAuthUriException(UriErrorCode.MULTIPLE_SECRETS);
        }

        int base = CodeParams.Defaults.BASE;
        if (queryParams.containsKey("base"))
        {
            if (queryParams.get("base").size() != 1)
            {
                throw new InvalidOptAuthUriException(UriErrorCode.MULTIPLE_BASES);
            }
            else
            {
                if (!queryParams.get("base").get(0).equals("32") &&
                        !queryParams.get("base").get(0).equals("64"))
                {
                    throw new InvalidOptAuthUriException(UriErrorCode.UNSUPPORTED_BASE)
                            .set("base", queryParams.get("base").get(0));
                }
                else
                {
                    base = Integer.valueOf(queryParams.get("base").get(0));
                }
            }
        }

        byte[] key;

        if (base == 32)
        {
            key = Base32.getDecoder().decode(queryParams.get("secret").get(0));
        }
        else
        {
            key = Base64.getDecoder().decode(queryParams.get("secret").get(0));
        }

        /*
        // TODO decide if we really want this behavior (that the base32 secret must contain no padding)
        if (base == 32)
        {
            if (queryParams.get("secret").get(0).length() % 8 != 0)
            {
                throw new IllegalArgumentException("Base-32 secret key must have N chars where " +
                        "N is a multiple of 8 but length was: " + queryParams.get("secret").get(0).length());
            }
        }
        */

        Algorithm algorithm = CodeParams.Defaults.ALGORITHM;
        if (queryParams.containsKey("algorithm"))
        {
            if (queryParams.get("algorithm").size() != 1)
            {
                throw new InvalidOptAuthUriException(UriErrorCode.MULTIPLE_ALGORITHMS)
                        .set("algorithms", queryParams.get("algorithm"));
            }
            else
            {
                String algo = queryParams.get("algorithm").get(0).toUpperCase(Locale.US);
                switch (algo)
                {
                    case "SHA1":
                        algorithm = Algorithm.SHA1;
                        break;
                    case "SHA256":
                        algorithm = Algorithm.SHA256;
                        break;
                    case "SHA512":
                        algorithm = Algorithm.SHA512;
                        break;
                    default:
                        throw new InvalidOptAuthUriException(UriErrorCode.UNSUPPORTED_ALGORITHM)
                                .set("algorithm", algo);
                }

            }
        }

        int digits = CodeParams.Defaults.DIGITS;
        if (queryParams.containsKey("digits"))
        {
            if (queryParams.get("digits").size() != 1)
            {
                throw new InvalidOptAuthUriException(UriErrorCode.MULTIPLE_DIGITS)
                        .set("digits", queryParams.get("digits"));
            }
            else
            {
                int d = Integer.valueOf(queryParams.get("digits").get(0));
                switch (d)
                {
                    case 6:
                    case 7:
                    case 8:
                        digits = d;
                        break;
                    default:
                        throw new InvalidOptAuthUriException(UriErrorCode.UNSUPPORTED_DIGITS)
                                .set("digits", d);
                }
            }
        }

        int period = CodeParams.Defaults.TOTP_PERIOD;
        if (queryParams.containsKey("period"))
        {
            if (queryParams.get("period").size() != 1)
            {
                throw new InvalidOptAuthUriException(UriErrorCode.MULTIPLE_PERIODS)
                        .set("periods", queryParams.get("period"));
            }
            else
            {
                int p = Integer.valueOf(queryParams.get("period").get(0));
                if (p <= 0)
                {
                    throw new InvalidOptAuthUriException(UriErrorCode.TOTP_NON_POSITIVE_PERIOD)
                            .set("period", p);
                }
                else
                {
                    period = p;
                }
            }
        }

        return new Account(accountLabel, issuer, key, new CodeParams.Builder(codeType)
                .algorithm(algorithm).digits(digits).totpPeriod(period).hotpCounter(counter)
                .base(base).build());
    }

    private static Map<String, List<String>> splitQuery(URI uri)
    {
        final Map<String, List<String>> keyValuePairs = new LinkedHashMap<>();
        final String[] pairs = uri.getQuery().split("&");
        for (String pair : pairs)
        {
            final int index = pair.indexOf("=");
            final String key;
            try
            {
                key = index > 0 ? URLDecoder.decode(pair.substring(0, index), UTF_8.name())
                        .toLowerCase(Locale.US).replace(' ', '+') : pair.toLowerCase(Locale.US);
                if (!keyValuePairs.containsKey(key)) {
                    keyValuePairs.put(key, new LinkedList<>());
                }
                final String value = index > 0 && pair.length() > index + 1 ?
                        URLDecoder.decode(pair.substring(index + 1), UTF_8.name())
                                .replace(' ', '+') : null;
                keyValuePairs.get(key).add(value);
            }
            catch (UnsupportedEncodingException e)
            {
                // can't happen (wish URLDecoder took a Charset instead of String)
                throw new RuntimeException(e);
            }
        }
        return keyValuePairs;
    }
}
