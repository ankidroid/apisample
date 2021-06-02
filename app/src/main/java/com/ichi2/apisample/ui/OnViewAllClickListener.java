package com.ichi2.apisample.ui;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.provider.DocumentsContract;
import android.provider.OpenableColumns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ichi2.apisample.R;
import com.ichi2.apisample.helper.UriUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;

public class OnViewAllClickListener implements View.OnClickListener {
    private final MainActivity mainActivity;
    private final FilenameAdapter.UriPathName[] uriPathNames;

    public OnViewAllClickListener(MainActivity mainActivity, FilenameAdapter.UriPathName[] uriPathNames) {
        this.mainActivity = mainActivity;
        this.uriPathNames = uriPathNames;
    }

    @Override
    public void onClick(View view) {
        FilenameAdapter.UriPathName[] filenames;
        if (!mainActivity.mismatchingSorting) {
            filenames = uriPathNames;
        } else {
            ContentResolver resolver = mainActivity.getContentResolver();
            ArrayList<String> names = new ArrayList<>(uriPathNames.length);
            ArrayList<Long> lastModifiedValues = new ArrayList<>(uriPathNames.length);
            for (FilenameAdapter.UriPathName uriPathName : uriPathNames) {
                Uri uri = uriPathName.getUri();
                Uri contentUri = UriUtil.getContentUri(mainActivity, uri);
                Cursor cursor = resolver.query(contentUri, null, null, null, null);
                int nameIdx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                int lastModifiedIdx = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_LAST_MODIFIED);
                cursor.moveToFirst();
                names.add(cursor.getString(nameIdx));
                lastModifiedValues.add(
                        lastModifiedIdx != -1 ?
                                cursor.getLong(lastModifiedIdx) :
                                new File(uri.getPath()).lastModified()
                );
                cursor.close();
            }

            FilenameAdapter.UriPathName[] sortedUriPathNames = new FilenameAdapter.UriPathName[uriPathNames.length];

            if (mainActivity.sortByName) {
                final ArrayList<String> namesSorted = new ArrayList<>(names);
                namesSorted.sort(new Comparator<String>() {
                    @Override
                    public int compare(String s, String t1) {
                        return s.compareTo(t1);
                    }
                });
                for (int j = 0; j < sortedUriPathNames.length; j++) {
                    int sortedNameIdx = names.indexOf(namesSorted.get(j));
                    sortedUriPathNames[j] = uriPathNames[sortedNameIdx];
                }

            } else if (mainActivity.sortByDate) {
                final ArrayList<Long> lastModifiedSorted = new ArrayList<>(lastModifiedValues);
                lastModifiedSorted.sort(new Comparator<Long>() {
                    @Override
                    public int compare(Long s, Long t1) {
                        return Long.compare(s, t1);
                    }
                });
                for (int j = 0; j < sortedUriPathNames.length; j++) {
                    int sortedLastModifiedIdx = lastModifiedValues.indexOf(lastModifiedSorted.get(j));
                    sortedUriPathNames[j] = uriPathNames[sortedLastModifiedIdx];
                }
            }

            filenames = sortedUriPathNames;
        }
        for (int i = 0; i < filenames.length; i++) {
            filenames[i] = mainActivity.makeLabel(filenames[i], i);
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
