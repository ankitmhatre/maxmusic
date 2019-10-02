/*
 * Copyright (C) 2014 Saravan Pantham
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dragonide.maxmusic;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * SQLite database implementation. Note that this class
 * only contains methods that access Jams' private
 * database. For methods that access Android's
 * MediaStore database, see MediaStoreAccessHelper.
 *
 * @author Saravan Pantham
 */
public class DBAccessHelper extends SQLiteOpenHelper {

    //Database instance. Will last for the lifetime of the application.
    private static DBAccessHelper sInstance;

    //Writable database instance.
    private SQLiteDatabase mDatabase;
    Context mContext;


    //Database Version.
    private static final int DATABASE_VERSION = 1;

    //Database Name.
    private static final String DATABASE_NAME = "Equalizer.db";

    //Common fields.
    public static final String _ID = "_id";


    public static final String EQ_1 = "EQ_1";
    public static final String EQ_2 = "EQ_2";
    public static final String EQ_3 = "EQ_3";
    public static final String EQ_4 = "EQ_4";
    public static final String EQ_5 = "EQ_5";
    public static final String EQ_6 = "EQ_6";
    public static final String EQ_7 = "EQ_7";
    public static final String EQ_8 = "EQ_8";
    public static final String VIRTUALIZER = "eq_virtualizer";
    public static final String BASS_BOOST = "eq_bass_boost";
    public static final String REVERB = "eq_reverb";


    //Equalizer settings table for individual songs.
    public static final String EQUALIZER_TABLE = "EqualizerTable";

    //Equalizer presets table.
    public static final String EQUALIZER_PRESETS_TABLE = "EqualizerPresetsTable";
    public static final String PRESET_NAME = "preset_name";


    public DBAccessHelper(Context context) {

        super(context, DATABASE_NAME, null, DATABASE_VERSION);

        mContext = context;
    }

    /**
     * Returns a singleton instance for the database.
     *
     * @param context
     * @return
     */
    public static synchronized DBAccessHelper getInstance(Context context) {
        if (sInstance == null)
            sInstance = new DBAccessHelper(context.getApplicationContext());

        return sInstance;
    }

    /**
     * Returns a writable instance of the database. Provides an additional
     * null check for additional stability.
     */
    private synchronized SQLiteDatabase getDatabase() {
        if (mDatabase == null)
            mDatabase = getWritableDatabase();

        return mDatabase;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        //Music folders table.

        //Equalizer table.
      /*  String[] equalizerTableCols = {_ID ,EQ_1, EQ_2,
                EQ_3, EQ_4, EQ_5,
                EQ_6, EQ_7, EQ_8, VIRTUALIZER,
                BASS_BOOST, REVERB};*/

  /*      String[] equalizerTableColTypes = {"TEXT","TEXT", "TEXT", "TEXT",
                "TEXT", "TEXT", "TEXT",
                "TEXT", "TEXT", "TEXT",
                "TEXT", "TEXT"};
*/
    /*    String createEqualizerTable = buildCreateStatement(EQUALIZER_TABLE,
                equalizerTableCols,
                equalizerTableColTypes);
*/
        //Equalizer presets table.
        String[] equalizerPresetsTableCols = {PRESET_NAME, EQ_1, EQ_2,
                EQ_3, EQ_4, EQ_5,
                EQ_6, EQ_7, EQ_8,
                VIRTUALIZER,BASS_BOOST, REVERB};

        String[] equalizerPresetsTableColTypes = {"TEXT", "TEXT", "TEXT",
                "TEXT", "TEXT", "TEXT",
                "TEXT", "TEXT", "TEXT",
                "TEXT", "TEXT", "TEXT"};

        String createEqualizerPresetsTable = buildCreateStatement(EQUALIZER_PRESETS_TABLE,
                equalizerPresetsTableCols,
                equalizerPresetsTableColTypes);


        //Execute the CREATE statements.

       // db.execSQL(createEqualizerTable);
        db.execSQL(createEqualizerPresetsTable);
        Log.d("Eqw", db.toString());


    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub

    }

