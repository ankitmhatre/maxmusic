package com.dragonide.maxmusic;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.squareup.picasso.Picasso;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotWriteException;
import org.jaudiotagger.tag.FieldDataInvalidException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.KeyNotFoundException;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagOptionSingleton;
import org.jaudiotagger.tag.datatype.Artwork;
import org.jsoup.Jsoup;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

public class EditDetails extends PlaybackActivity implements RetrieveCompleteListener, DownloadAlbumArtListener, View.OnClickListener, MaterialDialog.ListCallback {
    RetrieveFeedtask retrieveFeedtask = new RetrieveFeedtask();
    public RetrieveCompleteListener callback;

    DownloadImageTask downloadImageTask = new DownloadImageTask();
    public DownloadAlbumArtListener downloadimageCallback;

    public String finalfilePath;
    String size = "extralarge";
    boolean not_supported = false;
    AudioFile audio_file;
    EditText etAlbum, etArtist, etComposer, etGenre, etTitle, etTrack, etYear,
            etLyrics;
    Bundle extras;
    static String url, mAlbum, mArtist, mComposer, mGenre, mTitle, mTrack,
            mYear, mLyrics, mygivenpath;
    File mp3_File;
    File filepath = null;
    Tag tag;
    Artwork rtr = null;

    ImageView mImageView;
    long _id;
    String LOG_NAME = "docc";

    // Progress dialog type (0 - for Horizontal progress bar)
    public static final int progress_bar_type = 0;

    private void nowSetThierFields() {
        etTitle.setText(mTitle);
        etArtist.setText(mArtist);
        etAlbum.setText(mAlbum);
        etGenre.setText(mGenre);
        etYear.setText(mYear);
        etTrack.setText(mTrack);
        etComposer.setText(mComposer);
        etLyrics.setText(mLyrics);

        // mImageView.setImageBitmap(sng.getCover(PlaybackService.sInstance));
    }

    public void getEDItTExtToSTring() {
        mAlbum = etAlbum.getText().toString();
        mTitle = etTitle.getText().toString();
        mArtist = etArtist.getText().toString();
        mGenre = etGenre.getText().toString();
        mYear = etYear.getText().toString();
        mTrack = etTrack.getText().toString();
        mComposer = etComposer.getText().toString();
        mLyrics = etLyrics.getText().toString();
    }

    public void initiateIDS() {
        etTitle = ((EditText) findViewById(R.id.edit_song_title));
        etArtist = ((EditText) findViewById(R.id.edit_song_artist));
        etAlbum = ((EditText) findViewById(R.id.edit_song_album));
        etGenre = ((EditText) findViewById(R.id.edit_song_genre));
        etYear = ((EditText) findViewById(R.id.edit_song_year));
        etTrack = ((EditText) findViewById(R.id.edit_track_number));
        etComposer = ((EditText) findViewById(R.id.edit_composer));
        etLyrics = ((EditText) findViewById(R.id.edit_song_lyrics));
        mImageView = (ImageView) findViewById(R.id.albumArtEdit);
        mImageView.setFocusable(true);
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

    private String getImageSize() {

        return size;
    }


    public class RetrieveFeedtask extends AsyncTask<String, Void, String> {
        private RetrieveCompleteListener callback;


        String albumArtUrl = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();


        }

        @Override
        protected String doInBackground(String... urls) {
            try {
                boolean check = false;

                String text = null;
                String ur = Jsoup.connect(urls[0]).get().toString();
                XmlPullParserFactory xmlPullParserFactory = XmlPullParserFactory.newInstance();
                xmlPullParserFactory.setNamespaceAware(true);
                XmlPullParser parser = xmlPullParserFactory.newPullParser();
                parser.setInput(new StringReader(ur));
                Log.d("doc", ur);


                int event;
                String TAG_ITEM = "image";
                while ((event = parser.next()) != XmlPullParser.END_DOCUMENT) {
                    if (event == XmlPullParser.START_TAG) {
                        String tag = parser.getName();
                        if (TAG_ITEM.equals(tag)) {

                            if ((parser.getAttributeValue(null, "size").equals(urls[1]))) {
                                Log.d("result", parser.getText());
                                return parser.getText();
                            }

                        }
                    }
                }


            } catch (Exception e) {
                e.printStackTrace();

            }
            return "dont_know";

        }


        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);

        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (result != null) {
                Log.d("DONEEEEEEEEEEEE", result + "");
                callback.onRetrieveTaskComplete(result);
                downloadImageTask.execute(result);
                try {
                    Picasso.with(EditDetails.this)
                            .load(url)
                            .error(R.mipmap.cover_circle)
                            .into(mImageView);
                    Toast.makeText(
                            getApplicationContext(),
                            R.string.found,
                            Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (url != null) {
                    try {
                        rtr = Artwork
                                .createArtworkFromFile(getArtworkfromfile());

                    } catch (Exception e) {
                        e.printStackTrace();
                        // Toast.makeText(EditDetails.this,
                        // e.toString(),Toast.LENGTH_LONG).show();

                    }
                } else {
                    Toast.makeText(EditDetails.this, "Cannot find Matching URL", Toast.LENGTH_LONG).show();

                }
            } else {
                Toast.makeText(
                        getApplicationContext(),
                        R.string.download_manually,
                        Toast.LENGTH_LONG).show();
                callback.onRetrieveTaskComplete("_not_available");
            }

        }


    }

