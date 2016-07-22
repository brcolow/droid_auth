package net.cryptodirect.authenticator;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class MockCenturion extends Centurion
{
    private final Map<String, String> getResponses;
    private final Map<PathPayloadPair, String> postResponses;

    public MockCenturion()
    {
        getResponses = new HashMap<>();
        postResponses = new HashMap<>();
    }

    public void setGetResponse(String path, String response)
    {
        if (path == null)
        {
            throw new IllegalArgumentException("path must not be null");
        }
        if (response == null)
        {
            throw new IllegalArgumentException("response must not be null");
        }

        getResponses.put(path, response);
    }

    public void setPostResponse(String path, String payload, String response)
    {
        if (path == null)
        {
            throw new IllegalArgumentException("path must not be null");
        }

        if (payload == null)
        {
            throw new IllegalArgumentException("payload must not be null");
        }

        if (response == null)
        {
            throw new IllegalArgumentException("response must not be null");
        }

        postResponses.put(new PathPayloadPair(path, payload), response);
    }

    @Override
    public JSONObject get(String path)
    {
        try
        {
            return new JSONObject(getResponses.get(path));
        }
        catch (JSONException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public JSONObject post(String path, String payload)
    {
        try
        {
            return new JSONObject(postResponses.get(new PathPayloadPair(path, payload)));
        }
        catch (JSONException e)
        {
            throw new RuntimeException(e);
        }
    }

    private static class PathPayloadPair implements Serializable
    {
        private static final long serialVersionUID = 1L;

        private final String path;
        private final String payload;

        public PathPayloadPair(String path, String payload)
        {
            if (path == null)
            {
                throw new IllegalArgumentException("path must not be null");
            }

            if (payload == null)
            {
                throw new IllegalArgumentException("payload must not be null");
            }

            this.path = path;
            this.payload = payload;
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

            PathPayloadPair other = (PathPayloadPair) o;

            return path.equals(other.path) && payload.equals(other.payload);
        }

        @Override
        public int hashCode()
        {
            int result = path.hashCode();
            result = 31 * result + payload.hashCode();
            return result;
        }
    }
}
