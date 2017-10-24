package com.mycompany.myapplication;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mobeta.android.dslv.DragSortListView;
import com.mycompany.myapplication.activities.SpotifyActivity;
import com.spotify.sdk.android.player.Config;
import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.Spotify;
import com.spotify.sdk.android.player.SpotifyPlayer;

import java.net.URISyntaxException;

/**
 * Created by jerem on 10/23/2017.
 */

public class SpotifyController {

    final SongList songList;
    final SpotifyActivity spotifyActivity;
    final SongListAdapter songListAdapter;
    final DragSortListView dragSortListView;
    final SocketHandler socketHandler;
    private Config playerConfig;
    private Player player;

    public SpotifyController(final SpotifyActivity spotifyActivity, DragSortListView dragSortListView, Config config) {
        this.playerConfig = config;
        this.songList = new SongList();;
        this.spotifyActivity = spotifyActivity;
        this.dragSortListView = dragSortListView;
        this.songListAdapter = new SongListAdapter(this.spotifyActivity, R.layout.song_list, songList.songsList) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                Songs song = getItem(position);
                ((TextView) view.findViewById(R.id.song_name)).setText(song.getSong());
                return view;
            }
        };
        this.socketHandler = new SocketHandler(this.spotifyActivity);
        try {
            this.socketHandler.initialize();
        } catch (Exception e) {
            e.printStackTrace();
        }
        setPlayer();

        this.intialize();
    }

    private void intialize() {
        songList.addListener(spotifyActivity);
        dragSortListView.setAdapter(songListAdapter);
        final SongListAdapter finalSongListAdapter = songListAdapter;

        dragSortListView.setDropListener(new DragSortListView.DropListener() {
            @Override
            public void drop(int from, int to) {
                Songs movedItem = songList.songsList.get(from);
                songList.songsList.remove(from);
                if (from > to) --from;
                songList.songsList.add(to, movedItem);
                finalSongListAdapter.notifyDataSetChanged();
                if (from == 0 || to == 0 && from != to) {
                    SpotifyController.this.playTrack(songList.songsList.get(0).getId());
                }
            }
        });
    }

    public void  playTrack(String track) {
        songList.sortSongs();
        player.playUri(null, "spotify:track:" + track, 0, 0);
        socketHandler.onFetchPlayList.call();
        songListAdapter.notifyDataSetChanged();
    }

    public void playNext() {
        if (songList.songsList.size() > 1) {
            songList.songsList.remove(0);
            songListAdapter.notifyDataSetChanged();
        }
        if (songList.songsList.size() > 0) {
            playTrack(songList.songsList.get(0).getId());
        }
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

    private void setPlayer() {
        Spotify.getPlayer(playerConfig, this, new SpotifyPlayer.InitializationObserver() {
            @Override
            public void onInitialized(SpotifyPlayer player) {
                SpotifyController.this.player = player;
                SpotifyController.this.player.addConnectionStateCallback(spotifyActivity);
                SpotifyController.this.player.addNotificationCallback(spotifyActivity);
            }

            @Override
            public void onError(Throwable throwable) {
                Log.e("SpotifyActivity", "Could not initialize player: " + throwable.getMessage());
            }
        });
    }
}
