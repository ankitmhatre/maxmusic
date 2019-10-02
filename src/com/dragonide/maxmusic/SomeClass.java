package com.dragonide.maxmusic;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by Ankit on 18-07-2016.
 */
public class SomeClass extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ThemeHelper.setTheme(this, R.style.Playback);
        setContentView(R.layout.nsjan);
    }
}
