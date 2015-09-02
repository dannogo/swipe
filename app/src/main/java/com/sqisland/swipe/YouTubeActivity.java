package com.sqisland.swipe;


import android.os.Bundle;
import android.widget.Toast;

import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;

/**
 * Created by oleh on 9/2/15.
 */
public class YouTubeActivity extends YouTubeBaseActivity
        implements YouTubePlayer.OnInitializedListener{

    private String video;
    private final String API_KEY = "AIzaSyAe3EUpCeVJVsnYHg0abxmeiAugORX7DHE";

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        setContentView(R.layout.youtube);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            video = extras.getString("video");
        }

        YouTubePlayerView youTubeView = (YouTubePlayerView) findViewById(R.id.youtube_view);
        youTubeView.initialize(API_KEY, this);

    }

    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean b) {
        youTubePlayer.loadVideo(video);
    }

    @Override
    public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {
        Toast.makeText(this, "Error: " + youTubeInitializationResult.toString(), Toast.LENGTH_LONG).show();
    }
}
