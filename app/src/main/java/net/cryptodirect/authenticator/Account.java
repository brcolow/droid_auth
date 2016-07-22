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

/**
 * Represents a registered Cryptodash account, consisting of
 * an email and secret key bytes to generate the TOTP codes.
 */
public class Account implements Serializable
{
    private final String email;
    private final String issuer;
    private final byte[] secretKey;
    private final CodeParams codeParams;
    static final long serialVersionUID = 1L;

    public Account(String email, String issuer, byte[] secretKey, CodeParams codeParams)
    {
        this.email = email;
        this.issuer = issuer;
        this.secretKey = secretKey;
        this.codeParams = codeParams;
    }

    public String getEmail()
    {
        return email;
    }

    public String getIssuer()
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

        return email.equals(account.email) &&
                issuer.equals(account.issuer) &&
                Arrays.equals(secretKey, account.secretKey) &&
                codeParams.equals(account.codeParams);
    }

    @Override
    public int hashCode()
    {
        int result = email.hashCode();
        result = 31 * result + issuer.hashCode();
        result = 31 * result + Arrays.hashCode(secretKey);
        result = 31 * result + codeParams.hashCode();
        return result;
    }

    @Override
    public String toString()
    {
        return "Account{" +
                "email='" + email + '\'' +
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
     * @throws URISyntaxException if the given uriString is invalid
     */
    public static Account parse(String uriString) throws URISyntaxException
    {
        // otpauth://totp/test@gmail.com?secret=OEbnsWQidFxYp3TUcAqzREIuywzD7Gz3wFZhLz8qXEI%3D&issuer=Cryptodash&base=64&algorithm=SHA1&digits=6&period=30
        URI uri = new URI(uriString);
        if (!uri.getScheme().equals("otpauth"))
        {
            throw new URISyntaxException(uriString, "Does not have scheme \"otpauth\" " +
                    "but was: " + uri.getScheme());
        }

        CodeType codeType;
        if (!uri.getHost().equals("totp") && !uri.getHost().equals("hotp"))
        {
            throw new URISyntaxException(uriString, "Does not have host of either " +
                    "\"totp\" or \"hotp\" but was: " + uri.getHost());
        }
        else
        {
            codeType = uri.getHost().equals("totp") ? CodeType.TOTP : CodeType.HOTP;
        }

        String accountLabel = uri.getPath().substring(1);
        while (accountLabel.charAt(0) == '/') {
            accountLabel = accountLabel.substring(1);
        }

        if (accountLabel.isEmpty()) {
            throw new URISyntaxException(uriString, "Path must be non-empty");
        }

        Map<String, List<String>> queryParams = splitQuery(uri);

        int counter = -1;
        if (codeType == CodeType.HOTP)
        {
            if (queryParams.containsKey("counter") || queryParams.get("counter").size() != 1)
            {
                throw new URISyntaxException(uriString, "Counter query parameter must be " +
                        "specified exactly once when using HOTP code type");
            }
            else
            {
                counter = Integer.valueOf(queryParams.get("counter").get(0));
                if (counter < 0)
                {
                    throw new URISyntaxException(uriString, "Counter query parameter must be " +
                            "positive but was: " + counter);
                }
            }
        }

        if (!queryParams.containsKey("secret") || queryParams.get("secret").size() != 1)
        {
            throw new URISyntaxException(uriString, "Invalid secret query parameter");
        }

        int base = -1;
        if (queryParams.containsKey("base"))
        {
            if (queryParams.get("base").size() != 1)
            {
                throw new URISyntaxException(uriString,
                        "Base query parameter specified more than once");
            }
            else
            {
                if (!queryParams.get("base").get(0).equals("32") &&
                        !queryParams.get("base").get(0).equals("64"))
                {
                    throw new URISyntaxException(uriString, "Unsupported base for key param: " +
                            queryParams.get("base").get(0));
                }
                else
                {
                    base = queryParams.get("base").get(0).equals("32") ? 32 : 64;
                }
            }
        }

        if (base == -1)
        {
            // use default base 32
            base = 32;
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

        String issuer = "Unknown";
        if (queryParams.containsKey("issuer"))
        {
            issuer = queryParams.get("issuer").get(0);
        }

        Algorithm algorithm = CodeParams.Defaults.ALGORITHM;
        if (queryParams.containsKey("algorithm"))
        {
            if (queryParams.get("algorithm").size() != 1)
            {
                throw new URISyntaxException(uriString, "Algorithm query parameter specified more than once");
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
                        throw new URISyntaxException(uriString, "Unsupported algorithm: " + algo +
                                " - must be one of [SHA1, SHA256, SHA512]");
                }

            }
        }

        int digits = CodeParams.Defaults.DIGITS;
        if (queryParams.containsKey("digits"))
        {
            if (queryParams.get("digits").size() != 1)
            {
                throw new URISyntaxException(uriString, "Digits query parameter specified more than once");
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
                        throw new URISyntaxException(uriString, "Unsupported number of digits: " +
                                digits + " - must 6, 7, or 8");
                }
            }
        }

        int period = CodeParams.Defaults.TOTP_PERIOD;
        if (queryParams.containsKey("period"))
        {
            if (queryParams.get("period").size() != 1)
            {
                throw new URISyntaxException(uriString, "Period query parameter specified more than once");
            }
            else
            {
                int p = Integer.valueOf(queryParams.get("period").get(0));
                if (p <= 0)
                {
                    throw new URISyntaxException(uriString, "Period query parameter must be greater than 0");
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
        for (String pair : pairs) {
            final int index = pair.indexOf("=");
            final String key;
            try
            {
                key = index > 0 ? URLDecoder.decode(pair.substring(0, index), UTF_8.name())
                        .replace(' ', '+') : pair;
                if (!keyValuePairs.containsKey(key)) {
                    keyValuePairs.put(key, new LinkedList<String>());
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
