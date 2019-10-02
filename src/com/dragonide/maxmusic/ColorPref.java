package com.dragonide.maxmusic;


import android.content.Context;
import android.content.SharedPreferences;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.afollestad.materialdialogs.color.CircleView;

public class ColorPref extends Preference {
    private int color1, color2;
    private View mView;
    CircleView circle;

    public ColorPref(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ColorPref(Context context) {
        this(context, null, 0);
    }

    public ColorPref(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setWidgetLayoutResource(R.layout.preference_color);


    }

    @Override
    protected void onBindView(View view) {

        super.onBindView(view);
        ((TextView) view.findViewById(android.R.id.title)).setTextSize(16);


        switch (getKey().toString()) {
            case "primary_color_int":
                color1 = PrefUtils.getInt(getContext(), PrefKeys.PRIMARY_INT, PrefDefaults.PRIMARY_DEF);
                color2 = PrefUtils.getInt(getContext(), PrefKeys.ACCENT_INT, PrefDefaults.ACCENT_DEF);

                break;
            case "accent_color_int":
                color1 = PrefUtils.getInt(getContext(), PrefKeys.ACCENT_INT, PrefDefaults.ACCENT_DEF);
                color2 = PrefUtils.getInt(getContext(), PrefKeys.PRIMARY_INT, PrefDefaults.PRIMARY_DEF);

                break;


        }


        mView = view;
        circle = (CircleView) mView.findViewById(R.id.circdale);
        circle.setBackgroundColor(color1);

    }

}
