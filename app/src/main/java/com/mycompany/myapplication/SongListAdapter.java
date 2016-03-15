package com.mycompany.myapplication;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import android.widget.ImageButton;
import android.widget.TextView;
import mehdi.sakout.fancybuttons.FancyButton;
import java.util.List;



/**
 * Created by Jeremy on 2/5/2016.
 */

public class SongListAdapter extends ArrayAdapter<Songs> {
    private List<Songs> items;
    private int layoutResourceId;
    private Context context;

    public SongListAdapter(Context context, int layoutResourceId, List<Songs> items) {
        super(context, layoutResourceId, items);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.items = items;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row;
        SongListHolder holder;

        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        row = inflater.inflate(layoutResourceId, parent, false);

        holder = new SongListHolder();
        holder.Songs = items.get(position);
        holder.removeSongButton = (FancyButton)row.findViewById(R.id.delete_song);
        holder.songCounter = (FancyButton)row.findViewById(R.id.counter);
        holder.removeSongButton.setTag(holder.Songs);

        holder.name = (TextView)row.findViewById(R.id.song_name);
        holder.artist = (TextView)row.findViewById(R.id.artist_name);

        row.setTag(holder);

        setupItem(holder);
        return row;
    }

    private void setupItem(SongListHolder holder) {
        holder.artist.setText(holder.Songs.getArtist());
        holder.name.setText(holder.Songs.getSong());
        holder.songCounter.setText(Integer.toString(holder.Songs.getCount()));
    }

    public static class SongListHolder {
        Songs Songs;
        TextView name;
        TextView artist;
        FancyButton removeSongButton;
        FancyButton songCounter;
    }


}