package com.mycompany.myapplication;

import android.util.Log;


import com.mycompany.myapplication.activities.SpotifyActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;


/**
 * Created by Jeremy on 2/26/2016.
 */
public class SocketHandler {
    public static Socket mSocket;
    private static SpotifyActivity activity;

    public SocketHandler(SpotifyActivity spotifyActivity) {
        activity = spotifyActivity;
    }

    public void initialize() throws URISyntaxException {

        IO.Options opts = new IO.Options();
        opts.forceNew = true;
        opts.reconnection = true;
        if(mSocket == null){

            mSocket = IO.socket("http://192.168.0.23:8888/", opts);
            //mSocket = IO.socket("https://ancient-tor-6266.herokuapp.com/");
        } else {
            mSocket.close();
            mSocket = IO.socket("http://192.168.0.23:8888/", opts);
            //mSocket = IO.socket("https://ancient-tor-6266.herokuapp.com/");
        }
        connect();
        subscribe();
    }


    public void connect() {
        mSocket.connect();
    }

    public void subscribe() {
        mSocket.emit("subscribe", activity.userId);
        mSocket.on("fetchplaylist", onFetchPlayList);
        mSocket.on("message", onPlaySong);
        mSocket.on(Socket.EVENT_CONNECT_TIMEOUT, new Emitter.Listener() {
            @Override
            public void call(Object... objects) {
                Log.d("socket", "Connect to application");
                try {
                    initialize();
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
            }
        });

        mSocket.on(Socket.EVENT_CONNECT_ERROR, new Emitter.Listener() {
            @Override
            public void call(Object... objects) {
                Log.d("socket", "event connect error");
                try {
                    initialize();
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
            }
        });

        mSocket.on(Socket.EVENT_ERROR, new Emitter.Listener() {
            @Override
            public void call(Object... objects) {
                Log.d("socket", "event error");
                try {
                    initialize();
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
            }
        });

        mSocket.on(Socket.EVENT_RECONNECTING, new Emitter.Listener() {
            @Override
            public void call(Object... objects) {
                Log.d("socket", "event reconnecting");
                try {
                    initialize();
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private Emitter.Listener onPlaySong = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    Log.d("socket", "song added");
                    try {
                        Songs s = new Songs(data.getString("artist"), data.getString("song"), data.getString("message"));
                        activity.sl.addSong(s);
                        mSocket.emit("songAdded", "songAdded");
                        activity.spotifyController.getSongListAdapter().notifyDataSetChanged();
                        //  listView.setAdapter(adapter);
                    } catch (JSONException e) {
                        Log.d("socket", "we hit an error here");
                    }
                    // add the message to view
                }
            });
        }
    };

    public static Emitter.Listener onFetchPlayList = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            Log.d("socket", "send da playlist");
            mSocket.emit("sendplaylist", jsonPlayListBuilder());
        }
    };

    public static JSONObject jsonPlayListBuilder() {
        JSONObject objectHolder = new JSONObject();
        JSONArray json = new JSONArray();
        for (Songs song : activity.sl.songsList) {
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
            objectHolder.put("room", activity.userId);
            objectHolder.put("songs", json);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return objectHolder;
    }
}
