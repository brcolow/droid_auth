package net.cryptodirect.authenticator;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;

/**
 * A NumberPicker that allows for us to easily (i.e. without reflection)
 * style the text size and color of the selector wheel.
 *
 * @see <a href="http://stackoverflow.com/questions/6958460/android-can-i-increase-the-textsize-
 * for-the-numberpicker-widget/12084420#12084420">Stack Overflow: Increase textsize for
 * numberpicker widget</a>
 */
public class NumberPicker extends android.widget.NumberPicker
{
    public NumberPicker(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    @Override
    public void addView(View child)
    {
        super.addView(child);
        updateView(child);
    }

    @Override
    public void addView(View child, int index, android.view.ViewGroup.LayoutParams params)
    {
        super.addView(child, index, params);
        updateView(child);
    }

    @Override
    public void addView(View child, android.view.ViewGroup.LayoutParams params)
    {
        super.addView(child, params);
        updateView(child);
    }

    private void updateView(View view)
    {
        if (view instanceof EditText)
        {
            ((EditText) view).setTextSize(25);
            ((EditText) view).setTextColor(Color.parseColor("#b8943b"));
        }
    }
}