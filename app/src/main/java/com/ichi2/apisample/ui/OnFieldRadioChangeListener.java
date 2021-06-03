package com.ichi2.apisample.ui;

import android.widget.RadioGroup;

public class OnFieldRadioChangeListener implements RadioGroup.OnCheckedChangeListener {
    private final MainActivity mainActivity;

    public OnFieldRadioChangeListener(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int i) {
        mainActivity.clearAddedFilenames();
        mainActivity.refreshExisting();
    }
}
