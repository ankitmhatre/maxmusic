/*
 * Copyright (C) 2012 Christopher Eby <kreed@kreed.org>
 * Copyright (C) 2016 Adrian Ulrich <adrian@blinkenlights.ch>
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

import java.util.ArrayList;
import java.util.Calendar;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.iosched.tabs.VanillaTabLayout;
import android.support.v4.view.WindowCompat;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.content.ContentResolver;

import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import android.app.AlertDialog;
import android.content.DialogInterface;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.wdullaer.materialdatetimepicker.time.RadialPickerLayout;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

/**
 * The primary playback screen with playback controls and large cover display.
 */
public class FullPlaybackActivity extends SlidingPlaybackActivity
        implements View.OnLongClickListener, TimePickerDialog.OnTimeSetListener {

    int REQUEST_CODE_EDIT = 54;


    private TextView mOverlayText;
    private View mControlsTop;

    private TableLayout mInfoTable;
    private TextView mQueuePosView;

    private TextView mTitle;
    private TextView mAlbum;
    private TextView mArtist;
    private int mVibrant, mVibrantDark, mVibrantLight, mMuted, mMutedDark, mMutedLight;
    /**
     * True if the controls are visible (play, next, seek bar, etc).
     */
    private boolean mControlsVisible;
    /**
     * True if the extra info is visible.
     */
    private boolean mExtraInfoVisible;

    /**
     * The current display mode, which determines layout and cover render style.
     */
    private int mDisplayMode;

    private Action mCoverPressAction;
    private Action mCoverLongPressAction;

    /**
     * The currently playing song.
     */
    private Song mCurrentSong;

    private String mGenre;
    private TextView mGenreView;
    private String mTrack;
    private TextView mTrackView;
    private String mYear;
    private TextView mYearView;
    private String mComposer;
    private TextView mComposerView;
    private String mPath;
    private TextView mPathView;
    private String mFormat;
    private TextView mFormatView;
    private String mReplayGain;
    private TextView mReplayGainView;
    private MenuItem mFavorites;
    private static final String MLyrics = "com.dragonide.maxmusic.lyrics";
    public PlayPauseView pview;

    @Override
    public void onCreate(Bundle icicle) {
        ThemeHelper.setTheme(this, R.style.FullPlayback);
        super.onCreate(icicle);
        setTitle(R.string.playback_view);

        SharedPreferences settings = PlaybackService.getSettings(this);
        int displayMode = Integer.parseInt(settings.getString(PrefKeys.DISPLAY_MODE, PrefDefaults.DISPLAY_MODE));
        mDisplayMode = displayMode;


        int coverStyle = CoverBitmap.STYLE_NO_INFO;


        setContentView(R.layout.full_playback_alt);
        try {
            Song sonhg = mCurrentSong;
            Bitmap songCover = sonhg.getCover(PlaybackService.sInstance);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            applyTintToBackground(songCover);
        } catch (Exception e) {

            e.printStackTrace();
        }
        CoverView coverView = (CoverView) findViewById(R.id.cover_view);
        coverView.setup(mLooper, this, coverStyle);
        coverView.setOnClickListener(this);
        coverView.setOnLongClickListener(this);
        mCoverView = coverView;

        TableLayout table = (TableLayout) findViewById(R.id.info_table);
        if (table != null) {
            table.setOnClickListener(this);
            table.setOnLongClickListener(this);
            mInfoTable = table;
        }

        mTitle = (TextView) findViewById(R.id.title);
        mAlbum = (TextView) findViewById(R.id.album);
        mArtist = (TextView) findViewById(R.id.artist);
        pview = (PlayPauseView) findViewById(R.id.bg_view);
        mControlsTop = findViewById(R.id.controls_top);
        //  mQueuePosView = (TextView) findViewById(R.id.queue_pos);

        try {
            pview.setState(!PlaybackService.sInstance.isPlaying());
        } catch (Exception e) {
            e.printStackTrace();
        }
        pview.setOnClickListener(this);

        bindControlButtons();

        setControlsVisible(settings.getBoolean(PrefKeys.VISIBLE_CONTROLS, PrefDefaults.VISIBLE_CONTROLS));

    }

    @Override
    public void onStart() {
        super.onStart();


        try {
            Song sonhg = mCurrentSong;
            Bitmap songCover = sonhg.getCover(PlaybackService.sInstance);

            if (songCover != null) {
                applyTintToBackground(songCover);
            } else {
                tintMyBarss();

                getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            }

        } catch (Exception e) {

            e.printStackTrace();
        }

        SharedPreferences settings = PlaybackService.getSettings(this);
        if (mDisplayMode != Integer.parseInt(settings.getString(PrefKeys.DISPLAY_MODE, PrefDefaults.DISPLAY_MODE))) {
            finish();
            startActivity(new Intent(this, FullPlaybackActivity.class));
        }

        mCoverPressAction = Action.getAction(settings, PrefKeys.COVER_PRESS_ACTION, PrefDefaults.COVER_PRESS_ACTION);
        mCoverLongPressAction = Action.getAction(settings, PrefKeys.COVER_LONGPRESS_ACTION, PrefDefaults.COVER_LONGPRESS_ACTION);
    }

    /**
     * Hide the message overlay, if it exists.
     */
    private void hideMessageOverlay() {
        if (mOverlayText != null)
            mOverlayText.setVisibility(View.GONE);
    }

    /**
     * Show some text in a message overlay.
     *
     * @param text Resource id of the text to show.
     */
    private void showOverlayMessage(int text) {
        if (mOverlayText == null) {
            TextView view = new TextView(this);
            // This will be drawn on top of all other controls, so we flood this view
            // with a non-alpha color
            int[] colors = ThemeHelper.getDefaultCoverColors(this);
            view.setBackgroundColor(colors[0]); // background of default cover
            view.setGravity(Gravity.CENTER);
            view.setPadding(25, 25, 25, 25);
            // Make the view clickable so it eats touch events
            view.setClickable(true);
            view.setOnClickListener(this);
            addContentView(view,
                    new ViewGroup.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.MATCH_PARENT));
            mOverlayText = view;
        } else {
            mOverlayText.setVisibility(View.VISIBLE);
        }

        mOverlayText.setText(text);
    }

    @Override
    protected void onStateChange(int state, int toggled) {
        super.onStateChange(state, toggled);
    pview.setState((state & PlaybackService.FLAG_PLAYING) != 0);
        if ((toggled & (PlaybackService.FLAG_NO_MEDIA | PlaybackService.FLAG_EMPTY_QUEUE)) != 0) {
            if ((state & PlaybackService.FLAG_NO_MEDIA) != 0) {
                showOverlayMessage(R.string.no_songs);
            } else if ((state & PlaybackService.FLAG_EMPTY_QUEUE) != 0) {
                showOverlayMessage(R.string.empty_queue);
            } else {
                hideMessageOverlay();
            }
        }

        //   if (mQueuePosView != null)
        //     updateQueuePosition();
    }

    @Override
    protected void onSongChange(Song song) {




        try {

            Bitmap songCover = song.getCover(PlaybackService.sInstance);
            if (songCover != null)
                applyTintToBackground(songCover);
            else {
                tintMyBarss();
            }
        } catch (Exception e) {
            tintMyBarss();
        }

        if (mTitle != null) {
            if (song == null) {
                mTitle.setText(null);
                mAlbum.setText(null);
                mArtist.setText(null);
            } else {
                mTitle.setText(song.title);
                mAlbum.setText(song.album);
                mArtist.setText(song.artist);
            }
            updateQueuePosition();
        }

        mCurrentSong = song;

        mHandler.sendEmptyMessage(MSG_LOAD_FAVOURITE_INFO);
      //  pview.setState(PlaybackService.sInstance.isPlaying());
        // All quick UI updates are done: Time to update the cover
        // and parse additional info
        if (mExtraInfoVisible) {
            mHandler.sendEmptyMessage(MSG_LOAD_EXTRA_INFO);
        }
        super.onSongChange(song);
    }

    public void tintMyBarss() {
        tintMyBarschanged();
        pview.setColor(PrefUtils.getInt(this, PrefKeys.ACCENT_INT, PrefDefaults.ACCENT_DEF));
        if (MediaUtils.isLollipop()) {

            getWindow().setNavigationBarColor(Color.BLACK);
            if (MediaUtils.isLollipop()) {
                mSeekBar.setProgressTintList(ColorStateList.valueOf(PrefUtils.getInt(this, PrefKeys.ACCENT_INT, PrefDefaults.ACCENT_DEF)));
                mSeekBar.setThumbTintList(ColorStateList.valueOf(PrefUtils.getInt(this, PrefKeys.ACCENT_INT, PrefDefaults.ACCENT_DEF)));
            } else {
                mSeekBar.setProgressDrawable(new ColorDrawable(PrefUtils.getInt(this, PrefKeys.ACCENT_INT, PrefDefaults.ACCENT_DEF)));
                mSeekBar.setThumb(new ColorDrawable(PrefUtils.getInt(this, PrefKeys.ACCENT_INT, PrefDefaults.ACCENT_DEF)));

            }
        }
        getColorForBG();
    }

    void tintMyBarschanged() {


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

    public void getColorForBG() {

        switch (Integer.parseInt(PrefUtils.getString(getApplicationContext(), PrefKeys.THEME_INT, PrefDefaults.THEME_INT_DEFAULT))) {
            case 0:
                getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
                break;
            case 1:
                getWindow().setBackgroundDrawable(new ColorDrawable(Color.argb(255, 48, 48, 48)));
                break;
            case 2:
                getWindow().setBackgroundDrawable(new ColorDrawable(Color.BLACK));
                break;
        }
    }

    /**
     * Update the queue position display. mQueuePos must not be null.
     */
    private void updateQueuePosition() {
        if (PlaybackService.finishAction(mState) == SongTimeline.FINISH_RANDOM) {
            // Not very useful in random mode; it will always show something
            // like 11/13 since the timeline is trimmed to 10 previous songs.
            // So just hide it.
            //  mQueuePosView.setText(null);
        } else {
            PlaybackService service = PlaybackService.get(this);
            //mQueuePosView.setText((service.getTimelinePosition() + 1) + "/" + service.getTimelineLength());
        }
        //  mQueuePosView.requestLayout(); // ensure queue pos column has enough room
    }

    @Override
    public void onPositionInfoChanged() {
        //  if (mQueuePosView != null)
        //       mUiHandler.sendEmptyMessage(MSG_UPDATE_POSITION);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(0, MENU_DELETE, 30, R.string.delete);
        menu.add(0, MENU_ENQUEUE_ALBUM, 30, R.string.enqueue_current_album);
        menu.add(0, MENU_ENQUEUE_ARTIST, 30, R.string.enqueue_current_artist);
        menu.add(0, MENU_ENQUEUE_GENRE, 30, R.string.enqueue_current_genre);
        menu.add(0, MENU_ADD_TO_PLAYLIST, 30, R.string.add_to_playlist);
        menu.add(0, MENU_NOWPLAYING_SHARE, 30, R.string.share_lib);
        // Drawable d = getResources().getDrawable(R.drawable.btn_rating_star_off_mtrl_alpha);

        int d = R.drawable.ic_favorite_border_black_36dp;
        switch (PrefUtils.getString(this, PrefKeys.THEME_INT, PrefDefaults.THEME_INT_DEFAULT)) {
            case "0":
                break;
            case "1":
                d = R.drawable.ic_favorite_border_white_36dp;
                break;
            case "2":
                d = R.drawable.ic_favorite_border_white_36dp;
                break;
        }

        mFavorites = menu.add(0, MENU_SONG_FAVORITE, 0, R.string.add_to_favorites).setIcon(d).setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        menu.add(0, MENU_EQUALIZER, 30, R.string.equalizer);
        menu.add(0, MENU_INFO, 30, R.string.song_info);
        menu.add(0, MENUTIM, 30, R.string.timer);
        menu.add(0,MENU_SET_RINGTONE, 30, R.string.rington_set);


        if (!isPackageInstalled(MLyrics, this)) {
            menu.add(0, MENU_LYRICS, 30, R.string.lyrics);
        }
        // ensure that mFavorites is updated
        mHandler.sendEmptyMessage(MSG_LOAD_FAVOURITE_INFO);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final Song song = mCurrentSong;

        switch (item.getItemId()) {
            case android.R.id.home:
            case MENU_LIBRARY:
                openLibrary(null);
                break;
            case MENU_ENQUEUE_ALBUM:
                PlaybackService.get(this).enqueueFromSong(song, MediaUtils.TYPE_ALBUM);
                break;
            case MENU_ENQUEUE_ARTIST:
                PlaybackService.get(this).enqueueFromSong(song, MediaUtils.TYPE_ARTIST);
                break;
            case MENU_ENQUEUE_GENRE:
                PlaybackService.get(this).enqueueFromSong(song, MediaUtils.TYPE_GENRE);
                break;
            case MENU_NOWPLAYING_SHARE:{
                if (song != null)
                MediaUtils.shareMedia(this, MediaUtils.TYPE_SONG, song.id);

                break;
            }
            case MENU_SONG_FAVORITE:
                long playlistId = Playlist.getFavoritesId(this, true);
                if (song != null) {
                    PlaylistTask playlistTask = new PlaylistTask(playlistId, getString(R.string.playlist_favorites));
                    playlistTask.audioIds = new ArrayList<Long>();
                    playlistTask.audioIds.add(song.id);
                    int action = Playlist.isInPlaylist(getContentResolver(), playlistId, song) ? MSG_REMOVE_FROM_PLAYLIST : MSG_ADD_TO_PLAYLIST;
                    mHandler.sendMessage(mHandler.obtainMessage(action, playlistTask));
                }
                break;
            case MENU_ADD_TO_PLAYLIST:
                if (song != null) {
                    Intent intent = new Intent();
                    intent.putExtra("type", MediaUtils.TYPE_SONG);
                    intent.putExtra("id", song.id);
                    PlaylistDialog dialog = new PlaylistDialog(this, intent, null);
                    dialog.show(getFragmentManager(), "PlaylistDialog");
                }
                break;
            case MENU_DELETE:
                final PlaybackService playbackService = PlaybackService.get(this);
                final PlaybackActivity activity = this;

                if (song != null) {
                    String delete_message = getString(R.string.delete_file, song.title);
                    AlertDialog.Builder dialog = new AlertDialog.Builder(this);
                    dialog.setTitle(R.string.delete);
                    dialog
                            .setMessage(delete_message)
                            .setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    // MSG_DELETE expects an intent (usually called from listview)
                                    Intent intent = new Intent();
                                    intent.putExtra(LibraryAdapter.DATA_TYPE, MediaUtils.TYPE_SONG);
                                    intent.putExtra(LibraryAdapter.DATA_ID, song.id);
                                    mHandler.sendMessage(mHandler.obtainMessage(MSG_DELETE, intent));
                                }
                            })
                            .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                }
                            });
                    dialog.create().show();
                }
                break;
            case MENU_INFO:
                TypedValue outValue = new TypedValue();
                this.getTheme().resolveAttribute(android.R.attr.textColorPrimary, outValue, true);
                final int themeForegroundColor = outValue.data;

                TypedValue outValue2 = new TypedValue();
                this.getTheme().resolveAttribute(android.R.attr.windowBackground, outValue2, true);
                final int themeForegroundColor2 = outValue2.data;

                if (mCurrentSong != null) {

                    MediaMetadataRetriever data = new MediaMetadataRetriever();

                    try {
                        data.setDataSource(mCurrentSong.path);
                    } catch (Exception e) {
                        Log.w("MaxMusic", "Failed to extract metadata from "
                                + mCurrentSong.path);
                    }

                    mGenre = data
                            .extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE);
                    mTrack = data
                            .extractMetadata(MediaMetadataRetriever.METADATA_KEY_CD_TRACK_NUMBER);
                    String composer = data
                            .extractMetadata(MediaMetadataRetriever.METADATA_KEY_COMPOSER);
                    if (composer == null)
                        composer = "";
                    mComposer = composer;

                    String year = data
                            .extractMetadata(MediaMetadataRetriever.METADATA_KEY_YEAR);
                    if (year == null || "0".equals(year)) {
                        year = "";
                    } else {
                        int dash = year.indexOf('-');
                        if (dash != -1)
                            year = year.substring(0, dash);
                    }
                    mYear = year;

                    StringBuilder sb = new StringBuilder(12);
                    sb.append(decodeMimeType(data
                            .extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE)));
                    String bitrate = data
                            .extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE);
                    if (bitrate != null && bitrate.length() > 3) {
                        sb.append(' ');
                        sb.append(bitrate.substring(0, bitrate.length() - 3));
                        sb.append("kbps");
                    }
                    mFormat = sb.toString();

                    if (mCurrentSong.path != null) { /* ICS bug? */

                    }

                    data.release();
                }
                String finalString = "Title : " + mCurrentSong.title + "\n\n" + "Artist : "
                        + mCurrentSong.artist + "\n\n" + "Album : "
                        + mCurrentSong.album + "\n\n" + "Composer : " + mComposer
                        + "\n\n" + "Track : " + mTrack + "\n\n" + "Genre : " + mGenre
                        + "\n\n" + "Year : " + mYear + "\n\n" + "Bitrate : " + mFormat
                        + "\n\n" + "Replay Gain : " + mReplayGain
                        + "\n\n" + "Path : " + song.path;

                MaterialDialog dialog = new MaterialDialog.Builder(this)
                        .titleColor(themeForegroundColor)
                        .title(R.string.details)
                        .content(finalString)
                        .contentColor(themeForegroundColor) // notice no 'res' postfix for literal color
                        .backgroundColor(themeForegroundColor2)
                        .positiveText(R.string.edit)
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                Intent t = new Intent(getApplicationContext(), EditDetails.class);
                                t.putExtra("PATH", mCurrentSong.path);
                                t.putExtra("_ID", mCurrentSong.id);
                                startActivity(t);
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

                break;
            case MENU_EQUALIZER:


                Intent ddfsf = new Intent();
                ddfsf.setAction("android.media.action.DISPLAY_AUDIO_EFFECT_CONTROL_PANEL");
                if ((ddfsf.resolveActivity(getPackageManager()) != null)) {
                    startActivity(ddfsf);
                }else{
                    startActivity(new Intent(this, MaxMusicEqualizer.class));
                }


                break;
            case MENU_SET_RINGTONE:

                Intent intent = new Intent(Intent.ACTION_EDIT, Uri.parse(mCurrentSong.path));
                intent.putExtra("was_get_content_intent", false);
                intent.setClassName("com.dragonide.maxmusic", "com.dragonide.maxmusic.RingdroidEditActivity");
                startActivityForResult(intent, REQUEST_CODE_EDIT);

                break;
            case MENUTIM:

                TimePickerDialog dpd = TimePickerDialog.newInstance(this, 0, 0, 0, true);
                dpd.enableSeconds(true);
                dpd.show(getFragmentManager(), "TIMEPICKER");
                break;
            case MENU_LYRICS:
                Intent marketIntent = new Intent(Intent.ACTION_VIEW);
                marketIntent.setData(Uri.parse("market://details?id=" + MLyrics));
                startActivity(marketIntent);
                break;


        }

        return super.onOptionsItemSelected(item);

    }

    @Override
    public void onTimeSet(TimePickerDialog view, int hourOfDay, int minute, int second) {
        long x = (second * 1000) + (minute * 60 * 1000) + ((hourOfDay * 60 * 60 * 1000));
        Yyelebhai(x, 1000);
    }



    private void Yyelebhai(long millisinFuture, long delayTime) {

        new CountDownTimer(millisinFuture, delayTime) {
            public void onTick(long millisUntilFinished) {
            }

            public void onFinish() {
                PlaybackService.sInstance.pause();
            }
        }.start();

    }

    @Override
    public boolean onSearchRequested() {
        openLibrary(null);
        return false;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                shiftCurrentSong(SongTimeline.SHIFT_NEXT_SONG);
                findViewById(R.id.next).requestFocus();
                return true;
            case KeyEvent.KEYCODE_DPAD_LEFT:
                shiftCurrentSong(SongTimeline.SHIFT_PREVIOUS_SONG);
                findViewById(R.id.previous).requestFocus();
                return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_ENTER:
                setControlsVisible(!mControlsVisible);
                mHandler.sendEmptyMessage(MSG_SAVE_CONTROLS);
                return true;
            case KeyEvent.KEYCODE_BACK:
                if (mSlidingView.isHidden() == false) {
                    mSlidingView.hideSlide();
                    return true;
                }
        }

        return super.onKeyUp(keyCode, event);
    }

    public void applyTintToBackground(Bitmap bitmap) {

        Palette.from(bitmap).generate(
                new Palette.PaletteAsyncListener() {
                    @Override
                    public void onGenerated(Palette palette) {

                        mMutedLight = palette.getLightMutedColor((PrefUtils.getInt(FullPlaybackActivity.this, PrefKeys.PRIMARY_INT, PrefDefaults.PRIMARY_DEF)));
                        mVibrantLight = palette.getLightVibrantColor(mMutedLight);
                        mMuted = palette.getMutedColor(mVibrantLight);
                        mMutedDark = palette.getDarkMutedColor(mMuted);
                        mVibrant = palette.getVibrantColor(mMutedDark);
                        mVibrantDark = palette.getDarkVibrantColor(mVibrant);

                        float[] hsv = new float[3];
                        int darkcolor = mVibrantDark;
                        Color.colorToHSV(darkcolor, hsv);
                        hsv[2] *= 0.8f; // value component
                        darkcolor = Color.HSVToColor(hsv);


                        try {
                            getSupportActionBar().setBackgroundDrawable(
                                    new ColorDrawable(Color.TRANSPARENT));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        getWindow().setBackgroundDrawable(
                                new ColorDrawable(mVibrantDark));

                        pview.setColor(darkcolor);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            getWindow().setNavigationBarColor(mVibrantDark);


                            getWindow().setStatusBarColor(darkcolor);
                            if (MediaUtils.isLollipop()) {
                                mSeekBar.setProgressTintList(ColorStateList.valueOf(darkcolor));
                                mSeekBar.setThumbTintList(ColorStateList.valueOf(darkcolor));
                            } else {
                                mSeekBar.setProgressDrawable(new ColorDrawable(darkcolor));
                                mSeekBar.setThumb(new ColorDrawable(darkcolor));

                            }
                        }

                    }
                });


    }

    /**
     * Set the visibility of the controls views.
     *
     * @param visible True to show, false to hide
     */
    private void setControlsVisible(boolean visible) {
        int mode = visible ? View.VISIBLE : View.GONE;
        mControlsTop.setVisibility(mode);
        mSlidingView.setVisibility(mode);
        mControlsVisible = visible;

        if (visible) {
            pview.requestFocus();
        }
    }

    /**
     * Set the visibility of the extra metadata view.
     *
     * @param visible True to show, false to hide
     */


    /**
     * Retrieve the extra metadata for the current song.
     */
    private void loadExtraInfo() {
        Song song = mCurrentSong;

        mGenre = null;
        mTrack = null;
        mYear = null;
        mComposer = null;
        mPath = null;
        mFormat = null;
        mReplayGain = null;
        mPath = null;

        if (song != null) {

            MediaMetadataRetriever data = new MediaMetadataRetriever();

            try {
                data.setDataSource(song.path);
            } catch (Exception e) {
                Log.w("MaxMusic", "Failed to extract metadata from " + song.path);
            }

            mGenre = data.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE);
            mTrack = data.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CD_TRACK_NUMBER);
            String composer = data.extractMetadata(MediaMetadataRetriever.METADATA_KEY_COMPOSER);
            if (composer == null)
                composer = data.extractMetadata(MediaMetadataRetriever.METADATA_KEY_WRITER);
            mComposer = composer;

            String year = data.extractMetadata(MediaMetadataRetriever.METADATA_KEY_YEAR);
            if (year == null || "0".equals(year)) {
                year = null;
            } else {
                int dash = year.indexOf('-');
                if (dash != -1)
                    year = year.substring(0, dash);
            }
            mYear = year;

            mPath = song.path;
            StringBuilder sb = new StringBuilder(12);
            sb.append(decodeMimeType(data.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE)));
            String bitrate = data.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE);
            if (bitrate != null && bitrate.length() > 3) {
                sb.append(' ');
                sb.append(bitrate.substring(0, bitrate.length() - 3));
                sb.append("kbps");
            }
            mFormat = sb.toString();

            BastpUtil.GainValues rg = PlaybackService.get(this).getReplayGainValues(song.path);
            mReplayGain = String.format("base=%.2f, track=%.2f, album=%.2f", rg.base, rg.track, rg.album);

            data.release();

        }

        mUiHandler.sendEmptyMessage(MSG_COMMIT_INFO);
    }

    /**
     * Decode the given mime type into a more human-friendly description.
     */
    private static String decodeMimeType(String mime) {
        if ("audio/mpeg".equals(mime)) {
            return "MP3";
        } else if ("audio/mp4".equals(mime)) {
            return "AAC";
        } else if ("audio/vorbis".equals(mime)) {
            return "Ogg Vorbis";
        } else if ("application/ogg".equals(mime)) {
            return "Ogg Vorbis";
        } else if ("audio/flac".equals(mime)) {
            return "FLAC";
        }
        return mime;
    }

    /**
     * Save the hidden_controls preference to storage.
     */
    private static final int MSG_SAVE_CONTROLS = 10;
    /**
     * Call {@link #loadExtraInfo()}.
     */
    private static final int MSG_LOAD_EXTRA_INFO = 11;
    /**
     * Pass obj to mExtraInfo.setText()
     */
    private static final int MSG_COMMIT_INFO = 12;
    /**
     * Calls {@link #updateQueuePosition()}.
     */
    private static final int MSG_UPDATE_POSITION = 13;
    /**
     * Check if passed song is a favorite
     */
    private static final int MSG_LOAD_FAVOURITE_INFO = 14;
    /**
     * Updates the favorites state
     */
    private static final int MSG_COMMIT_FAVOURITE_INFO = 15;

    @Override
    public boolean handleMessage(Message message) {
        switch (message.what) {
            case MSG_SAVE_CONTROLS: {
                SharedPreferences.Editor editor = PlaybackService.getSettings(this).edit();
                editor.putBoolean(PrefKeys.VISIBLE_CONTROLS, mControlsVisible);
                editor.putBoolean(PrefKeys.VISIBLE_EXTRA_INFO, mExtraInfoVisible);
                editor.apply();
                break;
            }
            case MSG_LOAD_EXTRA_INFO:
                loadExtraInfo();
                break;
            case MSG_COMMIT_INFO: {
                mGenreView.setText(mGenre);
                mTrackView.setText(mTrack);
                mYearView.setText(mYear);
                mComposerView.setText(mComposer);
                mPathView.setText(mPath);
                mFormatView.setText(mFormat);
                mReplayGainView.setText(mReplayGain);
                break;
            }
            case MSG_UPDATE_POSITION:
                updateQueuePosition();
                break;
            case MSG_NOTIFY_PLAYLIST_CHANGED: // triggers a fav-refresh
            case MSG_LOAD_FAVOURITE_INFO:
                if (mCurrentSong != null) {
                    boolean found = Playlist.isInPlaylist(getContentResolver(), Playlist.getFavoritesId(this, false), mCurrentSong);
                    mUiHandler.sendMessage(mUiHandler.obtainMessage(MSG_COMMIT_FAVOURITE_INFO, found));
                }
                break;
            case MSG_COMMIT_FAVOURITE_INFO:
                if (mFavorites != null) {
                    boolean found = (boolean) message.obj;


                    int d = R.drawable.ic_favorite_border_black_36dp;
                    switch (PrefUtils.getString(this, PrefKeys.THEME_INT, PrefDefaults.THEME_INT_DEFAULT)) {
                        case "0":
                            break;
                        case "1":
                            d = R.drawable.ic_favorite_border_white_36dp;
                            break;
                        case "2":
                            d = R.drawable.ic_favorite_border_white_36dp;
                            break;
                    }
                    int d2 = R.drawable.ic_favorite_black_36dp;
                    switch (PrefUtils.getString(this, PrefKeys.THEME_INT, PrefDefaults.THEME_INT_DEFAULT)) {
                        case "0":
                            break;
                        case "1":
                            d2 = R.drawable.ic_favorite_white_36dp;
                            break;
                        case "2":
                            d2 = R.drawable.ic_favorite_white_36dp;
                            break;
                    }


                    mFavorites.setIcon(found ? d2 : d);
                    mFavorites.setTitle(found ? R.string.remove_from_favorites : R.string.add_to_favorites);
                }
                break;
            default:
                return super.handleMessage(message);
        }

        return true;
    }

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
    protected void performAction(Action action) {
        switch (action) {
            case ShowQueue:
                mSlidingView.expandSlide();
                break;
            default:
                super.performAction(action);
        }
    }

    @Override
    public void onClick(View view) {
        if (view == mOverlayText && (mState & PlaybackService.FLAG_EMPTY_QUEUE) != 0) {
            setState(PlaybackService.get(this).setFinishAction(SongTimeline.FINISH_RANDOM));
        } else if (view == mCoverView) {
            performAction(mCoverPressAction);
        } else if (view.getId() == R.id.info_table) {
            openLibrary(mCurrentSong);
        } else if (view.getId() == R.id.bg_view) {

            if (PlaybackService.sInstance.isPlaying()) {
                PlaybackService.sInstance.pause();
            } else {
                PlaybackService.sInstance.play();
            }
         //   pview.setState(PlaybackService.sInstance.isPlaying());

        } else {
            super.onClick(view);
        }
    }

    @Override
    public boolean onLongClick(View view) {
        switch (view.getId()) {
            case R.id.cover_view:
                performAction(mCoverLongPressAction);
                break;
            case R.id.info_table:

                //mHandler.sendEmptyMessage(MSG_SAVE_CONTROLS);
                break;
            default:
                return false;
        }

        return true;
    }

    @Override
    public void onSlideFullyExpanded(boolean expanded) {
        super.onSlideFullyExpanded(expanded);

    }

}
