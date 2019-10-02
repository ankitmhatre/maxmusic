package com.dragonide.maxmusic;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.GridView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagOptionSingleton;
import org.jaudiotagger.tag.datatype.Artwork;
import org.w3c.dom.Document;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

/**
 * Created by Ankit on 5/18/2017.
 */

public class AlbumArtDialog extends AppCompatActivity implements ImageFoundListener, DownloadAlbumArtListener {
    AlbumArtAdapter albumArtAdapter;
    AlbumFetcher albumFetcher = new AlbumFetcher();
    DownloadImageTask downloadImageTask = new DownloadImageTask();
    public DownloadAlbumArtListener downloadimageCallback;
    private static final int KEEP_ALIVE_TIME = 1;
    // Sets the Time Unit to seconds
    private static final TimeUnit KEEP_ALIVE_TIME_UNIT = TimeUnit.SECONDS;
    // Sets the threadpool size to 8
    private static final int CORE_POOL_SIZE = 2;
    private static final int MAXIMUM_POOL_SIZE = 8;
    AudioFile audio_file;
    File mp3_File;
    public List<AlbumArtItem> albumArtItems;
    public ThreadPoolExecutor threadPoolExecutor;
    GridView gridView;
    int i = 0;
    File filepath = null;
    Artwork rtr = null;
    String mPath;
    MaterialDialog md, check;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        albumArtItems = new ArrayList<>();
        albumArtAdapter = new AlbumArtAdapter(albumArtItems);
        mPath = getIntent().getExtras().getString("PATH", null);
        albumFetcher.delegate = this;
        downloadImageTask.downloadimageCallback = this;

