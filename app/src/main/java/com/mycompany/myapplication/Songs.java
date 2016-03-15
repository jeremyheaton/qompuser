package com.mycompany.myapplication;

/**
 * Created by Jeremy on 1/13/2016.
 */
public class Songs {
    String artist;
    String song;
    String id;

    int count = 0;

    public Songs(String artistName, String songInfo, String playId){
        this.artist = artistName;
        this.song = songInfo;
        this.id=playId;
    }


    public void likeSong(){
            this.count++;
    }
    public int getCount(){return  this.count;}
    public String getArtist(){
        return this.artist;
    }
    public String getSong(){
        return this.song;
    }
    public String getId(){
        return this.id;
    }
}
