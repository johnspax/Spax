package movie.software.com.spax.controller;

/**
 * Created by John Muya on 08/02/2017.
 */

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayer.Provider;
import com.google.android.youtube.player.YouTubePlayerView;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import movie.software.com.spax.R;


public class YoutubeActivity extends YouTubeBaseActivity implements YouTubePlayer.OnInitializedListener {

    private static final String API_KEY = "AIzaSyDe4LYliUr_onWS3JiKD4CmlR_6HWsSxCo";
    private static final String VIDEO_ID = "7bDLIV96LD4";
    private YouTubePlayerView videoPlayer;
    public String YoutubeURL = "", YoutubID = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.youtubeplayer);

        Intent intent = getIntent();
        YoutubeURL = intent.getExtras().getString("YoutubeURL");
        YoutubID = YoutubeID(YoutubeURL);

        videoPlayer = (YouTubePlayerView)findViewById(R.id.youtube_player);
        videoPlayer.initialize(API_KEY, this);

    }

    @Override
    public void onInitializationSuccess(Provider provider, YouTubePlayer youTubePlayer, boolean complete) {
        if (!complete) {
            //youTubePlayer.cueVideo(YoutubID);
            youTubePlayer.loadVideo(YoutubID);
            //youTubePlayer.setFullscreen(true);
        }

    }

    @Override
    public void onInitializationFailure(Provider provider, YouTubeInitializationResult youTubeInitializationResult) {
        Toast.makeText(YoutubeActivity.this, youTubeInitializationResult.toString(), Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public String YoutubeID(String YouRL) {
        String YouID = YouRL;
        String pattern = "(?<=youtu.be/|watch\\?v=|/videos/|embed\\/)[^#\\&\\?]*";
        Pattern compiledPattern = Pattern.compile(pattern);
        Matcher matcher = compiledPattern.matcher(YouRL);

        if(matcher.find())
            YouID = matcher.group();
        return YouID;
    }
}
