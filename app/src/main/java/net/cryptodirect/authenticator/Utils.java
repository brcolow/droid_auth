package net.cryptodirect.authenticator;

import android.graphics.Color;

public class Utils
{
    public enum MaterialDesignColors
    {
        // reds
        MD_RED_50(Color.parseColor("#FFEBEE")),
        MD_RED_100(Color.parseColor("#FFCDD2")),
        MD_RED_600(Color.parseColor("#E53935")),

        //greens
        MD_GREEN_500(Color.parseColor("#4CAF50")),
        MD_GREEN_800(Color.parseColor("#2E7D32"));


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
}
