package net.cryptodirect.authenticator;

import java.util.List;
import java.util.Locale;
import java.util.Map;

public enum Issuer
{
    BITFINEX(0, R.drawable.bitfinex),
    BITSTAMP(1, R.drawable.bitstamp),
    COINBASE(2, R.drawable.coinbase),
    CRYPTODASH(3, R.drawable.cryptodash),
    UNKNOWN(-1, -1);

    private final int id;
    private final int drawable;

    Issuer(int id, int drawable)
    {
        this.id = id;
        this.drawable = drawable;
    }

    public int getId()
    {
        return id;
    }

    public int getDrawable()
    {
        return drawable;
    }

    public String getLabel()
    {
        switch (this)
        {
            case BITFINEX:
                return "QR Code Generated On:";
            case BITSTAMP:
                return "Bitstamp Account Id:";
            case COINBASE:
                return "Coinbase Account Email:";
            case CRYPTODASH:
                return "Cryptodash Account Email:";
            case UNKNOWN:
            default:
                return null;
        }
    }

    public String getDisplayableLabel(String label)
    {
        switch (this)
        {
            case BITFINEX:
                return label.substring(label.indexOf('-') + 1);
            case BITSTAMP:
                return label.split("@")[0];
            case COINBASE:
            case CRYPTODASH:
                return label;
            case UNKNOWN:
            default:
                return null;
        }
    }

    @Override
    public String toString()
    {
        return name().substring(0, 1) + name().substring(1).toLowerCase(Locale.US);
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
