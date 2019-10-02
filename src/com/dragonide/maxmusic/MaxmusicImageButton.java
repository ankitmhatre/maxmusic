/*
 * Copyright (C) 2015-2016 Adrian Ulrich <adrian@blinkenlights.ch>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>. 
 */


package com.dragonide.maxmusic;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.support.v7.widget.AppCompatImageButton;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.ImageButton;

public class MaxmusicImageButton extends AppCompatImageButton {
    private int mVibrant, mVibrantDark, mVibrantLight, mMuted, mMutedDark, mMutedLight;

    private Context mContext;
    private static int mNormalTint;
    private static int mActiveTint;

    public MaxmusicImageButton(Context context) {
        this(context, null);
    }

    public MaxmusicImageButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MaxmusicImageButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        /*try {
            Song sonhg = PlaybackService.sInstance.getSong(0);
			Bitmap songCover = sonhg.getCover(PlaybackService.sInstance);


			setBackgroundColor(applyTintToBackground(songCover));
		} catch (Exception e) {

			e.printStackTrace();
		}*/

        mContext = context;
        mNormalTint = fetchAttrColor(R.attr.controls_normal);
        mActiveTint = fetchAttrColor(R.attr.controls_active);
        updateImageTint(-1);
    }

    @Override
    public void setImageResource(int resId) {
        super.setImageResource(resId);
        this.updateImageTint(resId);
    }

    private void updateImageTint(int resHint) {
        int filterColor;
        int theme_val = Integer.parseInt(PrefUtils.getString(getContext(), PrefKeys.THEME_INT, PrefDefaults.THEME_INT_DEFAULT));
        if (theme_val == 0) {
            filterColor = Color.BLACK;
        } else {
            filterColor = Color.WHITE;
        }
//= PrefUtils.getInt(getContext(), PrefKeys.ACCENT_INT, PrefDefaults.ACCENT_DEF);

        switch (resHint) {
            case R.drawable.repeat_active:
            case R.drawable.repeat_current_active:
            case R.drawable.stop_current_active:
            case R.drawable.shuffle_active:
            case R.drawable.shuffle_album_active:
            case R.drawable.random_active:
                //filterColor = PrefUtils.getInt(getContext(), PrefKeys.ACCENT_INT, PrefDefaults.ACCENT_DEF);;
        }

        this.setColorFilter(filterColor);
    }

    private int fetchAttrColor(int attr) {
        TypedValue typedValue = new TypedValue();
        TypedArray a = mContext.obtainStyledAttributes(typedValue.data, new int[]{attr});
        int color = a.getColor(0, 0);
        a.recycle();
        return color;
    }
    /*public int applyTintToBackground(Bitmap bitmap) {

		Palette.from(bitmap).generate(
				new Palette.PaletteAsyncListener() {
					@Override
					public void onGenerated(Palette palette) {
						mMuted = palette.getMutedColor(PrefUtils.getInt(getContext(), PrefKeys.PRIMARY_INT, PrefDefaults.PRIMARY_DEF));
						mMutedDark = palette.getDarkMutedColor(mMuted);
						mMutedLight = palette.getLightMutedColor(mMutedDark);
						mVibrant = palette.getVibrantColor(mMutedLight);
						mVibrantDark = palette.getDarkVibrantColor(mVibrant);
						mVibrantLight = palette.getLightVibrantColor(mVibrantDark);


					}
				}
		);
		return mVibrantLight;
	}*/

}
