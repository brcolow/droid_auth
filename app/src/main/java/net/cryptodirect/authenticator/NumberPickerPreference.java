/*
 * Copyright (C) 2011 The CyanogenMod Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.cryptodirect.authenticator;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.NumberPicker;

import java.lang.reflect.Field;

public class NumberPickerPreference extends DialogPreference
{
    private int mMin, mMax, mDefault;
    private String mMaxExternalKey, mMinExternalKey;
    private NumberPicker mNumberPicker;
    private static final String TAG = NumberPickerPreference.class.getSimpleName();

    public NumberPickerPreference(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        TypedArray numberPickerType = context.obtainStyledAttributes(attrs,
                R.styleable.NumberPickerPreference, 0, 0);

        mMaxExternalKey = numberPickerType.getString(R.styleable.NumberPickerPreference_maxExternal);
        mMinExternalKey = numberPickerType.getString(R.styleable.NumberPickerPreference_minExternal);

        mMax = numberPickerType.getInt(R.styleable.NumberPickerPreference_max, 5);
        mMin = numberPickerType.getInt(R.styleable.NumberPickerPreference_min, 0);

        mDefault = mMin;
        // dialogType.recycle();
        numberPickerType.recycle();
    }

    @Override
    protected View onCreateDialogView()
    {
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

        LayoutInflater inflater =
                (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.number_picker_dialog, null);
        setWidgetLayoutResource(R.layout._current_number);
        notifyChanged();
        mNumberPicker = (NumberPicker) view.findViewById(R.id.number_picker);

        if (mNumberPicker == null)
        {
            throw new RuntimeException("mNumberPicker is null!");
        }

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
}