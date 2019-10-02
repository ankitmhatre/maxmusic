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
import android.content.SharedPreferences;
import android.os.Build;

public class ThemeHelper {

    /**
     * Calls context.setTheme() with given theme.
     * Will automatically swap the theme with an alternative
     * version if the user requested us to use it
     */
    final public static int setTheme(Context context, int theme) {


        int theme_val = Integer.parseInt(PrefUtils.getString(context, PrefKeys.THEME_INT, PrefDefaults.THEME_INT_DEFAULT));
        if (theme_val == 0) {
            switch (theme) {
                case R.style.Playback:
                    theme = R.style.Playback_Light;
                    break;
                case R.style.Library:
                    theme = R.style.Library_Light;
                    break;
                case R.style.BackActionBar:
                    theme = R.style.BackActionBar_Light;
                    break;
                case R.style.PopupDialog:
                    theme = R.style.PopupDialog_Light;
                    break;
                case R.style.NoActionBar:
                    theme = R.style.NoActionBar_Light;
                    break;
                case R.style.FullPlayback:
                    theme = R.style.FullPlayback_Light;
                  break;

                default:
            }
        } else if (theme_val == 1) {
            switch (theme) {
                case R.style.Playback:
                    theme = R.style.Playback_Dark;
                    break;
                case R.style.Library:
                    theme = R.style.Library_Dark;
                    break;
                case R.style.BackActionBar:
                    theme = R.style.BackActionBar_Dark;
                    break;
                case R.style.PopupDialog:
                    theme = R.style.PopupDialog_Dark;
                    break;
                case R.style.NoActionBar:
                    theme = R.style.NoActionBar_Dark;
                    break;
                case R.style.FullPlayback:
                    theme = R.style.FullPlayback_Dark;
                    break;

                default:
            }
        } else if (theme_val == 2) {
            switch (theme) {
                case R.style.Playback:
                    theme = R.style.Playback_PureDark;
                    break;
                case R.style.Library:
                    theme = R.style.Library_PureDark;
                    break;
                case R.style.BackActionBar:
                    theme = R.style.BackActionBar_PureDark;
                    break;
                case R.style.PopupDialog:
                    theme = R.style.PopupDialog_PureDark;
                    break;
                case R.style.NoActionBar:
                    theme = R.style.NoActionBar_PureDark;
                    break;
                case R.style.FullPlayback:
                    theme = R.style.FullPlayback_PureDark;
                    break;
                default:
            }


        }
        context.setTheme(theme);
        return theme;
    }

    /**
     * Helper function to get the correct play button drawable for our
     * notification: The notification does not depend on the theme but
     * depends on the API level
     */
    final public static int getPlayButtonResource(boolean playing) {
        int playButton = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // Android >= 5.0 uses the dark version of this drawable
            playButton = playing ? R.drawable.widget_pause : R.drawable.widget_play;
        } else {
            playButton = playing ? R.drawable.pause : R.drawable.play;
        }
        return playButton;
    }

    /**
     * Returns TRUE if we should use the dark material theme,
     * Returns FALSE otherwise - always returns FALSE on pre-5.x devices
     */
    final private static boolean usesDarkTheme(Context context) {
        boolean useDark = false;
        if (usesHoloTheme() == false) {
            useDark = (getSelectedTheme(context) % 2 != 0); // odd values are always dark
        }
        return useDark;
    }

    /**
     * Returns the user-selected theme id from the shared peferences provider
     *
     * @param context the context to use
     * @return integer of the selected theme
     */
    final private static int getSelectedTheme(Context context) {

        return Integer.parseInt(PrefUtils.getString(context, PrefKeys.THEME_INT, PrefDefaults.THEME_INT_DEFAULT));
    }

    /**
     * Returns TRUE if this device uses the HOLO (android 4) theme
     */
    final public static boolean usesHoloTheme() {
        return (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP);
    }

    /**
     * Hacky function to get the colors needed to draw the default cover
     * These colors should actually be attributes, but getting them programatically
     * is a big mess
     */
    final public static int[] getDefaultCoverColors(Context context) {
        int[] reColor={} ;
        int[] colors_holo_yolo = {0xff000000, 0xff606060, 0xff404040, 0x88000000};
        int[] colors_material_light = {0xffffffff, 0xffd6d7d7, 0xffd6d7d7, 0x55ffffff};
        int[] colors_material_dark = {0xff303030, 0xff606060, 0xff404040, 0x33ffffff};
        int[] colors_marshmallow_light = {0xfffafafa, 0xffd6d7d7, 0xffd6d7d7, 0x55ffffff};
        int[] colors_material_pure_dark = {0xff000000, 0xff606060, 0xfffafafa, 0xfffafafa};
        int[] colors_marshmallow_dark = colors_material_dark;
        switch (PrefUtils.getString(context, PrefKeys.THEME_INT, PrefDefaults.THEME_INT_DEFAULT)) {
            case "0":

            reColor = colors_material_light;
            break;
            case "1":
                reColor= colors_marshmallow_dark;
                break;
            case "2":
                reColor=colors_material_pure_dark;
                break;

            default:

        }
        return reColor;
    }

}
