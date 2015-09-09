package net.cryptodirect.authenticator;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.support.v4.content.ContextCompat;

public class Utils
{
    public static Drawable getTintedDrawable(Context context,
                                             @DrawableRes int drawableResId,
                                             int red, int green, int blue)
    {
        Drawable drawable = ContextCompat.getDrawable(context, drawableResId);
        drawable.setColorFilter(getIntFromRGB(red, green, blue), PorterDuff.Mode.SRC_IN);
        return drawable;
    }

    public static int getIntFromRGB(int red, int green, int blue)
    {
        red = (red << 16) & 0x00FF0000;
        green = (green << 8) & 0x0000FF00;
        blue = blue & 0x000000FF;

        return 0xFF000000 | red | green | blue;
    }
}
