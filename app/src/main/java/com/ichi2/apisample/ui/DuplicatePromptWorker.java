package com.ichi2.apisample.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Handler;

import com.ichi2.apisample.R;
import com.ichi2.apisample.helper.AnkiDroidHelper;
import com.ichi2.apisample.model.AddingHandler;
import com.ichi2.apisample.model.MusInterval;

public class DuplicatePromptWorker implements Runnable {
    private final MainActivity mainActivity;
    private final Handler handler;

    private final boolean tagDuplicates;
    private final String duplicateTag;

    private final MusInterval[] existingMis;
    private final AddingHandler duplicateAddingHandler;

    public DuplicatePromptWorker(MainActivity mainActivity, Handler handler, boolean tagDuplicates, String duplicateTag, MusInterval[] existingMis, AddingHandler duplicateAddingHandler) {
        this.mainActivity = mainActivity;
        this.handler = handler;

        this.tagDuplicates = tagDuplicates;
        this.duplicateTag = duplicateTag;

        this.existingMis = existingMis;
        this.duplicateAddingHandler = duplicateAddingHandler;
    }

    @Override
    public void run() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity)
                .setPositiveButton(R.string.add_anyway, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        try {
                            MusInterval newMi = duplicateAddingHandler.add();
                            if (tagDuplicates) {
                                duplicateAddingHandler.tag(duplicateTag);
                            }
                            mainActivity.handleInsertion(newMi);
                            mainActivity.showQuantityMsg(R.plurals.mi_added, 1);
                            duplicateAddingHandler.proceed();
                        } catch (Throwable e) {
                            mainActivity.handleError(e);
                        }
                    }
                });
        int existingCount = existingMis.length;
        MusInterval existingMi = existingMis[0];
        try {
            int markedCount = existingMi.getExistingMarkedNotesCount();
            if (existingCount > markedCount) {
                builder.setNeutralButton(R.string.mark_existing, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        try {
                            final int count = duplicateAddingHandler.mark();
                            mainActivity.showQuantityMsg(R.plurals.mi_marked_result, count, count);
                            mainActivity.refreshExisting();
                            duplicateAddingHandler.proceed();
                        } catch (Throwable e) {
                            mainActivity.handleError(e);
                        }
                    }
                });
            }
        } catch (AnkiDroidHelper.InvalidAnkiDatabaseException e) {
            // simply don't give the option to mark if unable to count existing
        }
        Resources res = mainActivity.getResources();
        String msg;
        if (existingCount == 1) {
            msg = res.getQuantityString(
                    R.plurals.duplicate_warning, existingCount,
                    existingMi.notes[0] + existingMi.octaves[0],
                    existingMi.direction,
                    existingMi.timing,
                    existingMi.intervals[0],
                    existingMi.tempo,
                    existingMi.instrument);
            builder.setNegativeButton(R.string.replace_existing, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    try {
                        MusInterval newMi = duplicateAddingHandler.replace();
                        mainActivity.handleInsertion(newMi);
                        mainActivity.showMsg(R.string.item_replaced);
                        duplicateAddingHandler.proceed();
                    } catch (Throwable e) {
                        mainActivity.handleError(e);
                    }
                }
            });
        } else {
            msg = res.getQuantityString(R.plurals.duplicate_warning, existingCount,
                    existingCount,
                    existingMi.notes[0] + existingMi.octaves[0],
                    existingMi.direction,
                    existingMi.timing,
                    existingMi.intervals[0],
                    existingMi.tempo,
                    existingMi.instrument);
        }
        if (existingCount > 1) {
            if (tagDuplicates) {
                try {
                    duplicateAddingHandler.tag(duplicateTag);
                } catch (Throwable e) {
                    mainActivity.handleError(e);
                }
            }
        }
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                try {
                    duplicateAddingHandler.proceed();
                } catch (Throwable e) {
                    mainActivity.handleError(e);
                }
            }
        });
        builder.setMessage(msg);
        builder.show();
    }
}
