package com.mycompany.myapplication.SongUtils;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.AppCompatImageView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.mycompany.myapplication.R;
import com.mycompany.myapplication.models.Song;

import java.util.List;

import mehdi.sakout.fancybuttons.FancyButton;


/**
 * Created by Jeremy on 2/5/2016.
 */

public class SongListAdapter extends ArrayAdapter<Song> {
    private List<Song> items;
    private int layoutResourceId;
    private Context context;

    public SongListAdapter(Context context, int layoutResourceId, SongList songList) {
        super(context, layoutResourceId, songList.getSongs());
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.items = songList.getSongs();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row;
        SongListHolder holder;

        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        row = inflater.inflate(layoutResourceId, parent, false);

        holder = new SongListHolder();
        holder.setSong(items.get(position));
        holder.setRemoveSongButton(row.findViewById(R.id.delete_song));
        holder.setSongCounter(row.findViewById(R.id.counter));
        holder.removeSongButton.setTag(holder.song);
        holder.setName(row.findViewById(R.id.song_name));
        holder.setArtist(row.findViewById(R.id.artist_name));
        setupItem(holder);
        row.setTag(holder);
        return row;
    }

    private void setupItem(SongListHolder holder) {
        holder.artist.setText(holder.song.getArtist());
        holder.name.setText(holder.song.getSong());
        holder.songCounter.setText(Integer.toString(holder.song.getCount()));
    }

    private static class SongListHolder {
        Song song;
        TextView name;
        TextView artist;
        FancyButton removeSongButton;
        FancyButton songCounter;

        public void setSong(Song song) {
            this.song = song;
        }

        public void setName(TextView name) {
            this.name = name;
        }

        public void setArtist(TextView artist) {
            this.artist = artist;
        }

        public void setRemoveSongButton(FancyButton removeSongButton) {
            this.removeSongButton = removeSongButton;
        }

        public void setSongCounter(FancyButton songCounter) {
            this.songCounter = songCounter;
        }
    }


}