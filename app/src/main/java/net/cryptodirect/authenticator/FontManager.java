package net.cryptodirect.authenticator;

import android.content.Context;
import android.graphics.Typeface;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FontManager
{
    private static final FontManager INSTANCE = new FontManager();
    private static final String ANON_FONT_PATH = "fonts/Anonymous_Pro.otf";
    private static final Map<String, Typeface> TYPEFACE_CACHE = new ConcurrentHashMap<>();

    public static FontManager getInstance()
    {
        return INSTANCE;
    }

    public synchronized void init(Context context)
    {
        TYPEFACE_CACHE.put("ANONYMOUS_PRO", Typeface.createFromAsset(context.getAssets(), ANON_FONT_PATH));
    }

    public Typeface getTypeface(String typeface)
    {
        return TYPEFACE_CACHE.get(typeface);
    }
}
