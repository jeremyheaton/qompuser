package com.mycompany.myapplication.activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.mobeta.android.dslv.DragSortListView;
import com.mycompany.myapplication.SpotifyController;
import com.mycompany.myapplication.helpers.Helpers;
import com.mycompany.myapplication.R;
import com.mycompany.myapplication.SocketHandler;
import com.mycompany.myapplication.SongList;
import com.mycompany.myapplication.SongListener;
import com.spotify.sdk.android.player.Config;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.spotify.sdk.android.player.*;

import org.json.*;
import com.loopj.android.http.*;
import com.spotify.sdk.android.player.Error;

import cz.msebera.android.httpclient.Header;

public class SpotifyActivity extends Activity implements SongListener, ConnectionStateCallback {
    DragSortListView listView;
    private static final String CLIENT_ID = "05a31b738d734867855136eeedd18d0a";
    private static final String REDIRECT_URI = "my-first-android-app-login://callback";
    public static Config playerConfig;
    private static final int REQUEST_CODE = 1337;
    CountDownTimer songTimer;
    public SongList sl = new SongList();
    public String userId ="";
    public static SocketHandler sockethandler;
    public SpotifyController spotifyController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spotify);
        AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(CLIENT_ID,
                AuthenticationResponse.Type.TOKEN,
                REDIRECT_URI);
        builder.setScopes(new String[]{"user-read-private", "streaming"});

        AuthenticationRequest request = builder.build();
        AuthenticationClient.openLoginActivity(this, REQUEST_CODE, request);

        Button resumeSong = (Button) findViewById(R.id.resumeSong);
        Button skipSong = (Button) findViewById(R.id.skipSong);
        Button pauseSong = (Button) findViewById(R.id.stopSong);
        resumeSong.setOnClickListener(resume);
        skipSong.setOnClickListener(skip);
        pauseSong.setOnClickListener(stop);
        listView = (DragSortListView) findViewById(R.id.list);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if(handleAuth(requestCode,this,resultCode,intent)){
            spotifyController = new SpotifyController(this, listView, playerConfig);
            String url = "https://ancient-tor-6266.herokuapp.com/client/" + userId;
            Bitmap urlQR = Helpers.encodeToQrCode(url, 512, 512);
            ImageView imageView1 = (ImageView) findViewById(R.id.imageView1);
            imageView1.setImageBitmap(urlQR);
            TextView t = (TextView) findViewById(R.id.urlHolder);
            t.setText(url);
        }
    }

    @Override
    public void onLoggedIn() {
        Log.d("SpotifyActivity", "User logged in");
    }

    @Override
    public void onLoggedOut() {
        Log.d("SpotifyActivity", "User logged out");
    }


    @Override
    public void onLoginFailed(Error error) {
        TextView fail = (TextView) findViewById(R.id.errorMessage);
        fail.setText("You must have a Spotify Premium Account to use the Musiq app");
        Log.d("SpotifyActivity", "Login failed");
    }

    @Override
    public void onTemporaryError() {
        Log.d("SpotifyActivity", "Temporary error occurred");
    }

    @Override
    public void onConnectionMessage(String message) {
        Log.d("SpotifyActivity", "Received connection message: " + message);
    }

    @Override
    protected void onDestroy() {
        if (songTimer != null) {
            songTimer.cancel();
        }
        Spotify.destroyPlayer(this);
        spotifyController.getSocketHandler().mSocket.off();
        spotifyController.getSocketHandler().mSocket.disconnect();
        super.onDestroy();
    }

    View.OnClickListener resume = new View.OnClickListener() {
        public void onClick(View v) {
            spotifyController.getPlayer().resume(new Player.OperationCallback() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onError(Error error) {

                }
            });
        }
    };
    View.OnClickListener skip = new View.OnClickListener() {
        public void onClick(View v) {
            spotifyController.getPlayer().skipToNext(new Player.OperationCallback() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onError(Error error) {

                }
            });
        }
    };
    View.OnClickListener stop = new View.OnClickListener() {
        public void onClick(View v) {
            spotifyController.getPlayer().pause(new Player.OperationCallback() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onError(Error error) {

                }
            });
        }
    };

    public void removeSongOnClickHandler(View v) {
        View parentRow = (View) v.getParent().getParent();
        DragSortListView listView2 = (DragSortListView) parentRow.getParent();
        int position = listView2.getPositionForView(parentRow);
        if (position == 0) {
            spotifyController.getPlayer().skipToNext(new Player.OperationCallback() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onError(Error error) {

                }
            });
        } else {
            spotifyController.getSongList().songsList.remove(position);
        }
        spotifyController.getSongListAdapter().notifyDataSetChanged();
    }

    @Override
    public void songAdded() {
        if (spotifyController.getSongList().songsList.size() == 1) {
            spotifyController.playNext();
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
        Log.d("socket", "resume");
    }

    @Override
    protected void onPause(){
        super.onPause();
        Log.d("socket", "pause");
    }

    public boolean handleAuth(int requestCode, SpotifyActivity spotifyActivity, int resultCode, Intent intent) {
        if (requestCode == REQUEST_CODE) {
            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, intent);
            if (response.getType() == AuthenticationResponse.Type.TOKEN) {
                playerConfig = new Config(spotifyActivity, response.getAccessToken(), CLIENT_ID);
                AsyncHttpClient client = new AsyncHttpClient();
                client.addHeader("Authorization", "Bearer " + response.getAccessToken());
                final String url = "https://api.spotify.com/v1/me";
                client.get(url, null, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        try {
                            userId = response.getString("id");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        Log.d("SpotifyActivity", "lkjlkj");
                    }
                });
                if(!userId.equals("")){
                    return true;
                }
            }
        }
        return false;
    }
}