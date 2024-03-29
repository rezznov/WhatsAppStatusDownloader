package com.example.whatsappstatusdownloader.view.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.net.Uri;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import com.bumptech.glide.Glide;
import com.example.whatsappstatusdownloader.R;
import com.example.whatsappstatusdownloader.util.Constants;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.ortiz.touchview.TouchImageView;

import org.greenrobot.eventbus.EventBus;

import java.io.File;


public class Status extends AppCompatActivity {
    private static final String TAG = "Status";

    int type;
    String path;
    String starter;

    TouchImageView imageView;
    PlayerView playerView;
    ConstraintLayout layout;
    ImageButton deleteButton;
    ImageButton shareButton;


    SimpleExoPlayer player;
    DataSource.Factory mediaDataSourceFactory;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);
        Intent intent = getIntent();

        imageView = findViewById(R.id.status_imageView);
        playerView = findViewById(R.id.status_videoView);
        layout = findViewById(R.id.status_layout);
        deleteButton = findViewById(R.id.delete_button);
        shareButton = findViewById(R.id.share_button);

        deleteButton.setOnClickListener(v -> {
            Log.i(TAG, "onCreate: delete button clicked");
        });


        path = intent.getStringExtra("path");
        type = intent.getIntExtra("type", 2);
        starter = intent.getStringExtra("starter");

        if (starter.equals(Constants.CACHED_STARTER_INTENT)) {
            deleteButton.setVisibility(View.GONE);
        } else if (starter.equals(Constants.GALLERY_TARTER_INTENT)) {
            deleteButton.setOnClickListener(v -> {
                File file = new File(path);
                try {

                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage(R.string.delete_dialog_messege)
                            .setPositiveButton(R.string.delete_accepted, (dialog, id) -> {
                                boolean isDeleted = file.delete();
                                Log.i(TAG, "onCreate: deleting file" + isDeleted);
                                EventBus.getDefault().post(Constants.REFRESH_GALLERY_EVENT_MESSAGE);
                                finish();
                            })
                            .setNegativeButton(R.string.delete_rejected, (dialog, id) -> {
                            }).create().show();

                } catch (Exception e) {
                    Log.e(TAG, "onCreate: ", e);
                }
            });
        }

        if (type == Constants.STATUS_TYPE_IMAGE) {
            playerView.setVisibility(View.GONE);
            Glide.with(this).load(new File(path)).into(imageView);
            shareButton.setOnClickListener(v -> {
                Log.i(TAG, "onCreate: share button clicked");
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(this,
                        this.getApplicationContext().getPackageName() + ".provider"
                        , new File(path)));
                shareIntent.setType("image/jpg");
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(Intent.createChooser(shareIntent, getResources().getString(R.string.share_intent_title)));
            });
        } else if (type == Constants.STATUS_TYPE_VIDEO) {
            imageView.setVisibility(View.GONE);
            shareButton.setOnClickListener(v -> {
                Log.i(TAG, "onCreate: share button clicked");
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(this,
                        this.getApplicationContext().getPackageName() + ".provider"
                        , new File(path)));
                shareIntent.setType("video/mp4");
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(Intent.createChooser(shareIntent, getResources().getString(R.string.share_intent_title)));
            });
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    private void initializePlayer() {

        player = ExoPlayerFactory.newSimpleInstance(this);

        mediaDataSourceFactory = new DefaultDataSourceFactory(this, Util.getUserAgent(this, "mediaPlayerSample"));

        MediaSource mediaSource = new ExtractorMediaSource.Factory(mediaDataSourceFactory)
                .createMediaSource(Uri.parse(path));


        player.prepare(mediaSource, false, false);
        player.setPlayWhenReady(true);


        playerView.setShutterBackgroundColor(Color.TRANSPARENT);
        playerView.setPlayer(player);
        playerView.requestFocus();

    }

    private void releasePlayer() {
        player.release();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (Util.SDK_INT > 23) initializePlayer();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Util.SDK_INT <= 23) initializePlayer();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (Util.SDK_INT <= 23) releasePlayer();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (Util.SDK_INT > 23) releasePlayer();
    }
}
