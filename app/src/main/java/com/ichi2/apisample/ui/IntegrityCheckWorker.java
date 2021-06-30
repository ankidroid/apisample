package com.ichi2.apisample.ui;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Handler;

import com.ichi2.apisample.R;
import com.ichi2.apisample.helper.StringUtil;
import com.ichi2.apisample.model.MusInterval;
import com.ichi2.apisample.model.NotesIntegrity;

import java.util.HashMap;
import java.util.Map;

public class IntegrityCheckWorker implements Runnable {
    private final MainActivity mainActivity;
    private final ProgressDialog progressDialog;
    private final Handler handler;

    private final NotesIntegrity notesIntegrity;


    public IntegrityCheckWorker(NotesIntegrity notesIntegrity, MainActivity mainActivity, ProgressDialog progressDialog, Handler handler) {
        this.notesIntegrity = notesIntegrity;
        this.mainActivity = mainActivity;
        this.progressDialog = progressDialog;
        this.handler = handler;
    }

    @Override
    public void run() {
        try {
            NotesIntegrity.Summary summary = notesIntegrity.check();
            final String report = getReport(summary, mainActivity);
            handler.post(new Thread(new Runnable() {
                @Override
                public void run() {
                    progressDialog.dismiss();
                    new AlertDialog.Builder(mainActivity)
                            .setMessage(report)
                            .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                }
                            })
                            .show();
                }
            }));
        } catch (final Throwable t) {
            handler.post(new Thread(new Runnable() {
                @Override
                public void run() {
                    progressDialog.dismiss();
                    mainActivity.handleError(t);
                }
            }));
        }
    }

    private static final String ALLOWED_START_NOTES_STR = StringUtil.joinStrings(", ", MusInterval.Fields.StartNote.VALUES);
    private static final String ALLOWED_DIRECTIONS_STR = String.format("%s, %s", MusInterval.Fields.Direction.ASC, MusInterval.Fields.Direction.DESC);
    private static final String ALLOWED_TIMINGS_STR = String.format("%s, %s", MusInterval.Fields.Timing.MELODIC, MusInterval.Fields.Timing.HARMONIC);
    private static final String ALLOWED_INTERVALS_STR = StringUtil.joinStrings(", ", MusInterval.Fields.Interval.VALUES);
    private static final Map<String, GetStringArgs> FIELD_VALIDATION_STRING_ARGS = new HashMap<String, GetStringArgs>() {{
        put(MusInterval.Fields.SOUND, new GetStringArgs(R.string.validation_sound));
        put(MusInterval.Fields.SOUND_SMALLER, new GetStringArgs(R.string.validation_sound));
        put(MusInterval.Fields.SOUND_LARGER, new GetStringArgs(R.string.validation_sound));
        put(MusInterval.Fields.START_NOTE, new GetStringArgs(R.string.validation_allowed_values, ALLOWED_START_NOTES_STR));
        put(MusInterval.Fields.DIRECTION, new GetStringArgs(R.string.validation_direction, ALLOWED_DIRECTIONS_STR));
        put(MusInterval.Fields.TIMING, new GetStringArgs(R.string.validation_allowed_values, ALLOWED_TIMINGS_STR));
        put(MusInterval.Fields.INTERVAL, new GetStringArgs(R.string.validation_allowed_values, ALLOWED_INTERVALS_STR));
        put(MusInterval.Fields.TEMPO, new GetStringArgs(R.string.validation_range,
                MusInterval.Fields.Tempo.MIN_VALUE, MusInterval.Fields.Tempo.MAX_VALUE));
        put(MusInterval.Fields.INSTRUMENT, new GetStringArgs(R.string.validation_mandatory));
        put(MusInterval.Fields.FIRST_NOTE_DURATION_COEFFICIENT, new GetStringArgs(R.string.validation_positive_decimal));
    }};
    static {
        if (!FIELD_VALIDATION_STRING_ARGS.keySet().equals(MusInterval.Fields.VALIDATORS.keySet())) {
            throw new AssertionError();
        }
    }

    private static String getReport(NotesIntegrity.Summary integritySummary, Context context) {
        MusInterval mi = integritySummary.getMusInterval();

        String corruptedTag = integritySummary.getCorruptedTag();
        String suspiciousTag = integritySummary.getSuspiciousTag();
        String duplicateTag = integritySummary.getDuplicateTag();

        int notesCount = integritySummary.getNotesCount();
        int corruptedNotesCount = integritySummary.getCorruptedNotesCount();
        Map<String, Integer> corruptedFieldCounts = integritySummary.getCorruptedFieldCounts();
        int fixedCorruptedFieldsCount = integritySummary.getFixedCorruptedFieldsCount();
        int suspiciousNotesCount = integritySummary.getSuspiciousNotesCount();
        Map<String, Integer> suspiciousFieldCounts = integritySummary.getSuspiciousFieldCounts();
        int fixedSuspiciousRelationsCount = integritySummary.getFixedSuspiciousRelationsCount();
        int autoFilledRelationsCount = integritySummary.getAutoFilledRelationsCount();
        int duplicateNotesCount = integritySummary.getDuplicateNotesCount();

        StringBuilder report = new StringBuilder();
        Resources res = context.getResources();
        report.append(res.getString(R.string.integrity_check_completed, notesCount));
        if (corruptedNotesCount > 0) {
            report.append("\n\n");
            if (corruptedNotesCount == 1) {
                report.append(res.getQuantityString(R.plurals.integrity_corrupted, corruptedNotesCount, corruptedTag));
            } else {
                report.append(res.getQuantityString(R.plurals.integrity_corrupted, corruptedNotesCount, corruptedNotesCount, corruptedTag));
            }
            for (Map.Entry<String, Integer> corruptedFieldCount : corruptedFieldCounts.entrySet()) {
                String fieldKey = corruptedFieldCount.getKey();
                int count = corruptedFieldCount.getValue();
                if (count > 0) {
                    String field = mi.modelFields.getOrDefault(fieldKey, fieldKey);
                    report.append("\n\n");
                    GetStringArgs fieldValidationStringArgs = FIELD_VALIDATION_STRING_ARGS.get(fieldKey);
                    String fieldValidationString = res.getString(fieldValidationStringArgs.getResId(), fieldValidationStringArgs.getFormatArgs());
                    report.append(res.getString(R.string.integrity_field_corrupted, field, count, fieldValidationString));
                }
            }
        }
        if (fixedCorruptedFieldsCount > 0) {
            report.append("\n\n");
            if (fixedCorruptedFieldsCount == 1) {
                report.append(res.getQuantityString(R.plurals.integrity_corrupted_field_values_fixed, fixedCorruptedFieldsCount));
            } else {
                report.append(res.getQuantityString(R.plurals.integrity_corrupted_field_values_fixed, fixedCorruptedFieldsCount, fixedCorruptedFieldsCount));
            }
        }
        if (suspiciousNotesCount > 0) {
            report.append("\n\n");
            if (suspiciousNotesCount == 1) {
                report.append(res.getQuantityString(R.plurals.integrity_suspicious, suspiciousNotesCount, suspiciousTag));
            } else {
                report.append(res.getQuantityString(R.plurals.integrity_suspicious, suspiciousNotesCount, suspiciousNotesCount, suspiciousTag));
            }
            for (Map.Entry<String, Integer> suspiciousFieldCount : suspiciousFieldCounts.entrySet()) {
                String fieldKey = suspiciousFieldCount.getKey();
                int count = suspiciousFieldCount.getValue();
                if (count > 0) {
                    String field = mi.modelFields.getOrDefault(fieldKey, fieldKey);
                    report.append("\n\n");
                    report.append(res.getString(R.string.integrity_field_suspicious, field, count));
                }
            }
        }
        if (fixedSuspiciousRelationsCount > 0) {
            report.append("\n\n");
            if (fixedSuspiciousRelationsCount == 1) {
                report.append(res.getQuantityString(R.plurals.integrity_suspicious_relations_fixed, fixedSuspiciousRelationsCount));
            } else {
                report.append(res.getQuantityString(R.plurals.integrity_suspicious_relations_fixed, fixedSuspiciousRelationsCount, fixedSuspiciousRelationsCount));
            }
        }
        if (autoFilledRelationsCount > 0) {
            report.append("\n\n");
            report.append(res.getQuantityString(R.plurals.integrity_links, autoFilledRelationsCount, autoFilledRelationsCount));
        }
        if (corruptedNotesCount == 0 && suspiciousNotesCount == 0) {
            report.append("\n\n");
            report.append(res.getString(R.string.integrity_ok));
        }
        if (duplicateNotesCount > 0) {
            report.append("\n\n");
            report.append(res.getString(R.string.integrity_duplicates, duplicateNotesCount));
            if (duplicateTag != null) {
                report.append(res.getString(R.string.integrity_duplicates_tagged, duplicateTag));
            }
        }
        return report.toString();
    }
}
