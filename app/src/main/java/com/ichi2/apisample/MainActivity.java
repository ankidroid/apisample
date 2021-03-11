package com.ichi2.apisample;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;


public class MainActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback {

    private static final int AD_PERM_REQUEST = 0;
    private static final int PERMISSIONS_REQUEST_EXTERNAL_STORAGE = 1;
    private static final int AD_PERM_REQUEST_VALID = 2;

    private static final int ACTION_SELECT_FILE = 10;

    private static final String STATE_REF_DB = "com.ichi2.apisample.uistate";

    private final Map<String, String> fieldLabels = new HashMap<>();

    private EditText inputFilename;
    private AutoCompleteTextView inputStartNote;
    private RadioGroup radioGroupDirection;
    private RadioGroup radioGroupTiming;
    private Spinner selectInterval;
    private SeekBar seekTempo;
    private AutoCompleteTextView inputInstrument;

    private HashSet<String> savedStartNotes = new HashSet<>();
    private HashSet<String> savedInstruments = new HashSet<>();

    private AnkiDroidHelper mAnkiDroid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        fieldLabels.put(MusInterval.Fields.SOUND, getResources().getString(R.string.label_filename));
        fieldLabels.put(MusInterval.Fields.START_NOTE, getResources().getString(R.string.start_note));
        fieldLabels.put(MusInterval.Fields.DIRECTION, getResources().getString(R.string.direction));
        fieldLabels.put(MusInterval.Fields.TIMING, getResources().getString(R.string.timing));
        fieldLabels.put(MusInterval.Fields.INTERVAL, getResources().getString(R.string.interval));
        fieldLabels.put(MusInterval.Fields.TEMPO, getResources().getString(R.string.tempo));
        fieldLabels.put(MusInterval.Fields.INSTRUMENT, getResources().getString(R.string.instrument));

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar main_toolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(main_toolbar);

        inputFilename = findViewById(R.id.inputFilename);
        inputStartNote = findViewById(R.id.inputStartNote);
        radioGroupDirection = findViewById(R.id.radioGroupDirection);
        radioGroupTiming = findViewById(R.id.radioGroupTiming);
        selectInterval = findViewById(R.id.selectInterval);
        seekTempo = findViewById(R.id.seekTempo);
        inputInstrument = findViewById(R.id.inputInstrument);

