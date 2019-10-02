package com.dragonide.maxmusic;


import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import java.util.ArrayList;

public class MaxMusicEqualizer extends AppCompatActivity implements
        SeekBar.OnSeekBarChangeListener, View.OnClickListener {
    static final int MAX_SLIDERS = 8; // Must match the XML layout
    TextView bass_boost_label = null;
    SeekBar bass_boost = null;
    TextView virtualizer_label = null;
    SeekBar virtualizer = null;

    Button flat = null;
    TextView reverbTitle = null;
    Spinner reverbSpinner = null;


    int BANDCOunt;
    Spinner presetSPinner;

    int min_level = 0;
    int max_level = 100;
    SeekBar sliders[] = new SeekBar[MAX_SLIDERS];
    TextView slider_labels[] = new TextView[MAX_SLIDERS];
    int num_sliders = 0;
    int axolor, pcolor;


    private OnItemSelectedListener reverbListener = new OnItemSelectedListener() {

        @Override
        public void onItemSelected(AdapterView<?> arg0, View arg1, int index,
                                   long arg3) {

            try {
                if (PlaybackService.hasInstance())
                    PrefUtils.setInt(getBaseContext(), PrefKeys.PRESET_REVERB_LEVEL, index);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> arg0) {


        }
    };


    public void updateUIAfterPresets() {
//Update Eq bands


        if (PrefUtils.getBoolean(getBaseContext(), PrefKeys.USE_EQUALIZER, PrefDefaults.USE_EQUALIZER)) {
            for (int i = 0; i < BANDCOunt; i++) {
                int level = PlaybackService.sInstance.getEqualizer().getBandLevel((short) i);
                int pos = 100 * level / (max_level - min_level) + 50;

                sliders[i].setProgress(pos);
            }
        }


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.equalizer_item, menu);
        MenuItem dd = (MenuItem) menu.findItem(R.id.equalizerSwitch);
        dd.setActionView(R.layout.test);
        SwitchCompat mswitch = (SwitchCompat) dd.getActionView();
        presetSPinner = (Spinner) menu.findItem(R.id.presetSpinner).getActionView();
        presetSPinner.setEnabled(PrefUtils.getBoolean(this, PrefKeys.USE_EQUALIZER, PrefDefaults.USE_EQUALIZER));
        ArrayList<String> presetsarray = new ArrayList();
        for (int t = 0; t < PlaybackService.sInstance.getEqualizer().getNumberOfPresets(); t++) {
            presetsarray.add(PlaybackService.sInstance.getEqualizer().getPresetName((short) t));
        }
        presetsarray.add("Custom");

        try {
            Cursor customPresets = DBAccessHelper.getInstance(this).getAllEQPresets();
            if (customPresets != null) {
                while (customPresets.moveToNext()) {
                    presetsarray.add(customPresets.getString(customPresets.getColumnIndex("preset_name")));


                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        presetSPinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position < 10) {
                    PlaybackService.sInstance.getEqualizer().usePreset((short) position);
                } else if (position > 10) {

                    int a[] = DBAccessHelper.getInstance(MaxMusicEqualizer.this).getPresetEQValues(parent.getSelectedItem().toString());
                    for (int i = 0; i < num_sliders; i++) {
                        int level;
                        if (PrefUtils.getBoolean(getBaseContext(), PrefKeys.USE_EQUALIZER, PrefDefaults.USE_EQUALIZER)) {
                            PlaybackService.sInstance.getEqualizer().setBandLevel((short) i, (short) a[i]);
                            int pos = 100 * a[i] / (max_level - min_level) + 50;
                            sliders[i].setProgress(pos);
                        }

                    }
                    //8 virualizer, 9 basssboost, 10 reverb

                    PlaybackService.sInstance.getVirtualizer().setStrength((short) a[8]);
                    PlaybackService.sInstance.getBassBoost().setStrength((short) a[9]);
                    PlaybackService.sInstance.getPresetReverb().setPreset((short) a[10]);
                    virtualizer.setProgress(a[8]);
                    bass_boost.setProgress(a[9]);
                    reverbSpinner.setSelection(a[10], true);
                }

                PrefUtils.setInt(getBaseContext(), PrefKeys.PRESET_LEVEL, position);
                updateUIAfterPresets();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        ArrayAdapter<String> presetsAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, presetsarray);
        presetsAdapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        presetSPinner.setAdapter(presetsAdapter);

        presetSPinner.setSelection(PrefUtils.getInt(this, PrefKeys.PRESET_LEVEL, PrefDefaults.PRESET_LEVEL));


        mswitch.setPadding(mswitch.getPaddingLeft(),
                mswitch.getPaddingTop(), mswitch.getPaddingRight() + getResources().getDimensionPixelSize(R.dimen.toggle_right_padding),
                mswitch.getPaddingBottom());
        mswitch.setChecked(PrefUtils.getBoolean(getBaseContext(), PrefKeys.USE_EQUALIZER, PrefDefaults.USE_EQUALIZER));
        mswitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                PrefUtils.setBoolean(getBaseContext(), PrefKeys.USE_EQUALIZER, isChecked);
                recreate();
                presetSPinner.setEnabled(isChecked);
                toggleAudioFxElements();
            }
        });


        return super.onCreateOptionsMenu(menu);


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.saveAsPreset:

                if (PrefUtils.getBoolean(this, PrefKeys.USE_EQUALIZER, PrefDefaults.USE_EQUALIZER)) {
                    new MaterialDialog.Builder(this)
                            .title(R.string.save_preset_title)

                            .inputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE | InputType.TYPE_TEXT_FLAG_IME_MULTI_LINE)
                            .input("Preset Name", "", new MaterialDialog.InputCallback() {
                                @Override
                                public void onInput(MaterialDialog dialog, CharSequence input) {

                                }
                            }).neutralText(R.string.cancel).onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            dialog.dismiss();
                        }
                    }).positiveText(R.string.save_preset).onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {


                            try {
                                DBAccessHelper.getInstance(MaxMusicEqualizer.this).addNewEQPreset(dialog.getInputEditText().getText().toString(),
                                        PlaybackService.sInstance.getEqualizer().getBandLevel((short) 0),
                                        PlaybackService.sInstance.getEqualizer().getBandLevel((short) 1),
                                        PlaybackService.sInstance.getEqualizer().getBandLevel((short) 2),
                                        PlaybackService.sInstance.getEqualizer().getBandLevel((short) 3),
                                        PlaybackService.sInstance.getEqualizer().getBandLevel((short) 4),
                                        PlaybackService.sInstance.getEqualizer().getBandLevel((short) 5),
                                        PlaybackService.sInstance.getEqualizer().getBandLevel((short) 6),
                                        PlaybackService.sInstance.getEqualizer().getBandLevel((short) 7),
                                        PlaybackService.sInstance.getVirtualizer().getRoundedStrength(),
                                        PlaybackService.sInstance.getBassBoost().getRoundedStrength(),

                                        (short) reverbSpinner.getSelectedItemPosition());
                            } catch (Exception e) {
                                try {
                                    DBAccessHelper.getInstance(MaxMusicEqualizer.this).addNewEQPreset(dialog.getInputEditText().getText().toString(),
                                            PlaybackService.sInstance.getEqualizer().getBandLevel((short) 0),
                                            PlaybackService.sInstance.getEqualizer().getBandLevel((short) 1),
                                            PlaybackService.sInstance.getEqualizer().getBandLevel((short) 2),
                                            PlaybackService.sInstance.getEqualizer().getBandLevel((short) 3),
                                            PlaybackService.sInstance.getEqualizer().getBandLevel((short) 4),
                                            0,
                                            0,
                                            0,
                                            PlaybackService.sInstance.getVirtualizer().getRoundedStrength(),
                                            PlaybackService.sInstance.getBassBoost().getRoundedStrength(),

                                            (short) reverbSpinner.getSelectedItemPosition());
                                } catch (Exception e1) {
                                    e1.printStackTrace();
                                }
                                e.printStackTrace();
                            }
                        }
                    }).show();
                }


                break;

        }
        return super.onOptionsItemSelected(item);
    }

    public void toggleAudioFxElements() {

        boolean isEQenabled = PrefUtils.getBoolean(getBaseContext(), PrefKeys.USE_EQUALIZER, PrefDefaults.USE_EQUALIZER);

        bass_boost.setEnabled(isEQenabled);
        virtualizer.setEnabled(isEQenabled);
        reverbSpinner.setEnabled(isEQenabled);
        flat.setEnabled(isEQenabled);

        for (int i = 0; i < BANDCOunt; i++) {
            sliders[i].setEnabled(isEQenabled);
        }

    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BANDCOunt = PrefUtils.getInt(this, PrefKeys.BANDS, 0);
        ThemeHelper.setTheme(this, R.style.NoActionBar);

        axolor = PrefUtils.getInt(getBaseContext(), PrefKeys.ACCENT_INT, PrefDefaults.ACCENT_DEF);
        pcolor = PrefUtils.getInt(getBaseContext(), PrefKeys.PRIMARY_INT, PrefDefaults.PRIMARY_DEF);
        setContentView(R.layout.main);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(pcolor));
        getSupportActionBar().setTitle(" ");


        tintMyBars();
        setTheSeekBarIds();

        toggleAudioFxElements();

        flat.setOnClickListener(this);
        bass_boost.setOnSeekBarChangeListener(this);
        virtualizer.setOnSeekBarChangeListener(this);


        ArrayList<String> reverbPresets = new ArrayList<String>();
        reverbPresets.add("None");

        reverbPresets.add("Small Room");

        reverbPresets.add("Medium Room");
        reverbPresets.add("Large Room");
        reverbPresets.add("Medium Hall");
        reverbPresets.add("Large Hall");
        reverbPresets.add("Plate");

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, reverbPresets);
        dataAdapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        reverbSpinner.setAdapter(dataAdapter);


        reverbSpinner.setSelection(PrefUtils.getInt(getBaseContext(), PrefKeys.PRESET_REVERB_LEVEL, PrefDefaults.PRESET_REVERB_LEVEL), true);
        reverbSpinner.setOnItemSelectedListener(reverbListener);
        reverbListener.onItemSelected(reverbSpinner, null, reverbSpinner.getSelectedItemPosition(), 01);


        short r[] = PlaybackService.sInstance.getEqualizer().getBandLevelRange();
        min_level = r[0];
        max_level = r[1];


        num_sliders = BANDCOunt;

