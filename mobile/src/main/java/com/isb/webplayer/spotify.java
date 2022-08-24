package com.isb.webplayer;

import android.app.Activity;
import android.util.Log;

import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.spotify.protocol.client.Subscription;
import com.spotify.protocol.types.PlayerContext;
import com.spotify.protocol.types.PlayerState;
import com.spotify.protocol.types.Track;

import java.util.Locale;

public class spotify {

    private static final String CLIENT_ID = "03a8bcaa4e2e4cf183c5ca836f029ab6";
    private static final String REDIRECT_URI = "http://signage.isb.co.nz/sm_members/spotify/";

    private static final String TRACK_URI = "spotify:track:1UBQ5GK8JaQjm5VbkBZY66";
    private static final String ALBUM_URI = "spotify:album:1x0uzT3ETlIYjPueTyNfnQ";
    private static final String ARTIST_URI = "spotify:artist:3WrFJ7ztbogyGnTHbHJFl2";
    private static final String PLAYLIST_URI = "spotify:user:spotify:playlist:37i9dQZF1DWYLUQ5WYaArq";

    private SpotifyAppRemote mSpotifyAppRemote;
    private String URI;

    void start(Activity m_activity)
    {
                ConnectionParams connectionParams =
                new ConnectionParams.Builder(CLIENT_ID)
                        .setRedirectUri(REDIRECT_URI)
                        .showAuthView(true)
                        .build();

        SpotifyAppRemote.connect(m_activity, connectionParams,
                new Connector.ConnectionListener() {

                    public void onConnected(SpotifyAppRemote spotifyAppRemote) {
                        mSpotifyAppRemote = spotifyAppRemote;
                        Log.d("MainActivity", "Connected! Yay!");

                        // Now you can start interacting with App Remote
                        connected();

                    }

                    public void onFailure(Throwable throwable) {
                        Log.e("MainActivity", throwable.getMessage(), throwable);

                        // Something went wrong when attempting to connect! Handle errors here
                    }
                });
    }

    void Stop()
    {
        SpotifyAppRemote.disconnect(mSpotifyAppRemote);
    }

    void Resume()
    {
       if(mSpotifyAppRemote!=null) mSpotifyAppRemote.getPlayerApi().resume();
    }

    String GetUri()
    {
      if(URI!=null) {
          String[] values = URI.split(":");
          int index = values.length - 1;

          return values[index];
      }

        return null;
    }

    void SetURI(String uri)
    {
        if(mSpotifyAppRemote!=null) mSpotifyAppRemote.getPlayerApi().play(uri);
        URI=uri;
    }

    private void connected() {
        // Play a playlist
     //

        // Subscribe to PlayerState
        mSpotifyAppRemote.getPlayerApi()
                .subscribeToPlayerState()
                .setEventCallback(mPlayerStateEventCallback);

        mSpotifyAppRemote.getPlayerApi()
                .subscribeToPlayerContext()
                .setEventCallback(mPlayerContextEventCallback);


    }

    

    private final Subscription.EventCallback<PlayerState> mPlayerStateEventCallback = new Subscription.EventCallback<PlayerState>() {
        @Override
        public void onEvent(PlayerState playerState) {
            final Track track = playerState.track;

            if (track != null) {
                Log.d("MainActivity", track.name + " by " + track.artist.name +" paused " +playerState.isPaused);
            }
        }

        };

    private final Subscription.EventCallback<PlayerContext> mPlayerContextEventCallback = new Subscription.EventCallback<PlayerContext>() {
        @Override
        public void onEvent(PlayerContext playerContext) {

            Log.d("MainActivity", " uri " + playerContext.uri +" title "+playerContext.title);

            URI=playerContext.uri;
        }
    };


};
