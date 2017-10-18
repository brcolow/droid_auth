package net.cryptodirect.authenticator;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Paint;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import java.lang.reflect.Field;

public class NumberPickerPreference extends DialogPreference
{
    private final int mMin, mMax, mDefault;
    private final String mMaxExternalKey, mMinExternalKey;
    private NumberPicker mNumberPicker;
    private static final String TAG = NumberPickerPreference.class.getSimpleName();

    public NumberPickerPreference(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        setDialogLayoutResource(R.layout.number_picker_dialog);
        TypedArray numberPickerType = context.obtainStyledAttributes(attrs,
                R.styleable.NumberPickerPreference, 0, 0);

        mMaxExternalKey = numberPickerType.getString(R.styleable.NumberPickerPreference_maxExternal);
        mMinExternalKey = numberPickerType.getString(R.styleable.NumberPickerPreference_minExternal);

        mMax = numberPickerType.getInt(R.styleable.NumberPickerPreference_max, 5);
        mMin = numberPickerType.getInt(R.styleable.NumberPickerPreference_min, 0);

        mDefault = mMin;
        numberPickerType.recycle();
    }

    @Override
    protected View onCreateDialogView()
    {
        View view = super.onCreateDialogView();
        int max = mMax;
        int min = mMin;

        // External values
        if (mMaxExternalKey != null)
        {
            max = getSharedPreferences().getInt(mMaxExternalKey, mMax);
        }
        if (mMinExternalKey != null)
        {
            min = getSharedPreferences().getInt(mMinExternalKey, mMin);
        }

        //LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        //View view = inflater.inflate(R.layout.number_picker_dialog, null);
        setWidgetLayoutResource(R.layout._current_number);
        notifyChanged();
        mNumberPicker = view.findViewById(R.id.number_picker);

        if (mNumberPicker == null)
        {
            throw new RuntimeException("mNumberPicker is null");
        }

        setNumberPickerTextColor(mNumberPicker, "#b8943b");
        // Initialize state
        mNumberPicker.setWrapSelectorWheel(false);
        mNumberPicker.setMaxValue(max);
        mNumberPicker.setMinValue(min);
        mNumberPicker.setValue(getPersistedInt(mDefault));

        // No keyboard popup
        disableTextInput(mNumberPicker);
        return view;
    }

    @Override
    protected void onDialogClosed(boolean positiveResult)
    {
        if (positiveResult)
        {
            persistInt(mNumberPicker.getValue());
            Log.i(TAG, "Calling change listener with new value: " + mNumberPicker.getValue());
            callChangeListener(mNumberPicker.getValue());
            notifyChanged();
        }
    }

    /*
     * reflection of NumberPicker.java
     * verified in 4.1, 4.2
     * */
    private void disableTextInput(NumberPicker np)
    {
        if (np == null)
        {
            return;
        }
        Class<?> classType = np.getClass();
        Field inputTextField;
        try
        {
            inputTextField = classType.getDeclaredField("mInputText");
            inputTextField.setAccessible(true);
            EditText textInput = (EditText) inputTextField.get(np);
            if (textInput != null)
            {
                textInput.setCursorVisible(false);
                textInput.setFocusable(false);
                textInput.setFocusableInTouchMode(false);
            }
        }
        catch (Exception e)
        {
            Log.d(TAG, "NumberPickerPreference disableTextInput error", e);
        }
    }

    private static boolean setNumberPickerTextColor(NumberPicker numberPicker, String colorString)
    {
        final int count = numberPicker.getChildCount();
        for (int i = 0; i < count; i++)
        {
            View child = numberPicker.getChildAt(i);
            if (child instanceof EditText)
            {
                try
                {
                    Field selectorWheelPaintField = numberPicker.getClass()
                            .getDeclaredField("mSelectorWheelPaint");
                    selectorWheelPaintField.setAccessible(true);
                    int color = Color.parseColor(colorString);
                    ((Paint) selectorWheelPaintField.get(numberPicker)).setColor(color);
                    ((EditText) child).setTextColor(color);

                    Field selectorWheelTextSizeField = numberPicker.getClass()
                            .getDeclaredField("mTextSize");
                    selectorWheelTextSizeField.setAccessible(true);
                    selectorWheelTextSizeField.setInt(numberPicker, 24);
                    ((EditText) child).setTextSize(24f);
                    numberPicker.invalidate();
                    return true;
                }
                catch(NoSuchFieldException | IllegalAccessException | IllegalArgumentException e)
                {
                    Log.w(TAG, e);
                }
            }
        }
        return false;
    }
}