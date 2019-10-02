package com.dragonide.maxmusic;

import android.content.Context;
import android.util.AttributeSet;

/**
 * Created by Ankit on 10-07-2016.
 */
public class NavigationCheckBox extends ExtendedCheckBoxPref
{

    public NavigationCheckBox(Context context) {
        super(context);
    }

    public NavigationCheckBox(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public NavigationCheckBox(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public NavigationCheckBox(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public boolean isEnabled() {
        return MediaUtils.isLollipop();
    }
}
