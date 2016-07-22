package net.cryptodirect.authenticator;

import org.json.JSONObject;

import java.io.Serializable;

public abstract class Centurion implements Serializable
{
    private static final long serialVersionUID = 1L;

    public abstract JSONObject get(String path);
    public abstract JSONObject post(String path, String payload);
}