package org.rdtoolkit.util;


import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.TypedValue;

import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.widget.TextViewCompat;

import org.rdtoolkit.R;

public class ResizingTextView extends AppCompatTextView {

    private int minTextSize;
    private int maxTextSize;
    private int granularity;

    public ResizingTextView(Context context) {
        super(context);
        init();
    }

    public ResizingTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ResizingTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        minTextSize = TextViewCompat.getAutoSizeMinTextSize(this);
        maxTextSize = TextViewCompat.getAutoSizeMaxTextSize(this);
        granularity = Math.max(1, TextViewCompat.getAutoSizeStepGranularity(this));
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        // this method is called on every setText
        disableAutoSizing();
        setTextSize(TypedValue.COMPLEX_UNIT_PX, maxTextSize);
        super.setText(text, type);
        post(this::enableAutoSizing); // enable after the view is laid out and measured at max text size
    }

    private void disableAutoSizing() {
        TextViewCompat.setAutoSizeTextTypeWithDefaults(this, TextViewCompat.AUTO_SIZE_TEXT_TYPE_NONE);
    }

    private void enableAutoSizing() {
        TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(this,
                minTextSize, maxTextSize, granularity, TypedValue.COMPLEX_UNIT_PX);
    }
}