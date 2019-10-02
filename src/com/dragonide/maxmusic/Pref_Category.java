package com.dragonide.maxmusic;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

public class Pref_Category extends PreferenceCategory {


	public Pref_Category(Context context) {
		super(context);

	}

	public Pref_Category(Context context, AttributeSet attrs) {
		super(context, attrs);

	}

	public Pref_Category(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);

	}

	public Pref_Category(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);

	}

	@Override
	protected void onBindView(View view) {
		// TODO Auto-generated method stub
		super.onBindView(view);

				((TextView) view.findViewById(android.R.id.title)).setTextColor(PrefUtils.getInt(getContext(),PrefKeys.ACCENT_INT, PrefDefaults.ACCENT_DEF));
	}

}
