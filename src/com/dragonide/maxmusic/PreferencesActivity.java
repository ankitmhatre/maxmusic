
/*
 * Copyright (C) 2012-2015 Adrian Ulrich <adrian@blinkenlights.ch>
 * Copyright (C) 2012 Christopher Eby <kreed@kreed.org>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.dragonide.maxmusic;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewFragment;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.color.ColorChooserDialog;


/**
 * The preferences activity in which one can change application preferences.
 */

public class PreferencesActivity extends PlaybackActivity implements ColorChooserDialog.ColorCallback {

    /**
     * The package name of our external helper app
     */
    private static final String VPLUG_PACKAGE_NAME = "com.dragonide.maxmusic.headphone";

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */

    @Override
    public void onColorSelection(@NonNull ColorChooserDialog dialog, @ColorInt int color) {

        if (dialog.isAccentMode()) {

            PrefUtils.setInt(getApplicationContext(), PrefKeys.ACCENT_INT, color);
        } else {
            PrefUtils.setInt(getApplicationContext(), PrefKeys.PRIMARY_INT, color);

        }

        recreate();
    }

    @Override
    public void onColorChooserDismissed(@NonNull ColorChooserDialog dialog) {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        ThemeHelper.setTheme(this, R.style.NoActionBar);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));


        tintMyBars();


        getFragmentManager().beginTransaction().replace(R.id.htr, new UI()).addToBackStack("firstfrag").commit();


        if (MediaUtils.isLollipop()) {
            if (getFragmentManager().isDestroyed()) {
                finish();
            }

        }
    }


    @Override
    public boolean onNavigateUp() {
        finish();
        return super.onNavigateUp();
    }

    public void tintMyBars() {
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(PrefUtils.getInt(this, PrefKeys.PRIMARY_INT, PrefDefaults.PRIMARY_DEF)));

        if (MediaUtils.isLollipop()) {

            if (PrefUtils.getBoolean(this, PrefKeys.NAVIGATION_TINT, PrefDefaults.NAVIGATION_TINT)) {
                getWindow().setNavigationBarColor((PrefUtils.getInt(this, PrefKeys.PRIMARY_INT, PrefDefaults.PRIMARY_DEF)));
            }

            float[] hsv = new float[3];
            int darkcolor = PrefUtils.getInt(this, PrefKeys.PRIMARY_INT, PrefDefaults.PRIMARY_DEF);
            Color.colorToHSV(darkcolor, hsv);
            hsv[2] *= 0.8f; // value component
            darkcolor = Color.HSVToColor(hsv);
            getWindow().setStatusBarColor(darkcolor);
        }

    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.setGroupVisible(0, false);
        super.onPrepareOptionsMenu(menu);
        return true;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();


    }


    @Override
    public void onBackPressed() {


        if (getFragmentManager().getBackStackEntryCount() == 1) {
            finish();
        }
        if (!getFragmentManager().popBackStackImmediate()) {
            finish();


        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    public static class UI extends PreferenceFragment implements Preference.OnPreferenceClickListener {
        SharedPreferences sp = PlaybackService.getSettings(UI.this.getActivity());

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.all_pref);


            ExtendedCheckBoxPref mCheckBoxPref = (ExtendedCheckBoxPref) findPreference("tint_navigation");
            Pref_Category mCategory = (Pref_Category) findPreference("category_foo");
            if (!MediaUtils.isLollipop())
                mCategory.removePreference(mCheckBoxPref);


            findPreference("audio_frag").setOnPreferenceClickListener(this);
            findPreference("playback_frag").setOnPreferenceClickListener(this);
            findPreference("library_frag").setOnPreferenceClickListener(this);
            findPreference("notification_frag").setOnPreferenceClickListener(this);
            findPreference("shake_frag").setOnPreferenceClickListener(this);
            findPreference("cover_art_frag").setOnPreferenceClickListener(this);
            findPreference("misc_frag").setOnPreferenceClickListener(this);
            findPreference("about_frag").setOnPreferenceClickListener(this);
            findPreference("about_license_frag").setOnPreferenceClickListener(this);
            findPreference("primary_color_int").setOnPreferenceClickListener(this);
            findPreference("accent_color_int").setOnPreferenceClickListener(this);

        }

        @Override
        public boolean onPreferenceClick(Preference preference) {

            switch (preference.getKey()) {
                /*case "audio_frag":
                    getFragmentManager().beginTransaction().replace(R.id.htr, new AudioFragment()).addToBackStack(null).commit();
                    break;
                case "playback_frag":
                    getFragmentManager().beginTransaction().replace(R.id.htr, new PlaybackFragment()).addToBackStack(null).commit();
                    break;
                case "library_frag":
                    getFragmentManager().beginTransaction().replace(R.id.htr, new LibraryFragment()).addToBackStack(null).commit();
                    break;
                case "notification_frag":
                    getFragmentManager().beginTransaction().replace(R.id.htr, new NotificationsFragment()).addToBackStack(null).commit();
                    break;
                case "shake_frag":
                    getFragmentManager().beginTransaction().replace(R.id.htr, new ShakeFragment()).addToBackStack(null).commit();
                    break;
                case "cover_art_frag":
                    getFragmentManager().beginTransaction().replace(R.id.htr, new CoverArtFragment()).addToBackStack(null).commit();
                    break;
                case "misc_frag":
                    getFragmentManager().beginTransaction().replace(R.id.htr, new MiscFragment()).addToBackStack(null).commit();
                    break;

                case "replay_frag":
                    getFragmentManager().beginTransaction().replace(R.id.htr, new ReplayGainFragment()).addToBackStack(null).commit();
                    break;
                case "headset_launch":
                    getFragmentManager().beginTransaction().replace(R.id.htr, new HeadsetLaunchFragment()).addToBackStack(null).commit();
                    break;*/

                case "primary_color_int":

                    new ColorChooserDialog.Builder((PreferencesActivity) UI.this.getActivity(), R.string.color_primary)
                            .titleSub(R.string.color_primary)  // title of dialog when viewing shades of a color
                            .accentMode(false)  // when true, will display accent palette instead of primary palette
                            .doneButton(R.string.md_done_label)  // changes label of the done button
                            .cancelButton(R.string.md_cancel_label)  // changes label of the cancel button
                            .backButton(R.string.md_back_label)  // changes label of the back button
                            .preselect(sp.getInt(PrefKeys.PRIMARY_INT, PrefDefaults.PRIMARY_DEF))
                            .allowUserColorInputAlpha(false)// optionally preselects a color
                            .dynamicButtonColor(true)  // defaults to true, false will disable changing action buttons' color to currently selected color
                            .show();
                    break;
                case "accent_color_int":

                    new ColorChooserDialog.Builder((PreferencesActivity) UI.this.getActivity(), R.string.color_accent)
                            .titleSub(R.string.color_accent)  // title of dialog when viewing shades of a color
                            .accentMode(true)  // when true, will display accent palette instead of primary palette
                            .doneButton(R.string.md_done_label)  // changes label of the done button
                            .cancelButton(R.string.md_cancel_label)  // changes label of the cancel button
                            .backButton(R.string.md_back_label)  // changes label of the back button
                            .preselect(sp.getInt(PrefKeys.ACCENT_INT, PrefDefaults.ACCENT_DEF))
                            .allowUserColorInputAlpha(false)// optionally preselects a color
                            .dynamicButtonColor(true)  // defaults to true, false will disable changing action buttons' color to currently selected color
                            .show();
                    break;
                case "about_frag":
                    getFragmentManager().beginTransaction().replace(R.id.htr, new AboutFragment()).addToBackStack(null).commit();
                    break;
                case "about_license_frag":
                    getFragmentManager().beginTransaction().replace(R.id.htr, new AboutFragmenter()).addToBackStack(null).commit();
                    break;

            }

            return true;
        }

    }


    public static class AudioFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preference_audio);
            findPreference("replay_frag").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    getFragmentManager().beginTransaction().replace(R.id.htr, new ReplayGainFragment()).addToBackStack(null).commit();
                    return true;
                }
            });
            findPreference("headset_launch").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    getFragmentManager().beginTransaction().replace(R.id.htr, new HeadsetLaunchFragment()).addToBackStack(null).commit();
                    return true;
                }
            });

        }


    }


    public static class ReplayGainFragment extends PreferenceFragment {
        CheckBoxPreference cbTrackReplayGain;
        CheckBoxPreference cbAlbumReplayGain;
        SeekBarPreference sbGainBump;
        SeekBarPreference sbUntaggedDebump;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preference_replaygain);

            cbTrackReplayGain = (CheckBoxPreference) findPreference(PrefKeys.ENABLE_TRACK_REPLAYGAIN);
            cbAlbumReplayGain = (CheckBoxPreference) findPreference(PrefKeys.ENABLE_ALBUM_REPLAYGAIN);
            sbGainBump = (SeekBarPreference) findPreference(PrefKeys.REPLAYGAIN_BUMP);
            sbUntaggedDebump = (SeekBarPreference) findPreference(PrefKeys.REPLAYGAIN_UNTAGGED_DEBUMP);

            Preference.OnPreferenceClickListener pcListener = new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    updateConfigWidgets();
                    return false;
                }
            };

            cbTrackReplayGain.setOnPreferenceClickListener(pcListener);
            cbAlbumReplayGain.setOnPreferenceClickListener(pcListener);
            updateConfigWidgets();
        }

        @Override
        public void onDestroyView() {

            super.onDestroyView();
        }

        private void updateConfigWidgets() {
            boolean rgOn = (cbTrackReplayGain.isChecked() || cbAlbumReplayGain.isChecked());
            sbGainBump.setEnabled(rgOn);
            sbUntaggedDebump.setEnabled(rgOn);
        }
    }


    public static class PlaybackFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preference_playback);

            // Hide the dark theme preference if this device
            // does not support multiple themes


        }
    }

    public static class LibraryFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preference_library);
        }
    }

    public static class NotificationsFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preference_notifications);
        }
    }

    public static class ShakeFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preference_shake);
        }
    }

    public static class CoverArtFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preference_coverart);
        }
    }

    public static class MiscFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preference_misc);
            findPreference("scanner").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    getFragmentManager().beginTransaction().replace(R.id.htr, new SDScannerFragment()).addToBackStack(null).commit();

                    return true;
                }
            });


        }
    }

    public static class AboutFragmenter extends WebViewFragment {
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            WebView view = (WebView) super.onCreateView(inflater, container,
                    savedInstanceState);
            view.getSettings().setJavaScriptEnabled(true);

            TypedValue value = new TypedValue();
            getActivity().getTheme().resolveAttribute(
                    R.attr.overlay_foreground_color, value, true);
            String fontColor = TypedValue
                    .coerceToString(value.type, value.data);
            view.loadUrl("file:///android_asset/about.html?"
                    + Uri.encode(fontColor));
            view.setBackgroundColor(Color.TRANSPARENT);
            return view;
        }
    }

    public static class AboutFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preference_about_main);
            findPreference("changelog").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {


                    WebView view = new WebView(AboutFragment.this.getActivity());
                    view.getSettings().setJavaScriptEnabled(true);


                    view.loadUrl("file:///android_asset/features.html");

                    new MaterialDialog.Builder(AboutFragment.this.getActivity()).customView(view, true).title("Changelog").show();
                    return true;
                }
            });
            findPreference("privacy").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {


                    WebView rt = new WebView(AboutFragment.this.getActivity());
                    rt.getSettings().setJavaScriptEnabled(true);


                    rt.loadUrl("file:///android_asset/privacypolicy.html");

                    new MaterialDialog.Builder(AboutFragment.this.getActivity()).customView(rt, true).title("Privacy Policy").show();
                    return true;
                }
            });

            /*findPreference("faqs").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {


                    WebView rt = new WebView(AboutFragment.this.getActivity());
                    rt.getSettings().setJavaScriptEnabled(true);


                    rt.loadUrl("file:///android_asset/faqs.html");

                    new MaterialDialog.Builder(AboutFragment.this.getActivity()).customView(rt, true).title("Privacy Policy").show();
                    return true;
                }
            });*/
        }
    }

    public static class HeadsetLaunchFragment extends PreferenceFragment {


        private boolean isPackageInstalled(String packagename, Context context) {
            PackageManager pm = context.getPackageManager();
            try {
                pm.getPackageInfo(packagename, PackageManager.GET_SERVICES);
                Log.d("PACKAGE MANAGER ", pm.getPackageInfo(packagename, PackageManager.GET_SERVICES).toString());
                return true;
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            Context c = HeadsetLaunchFragment.this.getActivity();


            if (isPackageInstalled(VPLUG_PACKAGE_NAME, c)) {
                ComponentName service = new ComponentName("com.dragonide.maxmusic.headphone", "com.dragonide.maxmusic.headphone.MPlugService");
                Intent headIntent = new Intent(VPLUG_PACKAGE_NAME + ".MPlugService").setComponent(service);
                c.startService(headIntent);

                Log.d("Started", "headphone service");
            } else {
                // package is not installed, ask user to install it


                new MaterialDialog.Builder(c)
                        .titleColor(Color.CYAN)
                        .title(R.string.headset_launch_title)
                        .content(R.string.headset_launch_app_missing)
                        .contentColor(Color.CYAN) // notice no 'res' postfix for literal color
                        .backgroundColor(Color.BLACK)
                        .positiveText(R.string.yes)
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                Intent marketIntent = new Intent(Intent.ACTION_VIEW);
                                marketIntent.setData(Uri.parse("market://details?id=" + VPLUG_PACKAGE_NAME));
                                startActivity(marketIntent);
                                getActivity().finish();
                            }
                        })
                        .neutralText(R.string.cancel)
                        .onNeutral(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                dialog.dismiss();
                            }
                        })
                        .show();


            }
            getFragmentManager().beginTransaction().replace(R.id.htr, new AudioFragment()).commit();
        }
    }


}

