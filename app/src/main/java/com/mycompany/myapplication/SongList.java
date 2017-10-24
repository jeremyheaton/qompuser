package com.mycompany.myapplication;

import com.mycompany.myapplication.activities.SpotifyActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by jerem on 6/4/2017.
 */

public class SongList {

    public List<Songs> songsList = new ArrayList<>();
    public SongListener songListen;
    public boolean votingOn = true;

    public void addListener(SongListener sngl) {
        songListen = sngl;
    }

    public void addSong(Songs s) {
        if (!voteSongById(s.getId())) {
            songsList.add(s);
            songListen.songAdded();
        }
        SpotifyActivity.sockethandler.mSocket.emit("sendplaylist", SocketHandler.jsonPlayListBuilder());
    }

    public void popSong() {
        songsList.remove(0);
    }

    public boolean voteSongById(String id) {
        for (Songs song : this.songsList) {
            if (song.getId().equals(id)) {
                song.likeSong();
                return true;
            }
        }
        return false;
    }

    ;

    public void sortSongs() {
        Collections.sort(songsList, new Comparator<Songs>() {
            @Override
            public int compare(Songs lhs, Songs rhs) {
                return rhs.getCount() - lhs.getCount();
            }
        });
    }

    public void flipVote() {
        votingOn = votingOn ? false : true;
    }
}