    @Override
    public void onRetrieveTaskComplete(String linkresult) {

        url = linkresult;
        Log.d("thisislink", linkresult);
    }


    public class DownloadImageTask extends AsyncTask<String, Integer, String> {
        private DownloadAlbumArtListener downloadimageCallback;
        String path;
        ProgressDialog p;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            p = new ProgressDialog(EditDetails.this);
            p.setMessage("Downloading");
            p.setIndeterminate(false);
            p.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            p.setCancelable(false);
            p.setMax(100);
            p.setProgress(0);
            p.show();
        }

        @Override
        protected String doInBackground(String... f_url) {
            int count;
            try {
                URL url = new URL(f_url[0]);
                URLConnection conection = url.openConnection();
                conection.connect();
                // getting file length
                int lenghtOfFile = conection.getContentLength();

                // input stream to read file - with 8k buffer
                InputStream input = new BufferedInputStream(
                        url.openStream(), 8192);


                path = Environment.getExternalStorageDirectory()
                        + "/MaxMusic/albumarts"
                        // + System.currentTimeMillis()
                        + "/" + System.currentTimeMillis() + ".png";
                OutputStream output = new FileOutputStream(path);

                byte data[] = new byte[5120];

                long total = 0;

                while ((count = input.read(data)) != -1) {
                    total += count;
                    // publishing the progress....
                    // After this onProgressUpdate will be called
                    publishProgress((int) ((total * 100) / lenghtOfFile));
                    String op = ((total * 100) / lenghtOfFile) + "";
                    Log.d("Progress", op);

                    // writing data to file
                    output.write(data, 0, count);
                }

                // flushing output
                output.flush();

                // closing streams
                output.close();
                input.close();

            } catch (Exception e) {
                Log.e("Error: ", e.getMessage());
            }

            return path;
        }

        /**
         * Updating progress bar
         */
        @Override
        protected void onProgressUpdate(Integer... progress) {
            Log.d("Progress", progress[0] + "");
            p.setProgress(progress[0]);
        }

