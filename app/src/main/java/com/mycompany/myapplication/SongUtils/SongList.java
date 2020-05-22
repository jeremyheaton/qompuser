package com.mycompany.myapplication.SongUtils;

import com.mycompany.myapplication.models.Song;

import java.util.Collections;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Queue;

/**
 * Created by jerem on 6/4/2017.
 */

public class SongList {

    private Queue<Song> songsList = new LinkedList<>();
    private SongChangeListener songChangeListener;
    private boolean votingOn = true;

    public SongList(SongChangeListener songChangeListener) {
        this.songChangeListener = songChangeListener;
    }

    public void addSong(Song s) {
        if (!voteSongById(s.getId())) {
            songsList.add(s);
            songChangeListener.songAdded();
        }
    }

    public void removeSong() {
        songsList.poll();
        songChangeListener.songRemoved();
    }

    private boolean voteSongById(String id) {
        for (Song song : this.songsList) {
            if (song.getId().equals(id)) {
                song.likeSong();
                return true;
            }
        }
        return false;
    }

    public void sortSongs() {
        Collections.sort((LinkedList<Song>) songsList, (lhs, rhs) -> rhs.getCount() - lhs.getCount());
    }

    public void flipVote() {
        votingOn = !votingOn;
    }

    public LinkedList<Song> getSongs() {
        return (LinkedList<Song>) songsList;
    }

    public int size() {
        return songsList.size();
    }

    public String next() {
        return Objects.requireNonNull(songsList.peek()).getId();
    }
}