       /* for (int i = 1; i < 11; i++) {

            Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.fallback_cover);
            AlbumArtItem albumArtItem = new AlbumArtItem(icon, "asd");

            albumArtItems.add(albumArtItem);
        }
        albumArtAdapter.notifyDataSetChanged();*/
        check = new MaterialDialog.Builder(this)
                .title(R.string.progress_dialog)
                .content(R.string.please_wait)
                .onNeutral(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                        finish();
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                        finish();
                    }
                })
                .canceledOnTouchOutside(false)
                .cancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        finish();
                    }
                })
                .progress(true, 0)
                .show();

        //  recyclerView.setBackground(new ColorDrawable(Color.BLACK));//v.findViewById(R.id.albumrecylcer);// new RecyclerView(this);
       /* recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(albumArtAdapter);
*/

        try {
            if (PlaybackService.hasInstance()) {
                final String art_url =
                        "http://ws.audioscrobbler.com/2.0/?method=album.getinfo&api_key=a4f6306b50b6416c185726b8ea9a8242"
                                + "&artist="
                                + URLEncoder.encode(PlaybackService.sInstance.getSong(0).artist, "UTF-8")
                                + "&album="
                                + URLEncoder.encode(PlaybackService.sInstance.getSong(0).album, "UTF-8");

                try {
                    StrictMode.ThreadPolicy policy =
                            new StrictMode.ThreadPolicy.Builder().permitAll().build();
                    StrictMode.setThreadPolicy(policy);
                    String doasac = getXmlFromUrl(art_url);
                    if (doasac.equals("none")) {
                        Toast.makeText(this, "Cannot cannect", Toast.LENGTH_SHORT).show();
                    } else {
                        XMltoImages(doasac);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    public void XMltoImages(String s) throws Exception {
        XmlPullParserFactory xmlPullParserFactory = XmlPullParserFactory.newInstance();
        xmlPullParserFactory.setNamespaceAware(true);
        XmlPullParser parser = xmlPullParserFactory.newPullParser();
        parser.setInput(new StringReader(s));
        int event;
        String TAG_ITEM = "image";
        while ((event = parser.next()) != XmlPullParser.END_DOCUMENT) {
            if (event == XmlPullParser.START_TAG) {
                String tag = parser.getName();
                if (TAG_ITEM.equals(tag)) {

                    String a = parser.getAttributeValue(null, "size");
                    parser.next();
                    String b = parser.getText();
                    Log.d("albumartshit", b + " ---- " + a);

                    //We get Shoit here   :  parser.getAttributeValue(null, "size")  : parser.getText()


                    albumFetcher = new AlbumFetcher();
                    albumFetcher.delegate = AlbumArtDialog.this;
                    albumFetcher.execute(b, a);

                }
            }
        }

    }

    public void done() {
        Log.d("albumartshit", "done()");
        check.dismiss();
        md = new MaterialDialog.Builder(this)
                .title(R.string.albumart)
                .adapter(albumArtAdapter, new LinearLayoutManager(this))
                .negativeText(R.string.cancel)
                .cancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        finish();
                    }
                })
                .onNeutral(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                        finish();
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                        finish();
                    }
                })
                .canceledOnTouchOutside(false)
                .build();
        try {
            md.show();
        } catch (Exception e) {
            e.printStackTrace();
        }

        View v = md.getCustomView();

        RecyclerView recyclerView = md.getRecyclerView();
        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(AlbumArtDialog.this, recyclerView, new ClickListener() {
            @Override
            public void onClick(View view, int position) {
                AlbumArtItem albumArtAdapteadsa = albumArtItems.get(position);
               downloadImageTask.execute(albumArtAdapteadsa.getUrl());
                md.dismiss();
            }

            @Override
            public void onLongClick(View view, int position) {

                AlbumArtItem albumArtAdapteadsa = albumArtItems.get(position);
                Toast.makeText(AlbumArtDialog.this, albumArtAdapteadsa.getReso(), Toast.LENGTH_SHORT).show();
            }
        }));
    }

    public String getXmlFromUrl(String url) {
        try {
            Log.d("albumartshit", "XmlDocumentToString");
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(new URL(url).openStream());
            DOMSource domSource = new DOMSource(doc);
            StringWriter writer = new StringWriter();
            StreamResult result = new StreamResult(writer);
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.transform(domSource, result);
            return writer.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "none";
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public class AlbumFetcher extends AsyncTask<String, String, Bitmap> {

        public ImageFoundListener delegate = null;
        String size;
        String m_url;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);

            Log.d("albumartshit", "onPostExecute");
            if (bitmap != null) {
                delegate.passData(bitmap, size, m_url);
            } else {
                Log.d("albumartshit", "else in posetExceute");
                Toast.makeText(AlbumArtDialog.this, "Cannot find Album Art", Toast.LENGTH_SHORT).show();
            }
            albumFetcher = null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            try {
                Log.d("albumartshit", "doInBackgr");
                Log.d("albumartshit", params[0]);
                size = params[1];
                m_url = params[0];
                URL url = new URL(params[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                Bitmap myBitmap = BitmapFactory.decodeStream(input);
                return myBitmap;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    @Override
    public void passData(Bitmap data, String size, String url) {


        i++;
        Log.d("albumartshit", "inPassData" + i);
        AlbumArtItem albumArtItem = new AlbumArtItem(data, size, url);

        albumArtItems.add(albumArtItem);
        albumArtAdapter.notifyDataSetChanged();


        if (i == 5)
            done();
    }

    public interface ClickListener {
        void onClick(View view, int position);

        void onLongClick(View view, int position);
    }

    public static class RecyclerTouchListener implements RecyclerView.OnItemTouchListener {

        private GestureDetector gestureDetector;
        private AlbumArtDialog.ClickListener clickListener;

        public RecyclerTouchListener(Context context, final RecyclerView recyclerView, final AlbumArtDialog.ClickListener clickListener) {
            this.clickListener = clickListener;
            gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    return true;
                }

                @Override
                public void onLongPress(MotionEvent e) {
                    View child = recyclerView.findChildViewUnder(e.getX(), e.getY());
                    if (child != null && clickListener != null) {
                        clickListener.onLongClick(child, recyclerView.getChildPosition(child));
                    }
                }
            });
        }

        @Override
        public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {

            View child = rv.findChildViewUnder(e.getX(), e.getY());
            if (child != null && clickListener != null && gestureDetector.onTouchEvent(e)) {
                clickListener.onClick(child, rv.getChildPosition(child));
            }
            return false;
        }

        @Override
        public void onTouchEvent(RecyclerView rv, MotionEvent e) {
        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

        }
    }


    public class DownloadImageTask extends AsyncTask<String, Integer, String> {
        private DownloadAlbumArtListener downloadimageCallback;
        String path;
        ProgressDialog p;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            p = new ProgressDialog(AlbumArtDialog.this);
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
            if (file_url != null) {
                downloadimageCallback.onAlbumArtDownloadComplete(file_url);
                Toast.makeText(
                        getApplicationContext(),
                        R.string.saved,
                        Toast.LENGTH_LONG).show();
                p.dismiss();
            } else {
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

    public void setFile(String path) {

        try {

            TagOptionSingleton.getInstance().setAndroid(true);
            filepath = getArtworkfromfile();
            rtr = Artwork.createArtworkFromFile(filepath);

            mp3_File = new File(mPath);
            TagOptionSingleton.getInstance().setAndroid(true);
            audio_file = AudioFileIO.read(mp3_File);

            Tag tag = audio_file.getTag();


            if (rtr != null) {

                try {
                    tag.deleteArtworkField();
                    tag.createField(rtr);
                    tag.addField(rtr);
                    tag.setField(rtr);
                } catch (Exception e) {
                    e.printStackTrace();


                }

                audio_file.commit();
                Toast.makeText(PlaybackService.sInstance,
                        R.string.artworksaved, Toast.LENGTH_SHORT).show();

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.d("ImagePath", path);

    }

    public File getArtworkfromfile() {


        return filepath;
    }

}

