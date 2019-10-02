package com.dragonide.maxmusic;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.preference.CheckBoxPreference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

public class ExtendedCheckBoxPref extends CheckBoxPreference {


	public ExtendedCheckBoxPref(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public ExtendedCheckBoxPref(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public ExtendedCheckBoxPref(Context context, AttributeSet attrs,
			int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		// TODO Auto-generated constructor stub
	}

	@TargetApi(21)
	public ExtendedCheckBoxPref(Context context, AttributeSet attrs,
			int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		// TODO Auto-generated constructor stub
	}


	@Override
	protected void onBindView(View view) {
		// TODO Auto-generated method stub
		super.onBindView(view);
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getContext());


		try {
			if (MediaUtils.isLollipop()) {
				((CheckBox) view.findViewById(android.R.id.checkbox))
						.setButtonTintList((ColorStateList
								.valueOf(sp.getInt(PrefKeys.ACCENT_INT, PrefDefaults.ACCENT_DEF))));
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		view.findViewById(android.R.id.checkbox)
				.setClickable(true);
		view.findViewById(android.R.id.checkbox).animate();
	}

	

}
