package net.cryptodirect.authenticator;

import java.util.HashMap;
import java.util.Map;

public class InvalidOptAuthUriException extends Exception
{
    private final UriErrorCode error;
    private Map<String, Object> properties;

    public InvalidOptAuthUriException(UriErrorCode error)
    {
        this.error = error;
    }

    public InvalidOptAuthUriException set(String name, Object value)
    {
        if (properties == null)
        {
            properties = new HashMap<>();
        }
        properties.put(name, value);
        return this;
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String name)
    {
        return (T)properties.get(name);
    }
}