////////make unsupported bands Invisible
        for (int i = num_sliders; i < MAX_SLIDERS; i++) {
            sliders[i].setVisibility(View.GONE);
            slider_labels[i].setVisibility(View.GONE);
        }

////////set the sliders text and seekbar listener
        for (int i = 0; i < num_sliders && i < MAX_SLIDERS; i++) {
            int[] freq_range = PlaybackService.sInstance.getEqualizer().getBandFreqRange((short) i);

            sliders[i].setOnSeekBarChangeListener(this);
            slider_labels[i].setText(formatBandLabel(freq_range));
            slider_labels[i].setTextColor(PrefUtils.getInt(getBaseContext(), PrefKeys.ACCENT_INT, PrefDefaults.ACCENT_DEF));
        }

        justOpenedAndSetMyDefaultValues();
        updateUI();

    }


    public void justOpenedAndSetMyDefaultValues() {
        for (int i = 0; i < BANDCOunt; i++) {
            int level = (short) PrefUtils.getFloat(getBaseContext(), "eq" + i, (short) 0);
            int pos = 100 * level / (max_level - min_level) + 50;

            sliders[i].setProgress(pos);
        }
        bass_boost.setProgress((int) PrefUtils.getFloat(getBaseContext(), PrefKeys.BASS_LEVEL, PrefDefaults.BASS_LEVEL));
        virtualizer.setProgress((int) PrefUtils.getFloat(getBaseContext(), PrefKeys.VIRTUALIZER_LEVEL, PrefDefaults.VIRTUALIZER_LEVEL));

    }

    @Override
    public void onStart() {
        // TODO Auto-generated method stub
        super.onStart();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.

        tintMyBars();

    }

    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        tintMyBars();
    }


    public void setTheSeekBarIds() {

        flat = (Button) findViewById(R.id.flat);
        flat.setBackgroundColor(axolor);

        reverbTitle = (TextView) findViewById(R.id.reverb_title_text1);
        reverbTitle.setTextColor(PrefUtils.getInt(getBaseContext(), PrefKeys.PRIMARY_INT, PrefDefaults.PRIMARY_DEF));
        reverbSpinner = (Spinner) findViewById(R.id.reverb_spinner1);


        bass_boost = (SeekBar) findViewById(R.id.bass_boost);
        try {
            if (MediaUtils.isLollipop()) {
                bass_boost.setProgressTintList(ColorStateList.valueOf(axolor));
                bass_boost.setThumbTintList(ColorStateList.valueOf(axolor));
            }
        } catch (Exception e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        bass_boost_label = (TextView) findViewById(R.id.bass_boost_label);
        bass_boost_label.setTextColor(PrefUtils.getInt(getBaseContext(), PrefKeys.ACCENT_INT, PrefDefaults.ACCENT_DEF));
        virtualizer = (SeekBar) findViewById(R.id.new_vir);
        try {
            if (MediaUtils.isLollipop()) {
                virtualizer
                        .setProgressTintList(ColorStateList.valueOf(axolor));
                virtualizer.setThumbTintList(ColorStateList.valueOf(axolor));
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        virtualizer_label = (TextView) findViewById(R.id.vir_label);
        virtualizer_label.setTextColor(PrefUtils.getInt(getBaseContext(), PrefKeys.ACCENT_INT, PrefDefaults.ACCENT_DEF));

        sliders[0] = (SeekBar) findViewById(R.id.slider_1);
        try {
            if (MediaUtils.isLollipop()) {
                sliders[0].setProgressTintList(ColorStateList.valueOf(axolor));
                sliders[0].setThumbTintList(ColorStateList.valueOf(axolor));
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        slider_labels[0] = (TextView) findViewById(R.id.slider_label_1);
        sliders[1] = (SeekBar) findViewById(R.id.slider_2);
        try {
            if (MediaUtils.isLollipop()) {
                sliders[1].setProgressTintList(ColorStateList.valueOf(axolor));
                sliders[1].setThumbTintList(ColorStateList.valueOf(axolor));
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        slider_labels[1] = (TextView) findViewById(R.id.slider_label_2);
        sliders[2] = (SeekBar) findViewById(R.id.slider_3);
        try {
            if (MediaUtils.isLollipop()) {
                sliders[2].setProgressTintList(ColorStateList.valueOf(axolor));
                sliders[2].setThumbTintList(ColorStateList.valueOf(axolor));
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        slider_labels[2] = (TextView) findViewById(R.id.slider_label_3);
        sliders[3] = (SeekBar) findViewById(R.id.slider_4);
        try {
            if (MediaUtils.isLollipop()) {
                sliders[3].setProgressTintList(ColorStateList.valueOf(axolor));
                sliders[3].setThumbTintList(ColorStateList.valueOf(axolor));
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        slider_labels[3] = (TextView) findViewById(R.id.slider_label_4);
        sliders[4] = (SeekBar) findViewById(R.id.slider_5);
        try {
            if (MediaUtils.isLollipop()) {
                sliders[4].setProgressTintList(ColorStateList.valueOf(axolor));
                sliders[4].setThumbTintList(ColorStateList.valueOf(axolor));
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        slider_labels[4] = (TextView) findViewById(R.id.slider_label_5);
        sliders[5] = (SeekBar) findViewById(R.id.slider_6);
        try {
            if (MediaUtils.isLollipop()) {
                sliders[5].setProgressTintList(ColorStateList.valueOf(axolor));
                sliders[5].setThumbTintList(ColorStateList.valueOf(axolor));
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        slider_labels[5] = (TextView) findViewById(R.id.slider_label_6);
        sliders[6] = (SeekBar) findViewById(R.id.slider_7);
        try {
            if (MediaUtils.isLollipop()) {
                sliders[6].setProgressTintList(ColorStateList.valueOf(axolor));
                sliders[6].setThumbTintList(ColorStateList.valueOf(axolor));
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        slider_labels[6] = (TextView) findViewById(R.id.slider_label_7);
        sliders[7] = (SeekBar) findViewById(R.id.slider_8);
        try {
            if (MediaUtils.isLollipop()) {
                sliders[7].setProgressTintList(ColorStateList
                        .valueOf(Color.CYAN));
                sliders[7].setThumbTintList(ColorStateList.valueOf(axolor));
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        slider_labels[7] = (TextView) findViewById(R.id.slider_label_8);
    }

    public void tintMyBars() {

        // getWindow().setBackgroundDrawable(new
        // ColorDrawable( .getPrimaryColor(getBaseContext())));

        if (MediaUtils.isLollipop()) {

            getWindow().setNavigationBarColor(pcolor);

            getWindow().setStatusBarColor(pcolor);
        }

    }


    @Override
    public void onProgressChanged(SeekBar seekBar, int level, boolean fromTouch) {

        if (fromTouch) {
            presetSPinner.setSelection(10, true);
        }
        if (seekBar == bass_boost) {
            PlaybackService.sInstance.getBassBoost().setEnabled(level > 0);
            PlaybackService.sInstance.getBassBoost().setStrength((short) level); // Already in the right range 0-1000\
            PrefUtils.setFloat(getBaseContext(), PrefKeys.BASS_LEVEL, level);
        } else if (seekBar == virtualizer) {
            PlaybackService.sInstance.getVirtualizer().setEnabled(level > 0);
            PlaybackService.sInstance.getVirtualizer().setStrength((short) level); // Already in the right range 0-1000
            PrefUtils.setFloat(getBaseContext(), PrefKeys.VIRTUALIZER_LEVEL, level);
        } else {
            int new_level = min_level + (max_level - min_level) * level / 100;

            for (int i = 0; i < num_sliders; i++) {
                if (sliders[i] == seekBar) {
                  /*  eq.setBandLevel((short) i, (short) new_level);*/
                    PrefUtils.setFloat(getBaseContext(), "eq" + i, new_level);
                    break;
                }
            }
        }
        updateSliders();
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }


    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
    }

    public String formatBandLabel(int[] band) {
        return milliHzToString(band[0]);// + "-" + milliHzToString(band[1]);
    }


    public String milliHzToString(int milliHz) {
        if (milliHz < 1000)
            return "";
        if (milliHz < 1000000)
            return "" + (milliHz / 1000) + "Hz";
        else
            return "" + (milliHz / 1000000) + "kHz";
    }


    @Override
    public void onClick(View view) {
        if (view == flat) {
            setFlat();
            presetSPinner.setSelection(3, true);
        }
    }

    public void setFlat() {
        if (PlaybackService.sInstance.getEqualizer() != null) {
            for (int i = 0; i < num_sliders; i++) {
                PrefUtils.setFloat(getBaseContext(), "eq" + i, (short) 0);
            }
        }

        if (PlaybackService.sInstance.getBassBoost() != null) {

            try {
                PlaybackService.sInstance.getBassBoost().setStrength((short) 0);
            } catch (Exception e) {
                e.printStackTrace();
            }
            PrefUtils.setFloat(getBaseContext(), PrefKeys.BASS_LEVEL, 0);
        }

        if (PlaybackService.sInstance.getVirtualizer() != null) {

            PlaybackService.sInstance.getVirtualizer().setStrength((short) 0);
            PrefUtils.setFloat(getBaseContext(), PrefKeys.VIRTUALIZER_LEVEL, 0);
        }

        reverbSpinner.setSelection(0, true);
        PrefUtils.setInt(getBaseContext(), PrefKeys.PRESET_REVERB_LEVEL, 0);
        updateUI();

    }

    public void updateUI() {
        updateSliders();
        updateBassBoost();
        updateVirtualizer();
        updatePresetReverb();

    }

    public void updateBassBoost() {
        if (PlaybackService.sInstance.getBassBoost() != null)
            bass_boost.setProgress(PlaybackService.sInstance.getBassBoost().getRoundedStrength());
        else
            bass_boost.setProgress(0);
    }

    public void updatePresetReverb() {
        reverbSpinner.setSelection(PrefUtils.getInt(getBaseContext(),
                PrefKeys.PRESET_REVERB_LEVEL, PrefDefaults.PRESET_REVERB_LEVEL), true);

    }

    public void updateSliders() {
        for (int i = 0; i < num_sliders; i++) {
            int level;
            if (PrefUtils.getBoolean(getBaseContext(), PrefKeys.USE_EQUALIZER, PrefDefaults.USE_EQUALIZER)) {
                level = PlaybackService.sInstance.getEqualizer().getBandLevel((short) i);
            } else {
                level = (short) 0;
            }
            int pos = 100 * level / (max_level - min_level) + 50;

            sliders[i].setProgress(pos);
        }
    }

    public void updateVirtualizer() {
        if (PlaybackService.sInstance.getVirtualizer() != null)
            virtualizer.setProgress(PlaybackService.sInstance.getVirtualizer().getRoundedStrength());
        else
            virtualizer.setProgress(0);
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
    }
}
