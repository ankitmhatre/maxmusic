package com.dragonide.maxmusic;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Ankit on 5/5/2017.
 */

public class AlbumArtAdapter extends RecyclerView.Adapter<AlbumArtAdapter.TheHolder> {
    int i = 0;
    private static LayoutInflater inflater = null;
    String reso;
    Context context;
    private List<AlbumArtItem> albumArtItems;

    Bitmap bm;

    public AlbumArtAdapter(List<AlbumArtItem> albumArtItems) {
        this.albumArtItems = albumArtItems;
    }
    @Override
    public TheHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.albumart_item, parent, false);

        return new TheHolder(itemView);
    }

    @Override
    public void onBindViewHolder(TheHolder holder, int position) {
        AlbumArtItem dist_item = albumArtItems.get(position);
        holder.img.setImageBitmap(dist_item.getBm());
        holder.tv.setText(dist_item.getReso());

    }


    public AlbumArtAdapter(Context mainActivity, String reso, Bitmap bm) {
        i++;
        // TODO Auto-generated constructor stub
        this.reso = reso;
        context = mainActivity;
        this.bm = bm;
        inflater = (LayoutInflater) context.
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }
    @Override
    public int getItemCount() {
        return albumArtItems.size();
    }

    public class TheHolder extends RecyclerView.ViewHolder {
        TextView tv;
        ImageView img;


        public TheHolder(View view) {
            super(view);
            img = (ImageView) view.findViewById(R.id.grid_image_view);
            tv = (TextView) view.findViewById(R.id.grid_text_view);
        }
    }
}
