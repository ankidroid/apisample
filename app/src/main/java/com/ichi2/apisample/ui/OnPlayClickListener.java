package com.ichi2.apisample.ui;

import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;
import android.view.View;

import com.ichi2.apisample.R;

import java.io.IOException;

public class OnPlayClickListener implements View.OnClickListener {
    private final MainActivity mainActivity;
    private final Uri uri;
    private final String path;

    public OnPlayClickListener(MainActivity mainActivity, Uri uri, String path) {
        this.mainActivity = mainActivity;
        this.uri = uri;
        this.path = path;
    }

    @Override
    public void onClick(View view) {
        if (uri == null) {
            mainActivity.showMsg(R.string.access_error, path);
            return;
        }
        MediaPlayer mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioAttributes(
                new AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
        );
        try {
            mediaPlayer.setDataSource(mainActivity, uri);
            mediaPlayer.prepare();
            mediaPlayer.start();
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    mediaPlayer.release();
                }
            });
        } catch (IOException e) {
            mainActivity.showMsg(R.string.audio_playing_error);
        }
    }
}