        seekTempo.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                TextView label = findViewById(R.id.labelTempoValue);
                label.setText(Integer.toString(seekBar.getProgress()));
            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) { }
            @Override public void onStopTrackingTouch(SeekBar seekBar) { }
        });

        final ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item,
                MusInterval.Fields.Interval.VALUES);
        selectInterval.setAdapter(adapter);

        configureTempoButtons();
        configureClearAllButton();
        configureSelectFileButton();
        configureCheckExistenceButton();
        configureAddToAnkiButton();
        configureSettingsButton();
        configureCheckIntegrityButton();

        restoreUiState();

        mAnkiDroid = new AnkiDroidHelper(this);

        if (mAnkiDroid.shouldRequestPermission()) {
            mAnkiDroid.requestPermission(this, AD_PERM_REQUEST_VALID);
        } else if (!doesModelExist() || !doesModelHaveEnoughFields() || !doesModelHaveStoredFields()) {
            validateModel();
        }
    }

    private void configureTempoButtons() {
        final Button actionTempoMinus = findViewById(R.id.actionTempoMinus);
        actionTempoMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final int progress = seekTempo.getProgress();
                if (progress > 0) {
                    seekTempo.setProgress(progress - 1);
                }
            }
        });

        final Button actionTempoPlus = findViewById(R.id.actionTempoPlus);
        actionTempoPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final int progress = seekTempo.getProgress();
                if (progress < 200) { // @fixme Use some constant probably
                    seekTempo.setProgress(progress + 1);
                }
            }
        });
    }

    private void configureClearAllButton() {
        final Button actionClearAll = findViewById(R.id.actionClearAll);
        actionClearAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inputFilename.setText("");
                inputStartNote.setText("");
                radioGroupDirection.check(findViewById(R.id.radioDirectionAny).getId());
                radioGroupTiming.check(findViewById(R.id.radioTimingAny).getId());
                selectInterval.setSelection(0);
                seekTempo.setProgress(0);
                inputInstrument.setText("");
            }
        });
    }

    private void configureSelectFileButton() {
        final Button actionSelectFile = findViewById(R.id.actionSelectFile);
        actionSelectFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestPermissions(new String[] {
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        PERMISSIONS_REQUEST_EXTERNAL_STORAGE
                );

                Intent intent = new Intent()
                        .setAction(Intent.ACTION_GET_CONTENT)
                        .setType("audio/*")
                        .addCategory(Intent.CATEGORY_OPENABLE)
                        .putExtra(Intent.EXTRA_LOCAL_ONLY, true);

                startActivityForResult(Intent.createChooser(intent, getResources().getText(R.string.select_filename)),
                        ACTION_SELECT_FILE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ACTION_SELECT_FILE && resultCode == RESULT_OK) {
            final Uri selectedFile = data.getData();
            inputFilename.setText(selectedFile.toString());
        }
    }

    private void configureCheckExistenceButton() {
        final AlertDialog.Builder markNoteDialog = new AlertDialog.Builder(this);
        markNoteDialog
                .setPositiveButton(R.string.str_yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        try {
                            final int count = getMusInterval().markExistingNotes();
                            showMsg(getResources().getQuantityString(R.plurals.mi_marked, count, count));
                        } catch (MusInterval.Exception e) {
                            processMusIntervalException(e);
                        } catch (AnkiDroidHelper.InvalidAnkiDatabaseException e) {
                            processInvalidAnkiDatabase(e);
                        }
                    }
                })
                .setNegativeButton(R.string.str_no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        final Button actionCheckExistence = findViewById(R.id.actionCheckExistence);
        actionCheckExistence.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mAnkiDroid.shouldRequestPermission()) {
                    mAnkiDroid.requestPermission(MainActivity.this, AD_PERM_REQUEST);
                    return;
                }
                if (!doesModelExist() || !doesModelHaveEnoughFields() || !doesModelHaveStoredFields()) {
                    validateModel();
                    return;
                }
                try {
                    final MusInterval mi = getMusInterval();
                    final int count = mi.getExistingNotesCount();

                    if (count > 0) {
                        final int marked = mi.getExistingMarkedNotesCount();

                        if (count == marked) {
                            showMsg(getResources().getQuantityString(R.plurals.mi_exists_marked, count, count));
                        } else if (marked == 0) {
                            markNoteDialog.setMessage(getResources().getQuantityString(R.plurals.mi_exists_ask_mark, count, count)).show();
                        } else {
                            markNoteDialog.setMessage(getResources().getQuantityString(R.plurals.mi_exists_partially_marked_ask_mark, marked, count, marked)).show();
                        }
                    } else {
                        showMsg(R.string.mi_not_exists);
                    }
                } catch (MusInterval.Exception e) {
                    processMusIntervalException(e);
                } catch (AnkiDroidHelper.InvalidAnkiDatabaseException e) {
                    processInvalidAnkiDatabase(e);
                }
            }
        });
    }

    private void configureAddToAnkiButton() {
        final Button actionAddToAnki = findViewById(R.id.actionAddToAnki);
        actionAddToAnki.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mAnkiDroid.shouldRequestPermission()) {
                    mAnkiDroid.requestPermission(MainActivity.this, AD_PERM_REQUEST);
                    return;
                }
                if (!doesModelExist() || !doesModelHaveEnoughFields() || !doesModelHaveStoredFields()) {
                    validateModel();
                    return;
                }
                try {
                    MusInterval newMi = getMusInterval().addToAnki();
                    inputFilename.setText(newMi.sound);
                    inputStartNote.setText(newMi.startNote);

                    savedStartNotes.add(newMi.startNote);
                    savedInstruments.add(newMi.instrument);

                    showMsg(R.string.item_added);
                } catch (MusInterval.Exception e) {
                    processMusIntervalException(e);
                } catch (AnkiDroidHelper.InvalidAnkiDatabaseException e) {
                    processInvalidAnkiDatabase(e);
                }
            }
        });
    }

    private void configureSettingsButton() {
        final Button actionOpenSettings = findViewById(R.id.actionOpenSettings);
        actionOpenSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mAnkiDroid.shouldRequestPermission()) {
                    mAnkiDroid.requestPermission(MainActivity.this, AD_PERM_REQUEST);
                    return;
                }
                if (!doesModelExist()) {
                    DialogFragment f = new CreateModelDialogFragment();
                    f.show(getFragmentManager(), "createModelDialog");
                    return;
                } else if (!doesModelHaveEnoughFields()) {
                    showMsg(String.format(getResources().getString(R.string.invalid_model), MusInterval.Builder.DEFAULT_MODEL_NAME));
                    return;
                }
                openSettings();
            }
        });
    }

    private void openSettings() {
        startActivity(new Intent(this, SettingsActivity.class));
    }

    private void configureCheckIntegrityButton() {
        final Button actionCheckIntegrity = findViewById(R.id.actionCheckIntegrity);
        actionCheckIntegrity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mAnkiDroid.shouldRequestPermission()) {
                    mAnkiDroid.requestPermission(MainActivity.this, AD_PERM_REQUEST);
                    return;
                }
                if (!doesModelExist() || !doesModelHaveEnoughFields() || !doesModelHaveStoredFields()) {
                    validateModel();
                    return;
                }
                try {
                    final long modelId = findModel();
                    MusInterval mi = getMusInterval();

                    final String soundField = mi.modelFields.get(MusInterval.Fields.SOUND);

                    Map<String, Map<String, String>> soundDict = new HashMap<>();
                    LinkedList<Map<String, String>> allNotesData = mAnkiDroid.findNotes(modelId, new HashMap<String, String>());
                    for (Map<String, String> noteData : allNotesData) {
                        soundDict.put(noteData.getOrDefault(soundField, ""), noteData);
                    }

                    final String soundSmallerField = mi.modelFields.get(MusInterval.Fields.SOUND_SMALLER);
                    final String soundLargerField = mi.modelFields.get(MusInterval.Fields.SOUND_LARGER);
                    final String intervalField = mi.modelFields.get(MusInterval.Fields.INTERVAL);

                    Map<String, String> searchData = mi.getCollectedData();
                    searchData.remove(soundField);

                    ArrayList<Map<String, String>> invalidNotesData = new ArrayList<>();
                    ArrayList<Map<String, String>> validNotesData = new ArrayList<>();

                    Map<String, Integer> invalidFieldsCount = new HashMap<>();
                    Map<String, Integer> emptyFieldsCount = new HashMap<>();

                    LinkedList<Map<String, String>> searchResult = mAnkiDroid.findNotes(modelId, searchData);

                    // SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                    // final String invalidTag = sharedPreferences.getString(SettingsFragment.KEY_INVALID_TAG_PREFERENCE, SettingsFragment.DEFAULT_INVALID_TAG);

                    for (final Map<String, String> noteData : searchResult) {
                        boolean valid = true;
                        try {
                            Map<String, String> defaultKeyData = MusInterval.getDefaultKeyData(noteData, mi.modelFields);
                            try {
                                new MusInterval.Builder(mAnkiDroid)
                                        .sound(noteData.get(soundField))
                                        .sound_smaller(noteData.get(soundSmallerField))
                                        .sound_larger(noteData.get(soundLargerField))
                                        .start_note(noteData.get(mi.modelFields.get(MusInterval.Fields.START_NOTE)))
                                        .direction(noteData.get(mi.modelFields.get(MusInterval.Fields.DIRECTION)))
                                        .timing(noteData.get(mi.modelFields.get(MusInterval.Fields.TIMING)))
                                        .interval(noteData.get(mi.modelFields.get(MusInterval.Fields.INTERVAL)))
                                        .tempo(noteData.get(mi.modelFields.get(MusInterval.Fields.TEMPO)))
                                        .instrument(noteData.get(mi.modelFields.get(MusInterval.Fields.INSTRUMENT)))
                                        .build();
                            } catch (MusInterval.InvalidFieldsException e) {
                                for (String field : e.getFields()) {
                                    int currCount = invalidFieldsCount.getOrDefault(field, 0);
                                    invalidFieldsCount.put(field, currCount + 1);
                                }
                                valid = false;
                            }
                            MusInterval.checkMandatoryFields(defaultKeyData);
                        } catch (MusInterval.MandatoryFieldsEmptyException e) {
                            LinkedList<String> emptyFields = e.getFields();
                            for (String field : emptyFields) {
                                int currCount = emptyFieldsCount.getOrDefault(field, 0);
                                emptyFieldsCount.put(field, currCount + 1);
                            }
                            valid = false;
                        }

                        //final long noteId = Long.parseLong(noteData.get(AnkiDroidHelper.KEY_ID));
                        if (!valid) {
                            invalidNotesData.add(noteData);
                            // mAnkiDroid.addTagToNote(noteId, String.format(" %s ", invalidTag));
                            continue;
                        }
                        validNotesData.add(noteData);
                    }

                    ArrayList<Map<String, String>> susNotesData = new ArrayList<>();

                    for (Map<String, String> noteData : validNotesData) {
                        String interval = noteData.get(intervalField);
                        int intervalIdx = 0;
                        for (int i = 1; i < MusInterval.Fields.Interval.VALUES.length; i++) {
                            if (MusInterval.Fields.Interval.VALUES[i].equals(interval)) {
                                intervalIdx = i;
                                break;
                            }
                        }
                        Map<String, String> keyData = new HashMap<String, String>(noteData) {{
                            remove(soundField);
                            remove(soundSmallerField);
                            remove(soundLargerField);
                            remove(intervalField);
                            remove(AnkiDroidHelper.KEY_ID);
                            remove(AnkiDroidHelper.KEY_TAGS);
                        }};
                        boolean sus = false;
                        String soundSmaller = noteData.getOrDefault(soundSmallerField, "");
                        if (!soundSmaller.isEmpty()) {
                            Map<String, String> smallerNoteData = soundDict.getOrDefault(soundSmaller, null);
                            if (smallerNoteData != null) {
                                String smallerInterval = smallerNoteData.getOrDefault(intervalField, "");
                                Map<String, String> smallerNoteKeyData = new HashMap<String, String>(smallerNoteData) {{
                                    remove(soundField);
                                    remove(soundSmallerField);
                                    remove(soundLargerField);
                                    remove(intervalField);
                                    remove(AnkiDroidHelper.KEY_ID);
                                    remove(AnkiDroidHelper.KEY_TAGS);
                                }};
                                if (!keyData.equals(smallerNoteKeyData) || intervalIdx <= 1 ||
                                        !MusInterval.Fields.Interval.VALUES[intervalIdx - 1].equalsIgnoreCase(smallerInterval)) {
                                    if (!susNotesData.contains(smallerNoteData)) {
                                        susNotesData.add(smallerNoteData);
                                    }
                                    sus = true;
                                }
                            } else {
                                sus = true;
                            }
                        }
                        String soundLarger = noteData.getOrDefault(soundLargerField, "");
                        if (!soundLarger.isEmpty()) {
                            Map<String, String> largerNoteData = soundDict.getOrDefault(soundLarger, null);
                            if (largerNoteData != null) {
                                String largerInterval = largerNoteData.getOrDefault(intervalField, "");
                                Map<String, String> largerNoteKeyData = new HashMap<String, String>(largerNoteData) {{
                                    remove(soundField);
                                    remove(soundSmallerField);
                                    remove(soundLargerField);
                                    remove(intervalField);
                                    remove(AnkiDroidHelper.KEY_ID);
                                    remove(AnkiDroidHelper.KEY_TAGS);
                                }};
                                if (!keyData.equals(largerNoteKeyData) || intervalIdx >= MusInterval.Fields.Interval.VALUES.length - 1 ||
                                        !MusInterval.Fields.Interval.VALUES[intervalIdx + 1].equalsIgnoreCase(largerInterval)) {
                                    if (!susNotesData.contains(largerNoteData)) {
                                        susNotesData.add(largerNoteData);
                                    }
                                    sus = true;
                                }
                            } else {
                                sus = true;
                            }
                        }
                        if (sus) {
                            if (!susNotesData.contains(noteData)) {
                                susNotesData.add(noteData);
                            }
                        }
                    }

                    int fixedLinksCount = 0;
                    ArrayList<Map<String, String>> correctNotesData = new ArrayList<>();
                    for (Map<String, String> noteData : validNotesData) {
                        if (!susNotesData.contains(noteData)) {
                            correctNotesData.add(noteData);
                            long noteId = Long.parseLong((noteData.get(AnkiDroidHelper.KEY_ID)));
                            Map<String, String> updatedNoteData = mi.fillSimilarIntervals(noteData);
                            boolean updatedSmaller = !updatedNoteData.get(soundSmallerField).equals(noteData.get(soundSmallerField));
                            boolean updatedLarger = !updatedNoteData.get(soundLargerField).equals(noteData.get(soundLargerField));
                            if (!updatedSmaller && !updatedLarger) {
                                continue;
                            }
                            if (updatedSmaller && updatedLarger) {
                                fixedLinksCount += 2;
                            } else {
                                fixedLinksCount++;
                            }
                            mAnkiDroid.updateNote(modelId, noteId, updatedNoteData);
                        }
                    }

                    StringBuilder report = new StringBuilder();
                    Resources res = getResources();
                    report.append(res.getString(R.string.integrity_check_completed));
                    if (invalidNotesData.size() > 0) {
                        report.append("\n\n");
                        if (invalidNotesData.size() == 1) {
                            report.append(res.getQuantityString(R.plurals.integrity_invalid, invalidNotesData.size()));
                        } else {
                            report.append(res.getQuantityString(R.plurals.integrity_invalid, invalidNotesData.size(), invalidNotesData.size()));
                        }
                        report.append("\n");
                        for (String field : MusInterval.Fields.SIGNATURE) {
                            final int invalidCount = invalidFieldsCount.getOrDefault(field, 0);
                            final int emptyCount = emptyFieldsCount.getOrDefault(field, 0);
                            final int totalCount = invalidCount + emptyCount;
                            if (totalCount > 0) {
                                report.append("\n");
                                report.append(String.format("%s: ", mi.modelFields.get(field)));
                                if (invalidCount > 0) {
                                    report.append(String.format(
                                            res.getString(R.string.integrity_invalid),
                                            invalidCount));
                                }
                                if (emptyCount > 0) {
                                    if (invalidCount > 0) {
                                        report.append(", ");
                                    }
                                    report.append(String.format(
                                            res.getString(R.string.integrity_empty),
                                            emptyCount));
                                }
                            }
                        }
                    }
                    if (susNotesData.size() > 0) {
                        report.append("\n\n");
                        report.append(res.getString(R.string.integrity_suspicious, susNotesData.size()));
                    }
                    if (invalidNotesData.size() == 0 && susNotesData.size() == 0) {
                        report.append("\n\n");
                        report.append(res.getString(R.string.integrity_ok));
                    }
                    if (fixedLinksCount > 0) {
                        report.append("\n\n");
                        report.append(getResources().getQuantityString(R.plurals.integrity_links, fixedLinksCount, fixedLinksCount));
                    }

                    new AlertDialog.Builder(MainActivity.this)
                            .setMessage(report.toString())
                            .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                }
                            })
                            .show();
                } catch (MusInterval.Exception e) {
                    processMusIntervalException(e);
                } catch (AnkiDroidHelper.InvalidAnkiDatabaseException e) {
                    processInvalidAnkiDatabase(e);
                }
            }
        });
    }

    private Long findModel() {
        return mAnkiDroid.findModelIdByName(MusInterval.Builder.DEFAULT_MODEL_NAME);
    }

    private boolean doesModelExist() {
        return findModel() != null;
    }

    private boolean doesModelHaveEnoughFields() {
        return mAnkiDroid.getFieldList(findModel()).length >= MusInterval.Fields.SIGNATURE.length;
    }

    private boolean doesModelHaveStoredFields() {
        final ArrayList<String> existingModelFields = new ArrayList<>(Arrays.asList(mAnkiDroid.getFieldList(findModel())));
        final String[] storedFields = new String[MusInterval.Fields.SIGNATURE.length];
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        for (int i = 0; i < MusInterval.Fields.SIGNATURE.length; i++) {
            storedFields[i] = sharedPreferences.getString(MusInterval.Fields.SIGNATURE[i], MusInterval.Fields.SIGNATURE[i]);
        }
        ArrayList<String> takenFields = new ArrayList<>();
        for (String field : storedFields) {
            if (!existingModelFields.contains(field) || takenFields.contains(field)) {
                return false;
            }
            takenFields.add(field);
        }
        return true;
    }

    private void validateModel() {
        Long modelId = findModel();
        if (modelId == null) {
            DialogFragment f = new CreateModelDialogFragment();
            f.show(getFragmentManager(), "createModelDialog");
        } else if (!doesModelHaveEnoughFields()) {
            showMsg(String.format(getResources().getString(R.string.invalid_model), MusInterval.Builder.DEFAULT_MODEL_NAME));
        } else if (!doesModelHaveStoredFields()) {
            DialogFragment f = new ConfigureModelDialogFragment();
            f.show(getFragmentManager(), "configureModelDialog");
        }
    }

    @Override
    protected void onPause() {
        final SharedPreferences uiDb = getSharedPreferences(STATE_REF_DB, Context.MODE_PRIVATE);
        uiDb.edit()
                .putString("inputFilename", inputFilename.getText().toString())
                .putString("inputStartNote", inputStartNote.getText().toString())
                .putInt("radioGroupDirection", radioGroupDirection.getCheckedRadioButtonId())
                .putInt("radioGroupTiming", radioGroupTiming.getCheckedRadioButtonId())
                .putInt("selectInterval", selectInterval.getSelectedItemPosition())
                .putString("inputTempo", Integer.toString(seekTempo.getProgress()))
                .putString("inputInstrument", inputInstrument.getText().toString())
                .putStringSet("savedStartNotes", savedStartNotes)
                .putStringSet("savedInstruments", savedInstruments)
                .apply();

        super.onPause();
    }

    protected void restoreUiState() {
        final SharedPreferences uiDb = getSharedPreferences(STATE_REF_DB, Context.MODE_PRIVATE);
        inputFilename.setText(uiDb.getString("inputFilename", ""));
        inputStartNote.setText(uiDb.getString("inputStartNote", ""));
        radioGroupDirection.check(uiDb.getInt("radioGroupDirection", findViewById(R.id.radioDirectionAny).getId()));
        radioGroupTiming.check(uiDb.getInt("radioGroupTiming", findViewById(R.id.radioTimingAny).getId()));
        selectInterval.setSelection(uiDb.getInt("selectInterval", 0));
        seekTempo.setProgress(Integer.parseInt(uiDb.getString("inputTempo", "0")));
        inputInstrument.setText(uiDb.getString("inputInstrument", ""));

        savedStartNotes = (HashSet<String>) uiDb.getStringSet("savedStartNotes", new HashSet<String>());
        inputStartNote.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, savedStartNotes.toArray(new String[0])));

        savedInstruments = (HashSet<String>) uiDb.getStringSet("savedInstruments", new HashSet<String>());
        inputInstrument.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, savedInstruments.toArray(new String[0])));

        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case AD_PERM_REQUEST_VALID:
                if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    showMsg(R.string.anki_permission_denied);
                } else if (!doesModelExist() || !doesModelHaveEnoughFields() || !doesModelHaveStoredFields()) {
                    validateModel();
                }
                break;
            case AD_PERM_REQUEST: {
                if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    showMsg(R.string.anki_permission_denied);
                }
            }
            case PERMISSIONS_REQUEST_EXTERNAL_STORAGE: {
                if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    showMsg(R.string.fs_permission_denied);
                }
            }
        }
    }

    private MusInterval getMusInterval() throws MusInterval.InvalidFieldsException {
        final String anyStr = getResources().getString(R.string.radio_any);

        final int radioDirectionId = radioGroupDirection.getCheckedRadioButtonId();
        final RadioButton radioDirection = findViewById(radioDirectionId);
        final String directionStr = radioDirectionId != -1  && radioDirection != null ?
                radioDirection.getText().toString() : anyStr;

        final int radioTimingId = radioGroupTiming.getCheckedRadioButtonId();
        final RadioButton radioTiming = findViewById(radioTimingId);
        final String timingStr = radioTimingId != -1 && radioTiming != null ?
                radioTiming.getText().toString() : anyStr;

        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        final Map<String, String> storedFields = new HashMap<>();
        for (String field : MusInterval.Fields.SIGNATURE) {
            storedFields.put(field, sharedPreferences.getString(field, field));
        }

        return new MusInterval.Builder(mAnkiDroid)
                .model_fields(storedFields)
                .sound(inputFilename.getText().toString())
                .start_note(inputStartNote.getText().toString())
                .direction(!directionStr.equals(anyStr) ? directionStr : "")
                .timing(!timingStr.equals(anyStr) ? timingStr : "")
                .interval(selectInterval.getSelectedItem().toString())
                .tempo(seekTempo.getProgress() > 0 ? Integer.toString(seekTempo.getProgress()) : "")
                .instrument(inputInstrument.getText().toString())
                .build();
    }

    private void processMusIntervalException(MusInterval.Exception miException) {
        try {
            throw miException;
        } catch (MusInterval.NoteNotExistsException e) {
            showMsg(R.string.mi_not_exists);
        } catch (MusInterval.MandatoryFieldsEmptyException e) {
            showMsg(getFieldsValidationMessage(e.getFields(), R.plurals.empty_mandatory_fields));
        } catch (MusInterval.InvalidFieldsException e) {
            showMsg(getFieldsValidationMessage(e.getFields(), R.plurals.invalid_fields));
        } catch (MusInterval.CreateDeckException e) {
            showMsg(R.string.create_deck_error);
        } catch (MusInterval.AddToAnkiException e) {
            showMsg(R.string.add_card_error);
        } catch (MusInterval.SoundAlreadyAddedException e) {
            showMsg(R.string.already_added);
        } catch (MusInterval.AddSoundFileException e) {
            showMsg(R.string.add_file_error);
        } catch (MusInterval.Exception e) {
            showMsg(R.string.unknown_adding_error);
        }
    }

    private String getFieldsValidationMessage(LinkedList<String> fields, int msgId) {
        StringBuilder labelsJoined = new StringBuilder(fieldLabels.get(fields.getFirst()));
        int nFields = fields.size();
        for (int i = 1; i < nFields; i++) {
            labelsJoined.append(String.format(", %s", fieldLabels.get(fields.get(i))));
        }
        return getResources().getQuantityString(msgId, nFields, labelsJoined.toString());
    }

    private void processInvalidAnkiDatabase(AnkiDroidHelper.InvalidAnkiDatabaseException invalidAnkiDatabaseException) {
        try {
            throw invalidAnkiDatabaseException;
        } catch (AnkiDroidHelper.InvalidAnkiDatabase_fieldAndFieldNameCountMismatchException e) {
            showMsg(R.string.InvalidAnkiDatabase_unknownError);
        } catch (AnkiDroidHelper.InvalidAnkiDatabaseException e) {
            showMsg(R.string.InvalidAnkiDatabase_fieldAndFieldNameCountMismatch);
        }
    }

    private void showMsg(int msgResId) {
        Toast.makeText(MainActivity.this, getResources().getString(msgResId), Toast.LENGTH_LONG).show();
    }

    private void showMsg(final String message) {
        Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
    }

    public static class CreateModelDialogFragment extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final MainActivity mainActivity = (MainActivity) getActivity();
            return new AlertDialog.Builder(mainActivity)
                    .setMessage(String.format(
                            getResources().getString(R.string.create_model),
                            MusInterval.Builder.DEFAULT_MODEL_NAME))
                    .setPositiveButton(R.string.create, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            mainActivity.handleCreateModel();
                        }
                    })
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    })
                    .create();
        }
    }

    public static class ConfigureModelDialogFragment extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final MainActivity mainActivity = (MainActivity) getActivity();
            return new AlertDialog.Builder(mainActivity)
                    .setMessage(String.format(
                            getResources().getString(R.string.configure_model),
                            MusInterval.Builder.DEFAULT_MODEL_NAME))
                    .setPositiveButton(R.string.configure, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            mainActivity.openSettings();
                        }
                    })
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    })
                    .create();
        }
    }

    private void handleCreateModel() {
        String modelName = MusInterval.Builder.DEFAULT_MODEL_NAME;
        final Long newModelId = mAnkiDroid.addNewCustomModel(
                modelName,
                MusInterval.Fields.SIGNATURE,
                MusInterval.Builder.CARD_NAMES,
                MusInterval.Builder.QFMT,
                MusInterval.Builder.AFMT,
                MusInterval.Builder.CSS);
        if (newModelId != null) {
            showMsg(String.format(
                    getResources().getString(R.string.create_model_success),
                    modelName));
        } else {
            showMsg(R.string.create_model_error);
        }
    }

}
