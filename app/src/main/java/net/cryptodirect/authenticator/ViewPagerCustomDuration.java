package net.cryptodirect.authenticator;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.animation.Interpolator;
import android.widget.Scroller;

import org.acra.ACRA;

import java.lang.reflect.Field;

/**
 * Based on this Stackoverflow answer:
 * http://stackoverflow.com/questions/8155257/slowing-speed-of-viewpager-controller-in-android
 * <p/>
 * <p>This allows for setting a duration factor to slow down the transition speed
 * between page transformations of a ViewPager.</p>
 */
public class ViewPagerCustomDuration extends ViewPager
{
    public ViewPagerCustomDuration(Context context)
    {
        super(context);
        postInitViewPager();
        this.setOffscreenPageLimit(2);
    }

    public ViewPagerCustomDuration(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        postInitViewPager();
    }

    private ScrollerCustomDuration scroller = null;

    /**
     * Override the Scroller instance with our own class so we can change the
     * duration
     */
    private void postInitViewPager()
    {
        try
        {
            Class<?> viewpager = ViewPager.class;
            Field scroller = viewpager.getDeclaredField("mScroller");
            scroller.setAccessible(true);
            Field interpolator = viewpager.getDeclaredField("sInterpolator");
            interpolator.setAccessible(true);

            this.scroller = new ScrollerCustomDuration(getContext(),
                    (Interpolator) interpolator.get(null));
            scroller.set(this, this.scroller);
        }
        catch (Exception e)
        {
            ACRA.getErrorReporter().handleException(e);
        }
    }

    /**
     * Set the factor by which the duration will change
     */
    public void setScrollDurationFactor(double scrollFactor)
    {
        scroller.setScrollDurationFactor(scrollFactor);
    }

    private static class ScrollerCustomDuration extends Scroller
    {
        private double scrollFactor = 1;

        public ScrollerCustomDuration(Context context, Interpolator interpolator)
        {
            super(context, interpolator);
        }

        /**
         * Set the factor by which the duration will change
         */
        public void setScrollDurationFactor(double scrollFactor)
        {
            this.scrollFactor = scrollFactor;
        }

        @Override
        public void startScroll(int startX, int startY, int dx, int dy, int duration)
        {
            super.startScroll(startX, startY, dx, dy, (int) (duration * scrollFactor));
        }

    }
}