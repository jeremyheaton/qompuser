package com.mycompany.myapplication;

import android.util.Log;
import android.view.View;

import com.mobeta.android.dslv.DragSortListView;
import com.mycompany.myapplication.SongUtils.SongChangeListener;
import com.mycompany.myapplication.SongUtils.SongList;
import com.mycompany.myapplication.SongUtils.SongListAdapter;
import com.mycompany.myapplication.activities.SpotifyActivity;
import com.mycompany.myapplication.models.Song;
import com.spotify.sdk.android.player.Config;
import com.spotify.sdk.android.player.Error;
import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.PlayerEvent;
import com.spotify.sdk.android.player.Spotify;
import com.spotify.sdk.android.player.SpotifyPlayer;

/**
 * Created by jerem on 10/23/2017.
 */

public class SpotifyController implements SongChangeListener, SpotifyPlayer.NotificationCallback {

    private final SongList songList;
    private final SpotifyActivity spotifyActivity;
    private final SongListAdapter songListAdapter;
    private final DragSortListView dragSortListView;
    private final SocketHandler socketHandler;
    private Config playerConfig;
    private Player player;

    public SpotifyController(final SpotifyActivity spotifyActivity, DragSortListView dragSortListView, Config config) {
        this.playerConfig = config;
        this.songList = new SongList(this);
        this.spotifyActivity = spotifyActivity;
        this.dragSortListView = dragSortListView;
        this.songListAdapter = new SongListAdapter(this.spotifyActivity, R.layout.song_list, songList);
        this.socketHandler = new SocketHandler(this);
        try {
            this.socketHandler.initialize();
        } catch (Exception e) {
            e.printStackTrace();
        }
        setPlayer();
        this.intialize();
    }

    private void intialize() {
        dragSortListView.setAdapter(songListAdapter);
        dragSortListView.setDropListener((from, to) -> {
            Song movedItem = songList.getSongs().get(from);
            songList.getSongs().remove(from);
            if (from > to) --from;
            songList.getSongs().add(to, movedItem);
            songListAdapter.notifyDataSetChanged();
            if (from == 0 || to == 0) {
                SpotifyController.this.playTrack(songList.getSongs().get(0).getId());
            }
        });
    }

    private void playTrack(String track) {
        songList.sortSongs();
        player.playUri(null, "spotify:track:" + track, 0, 0);
        socketHandler.sendPlayList();
        songListAdapter.notifyDataSetChanged();
    }

    public void playNext() {
        if (songList.size() > 0) {
            playTrack(songList.next());
        }
    }

    public void songFinished() {
        songList.removeSong();
        playNext();
    }

    public SongList getSongList() {
        return songList;
    }

    public SongListAdapter getSongListAdapter() {
        return songListAdapter;
    }

    public DragSortListView getDragSortListView() {
        return dragSortListView;
    }

    public Player getPlayer() {
        return player;
    }

    public SocketHandler getSocketHandler() {
        return socketHandler;
    }

    SpotifyActivity getSpotifyActivity() {
        return spotifyActivity;
    }

    private void setPlayer() {
        Spotify.getPlayer(playerConfig, this, new SpotifyPlayer.InitializationObserver() {
            @Override
            public void onInitialized(SpotifyPlayer player) {
                SpotifyController.this.player = player;
                SpotifyController.this.player.addConnectionStateCallback(SpotifyController.this.spotifyActivity);
                SpotifyController.this.player.addNotificationCallback(SpotifyController.this);
            }

            @Override
            public void onError(Throwable throwable) {
                Log.e("SpotifyActivity", "Could not initialize player: " + throwable.getMessage());
            }
        });
    }

    @Override
    public void onPlaybackEvent(PlayerEvent playerEvent) {
        if (playerEvent.equals(PlayerEvent.kSpPlaybackNotifyAudioDeliveryDone)) {
            songFinished();
        }

        if (playerEvent == PlayerEvent.kSpPlaybackNotifyTrackChanged) {
            Log.d("player", player.getMetadata().toString());
            Log.d("player", player.getMetadata().currentTrack.toString());
            final long timer = getPlayer().getMetadata().currentTrack.durationMs;
//            songTimer = new CountDownTimer(timer, 1000) {
//                public void onTick(long millisUntilFinished) {
//                    TextView t = (TextView) findViewById(R.id.timer);
//                    @SuppressLint("DefaultLocale") String timer = String.format("%02d:%02d", ((getPlayer().getPlaybackState().positionMs / 1000) / 60),
//                            (getPlayer().getPlaybackState().positionMs / 1000) % 60) + "/" +
//                            String.format("%02d:%02d", ((getPlayer().getPlaybackState().positionMs / 1000) / 60),
//                                    (getPlayer().getPlaybackState().positionMs / 1000) % 60);
//                    t.setText(timer);
//                }
//                public void onFinish() {}
//            }.start();
        }

        Log.d("SpotifyActivity", "Playback event received: " + playerEvent.name());
    }

    //        controller.getSocketHandler().mSocket.emit("sendplaylist", SocketHandler.jsonPlayListBuilder());
    @Override
    public void onPlaybackError(Error error) {

    }

    @Override
    public void songAdded() {
        if (songList.size() == 1) {
            playNext();
        }
    }

    @Override
    public void songRemoved() {
        songListAdapter.notifyDataSetChanged();
    }

    public void removeSongOnClickHandler(View v) {
        View parentRow = (View) v.getParent().getParent();
        DragSortListView listView2 = (DragSortListView) parentRow.getParent();
        int position = listView2.getPositionForView(parentRow);
        if (position == 0) {
            player.skipToNext(new Player.OperationCallback() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onError(Error error) {

                }
            });
        } else {
            songList.getSongs().remove(position);
        }
        songListAdapter.notifyDataSetChanged();
    }

    public Config getConfig() {
        return playerConfig;
    }

    public void destoryController() {
        Spotify.destroyPlayer(this);
        if (socketHandler != null) {
            socketHandler.mSocket.off();
            socketHandler.mSocket.disconnect();
        }
    }

}