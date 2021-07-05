package com.ichi2.apisample.ui;

import android.view.View;

import java.util.ArrayList;

public class OnGroupPlayClickListener implements View.OnClickListener {
    private final OnPlayClickListener onPlayClickListener;
    private final ArrayList<OnPlayClickListener> memberListeners;

    public OnGroupPlayClickListener(OnPlayClickListener onPlayClickListener, ArrayList<OnPlayClickListener> memberListeners) {
        this.onPlayClickListener = onPlayClickListener;
        this.memberListeners = memberListeners;
    }

    @Override
    public void onClick(View view) {
        onPlayClickListener.onClick(view);
        for (OnPlayClickListener memberListener : memberListeners) {
            if (onPlayClickListener != memberListener) {
                onPlayClickListener.stop();
            }
        }
    }
}
