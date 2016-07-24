package net.cryptodirect.authenticator;

import java.util.List;
import java.util.Map;

public enum Issuer
{
    BITFINEX(0),
    BITSTAMP(1),
    COINBASE(2),
    CRYPTODASH(3),
    UNKNOWN(-1);

    private final int id;

    Issuer(int id)
    {
        this.id = id;
    }

    public int getId()
    {
        return id;
    }

    public static Issuer getIssuer(int id)
    {
        for (Issuer issuer : Issuer.values())
        {
            if (issuer.id == id)
            {
                return issuer;
            }
        }

        return null;
    }

    public static Issuer detect(String label, Map<String, List<String>> queryParams)
    {
        if (queryParams.containsKey("issuer") && !queryParams.get("issuer").isEmpty())
        {
            Issuer result = Issuer.UNKNOWN;
            boolean matched = false;
            for (Issuer issuer : Issuer.values())
            {
                for (String value : queryParams.get("issuer"))
                {
                    if (issuer.name().equalsIgnoreCase(value))
                    {
                        if (result == Issuer.UNKNOWN)
                        {
                            result = issuer;
                            matched = true;
                        }
                        else
                        {
                            result = Issuer.UNKNOWN;
                            break;
                        }
                    }
                }

                if (matched && result == Issuer.UNKNOWN)
                {
                    // there were multiple issuer query params that matched
                    break;
                }
            }

            if (matched && result != Issuer.UNKNOWN)
            {
                return result;
            }
        }

        // We could not detect who issued the code so we will try and detect via the label
        if (label.matches("^\\d+@Bitstamp$"))
        {
            // 872472@Bitstamp
            return Issuer.BITSTAMP;
        }
        else if (label.matches("^Bitfinex-[A-Z][a-z]{2,3}-\\d+-\\d{4}$"))
        {
            // Bitfinex-Jul-23-2016
            return Issuer.BITFINEX;
        }
        else
        {
            return Issuer.UNKNOWN;
        }
    }
}
