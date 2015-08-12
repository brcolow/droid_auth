package net.cryptodirect.authenticator;

import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FontManager
{
    private static FontManager INSTANCE = new FontManager();
    private static Context context;
    private static final String ANON_FONT_PATH = "fonts/Anonymous_Pro.otf";
    private static final Map<String, Typeface> typefaceCache = new ConcurrentHashMap<>();
    private static final String TAG = FontManager.class.getSimpleName();

    public static FontManager getInstance()
    {
        return INSTANCE;
    }

    public synchronized void init(Context context)
    {
        Typeface anonymousProTypeface = Typeface.createFromAsset(context.getAssets(), ANON_FONT_PATH);
        Log.i(TAG, "Adding anonymous pro typeface to cache: " + anonymousProTypeface);
        typefaceCache.put("ANONYMOUS_PRO", anonymousProTypeface);
    }

    public Typeface getTypeface(String typeface)
    {
        return typefaceCache.get(typeface);
    }
}
