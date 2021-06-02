package com.ichi2.apisample.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
            uriPathNames[i] = mainActivity.makeLabel(uriPathNames[i], i);
        }

        ViewGroup viewGroup = mainActivity.findViewById(R.id.content);
        View dialogView = LayoutInflater.from(mainActivity).inflate(R.layout.dialog_filenames, viewGroup, false);

        final RecyclerView recyclerView = dialogView.findViewById(R.id.recyclerView);
        recyclerView.setAdapter(new FilenameAdapter(uriPathNames, mainActivity.soundPlayer));
        recyclerView.setLayoutManager(new LinearLayoutManager(mainActivity));

        LinearLayout layoutSorting = dialogView.findViewById(R.id.layoutSorting);

        if (mainActivity.mismatchingSorting) {
            RadioGroup radioGroupSorting = layoutSorting.findViewById(R.id.radioGroupSorting);
            RadioButton radioByName = radioGroupSorting.findViewById(R.id.radioByName);
            radioByName.setChecked(mainActivity.sortByName);
            RadioButton radioByDate = radioGroupSorting.findViewById(R.id.radioByDate);
            radioByDate.setChecked(mainActivity.sortByDate);
            radioGroupSorting.setOnCheckedChangeListener(new OnFilenamesSortingCheckedChangeListener(mainActivity, uriPathNames, recyclerView));
        } else {
            layoutSorting.setVisibility(View.GONE);
        }

        new AlertDialog.Builder(mainActivity)
                .setView(dialogView)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .show();
    }
}
