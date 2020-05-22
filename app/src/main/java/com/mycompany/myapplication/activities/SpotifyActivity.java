package com.mycompany.myapplication.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.mobeta.android.dslv.DragSortListView;
import com.mycompany.myapplication.R;
import com.mycompany.myapplication.SpotifyController;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.spotify.sdk.android.player.Config;
import com.spotify.sdk.android.player.ConnectionStateCallback;
import com.spotify.sdk.android.player.Error;
import com.spotify.sdk.android.player.Player;

import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class SpotifyActivity extends AppCompatActivity implements ConnectionStateCallback {
    private static final String CLIENT_ID = "05a31b738d734867855136eeedd18d0a";
    private static final String REDIRECT_URI = "my-first-android-app-login://callback";
    private static final int REQUEST_CODE = 1337;
    public String userId = "";
    public SpotifyController spotifyController;
    private CountDownTimer songTimer;
    private OptionsFragment optionsFragment = new OptionsFragment();
    private PlayListFragment playListFragment = new PlayListFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spotify);
        SectionsPageAdapter mSectionsPageAdapter = new SectionsPageAdapter(getSupportFragmentManager());
        mSectionsPageAdapter.addFragment(playListFragment, "PlayList");
        mSectionsPageAdapter.addFragment(optionsFragment, "Options");
        ViewPager mViewPager = findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPageAdapter);
        AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(CLIENT_ID,
                AuthenticationResponse.Type.TOKEN,
                REDIRECT_URI);
        builder.setScopes(new String[]{"user-read-private", "streaming"});
        AuthenticationRequest request = builder.build();
        AuthenticationClient.openLoginActivity(this, REQUEST_CODE, request);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        handleAuth(requestCode, this, resultCode, intent);
    }

    @Override
    public void onLoggedIn() {
        Toast.makeText(this, "Logged In", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onLoggedOut() {
        Log.d("SpotifyActivity", "User logged out");
    }

    public void fetchUserDetails(final String accessToken) {
        AsyncHttpClient client = new AsyncHttpClient();
        client.addHeader("Authorization", "Bearer " + accessToken);
        String url = "https://api.spotify.com/v1/me";
        client.get(url, null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    userId = response.getString("id");
                    optionsFragment.createQR(userId);
                    spotifyController = new SpotifyController(SpotifyActivity.this, playListFragment.getListView(), new Config(SpotifyActivity.this, accessToken, CLIENT_ID));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onLoginFailed(Error error) {
        TextView fail = (TextView) findViewById(R.id.errorMessage);
        fail.setText(R.string.logonFailure);
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

        if (spotifyController != null) {
            spotifyController.destoryController();
        }
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("socket", "resume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("socket", "pause");
    }

    public void handleAuth(int requestCode, SpotifyActivity spotifyActivity, int resultCode, Intent intent) {
        if (requestCode == REQUEST_CODE) {
            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, intent);
            if (response.getType() == AuthenticationResponse.Type.TOKEN) {
                fetchUserDetails(response.getAccessToken());
            }
        }
    }

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
            spotifyController.getSongList().getSongs().remove(position);
        }
        spotifyController.getSongListAdapter().notifyDataSetChanged();
    }
}