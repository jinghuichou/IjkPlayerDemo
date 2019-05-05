package com.wehaicao.ijkplayerdemo.ui;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.wehaicao.ijkplayerdemo.R;

import tv.danmaku.ijk.media.example.widget.media.IjkVideoView;
import tv.danmaku.ijk.media.player.IMediaPlayer;

public class LiveActivity extends Activity {

    private static final String TAG = "LiveActivity";
    private IjkVideoView player;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live);

        initView();

        initPlayer();
    }

    private void initView() {
        player = findViewById(R.id.player);
    }

    private void initPlayer() {
        player.setVideoPath("rtmp://mobliestream.c3tv.com:554/live/goodtv.sdp");
        player.setOnPreparedListener(new IMediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(IMediaPlayer mp) {
                player.start();
            }
        });
    }

    @Override
    protected void onDestroy() {
        if (player != null) {
            player.stopPlayback();
        }
        super.onDestroy();
    }
}
