package com.dragonide.maxmusic;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Ankit on 5/26/2017.
 */

public class MusicAdapter extends RecyclerView.Adapter<MusicAdapter.MyViewHolder> {

    private List<SongModel> moviesList;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView title, artist, album, timestamp;

        public MyViewHolder(View view) {
            super(view);
            title = (TextView) view.findViewById(R.id.title);
            artist = (TextView) view.findViewById(R.id.arttist);
            album = (TextView) view.findViewById(R.id.album);
            timestamp = (TextView) view.findViewById(R.id.timestamp);
        }
    }
    public MusicAdapter(List<SongModel> moviesList) {
        this.moviesList = moviesList;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        SongModel movie = moviesList.get(position);
        holder.title.setText(movie.getTitle());
        holder.artist.setText(movie.getArtist());
        holder.album.setText(movie.getAlbum());
        holder.timestamp.setText(movie.getTime()+ "");
    }

    @Override
    public int getItemCount() {
        return moviesList.size();
    }
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycler_item, parent, false);

        return new MyViewHolder(itemView);
    }
}
