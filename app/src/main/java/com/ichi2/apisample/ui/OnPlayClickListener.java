package com.ichi2.apisample.ui;

import android.net.Uri;
import android.view.View;
import android.widget.Button;

import com.ichi2.apisample.R;
import com.ichi2.apisample.helper.AudioUtil;

class OnPlayClickListener implements View.OnClickListener {
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
            handlePlayingStop();
            mainActivity.soundPlayer.stop();
            mainActivity.handler.removeCallbacks(callback);
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
                handlePlayingStop();
            }
        };
        mainActivity.handler.postDelayed(callback, duration);
        isPlaying = true;
    }

    private void handlePlayingStop() {
        actionPlay.setText(R.string.play);
        isPlaying = false;
    }
}