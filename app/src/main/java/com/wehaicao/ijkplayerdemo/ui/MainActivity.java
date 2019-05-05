package com.wehaicao.ijkplayerdemo.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.wehaicao.ijkplayerdemo.R;

public class MainActivity extends AppCompatActivity {

    private Button btnPlayer;
    private Button btnLive;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();

        initListener();

    }

    private void initListener() {
        btnPlayer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, MediaPlayerActivity.class);
                startActivity(intent);
            }
        });
        btnLive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, LiveActivity.class);
                startActivity(intent);
            }
        });
    }

    private void initView() {
        btnPlayer = findViewById(R.id.btn_player_video);
        btnLive = findViewById(R.id.btn_live);
    }

}
