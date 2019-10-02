package com.dragonide.maxmusic;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by Ankit on 31-07-2016.
 */
public class CustomToast extends Toast {
    @Override
    public void setDuration(int duration) {
        super.setDuration(duration);
    }

    public CustomToast(Context context) {
        super(context);
    }

}
