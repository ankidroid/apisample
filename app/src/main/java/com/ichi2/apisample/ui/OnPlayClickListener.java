package com.ichi2.apisample.ui;

import android.net.Uri;
import android.view.View;
import android.widget.Button;

import com.ichi2.apisample.R;
import com.ichi2.apisample.helper.AudioUtil;

public class OnPlayClickListener implements View.OnClickListener {
    private final MainActivity mainActivity;
    private final FilenameAdapter.UriPathName uriPathName;
    private Button actionPlay;
    private Runnable callback;
    private boolean isPlaying;

    public OnPlayClickListener(MainActivity mainActivity, FilenameAdapter.UriPathName uriPathName, Button actionPlay) {
        this.mainActivity = mainActivity;
        this.uriPathName = uriPathName;
        this.actionPlay = actionPlay;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public void setActionPlay(Button actionPlay) {
        this.actionPlay = actionPlay;
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

        long duration = AudioUtil.getDuration(mainActivity, uri);
        callback = new Runnable() {
            @Override
            public void run() {
                handleStopPlaying();
            }
        };
        mainActivity.handler.postDelayed(callback, duration);

        if (actionPlay != null) {
            actionPlay.setText(R.string.stop);
        }
        isPlaying = true;
    }

    void stop() {
        mainActivity.handler.removeCallbacks(callback);
        handleStopPlaying();
    }

    private void handleStopPlaying() {
        if (actionPlay != null) {
            actionPlay.setText(R.string.play);
        }
        isPlaying = false;
    }
}