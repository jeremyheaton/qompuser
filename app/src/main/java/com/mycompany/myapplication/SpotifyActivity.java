package com.mycompany.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.mobeta.android.dslv.DragSortListView;
import com.spotify.sdk.android.player.Config;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.spotify.sdk.android.player.PlayerStateCallback;
import com.spotify.sdk.android.player.Spotify;
import com.spotify.sdk.android.player.ConnectionStateCallback;
import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.PlayerNotificationCallback;
import com.spotify.sdk.android.player.PlayerState;
import org.json.*;
import com.loopj.android.http.*;
import cz.msebera.android.httpclient.Header;

public class SpotifyActivity extends Activity implements SongListener, PlayerNotificationCallback, ConnectionStateCallback {
    DragSortListView listView;
    private static final String CLIENT_ID = "05a31b738d734867855136eeedd18d0a";
    private static final String REDIRECT_URI = "my-first-android-app-login://callback";
    public static Player mPlayer;
    private static final int REQUEST_CODE = 1337;
    public static Config playerConfig;
    public static SongListAdapter adapter;
    CountDownTimer songTimer;
    public  SongList sl = new SongList();
    public  String userId;
    public static SocketHandler sockethandler;

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
        if (requestCode == REQUEST_CODE) {
            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, intent);
            if (response.getType() == AuthenticationResponse.Type.TOKEN) {
                playerConfig = new Config(this, response.getAccessToken(), CLIENT_ID);
                AsyncHttpClient client = new AsyncHttpClient();
                client.addHeader("Authorization", "Bearer " + response.getAccessToken());
                final String url = "https://api.spotify.com/v1/me";
                client.get(url, null, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        try {
                            userId = response.getString("id");
                            sockethandler = new SocketHandler(SpotifyActivity.this);
                            sockethandler.initialize();
                            String url = "https://ancient-tor-6266.herokuapp.com/client/" + userId;
                            Bitmap urlQR = encodeToQrCode(url, 512, 512);
                            ImageView imageView1 = (ImageView) findViewById(R.id.imageView1);
                            imageView1.setImageBitmap(urlQR);
                            TextView t = (TextView) findViewById(R.id.urlHolder);
                            t.setText(url);
                            setPlayer();
                            sl.addListener(SpotifyActivity.this);
                            adapter = new SongListAdapter(SpotifyActivity.this, R.layout.song_list, sl.songsList) {
                                @Override
                                public View getView(int position, View convertView, ViewGroup parent) {
                                    View view = super.getView(position, convertView, parent);
                                    Songs song = getItem(position);
                                    ((TextView) view.findViewById(R.id.song_name)).setText(song.getSong());
                                    // ... Fill in other views ...
                                    return view;
                                }
                            };
                            listView.setAdapter(adapter);
                            listView.setDropListener(new DragSortListView.DropListener() {
                                @Override
                                public void drop(int from, int to) {
                                    Songs movedItem = sl.songsList.get(from);
                                    sl.songsList.remove(from);
                                    if (from > to) --from;
                                    sl.songsList.add(to, movedItem);
                                    adapter.notifyDataSetChanged();
                                    if (from == 0 || to == 0 && from != to) {
                                        playTrack(sl.songsList.get(0).getId());
                                    }
                                }
                            });
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        Log.d("SpotifyActivity", "lkjlkj");
                    }
                });
            }
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
    public void onLoginFailed(Throwable error) {
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
    public void onPlaybackEvent(final EventType eventType, final PlayerState playerState) {
        if (eventType == EventType.TRACK_CHANGED || playerState.playing) {
            final long timer = playerState.durationInMs;
            songTimer = new CountDownTimer(timer, 1000) {
                public void onTick(long millisUntilFinished) {
                    if (mPlayer.isInitialized()) {
                        mPlayer.getPlayerState(new PlayerStateCallback() {
                            @Override
                            public void onPlayerState(PlayerState playerState) {
                                TextView t = (TextView) findViewById(R.id.timer);
                                String timer = String.format("%02d:%02d", ((playerState.positionInMs / 1000) / 60), (playerState.positionInMs / 1000) % 60) + "/" +
                                        String.format("%02d:%02d", ((playerState.durationInMs / 1000) / 60), (playerState.durationInMs / 1000) % 60);
                                t.setText(timer);
                            }
                        });
                    }
                }

                public void onFinish() {
                }
            }.start();
        }
        if (eventType == EventType.TRACK_CHANGED) {
            if (sl.songsList.size() == 1 && !playerState.playing) {
                sl.songsList.remove(0);
                playNext();
            } else if (!playerState.playing) {
                playNext();
            }
            adapter.notifyDataSetChanged();
            //  listView.setAdapter(adapter);
        }

        Log.d("SpotifyActivity", "Playback event received: " + eventType.name());
    }

    @Override
    public void onPlaybackError(ErrorType errorType, String errorDetails) {
        Log.d("SpotifyActivity", "Playback error received: " + errorType.name());
    }

    private void setPlayer() {
        Spotify.getPlayer(playerConfig, this, new Player.InitializationObserver() {
            @Override
            public void onInitialized(Player player) {
                mPlayer = player;
                mPlayer.addConnectionStateCallback(SpotifyActivity.this);
                mPlayer.addPlayerNotificationCallback(SpotifyActivity.this);
            }

            @Override
            public void onError(Throwable throwable) {
                Log.e("SpotifyActivity", "Could not initialize player: " + throwable.getMessage());
            }
        });
    }

    private void playTrack(String track) {
        sl.sortSongs();
        if (!mPlayer.isInitialized()) {
            setPlayer();
            playTrack(track);
        } else if (mPlayer.isInitialized()) {
            mPlayer.play("spotify:track:" + track);
            adapter.notifyDataSetChanged();

        }
    }

    @Override
    protected void onDestroy() {
        if (songTimer != null) {
            songTimer.cancel();
        }
        Spotify.destroyPlayer(this);
        sockethandler.mSocket.off();
        sockethandler.mSocket.disconnect();
        super.onDestroy();
    }

    public static Bitmap encodeToQrCode(String text, int width, int height) {
        QRCodeWriter writer = new QRCodeWriter();
        BitMatrix matrix = null;
        try {
            matrix = writer.encode(text, BarcodeFormat.QR_CODE, 512, 512);
        } catch (WriterException ex) {
            ex.printStackTrace();
        }
        Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                bmp.setPixel(x, y, matrix.get(x, y) ? Color.BLACK : Color.WHITE);
            }
        }
        return bmp;
    }

    View.OnClickListener resume = new View.OnClickListener() {
        public void onClick(View v) {
            mPlayer.resume();
        }
    };
    View.OnClickListener skip = new View.OnClickListener() {
        public void onClick(View v) {
            mPlayer.skipToNext();
        }
    };
    View.OnClickListener stop = new View.OnClickListener() {
        public void onClick(View v) {
            mPlayer.pause();
        }
    };

    public void playNext() {
        if (sl.songsList.size() > 1) {
            sl.songsList.remove(0);
            adapter.notifyDataSetChanged();
            //  listView.setAdapter(adapter);
        }
        if (sl.songsList.size() > 0) {
            playTrack(sl.songsList.get(0).getId());
        }
    }

    public void voteController() {
        sl.flipVote();
    }

    public void removeSongOnClickHandler(View v) {
        View parentRow = (View) v.getParent().getParent();
        DragSortListView listView2 = (DragSortListView) parentRow.getParent();
        int position = listView2.getPositionForView(parentRow);
        if (position == 0) {
            mPlayer.skipToNext();
        } else {
            sl.songsList.remove(position);
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public void songAdded() {
        if (sl.songsList.size() == 1) {
            playNext();
        }
    }


}