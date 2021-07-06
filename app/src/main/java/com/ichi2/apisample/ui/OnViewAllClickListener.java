package com.ichi2.apisample.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ichi2.apisample.R;

public class OnViewAllClickListener implements View.OnClickListener {
    private final MainActivity mainActivity;
    private final FilenameAdapter.UriPathName[] uriPathNames;

    public OnViewAllClickListener(MainActivity mainActivity, FilenameAdapter.UriPathName[] uriPathNames) {
        this.mainActivity = mainActivity;
        this.uriPathNames = uriPathNames;
    }

    @Override
    public void onClick(View view) {
        for (int i = 0; i < uriPathNames.length; i++) {
            FilenameAdapter.UriPathName uriPathName = uriPathNames[i];
            uriPathName.setLabel(mainActivity.getFilenameLabel(uriPathName.getName(), i));
        }

        ViewGroup viewGroup = mainActivity.findViewById(R.id.content);
        final View dialogView = LayoutInflater.from(mainActivity).inflate(R.layout.dialog_filenames, viewGroup, false);

        final AlertDialog dialog = new AlertDialog.Builder(mainActivity)
                .setView(dialogView)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .setNeutralButton(R.string.play_all, null)
                .create();

        final OnPlayAllClickListener playAllListener = new OnPlayAllClickListener(mainActivity, uriPathNames, null, null);

        final FilenameAdapter adapter = new FilenameAdapter(mainActivity, uriPathNames, playAllListener);
        final RecyclerView recyclerView = dialogView.findViewById(R.id.recyclerView);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(mainActivity));

        playAllListener.setListeners(adapter.getListeners());
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button actionPlayAll = dialog.getButton(AlertDialog.BUTTON_NEUTRAL);
                playAllListener.setActionPlayAll(actionPlayAll);
                actionPlayAll.setOnClickListener(playAllListener);
            }
        });

        LinearLayout layoutSorting = dialogView.findViewById(R.id.layoutSorting);
        if (mainActivity.mismatchingSorting) {
            RadioGroup radioGroupSorting = layoutSorting.findViewById(R.id.radioGroupSorting);
            RadioButton radioByName = radioGroupSorting.findViewById(R.id.radioByName);
            radioByName.setChecked(mainActivity.sortByName);
            radioByName.setEnabled(!mainActivity.intersectingNames);
            RadioButton radioByDate = radioGroupSorting.findViewById(R.id.radioByDate);
            radioByDate.setChecked(mainActivity.sortByDate);
            radioByDate.setEnabled(!mainActivity.intersectingDates);
            radioGroupSorting.setOnCheckedChangeListener(new OnFilenamesSortingCheckedChangeListener(mainActivity, uriPathNames, recyclerView, playAllListener));
        } else {
            layoutSorting.setVisibility(View.GONE);
        }

        mainActivity.activeOnStartDialogs.add(dialog);
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                if (playAllListener.isPlaying()) {
                    playAllListener.stop();
                }
                mainActivity.onStartDialogDismissListener.onDismiss(dialogInterface);
                mainActivity.soundPlayer.stop();
            }
        });
        if (mainActivity.isCapturing) {
            mainActivity.closeCapturing();
        }
        dialog.show();
    }
}
