package com.dragonide.maxmusic;

import android.R.color;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

public class Extended_preference extends Preference {

	public Extended_preference(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public Extended_preference(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public Extended_preference(Context context, AttributeSet attrs,
			int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		// TODO Auto-generated constructor stub
	}

	public Extended_preference(Context context, AttributeSet attrs,
			int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onBindView(View view) {
		// TODO Auto-generated method stub
		super.onBindView(view);


		((TextView) view.findViewById(android.R.id.title))
				.setTextColor(PrefUtils.getInt(getContext(),PrefKeys.ACCENT_INT, PrefDefaults.ACCENT_DEF));
		
	}

}
