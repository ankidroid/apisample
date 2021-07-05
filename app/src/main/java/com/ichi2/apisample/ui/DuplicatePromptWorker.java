package com.ichi2.apisample.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.ichi2.apisample.R;
import com.ichi2.apisample.helper.AnkiDroidHelper;
import com.ichi2.apisample.model.AddingHandler;
import com.ichi2.apisample.model.MusInterval;

public class DuplicatePromptWorker implements Runnable {
    private final MainActivity mainActivity;

    private final boolean tagDuplicates;
    private final String duplicateTag;

    private final MusInterval[] existingMis;
    private final AddingHandler duplicateAddingHandler;

    public DuplicatePromptWorker(MainActivity mainActivity, boolean tagDuplicates, String duplicateTag, MusInterval[] existingMis, AddingHandler duplicateAddingHandler) {
        this.mainActivity = mainActivity;

        this.tagDuplicates = tagDuplicates;
        this.duplicateTag = duplicateTag;

        this.existingMis = existingMis;
        this.duplicateAddingHandler = duplicateAddingHandler;
    }

    @Override
    public void run() {
        ViewGroup viewGroup = mainActivity.findViewById(R.id.content);
        View dialogView = LayoutInflater.from(mainActivity).inflate(R.layout.dialog_adding_duplicate, viewGroup, false);
        final AlertDialog dialog = new AlertDialog.Builder(mainActivity)
                .setView(dialogView)
                .create();

        Button actionAdd = dialogView.findViewById(R.id.actionAddDuplicate);
        actionAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    MusInterval newMi = duplicateAddingHandler.add();
                    if (newMi == null) {
                        return;
                    }
                    if (tagDuplicates) {
                        duplicateAddingHandler.tag(duplicateTag);
                    }
                } catch (Throwable e) {
                    mainActivity.handleError(e);
                }
                dialog.dismiss();
            }
        });

        Button actionMark = dialogView.findViewById(R.id.actionMarkDuplicates);
        int existingCount = existingMis.length;
        MusInterval existingMi = existingMis[0];
        try {
            int markedCount = existingMi.getExistingMarkedNotesCount();
            if (existingCount > markedCount) {
                actionMark.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        try {
                            final int count = duplicateAddingHandler.mark();
                            mainActivity.showQuantityMsg(R.plurals.mi_marked_result, count, count);
                            mainActivity.refreshExisting();
                        } catch (Throwable e) {
                            mainActivity.handleError(e);
                        }
                        dialog.dismiss();
                    }
                });

            } else {
                actionMark.setVisibility(View.GONE);
            }
        } catch (AnkiDroidHelper.InvalidAnkiDatabaseException e) {
            actionMark.setVisibility(View.GONE);
        }

        TextView textMsg = dialogView.findViewById(R.id.textDuplicateMsg);
        Button actionReplace = dialogView.findViewById(R.id.actionReplaceDuplicate);
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
            actionReplace.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        MusInterval newMi = duplicateAddingHandler.replace();
                        if (newMi == null) {
                            return;
                        }
                        mainActivity.showMsg(R.string.item_replaced);
                    } catch (Throwable e) {
                        mainActivity.handleError(e);
                    }
                    dialog.dismiss();
                }
            });
        } else {
            actionReplace.setVisibility(View.GONE);
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

        Button actionSkip = dialogView.findViewById(R.id.actionSkipDuplicate);
        actionSkip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                try {
                    duplicateAddingHandler.proceed();
                } catch (Throwable e) {
                    mainActivity.handleError(e);
                }
            }
        });

        textMsg.setText(msg);
        dialog.show();
    }
}
