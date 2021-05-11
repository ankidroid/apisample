package com.ichi2.apisample.ui;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;
import android.view.View;

import java.io.IOException;

public class OnPlayClickListener implements View.OnClickListener {
    private final Context context;
    private final Uri uri;

    public OnPlayClickListener(Context context, Uri uri) {
        this.context = context;
        this.uri = uri;
    }

    @Override
    public void onClick(View view) {
        MediaPlayer mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioAttributes(
                new AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
        );
        try {
            mediaPlayer.setDataSource(context, uri);
            mediaPlayer.prepare();
            mediaPlayer.start();
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    mediaPlayer.release();
                }
            });
        } catch (IOException e) {
            // @todo: handle
        }
    }
}
