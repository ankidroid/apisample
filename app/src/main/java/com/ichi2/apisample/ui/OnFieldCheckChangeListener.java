package com.ichi2.apisample.ui;

import android.widget.CheckBox;
import android.widget.CompoundButton;

import java.util.ArrayList;

public class OnFieldCheckChangeListener implements CompoundButton.OnCheckedChangeListener {
    private MainActivity mainActivity;

    private final CheckBox[] checkBoxes;
    private final CheckBox checkBoxAny;
    private boolean enableMultiple;

    public OnFieldCheckChangeListener(MainActivity mainActivity, CheckBox[] checkBoxes, CheckBox checkBoxAny, boolean enableMultiple) {
        this.mainActivity = mainActivity;
        this.checkBoxes = checkBoxes;
        this.checkBoxAny = checkBoxAny;
        this.enableMultiple = enableMultiple;
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        if (compoundButton.getId() == checkBoxAny.getId()) {
            if (b) {
                for (CheckBox checkBox : checkBoxes) {
                    checkBox.setChecked(false);
                }
            } else if (enableMultiple) {
                for (CheckBox checkBox : checkBoxes) {
                    if (checkBox.isChecked()) {
                        return;
                    }
                }
                for (CheckBox checkBox : checkBoxes) {
                    checkBox.setChecked(true);
                }
            }
        } else if (b) {
            checkBoxAny.setChecked(false);
            if (!enableMultiple) {
                for (CheckBox checkBox : checkBoxes) {
                    if (checkBox.getId() != compoundButton.getId()) {
                        checkBox.setChecked(false);
                    }
                }
            }
        }
        mainActivity.clearAddedFilenames();
        mainActivity.refreshExisting();
        mainActivity.refreshPermutations();
    }

    public void setEnableMultiple(boolean enableMultiple) {
        if (!enableMultiple) {
            ArrayList<CheckBox> checked = new ArrayList<>();
            for (CheckBox checkBox : checkBoxes) {
                if (checkBox.isChecked()) {
                    checked.add(checkBox);
                }
            }
            if (checked.size() > 1) {
                for (CheckBox checkBox : checked) {
                    checkBox.setChecked(false);
                }
            }
        }
        this.enableMultiple = enableMultiple;
    }
}
