package com.ichi2.apisample.ui;

import android.net.Uri;
import android.view.View;
import android.widget.Button;

import com.ichi2.apisample.R;
import com.ichi2.apisample.helper.AudioUtil;

public class OnPlayClickListener implements View.OnClickListener {
    private final MainActivity mainActivity;
    private final Button actionPlay;
    private final FilenameAdapter.UriPathName uriPathName;
    private Runnable callback;
    private boolean isPlaying;

    public OnPlayClickListener(MainActivity mainActivity, Button actionPlay, FilenameAdapter.UriPathName uriPathName) {
        this.mainActivity = mainActivity;
        this.actionPlay = actionPlay;
        this.uriPathName = uriPathName;
    }

    @Override
    public void onClick(View view) {
        if (isPlaying) {
            mainActivity.soundPlayer.stop();
            stop();
            return;
        }

        Uri uri = uriPathName.getUri();
        String path = uriPathName.getPath();
        mainActivity.soundPlayer.play(uri, path);

        actionPlay.setText(R.string.stop);
        long duration = AudioUtil.getDuration(mainActivity, uri);
        callback = new Runnable() {
            @Override
            public void run() {
                handleStopPlaying();
            }
        };
        mainActivity.handler.postDelayed(callback, duration);
        isPlaying = true;
    }

    void stop() {
        mainActivity.handler.removeCallbacks(callback);
        handleStopPlaying();
    }

    private void handleStopPlaying() {
        actionPlay.setText(R.string.play);
        isPlaying = false;
    }
}