        /**
         * After completing background task Dismiss the progress dialog
         **/
        @Override
        protected void onPostExecute(String file_url) {
            // dismiss the dialog after the file was downloaded
            if (file_url!=null) {
                downloadimageCallback.onAlbumArtDownloadComplete(file_url);
                Toast.makeText(
                        getApplicationContext(),
                        R.string.saved,
                        Toast.LENGTH_LONG).show();
                p.dismiss();
            }else{
                p.dismiss();
                Toast.makeText(
                        getApplicationContext(),
                        R.string.cannot_find_the_album,
                        Toast.LENGTH_LONG).show();
            }
        }


    }


    @Override
    public void onAlbumArtDownloadComplete(String finalPath) {
        Log.d("Pathe", finalPath);
        setFile(finalPath);
    }

    @Override
    public void onCreate(Bundle paramBundle) {
        ThemeHelper.setTheme(this, R.style.Playback);


        super.onCreate(paramBundle);
        retrieveFeedtask.callback = this;
        downloadImageTask.downloadimageCallback = this;
        extras = getIntent().getExtras();
        mygivenpath = extras.getString("PATH", null);
        _id = extras.getLong("_ID");

        setContentView(R.layout.edit_details);
        Snackbar.make(findViewById(R.id.finishedit), R.string.enterproperly, Snackbar.LENGTH_LONG).show();
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        tintMyBars();
        try {

            mp3_File = new File(mygivenpath);
            TagOptionSingleton.getInstance().setAndroid(true);
            audio_file = AudioFileIO.read(mp3_File);

            tag = audio_file.getTag();

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        initiateIDS();
        nowSetThierFields();


        Bitmap localBitmap = null;
        byte[] arrayOfByte = null;
        try {
            TagOptionSingleton.getInstance().setAndroid(true);
            arrayOfByte = tag.getFirstArtwork().getBinaryData();
            localBitmap = BitmapFactory.decodeByteArray(arrayOfByte, 0,
                    arrayOfByte.length);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        if (localBitmap != null) {

            mImageView.setImageBitmap(localBitmap);
        } else {
            mImageView.setImageBitmap(BitmapFactory.decodeResource(
                    getResources(), R.mipmap.cover_circle));
        }
        TagOptionSingleton.getInstance().setAndroid(true);
        try {
            etArtist.setText(tag.getFirst(FieldKey.ARTIST));
            etAlbum.setText(tag.getFirst(FieldKey.ALBUM));
            etTitle.setText(tag.getFirst(FieldKey.TITLE));
            etYear.setText(tag.getFirst(FieldKey.YEAR));
            etTrack.setText(tag.getFirst(FieldKey.TRACK));
            etComposer.setText(tag.getFirst(FieldKey.COMPOSER));
            etGenre.setText(tag.getFirst(FieldKey.GENRE));
            etLyrics.setText(tag.getFirst(FieldKey.LYRICS));

        } catch (Exception e2) {
            not_supported = true;
            // TODO Auto-generated catch block
            e2.printStackTrace();
        }

        if (!not_supported) {
            mImageView.setOnClickListener(this);
        }

    }

    @Override
    public void onClick(View view) {
        super.onClick(view);

        new MaterialDialog.Builder(this)
                .title(R.string.albumart)
                .items(R.array.albumitems)
                .itemsCallback(this)
                .show();
    }

    @Override
    public void onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
        switch (which) {
            case 0:
                new MaterialDialog.Builder(EditDetails.this).title("Select a Size").items(R.array.artSizes).itemsCallback(new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                        switch (which) {
                            case 0:
                                size = "small";

                                break;
                            case 1:
                                size = "medium";

                                break;
                            case 2:
                                size = "large";

                                break;
                            case 3:
                                size = "extralarge";

                                break;
                            case 4:
                                size = "mega";

                                break;
                            default:
                                size = "extralarge";


                        }
                        fetchAlbumArt(size);


                    }
                }).show();


                break;
            case 1:
                Intent localIntent = new Intent("android.intent.action.PICK");
                localIntent.setType("image/*");
                startActivityForResult(localIntent, 676);
                break;
            case 2:
                mImageView.setImageDrawable(getResources().getDrawable(R.mipmap.cover_circle));
                try {
                    tag.deleteArtworkField();
                    audio_file.commit();
                } catch (CannotWriteException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                break;

        }
    }

    @Override
    public void onBackPressed() {


        new MaterialDialog.Builder(this)
                .title(R.string.done)
                .content(R.string.areyousure)
                .positiveText(R.string.keep)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        getEDItTExtToSTring();
                        saveTags();
                        finish();
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        finish();

                    }
                })
                .negativeText(R.string.discard)
                .show();

    }

  /*  @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        new MaterialDialog.Builder(this)
                .title(R.string.done)
                .content(R.string.areyousure)
                .positiveText(R.string.keep)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        getEDItTExtToSTring();
                        saveTags();
                        finish();
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        finish();

                    }
                })
                .negativeText(R.string.discard)
                .show();
        return false;
    }*/

    private void fetchAlbumArt(String no) {

        StringBuilder stringBuilder = null;

        try {
            stringBuilder = new StringBuilder(
                    "http://ws.audioscrobbler.com/2.0/");
            stringBuilder
                    .append("?method=album.getinfo");
            stringBuilder.append("&api_key=");
            stringBuilder
                    .append("a4f6306b50b6416c185726b8ea9a8242");

            stringBuilder.append("&artist="
                    + URLEncoder.encode(etArtist
                            .getText().toString(),
                    "UTF-8"));

            stringBuilder.append("&album="
                    + URLEncoder.encode(etAlbum
                            .getText().toString(),
                    "UTF-8"));
            Log.d("docurl", stringBuilder.toString());
            retrieveFeedtask.execute(stringBuilder.toString(), no);
        } catch (Exception e) {

            e.printStackTrace();
        }


    }

    public void setFile(String path) {

        try {
            filepath = new File(path);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.d("ImagePath", path);

    }

    public File getArtworkfromfile() {


        return filepath;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        sendBroadcast(new Intent(
                "android.intent.action.MEDIA_SCANNER_SCAN_FILE",
                Uri.fromFile(new File(mygivenpath))));
    }

    @Override
    protected void onActivityResult(int Int1, int Int2, Intent paramIntent) {
        super.onActivityResult(Int1, Int2, paramIntent);


        try {
            rtr = Artwork.createArtworkFromFile(new File(
                    getRealPathFromURI(
                            paramIntent.getData())));
            Bitmap localBitmap = BitmapFactory
                    .decodeFile(getRealPathFromURI(
                            paramIntent.getData()));
            mImageView.setImageBitmap(localBitmap);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public String getRealPathFromURI(Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = {MediaStore.Images.Media.DATA};
            cursor = getContentResolver().query(contentUri, proj, null,
                    null, null);
            int column_index = 0;
            try {
                column_index = cursor
                        .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            } catch (Exception e) {
                e.printStackTrace();
            }
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, MENU_SAVE, 0, R.string.savetags)
                .setIcon(R.drawable.ic_save_white_24dp)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return super.onCreateOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem menuitem) {
        switch (menuitem.getItemId()) {
            default:
            case MENU_SAVE:

                getEDItTExtToSTring();
                saveTags();
                sendBroadcast(new Intent(
                        "android.intent.action.MEDIA_SCANNER_SCAN_FILE",
                        Uri.fromFile(new File(mygivenpath))));
                Toast.makeText(getApplicationContext(), R.string.saved,
                        Toast.LENGTH_SHORT).show();
                finish();

                return super.onOptionsItemSelected(menuitem);
        }
    }

    public void saveTags() {

        if (!not_supported) {
            try {
                TagOptionSingleton.getInstance().setAndroid(true);
                if (rtr != null) {

                    try {
                        tag.createField(rtr);
                        tag.addField(rtr);
                        tag.setField(rtr);
                    } catch (Exception e) {
                        e.printStackTrace();


                    }

                    Toast.makeText(PlaybackService.sInstance,
                            R.string.artworksaved, Toast.LENGTH_SHORT).show();
                }
                tag.setField(FieldKey.TITLE, mTitle);
                tag.setField(FieldKey.ALBUM, mAlbum);
                tag.setField(FieldKey.ARTIST, mArtist);
                tag.setField(FieldKey.GENRE, mGenre);
                tag.setField(FieldKey.YEAR, mYear);

                try {
                    tag.setField(FieldKey.TRACK, mTrack);
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();/*
                    Toast.makeText(PlaybackService.sInstance,
                            R.string.checktrack, Toast.LENGTH_SHORT).show();*/
                }
                tag.setField(FieldKey.COMPOSER, mComposer);
                tag.setField(FieldKey.LYRICS, mLyrics);

                audio_file.commit();
                sendBroadcast(new Intent(
                        "android.intent.action.MEDIA_SCANNER_SCAN_FILE"));

                Toast.makeText(getApplicationContext(), R.string.properscan,
                        Toast.LENGTH_LONG).show();
                return;
            } catch (KeyNotFoundException localKeyNotFoundException) {
                localKeyNotFoundException.printStackTrace();
                return;
            } catch (FieldDataInvalidException localFieldDataInvalidException) {
                localFieldDataInvalidException.printStackTrace();
                return;
            } catch (CannotWriteException localCannotWriteException) {
                localCannotWriteException.printStackTrace();
            }
        }
    }

    public void insertArtworkUsingJAudio(File file) {
        if (file != null)
            try {
                AudioFile audioFile = AudioFileIO.read(new File(
                        mygivenpath));
                TagOptionSingleton.getInstance().setAndroid(true);
                audioFile.getTag().addField(
                        Artwork.createArtworkFromFile(file));
                audioFile.commit();
                return;
            } catch (Exception localException1) {
                try {
                    AudioFile audioFile = AudioFileIO.read(new File(
                            mygivenpath));
                    TagOptionSingleton.getInstance().setAndroid(true);
                    audioFile.getTag().setField(
                            Artwork.createArtworkFromFile(file));
                    audioFile.commit();
                    return;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
    }


}
