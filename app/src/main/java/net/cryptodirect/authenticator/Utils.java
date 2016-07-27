package net.cryptodirect.authenticator;

import android.graphics.Color;
import android.os.Build;
import android.view.View;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

public class Utils
{
    public static final Pattern BASE64_KEY_REGEX = Pattern.compile("^[a-zA-Z0-9+/]+=$");
    public static final Pattern VALID_BASE64_CHARS_REGEX = Pattern.compile("[a-zA-Z0-9+/=]+");

    public static final Pattern BASE32_KEY_REGEX = Pattern.compile("^[A-Z2-7]+$");
    public static final Pattern VALID_BASE32_CHARS_REGEX = Pattern.compile("[A-Z2-7]+");

    public static final int NEXUS_6_HEIGHT_PIXELS = 2368;
    public static final int NEXUS_6_DPI = 640;

    public enum MaterialDesignColors
    {
        // reds
        MD_RED_300(Color.parseColor("#E57373")),
        MD_RED_600(Color.parseColor("#E53935")),

        //greens
        MD_GREEN_300(Color.parseColor("#81C784")),
        MD_GREEN_500(Color.parseColor("#4CAF50"));

        private final int color;

        MaterialDesignColors(int color)
        {
            this.color = color;
        }

        public int getColor()
        {
            return color;
        }
    }

    private static final AtomicInteger sNextGeneratedId = new AtomicInteger(1);

    public static int generateViewId()
    {
        if (Build.VERSION.SDK_INT < 17)
        {
            for (; ; )
            {
                final int result = sNextGeneratedId.get();
                int newValue = result + 1;
                if (newValue > 0x00FFFFFF)
                {
                    newValue = 1; // Roll over to 1, not 0.
                }
                if (sNextGeneratedId.compareAndSet(result, newValue))
                {
                    return result;
                }
            }
        }
        else
        {
            return View.generateViewId();
        }
    }
}
