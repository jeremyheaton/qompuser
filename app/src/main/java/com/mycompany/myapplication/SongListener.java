package com.mycompany.myapplication;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Jeremy on 1/13/2016.
 */


interface SongListener {
    void songAdded();
}

// Someone who says "Hello"
class SongList {
    public List<Songs> songsList = new ArrayList<Songs>();
    public List<SongListener> songListen = new ArrayList<SongListener>();
    public boolean votingOn = true;

    public void addListener(SongListener sngl) {
        songListen.add(sngl);
    }

    public void addSong(Songs s) {
        if (!voteSongById(s.getId())){
            songsList.add(s);
            for (SongListener sl : songListen) {
                sl.songAdded();
            }
        }
        SpotifyActivity.sockethandler.mSocket.emit("sendplaylist", SocketHandler.jsonPlayListBuilder());

    }

    public boolean voteSongById(String id) {
        for (Songs song : this.songsList) {
                if(song.getId().equals(id)){
                    song.likeSong();
                    return true;
                }
        }
        return false;
    };

    public void sortSongs(){
        Collections.sort(songsList, new Comparator<Songs>() {
            @Override
            public int compare(Songs lhs, Songs rhs) {
                return  rhs.getCount() -  lhs.getCount();
            }
        });
    }

    public void flipVote(){
       votingOn = votingOn ? false : true;
    }
    public void removeSong() {
        songListen.remove(0);
    }


}

