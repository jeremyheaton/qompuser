package com.mycompany.myapplication.activities;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.mobeta.android.dslv.DragSortListView;
import com.mycompany.myapplication.R;
import com.mycompany.myapplication.SpotifyController;
import com.spotify.sdk.android.player.Error;
import com.spotify.sdk.android.player.Player;

public class PlayListFragment extends Fragment {

    public SpotifyController spotifyController;
    View.OnClickListener resume = v -> ((SpotifyActivity) getActivity()).spotifyController.getPlayer().resume(new Player.OperationCallback() {
        @Override
        public void onSuccess() {

        }

        @Override
        public void onError(Error error) {

        }
    });

    View.OnClickListener skip = v -> ((SpotifyActivity) getActivity()).spotifyController.getPlayer().skipToNext(new Player.OperationCallback() {
        @Override
        public void onSuccess() {

        }

        @Override
        public void onError(Error error) {

        }
    });
    View.OnClickListener stop = v -> ((SpotifyActivity) getActivity()).spotifyController.getPlayer().pause(new Player.OperationCallback() {
        @Override
        public void onSuccess() {

        }

        @Override
        public void onError(Error error) {

        }
    });
    private DragSortListView listView;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View rootView = inflater.inflate(R.layout.play_list_fragment, container, false);
        Button resumeSong = rootView.findViewById(R.id.resumeSong);
        Button skipSong = rootView.findViewById(R.id.skipSong);
        Button pauseSong = rootView.findViewById(R.id.stopSong);

        resumeSong.setOnClickListener(resume);
        skipSong.setOnClickListener(skip);
        pauseSong.setOnClickListener(stop);
        listView = rootView.findViewById(R.id.list);

        return rootView;
    }

    @Override
    public void onDestroy() {
        if (((SpotifyActivity) getActivity()).spotifyController != null) {
            spotifyController.destoryController();
        }
        super.onDestroy();
    }

    public DragSortListView getListView() {
        return listView;
    }
}
