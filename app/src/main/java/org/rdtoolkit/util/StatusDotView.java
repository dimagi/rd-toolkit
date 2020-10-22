package org.rdtoolkit.util;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import org.rdtoolkit.R;

public class StatusDotView extends LinearLayout {

    int maxDots;
    int listLength;
    int currentItem;

    ViewGroup baseView;
    LayoutInflater inflater;
    View[] dots;

    public StatusDotView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        inflater = LayoutInflater.from(context);
        baseView = (ViewGroup)inflater.inflate(R.layout.component_status_dots_base, this);


        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.StatusDots,
                0, 0);

        try {
            maxDots = a.getInteger(R.styleable.StatusDots_maxDots, 10);
            listLength = a.getInteger(R.styleable.StatusDots_listLength, maxDots);
            currentItem = a.getInteger(R.styleable.StatusDots_currentItem, 0);
        } finally {
            a.recycle();
        }
        buildViews();
    }

    private void buildViews() {
        baseView.removeAllViews();
        dots = new View[listLength];
        for (int i = 0 ; i < listLength; ++i) {
            dots[i] = inflater.inflate(R.layout.component_status_dot, baseView, false);
            baseView.addView(dots[i]);
        }
        setCurrentItem(currentItem);
    }

    public void setListLength(int listLength) {
        if (listLength > maxDots) {
            listLength = maxDots;
        }
        this.listLength = listLength;
        this.currentItem = 0;
        buildViews();
    }

    public int getListLength() {
        return listLength;
    }

    public void setCurrentItem(int currentItem) {
        if (currentItem >= maxDots) {
            currentItem = maxDots -1;
        }
        this.currentItem = currentItem;
        for (int i = 0 ; i < listLength; ++i) {
            if (i == currentItem) {
                ((ImageView) dots[i].findViewById(R.id.status_dots_base_dot_inactive)).setVisibility(View.INVISIBLE);
                ((ImageView) dots[i].findViewById(R.id.status_dots_base_dot_active)).setVisibility(View.VISIBLE);
            } else {
                ((ImageView) dots[i].findViewById(R.id.status_dots_base_dot_inactive)).setVisibility(View.VISIBLE);
                ((ImageView) dots[i].findViewById(R.id.status_dots_base_dot_active)).setVisibility(View.INVISIBLE);
            }
        }
    }

    public int getCurrentItem() {
        return currentItem;
    }
}
