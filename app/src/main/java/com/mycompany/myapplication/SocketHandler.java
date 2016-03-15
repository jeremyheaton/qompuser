package com.mycompany.myapplication;

import android.content.Context;
import android.os.Handler;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

/**
 * Created by Jeremy on 2/26/2016.
 */
public class SocketHandler {
    public static Socket mSocket;
    private static SpotifyActivity activity;
    public SocketHandler(SpotifyActivity spotifyActivity){
    activity = spotifyActivity;
    }

    public  void  initialize(){
        {
            try {
                mSocket = IO.socket("https://ancient-tor-6266.herokuapp.com/");
            } catch (URISyntaxException e) {
            }
        }
        connect();
        subscribe();
    }
    public  void connect(){
        mSocket.connect();
    }
    public  void subscribe(){
        mSocket.emit("subscribe", activity.userId);
        mSocket.on("fetchplaylist", onFetchPlayList);
        mSocket.on("message", onPlaySong);
    }






    private  Emitter.Listener onPlaySong = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    try {
                        Songs s = new Songs(data.getString("artist"), data.getString("song"), data.getString("message"));
                        activity.sl.addSong(s);
                        SpotifyActivity.adapter.notifyDataSetChanged();
                        //  listView.setAdapter(adapter);
                    } catch (JSONException e) {
                    }
                    // add the message to view
                }
            });
        }
    };

    private static Emitter.Listener onFetchPlayList = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            mSocket.emit("sendplaylist", jsonPlayListBuilder());
        }
    };

    public static JSONObject jsonPlayListBuilder(){
        JSONObject objectHolder = new JSONObject();
        JSONArray json = new JSONArray();
        for (Songs song: activity.sl.songsList) {
            JSONObject object = new JSONObject();
            try {
                object.put("songname", song.getSong());
                object.put("artistname", song.getArtist());
                object.put("id", song.getId());
                object.put("count", song.getCount());
                json.put(object);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        try {
            objectHolder.put("room",activity.userId);
            objectHolder.put("songs", json);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return objectHolder;
    }

}