    @Override
    protected void finalize() {
        try {
            getDatabase().close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Constructs a fully formed CREATE statement using the input
     * parameters.
     */
    private String buildCreateStatement(String tableName, String[] columnNames, String[] columnTypes) {
        String createStatement = "";
        if (columnNames.length == columnTypes.length) {
            createStatement += "CREATE TABLE IF NOT EXISTS " + tableName + "("
                    + _ID + " INTEGER PRIMARY KEY, ";

            for (int i = 0; i < columnNames.length; i++) {

                if (i == columnNames.length - 1) {
                    createStatement += columnNames[i]
                            + " "
                            + columnTypes[i]
                            + ")";
                } else {
                    createStatement += columnNames[i]
                            + " "
                            + columnTypes[i]
                            + ", ";
                }

            }

        }

        return createStatement;
    }


    /***********************************************************
     * EQUALIZER PRESETS TABLE METHODS.
     ***********************************************************/

    /**
     * Adds a new EQ preset to the table.
     */
    public void addNewEQPreset(String presetName,
                               int eq1,
                               int eq2,
                               int eq3,
                               int eq4,
                               int eq5,
                               int eq6,
                               int eq7,
                               int eq8,
                               short virtualizer,
                               short bassBoost,
                               short reverb) {

        ContentValues values = new ContentValues();
        values.put(PRESET_NAME, presetName);
        values.put(EQ_1, eq1);
        values.put(EQ_2, eq2);
        values.put(EQ_3, eq3);
        values.put(EQ_4, eq4);
        values.put(EQ_5, eq5);
        values.put(EQ_6, eq6);
        values.put(EQ_7, eq7);
        values.put(EQ_8, eq8);
        values.put(VIRTUALIZER, virtualizer);
        values.put(BASS_BOOST, bassBoost);
        values.put(REVERB, reverb);

        getDatabase().insert(EQUALIZER_PRESETS_TABLE, null, values);

    }

    /**
     * This method returns the specified eq preset.
     */
    public int[] getPresetEQValues(String presetName) {

        String condition = PRESET_NAME + "=" + "'" + presetName.replace("'", "''") + "'";
        String[] columnsToReturn = {EQ_1, EQ_2, EQ_3,
                EQ_4, EQ_5, EQ_6,
                EQ_7, EQ_8, VIRTUALIZER, BASS_BOOST, REVERB};

        Cursor cursor = getDatabase().query(EQUALIZER_PRESETS_TABLE, columnsToReturn, condition, null, null, null, null);
        int[] eqValues = new int[11];

        if (cursor.moveToFirst() && cursor.getCount() != 0) {
            eqValues[0] = cursor.getInt(cursor.getColumnIndex(EQ_1));
            eqValues[1] = cursor.getInt(cursor.getColumnIndex(EQ_2));
            eqValues[2] = cursor.getInt(cursor.getColumnIndex(EQ_3));
            eqValues[3] = cursor.getInt(cursor.getColumnIndex(EQ_4));
            eqValues[4] = cursor.getInt(cursor.getColumnIndex(EQ_5));
            eqValues[5] = cursor.getInt(cursor.getColumnIndex(EQ_6));
            eqValues[6] = cursor.getInt(cursor.getColumnIndex(EQ_7));
            eqValues[7] = cursor.getInt(cursor.getColumnIndex(EQ_8));
            eqValues[8] = cursor.getInt(cursor.getColumnIndex(VIRTUALIZER));
            eqValues[9] = cursor.getInt(cursor.getColumnIndex(BASS_BOOST));
            eqValues[10] = cursor.getInt(cursor.getColumnIndex(REVERB));

            cursor.close();

        } else {
            eqValues[0] = 16;
            eqValues[1] = 16;
            eqValues[2] = 16;
            eqValues[3] = 16;
            eqValues[4] = 16;
            eqValues[5] = 16;
            eqValues[6] = 16;
            eqValues[7] = 16;
            eqValues[8] = 16;
            eqValues[9] = 16;
            eqValues[10] = 16;

        }

        return eqValues;
    }

    /**
     * This method updates the specified EQ preset.
     */
    public void updateEQPreset(String presetName,
                               int eq1,
                               int eq2,
                               int eq3,
                               int eq4,
                               int eq5,
                               int eq6,
                               int eq7,
                               int eq8,
                               short virtualizer,
                               short bassBoost,
                               short reverb) {

        //Escape any rogue apostrophes.
        if (presetName != null) {

            if (presetName.contains("'")) {
                presetName = presetName.replace("'", "''");
            }

        }

        ContentValues values = new ContentValues();
        values.put(EQ_1, eq1);
        values.put(EQ_2, eq2);
        values.put(EQ_3, eq3);
        values.put(EQ_4, eq4);
        values.put(EQ_5, eq5);
        values.put(EQ_6, eq6);
        values.put(EQ_7, eq7);
        values.put(EQ_8, eq8);
        values.put(VIRTUALIZER, virtualizer);
        values.put(BASS_BOOST, bassBoost);
        values.put(REVERB, reverb);

        String condition = PRESET_NAME + " = " + "'" + presetName + "'";
        getDatabase().update(EQUALIZER_PRESETS_TABLE, values, condition, null);

    }

    /**
     * Returns a cursor with all EQ presets in the table.
     */
    public Cursor getAllEQPresets() {
        String query = "SELECT * FROM " + EQUALIZER_PRESETS_TABLE;
        return getDatabase().rawQuery(query, null);

    }

    //Deletes the specified preset.
    public void deletePreset(String presetName) {
        String condition = PRESET_NAME + " = " + "'" + presetName.replace("'", "''") + "'";
        getDatabase().delete(EQUALIZER_PRESETS_TABLE, condition, null);

    }

}