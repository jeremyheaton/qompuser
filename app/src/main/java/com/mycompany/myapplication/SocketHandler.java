package com.mycompany.myapplication;

import android.util.Log;

import com.mycompany.myapplication.models.Song;

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
class SocketHandler {
    Socket mSocket;
    private SpotifyController controller;

    SocketHandler(SpotifyController spotifyController) {
        controller = spotifyController;
    }


    private Emitter.Listener onFetchToken = (Object... objects) -> {
        Log.d("testing", "seeing if fetch is read");
        mSocket.emit("sendtoken", buildAuthResponse());
    };
    private Emitter.Listener onFetchPlayList = (Object... args) -> {
        Log.d("socket", "send da playlist");
        sendPlayList();
    };
    private Emitter.Listener addSong = (Object... args) -> {
        controller.songAdded(args);
    };

    void initialize() throws URISyntaxException {

        IO.Options opts = new IO.Options();
        opts.forceNew = true;
        opts.reconnection = true;
        if (mSocket == null) {
            mSocket = IO.socket("https://ancient-tor-6266.herokuapp.com/", opts);
            //mSocket = IO.socket("https://ancient-tor-6266.herokuapp.com/");
        } else {
            mSocket.close();
            mSocket = IO.socket("https://ancient-tor-6266.herokuapp.com/", opts);
            //mSocket = IO.socket("https://ancient-tor-6266.herokuapp.com/");
        }
        connect();
        subscribe();
    }

    private void connect() {
        mSocket.connect();
    }

    private void subscribe() {
        mSocket.emit("subscribe", controller.getSpotifyActivity().userId);
        mSocket.on("fetchplaylist", onFetchPlayList);
        mSocket.on("message", addSong);
        mSocket.on("fetchtoken", onFetchToken);
    }

    private JSONObject buildAuthResponse() {
        JSONObject object = new JSONObject();
        try {
            object.put("room", controller.getSpotifyActivity().userId);
            object.put("token", controller.getConfig().oauthToken);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return object;
    }

    private JSONObject jsonPlayListBuilder() {
        JSONObject objectHolder = new JSONObject();
        JSONArray json = new JSONArray();
        for (Song song : controller.getSongList().getSongs()) {
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
            objectHolder.put("room", controller.getSpotifyActivity().userId);
            objectHolder.put("songs", json);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return objectHolder;
    }

    void sendPlayList() {
        mSocket.emit("sendplaylist", jsonPlayListBuilder());
    }
}
