package com.ichi2.apisample.ui;

import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;

import com.ichi2.apisample.R;

import java.io.IOException;

public class SoundPlayer {
    private final MainActivity mainActivity;
    private final MediaPlayer mediaPlayer;

    public SoundPlayer(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioAttributes(
                new AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
        );
    }

    public void play(Uri uri, String path) {
        if (uri == null) {
            mainActivity.showMsg(R.string.access_error, path);
            return;
        }
        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(mainActivity, uri);
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException e) {
            mainActivity.showMsg(R.string.audio_playing_error);
        }
    }

    public void stop() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }
    }
}
