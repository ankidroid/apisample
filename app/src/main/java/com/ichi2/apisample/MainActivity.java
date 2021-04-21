package com.ichi2.apisample;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public class MainActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback {

    private static final int AD_PERM_REQUEST = 0;
    private static final int PERMISSIONS_REQUEST_EXTERNAL_STORAGE = 1;

    private static final int ACTION_SELECT_FILE = 10;

    private static final String STATE_REF_DB = "com.ichi2.apisample.uistate";
    private static final String KEY_BATCH_ADDING_NOTICE_SEEN = "batchAddingNoticeSeen";

    private final Map<String, String> fieldLabels = new HashMap<>();
    private final Map<String, String> intervalLabels = new HashMap<>();

    private TextView textFilename;
    private Button actionSelectFile;
    private CheckBox[] checkNotes;
    private CheckBox[] checkOctaves;
    private RadioGroup radioGroupDirection;
    private RadioGroup radioGroupTiming;
    private CheckBox[] checkIntervals;
    private SeekBar seekTempo;
    private AutoCompleteTextView inputInstrument;
    private TextView labelExisting;
    private Button actionMarkExisting;

    private final int[] checkNoteIds = new int[]{
            R.id.checkNoteC, R.id.checkNoteCSharp,
            R.id.checkNoteD, R.id.checkNoteDSharp,
            R.id.checkNoteE,
            R.id.checkNoteF, R.id.checkNoteFSharp,
            R.id.checkNoteG, R.id.checkNoteGSharp,
            R.id.checkNoteA, R.id.checkNoteASharp,
            R.id.checkNoteB
    };
    private final int[] checkOctaveIds = new int[]{
            R.id.checkOctave1,
            R.id.checkOctave2,
            R.id.checkOctave3,
            R.id.checkOctave4,
            R.id.checkOctave5,
            R.id.checkOctave6
    };
    private final int[] checkIntervalIds = new int[]{
            R.id.checkIntervalP1,
            R.id.checkIntervalm2,
            R.id.checkIntervalM2,
            R.id.checkIntervalm3,
            R.id.checkIntervalM3,
            R.id.checkIntervalP4,
            R.id.checkIntervalTT,
            R.id.checkIntervalP5,
            R.id.checkIntervalm6,
            R.id.checkIntervalM6,
            R.id.checkIntervalm7,
            R.id.checkIntervalM7,
            R.id.checkIntervalP8
    };

    private String[] filenames = new String[]{};
    private Integer permutationsNumber;

    private HashSet<String> savedInstruments = new HashSet<>();

    private AnkiDroidHelper mAnkiDroid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        fieldLabels.put(MusInterval.Fields.DIRECTION, getResources().getString(R.string.direction));
        fieldLabels.put(MusInterval.Fields.TIMING, getResources().getString(R.string.timing));
        fieldLabels.put(MusInterval.Fields.INTERVAL, getResources().getString(R.string.interval));
        fieldLabels.put(MusInterval.Fields.TEMPO, getResources().getString(R.string.tempo));
        fieldLabels.put(MusInterval.Fields.INSTRUMENT, getResources().getString(R.string.instrument));

        final String[] intervalLabels = new String[]{
                getString(R.string.interval_P1),
                getString(R.string.interval_m2),
                getString(R.string.interval_M2),
                getString(R.string.interval_m3),
                getString(R.string.interval_M3),
                getString(R.string.interval_P4),
                getString(R.string.interval_TT),
                getString(R.string.interval_P5),
                getString(R.string.interval_m6),
                getString(R.string.interval_M6),
                getString(R.string.interval_m7),
                getString(R.string.interval_M7),
                getString(R.string.interval_P8)
        };
        if (intervalLabels.length != MusInterval.Fields.Interval.VALUES.length) {
            throw new AssertionError();
        }
        for (int i = 0; i < MusInterval.Fields.Interval.VALUES.length; i++) {
            this.intervalLabels.put(intervalLabels[i], MusInterval.Fields.Interval.VALUES[i]);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar main_toolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(main_toolbar);

        textFilename = findViewById(R.id.textFilename);
        actionSelectFile = findViewById(R.id.actionSelectFile);
        TextView labelNote = findViewById(R.id.labelNote);
        checkNotes = new CheckBox[checkNoteIds.length];
        for (int i = 0; i < checkNoteIds.length; i++) {
            checkNotes[i] = findViewById(checkNoteIds[i]);
        }
        TextView labelOctave = findViewById(R.id.labelOctave);
        checkOctaves = new CheckBox[checkOctaveIds.length];
        for (int i = 0; i < checkOctaveIds.length; i++) {
            checkOctaves[i] = findViewById(checkOctaveIds[i]);
        }
        radioGroupDirection = findViewById(R.id.radioGroupDirection);
        radioGroupTiming = findViewById(R.id.radioGroupTiming);
        TextView labelInterval = findViewById(R.id.labelInterval);
        checkIntervals = new CheckBox[checkIntervalIds.length];
        for (int i = 0; i < checkIntervalIds.length; i++) {
            checkIntervals[i] = findViewById(checkIntervalIds[i]);
        }
        seekTempo = findViewById(R.id.seekTempo);
        inputInstrument = findViewById(R.id.inputInstrument);
        labelExisting = findViewById(R.id.labelExisting);
        actionMarkExisting = findViewById(R.id.actionMarkExisting);

        restoreUiState();

        textFilename.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (filenames.length > 1) {
                    StringBuilder msg = new StringBuilder();
                    final String[][] orderedPermutationKeys = getOrderedPermutationKeys();
                    for (int i = 0; i < filenames.length; i++) {
                        if (msg.length() > 0) {
                            msg.append(getString(R.string.filenames_list_separator));
                        }
                        if (i < orderedPermutationKeys.length) {
                            final String startNote = orderedPermutationKeys[i][1] + orderedPermutationKeys[i][0];
                            final String interval = orderedPermutationKeys[i][2];
                            msg.append(getString(R.string.filenames_list_item_with_key, i + 1, filenames[i], startNote, interval));
                        } else {
                            msg.append(getString(R.string.filenames_list_item, i + 1, filenames[i]));
                        }
                    }
                    new AlertDialog.Builder(MainActivity.this)
                            .setMessage(msg)
                            .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                }
                            })
                            .show();
                }
            }
        });
        labelNote.setOnLongClickListener(new OnFieldCheckLabelLongClickListener(checkNotes));
        for (CheckBox checkNote : checkNotes) {
            checkNote.setOnCheckedChangeListener(new OnFieldCheckChangeListener());
        }
        labelOctave.setOnLongClickListener(new OnFieldCheckLabelLongClickListener(checkOctaves));
        for (CheckBox checkOctave : checkOctaves) {
            checkOctave.setOnCheckedChangeListener(new OnFieldCheckChangeListener());
        }
        radioGroupDirection.setOnCheckedChangeListener(new OnFieldRadioChangeListener());
        radioGroupTiming.setOnCheckedChangeListener(new OnFieldRadioChangeListener());
        labelInterval.setOnLongClickListener(new OnFieldCheckLabelLongClickListener(checkIntervals));
        for (CheckBox checkInterval : checkIntervals) {
            checkInterval.setOnCheckedChangeListener(new OnFieldCheckChangeListener());
        }
        seekTempo.setOnSeekBarChangeListener(new OnFieldSeekChangeListener());
        inputInstrument.addTextChangedListener(new FieldInputTextWatcher());

        configureTempoButtons();
        configureClearAllButton();
        configureSelectFileButton();
        configureMarkExistingButton();
        configureAddToAnkiButton();
        configureSettingsButton();

        mAnkiDroid = new AnkiDroidHelper(this);
    }

    private String[][] getOrderedPermutationKeys() {
        final String[] selectedNotes = getCheckedTexts(checkNotes);
        final String[] selectedOctaves = getCheckedTexts(checkOctaves);
        final String[] selectedIntervals = getCheckedTexts(checkIntervals);
        ArrayList<String[]> orderedPermutationKeys = new ArrayList<>();
        for (String octave : selectedOctaves) {
            for (String note : selectedNotes) {
                for (String interval : selectedIntervals) {
                    orderedPermutationKeys.add(new String[]{octave, note, interval});
                }
            }
        }
        return orderedPermutationKeys.toArray(new String[0][]);
    }

    private String[] getCheckedTexts(CheckBox[] checkBoxes) {
        ArrayList<String> texts = new ArrayList<>();
        for (CheckBox check : checkBoxes) {
            if (check.isChecked()) {
                texts.add(check.getText().toString());
            }
        }
        return texts.toArray(new String[0]);
    }

    private void clearAddedFilenames() {
        ArrayList<String> unAddedFilenames = new ArrayList<>();
        for (String filename : filenames) {
            if (!filename.startsWith("[sound:")) {
                unAddedFilenames.add(filename);
            }
        }
        filenames = unAddedFilenames.toArray(new String[0]);
        refreshFilenameText();
    }

    private void refreshFilenameText() {
        StringBuilder text = new StringBuilder();
        if (filenames.length > 0) {
            text.append(filenames[0]);
            if (filenames.length > 1) {
                text.append(getString(R.string.additional_filenames, filenames.length - 1));
            }
        }
        textFilename.setText(text);
    }

    private void refreshPermutations() {
        if (mAnkiDroid == null) {
            return;
        }
        int permutationsNumber = 1;
        try {
            if (mAnkiDroid.shouldRequestPermission()) {
                mAnkiDroid.requestPermission(MainActivity.this, AD_PERM_REQUEST);
                return;
            }
            permutationsNumber = getMusInterval().getPermutationsNumber();

        } catch (MusInterval.ValidationException e) {
        } finally {
            Resources res = getResources();
            String selectFileText;
            if (permutationsNumber == 1) {
                selectFileText = res.getQuantityString(R.plurals.select_file, permutationsNumber);
            } else {
                selectFileText = res.getQuantityString(R.plurals.select_file, permutationsNumber, permutationsNumber);
            }
            actionSelectFile.setText(selectFileText);
            this.permutationsNumber = permutationsNumber;
        }
    }

    private static class OnFieldCheckLabelLongClickListener implements View.OnLongClickListener {
        private final CheckBox[] checks;

        public OnFieldCheckLabelLongClickListener(CheckBox[] checks) {
            super();
            this.checks = checks;
        }

        @Override
        public boolean onLongClick(View view) {
            boolean allChecked = true;
            for (CheckBox check : checks) {
                if (!check.isChecked()) {
                    allChecked = false;
                    break;
                }
            }
            final boolean value = !allChecked;
            for (CheckBox check : checks) {
                check.setChecked(value);
            }
            return true;
        }
    }

    private class OnFieldCheckChangeListener implements CompoundButton.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
            clearAddedFilenames();
            refreshExisting();
            refreshPermutations();
        }
    }

    private class OnFieldRadioChangeListener implements RadioGroup.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(RadioGroup radioGroup, int i) {
            clearAddedFilenames();
            refreshExisting();
        }
    }

    private class OnFieldSeekChangeListener implements SeekBar.OnSeekBarChangeListener {
        @SuppressLint("SetTextI18n")
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            TextView label = findViewById(R.id.labelTempoValue);
            label.setText(Integer.toString(seekBar.getProgress()));
            clearAddedFilenames();
            refreshExisting();
        }

        @Override public void onStartTrackingTouch(SeekBar seekBar) { }
        @Override public void onStopTrackingTouch(SeekBar seekBar) { }
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshExisting();
        refreshPermutations();
        refreshFilenameText();
    }

    private void refreshExisting() {
        if (mAnkiDroid == null) {
            return;
        }
        String textExisting = "";
        int existingCount = 0;
        int markedCount = 0;
        try {
            if (mAnkiDroid.shouldRequestPermission()) {
                mAnkiDroid.requestPermission(MainActivity.this, AD_PERM_REQUEST);
                return;
            }
            MusInterval mi = getMusInterval();
            existingCount = mi.getExistingNotesCount();
            markedCount = mi.getExistingMarkedNotesCount();
            Resources res = getResources();
            String textFound;
            String textMarked;
            if (existingCount == 1) {
                textFound = res.getQuantityString(R.plurals.mi_found, existingCount);
                if (markedCount == 1) {
                    textMarked = res.getString(R.string.mi_found_one_marked);
                } else {
                    textMarked = res.getString(R.string.mi_found_one_unmarked);
                }
            } else {
                textFound = res.getQuantityString(R.plurals.mi_found, existingCount, existingCount);
                if (markedCount == 1) {
                    textMarked = res.getQuantityString(R.plurals.mi_found_other_marked, markedCount);
                } else {
                    textMarked = res.getQuantityString(R.plurals.mi_found_other_marked, markedCount, markedCount);
                }
            }
            textExisting = existingCount == 0 ?
                    textFound :
                    textFound + textMarked;
        } catch (MusInterval.ValidationException | AnkiDroidHelper.InvalidAnkiDatabaseException e) {
            textExisting = ""; // might wanna set some error message here
        } finally {
            labelExisting.setText(textExisting);
            final int unmarkedCount = existingCount - markedCount;
            actionMarkExisting.setText(getString(R.string.action_mark, unmarkedCount));
            actionMarkExisting.setEnabled(unmarkedCount > 0);
        }
    }

    private class FieldInputTextWatcher implements TextWatcher {
        private String prev;

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            prev = charSequence.toString();
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            String curr = charSequence.toString();
            if (!curr.equalsIgnoreCase(prev)) {
                clearAddedFilenames();
                refreshExisting();
            }
        }

        @Override
        public void afterTextChanged(Editable editable) { }
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
                filenames = new String[]{};
                textFilename.setText("");
                for (CheckBox checkNote : checkNotes) {
                    checkNote.setChecked(false);
                }
                for (CheckBox checkOctave : checkOctaves) {
                    checkOctave.setChecked(false);
                }
                radioGroupDirection.check(findViewById(R.id.radioDirectionAny).getId());
                radioGroupTiming.check(findViewById(R.id.radioTimingAny).getId());
                for (CheckBox checkInterval : checkIntervals) {
                    checkInterval.setChecked(false);
                }
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
                requestPermissions(new String[]{
                                android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        PERMISSIONS_REQUEST_EXTERNAL_STORAGE
                );

                if (permutationsNumber == null || permutationsNumber <= 1) {
                    openChooser();
                    return;
                }

                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                boolean batchNoticeSeen = preferences.getBoolean(KEY_BATCH_ADDING_NOTICE_SEEN, false);
                if (batchNoticeSeen) {
                    openChooser();
                    return;
                }

                ViewGroup viewGroup = findViewById(R.id.content);
                View dialogView = LayoutInflater.from(MainActivity.this).inflate(R.layout.dialog_notice, viewGroup, false);
                TextView textNotice = dialogView.findViewById(R.id.textNotice);
                final CheckBox checkRemember = dialogView.findViewById(R.id.checkRemember);
                textNotice.setText(getResources().getString(R.string.batch_adding_notice));
                new AlertDialog.Builder(MainActivity.this)
                        .setView(dialogView)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        }).setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        if (checkRemember.isChecked()) {
                            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(MainActivity.this).edit();
                            editor.putBoolean(KEY_BATCH_ADDING_NOTICE_SEEN, true);
                            editor.apply();
                        }
                        openChooser();
                    }
                }).show();
            }
        });
    }

    private void openChooser() {
        Intent intent = new Intent()
                .setAction(Intent.ACTION_GET_CONTENT)
                .setType("audio/*")
                .addCategory(Intent.CATEGORY_OPENABLE)
                .putExtra(Intent.EXTRA_LOCAL_ONLY, true)
                .putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);

        startActivityForResult(Intent.createChooser(intent, actionSelectFile.getText().toString()),
                ACTION_SELECT_FILE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ACTION_SELECT_FILE && resultCode == RESULT_OK) {
            ArrayList<String> filenamesList = new ArrayList<>();
            if (data != null) {
                ClipData clipData = data.getClipData();
                if (clipData != null) {
                    for (int i = 0; i < clipData.getItemCount(); i++) {
                        filenamesList.add(clipData.getItemAt(i).getUri().toString());
                    }
                } else {
                    filenamesList.add(data.getData().toString());
                }
            }
            filenames = filenamesList.toArray(new String[0]);
            refreshFilenameText();
        }
    }

    private void configureMarkExistingButton() {
        actionMarkExisting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mAnkiDroid.shouldRequestPermission()) {
                    mAnkiDroid.requestPermission(MainActivity.this, AD_PERM_REQUEST);
                    return;
                }
                try {
                    final int count = getMusInterval().markExistingNotes();
                    showQuantityMsg(R.plurals.mi_marked_result, count, count);
                    refreshExisting();
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
                try {
                    MusInterval mi = getMusInterval();
                    final int nMis = mi.getPermutationsNumber();
                    MusInterval newMi = mi.addToAnki();
                    filenames = newMi.sounds;
                    refreshFilenameText();
                    savedInstruments.add(newMi.instrument);
                    refreshExisting();
                    String msg;
                    if (nMis == 1) {
                        showQuantityMsg(R.plurals.mi_added, nMis);
                    } else {
                        showQuantityMsg(R.plurals.mi_added, nMis, nMis);
                    }
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
                openSettings();
            }
        });
    }

    private void openSettings() {
        startActivity(new Intent(this, SettingsActivity.class));
    }

    @Override
    protected void onPause() {
        final SharedPreferences.Editor uiDbEditor = getSharedPreferences(STATE_REF_DB, Context.MODE_PRIVATE).edit();

        uiDbEditor.putStringSet("selectedFilenames", new HashSet<>(Arrays.asList(filenames)));
        for (int i = 0; i < checkNoteIds.length; i++) {
            uiDbEditor.putBoolean(String.valueOf(checkNoteIds[i]), checkNotes[i].isChecked());
        }
        for (int i = 0; i < checkOctaveIds.length; i++) {
            uiDbEditor.putBoolean(String.valueOf(checkOctaveIds[i]), checkOctaves[i].isChecked());
        }
        uiDbEditor.putInt("radioGroupDirection", radioGroupDirection.getCheckedRadioButtonId());
        uiDbEditor.putInt("radioGroupTiming", radioGroupTiming.getCheckedRadioButtonId());
        for (int i = 0; i < checkIntervalIds.length; i++) {
            uiDbEditor.putBoolean(String.valueOf(checkIntervalIds[i]), checkIntervals[i].isChecked());
        }
        uiDbEditor.putString("inputTempo", Integer.toString(seekTempo.getProgress()));
        uiDbEditor.putString("inputInstrument", inputInstrument.getText().toString());
        uiDbEditor.putStringSet("savedInstruments", savedInstruments);
        uiDbEditor.apply();

        super.onPause();
    }

    protected void restoreUiState() {
        final SharedPreferences uiDb = getSharedPreferences(STATE_REF_DB, Context.MODE_PRIVATE);
        Set<String> storedFilenames = uiDb.getStringSet("selectedFilenames", new HashSet<String>());
        filenames = storedFilenames.toArray(new String[0]);
        refreshFilenameText();
        for (int i = 0; i < checkNoteIds.length; i++) {
            checkNotes[i].setChecked(uiDb.getBoolean(String.valueOf(checkNoteIds[i]), false));
        }
        for (int i = 0; i < checkOctaveIds.length; i++) {
            checkOctaves[i].setChecked(uiDb.getBoolean(String.valueOf(checkOctaveIds[i]), false));
        }
        radioGroupDirection.check(uiDb.getInt("radioGroupDirection", findViewById(R.id.radioDirectionAny).getId()));
        radioGroupTiming.check(uiDb.getInt("radioGroupTiming", findViewById(R.id.radioTimingAny).getId()));
        for (int i = 0; i < checkIntervalIds.length; i++) {
            checkIntervals[i].setChecked(uiDb.getBoolean(String.valueOf(checkIntervalIds[i]), false));
        }
        seekTempo.setProgress(Integer.parseInt(uiDb.getString("inputTempo", "0")));
        inputInstrument.setText(uiDb.getString("inputInstrument", ""));

        savedInstruments = (HashSet<String>) uiDb.getStringSet("savedInstruments", new HashSet<String>());
        inputInstrument.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, savedInstruments.toArray(new String[0])));

        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        refreshExisting();
        refreshPermutations();
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case AD_PERM_REQUEST: {
                if (grantResults.length > 0) {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        refreshExisting();
                        refreshPermutations();
                    } else {
                        showMsg(R.string.anki_permission_denied);
                    }
                }
            }
            case PERMISSIONS_REQUEST_EXTERNAL_STORAGE: {
                if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    showMsg(R.string.fs_permission_denied);
                }
            }
        }
    }

    private MusInterval getMusInterval() throws MusInterval.ValidationException {
        final String anyStr = getResources().getString(R.string.radio_any);

        final int radioDirectionId = radioGroupDirection.getCheckedRadioButtonId();
        final View radioDirection = findViewById(radioDirectionId);
        final String directionStr =
                radioDirection instanceof RadioButton && radioDirectionId != -1 ?
                        ((RadioButton) radioDirection).getText().toString() :
                        anyStr;
        final int radioTimingId = radioGroupTiming.getCheckedRadioButtonId();
        final View radioTiming = findViewById(radioTimingId);
        final String timingStr =
                radioTiming instanceof RadioButton && radioTimingId != -1 ?
                        ((RadioButton) radioTiming).getText().toString() :
                        anyStr;

        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        final boolean versionField = sharedPreferences.getBoolean(SettingsFragment.KEY_VERSION_FIELD_SWITCH, SettingsFragment.DEFAULT_VERSION_FIELD_SWITCH);
        String[] signature = MusInterval.Fields.getSignature(versionField);
        final Map<String, String> storedFields = new HashMap<>();
        for (String fieldKey : signature) {
            String fieldPreferenceKey = SettingsFragment.getFieldPreferenceKey(fieldKey);
            storedFields.put(fieldKey, sharedPreferences.getString(fieldPreferenceKey, ""));
        }
        final String storedDeck = sharedPreferences.getString(SettingsFragment.KEY_DECK_PREFERENCE, MusInterval.Builder.DEFAULT_DECK_NAME);
        final String storedModel = sharedPreferences.getString(SettingsFragment.KEY_MODEL_PREFERENCE, MusInterval.Builder.DEFAULT_MODEL_NAME);

        MusInterval.Builder builder = new MusInterval.Builder(mAnkiDroid)
                .deck(storedDeck)
                .model(storedModel)
                .model_fields(storedFields)
                .sounds(filenames)
                .notes(getCheckedValues(checkNotes))
                .octaves(getCheckedValues(checkOctaves))
                .direction(!directionStr.equals(anyStr) ? directionStr : "")
                .timing(!timingStr.equals(anyStr) ? timingStr : "")
                .intervals(getCheckedValues(checkIntervals, intervalLabels))
                .tempo(seekTempo.getProgress() > 0 ? Integer.toString(seekTempo.getProgress()) : "")
                .instrument(inputInstrument.getText().toString());
        if (versionField) {
            builder.version(BuildConfig.VERSION_NAME);
        }
        return builder.build();
    }

    private static String[] getCheckedValues(CheckBox[] checkBoxes) {
        return getCheckedValues(checkBoxes, null);
    }

    private static String[] getCheckedValues(CheckBox[] checkBoxes, Map<String, String> valueLabels) {
        ArrayList<String> valuesList = new ArrayList<>();
        for (CheckBox checkBox : checkBoxes) {
            if (checkBox.isChecked()) {
                String value = checkBox.getText().toString();
                if (valueLabels != null) {
                    value = valueLabels.get(value);
                }
                valuesList.add(value);
            }
        }
        return valuesList.toArray(new String[0]);
    }

    private void processMusIntervalException(MusInterval.Exception miException) {
        try {
            throw miException;
        } catch (MusInterval.NoteNotSelectedException e) {
            showMsg(R.string.note_not_selected);
        } catch (MusInterval.OctaveNotSelectedException e) {
            showMsg(R.string.octave_not_selected);
        } catch (MusInterval.IntervalNotSelectedException e) {
            showMsg(R.string.interval_not_selected);
        } catch (MusInterval.UnexpectedSoundsAmountException e) {
            final int expected = e.getExpectedAmount();
            final int provided = e.getProvidedAmount();
            final boolean expectedSingle = expected == 1;
            Resources res = getResources();
            String msg;
            if (provided == 0) {
                if (expectedSingle) {
                    showQuantityMsg(R.plurals.sound_not_provided, expected);
                } else {
                    showQuantityMsg(R.plurals.sound_not_provided, expected, expected);
                }
            } else {
                if (expectedSingle) {
                    showQuantityMsg(R.plurals.unexpected_sounds_amount, expected, provided);
                } else {
                    showQuantityMsg(R.plurals.unexpected_sounds_amount, expected, expected, provided);
                }
            }
        } catch (MusInterval.ModelDoesNotExistException e) {
            final String modelName = e.getModelName();
            new AlertDialog.Builder(this)
                    .setMessage(String.format(
                            getResources().getString(R.string.create_model),
                            modelName))
                    .setPositiveButton(R.string.create, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            handleCreateModel(modelName);
                        }
                    })
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    })
                    .show();
        } catch (MusInterval.NotEnoughFieldsException e) {
            showMsg(R.string.invalid_model, e.getModelName());
        } catch (MusInterval.ModelNotConfiguredException e) {
            final String modelName = e.getModelName();
            new AlertDialog.Builder(this)
                    .setMessage(String.format(
                            getResources().getString(R.string.configure_model), modelName))
                    .setPositiveButton(R.string.configure, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            openSettings();
                        }
                    })
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    })
                    .show();
        } catch (MusInterval.NoteNotExistsException e) {
            showMsg(R.string.mi_not_exists);
        } catch (MusInterval.CreateDeckException e) {
            showMsg(R.string.create_deck_error);
        } catch (MusInterval.AddToAnkiException e) {
            showMsg(R.string.add_card_error);
        } catch (MusInterval.MandatoryFieldEmptyException e) {
            showMsg(R.string.mandatory_field_empty, fieldLabels.get(e.getField()));
        } catch (MusInterval.SoundAlreadyAddedException e) {
            showMsg(R.string.already_added);
        } catch (MusInterval.AddSoundFileException e) {
            showMsg(R.string.add_file_error);
        } catch (MusInterval.Exception e) {
            showMsg(R.string.unknown_adding_error);
        }
    }

    private void handleCreateModel(String modelName) {
        final String[] signature = MusInterval.Fields.getSignature(SettingsFragment.DEFAULT_VERSION_FIELD_SWITCH);
        final Long newModelId = mAnkiDroid.addNewCustomModel(
                modelName,
                signature,
                MusInterval.Builder.CARD_NAMES,
                MusInterval.Builder.QFMT,
                MusInterval.Builder.AFMT,
                MusInterval.Builder.CSS
        );
        if (newModelId != null) {
            SharedPreferences.Editor preferenceEditor = PreferenceManager.getDefaultSharedPreferences(MainActivity.this).edit();
            for (String fieldKey : signature) {
                String fieldPreferenceKey = SettingsFragment.getFieldPreferenceKey(fieldKey);
                preferenceEditor.putString(fieldPreferenceKey, fieldKey);
                String modelFieldPreferenceKey = SettingsFragment.getModelFieldPreferenceKey(newModelId, fieldPreferenceKey);
                preferenceEditor.putString(modelFieldPreferenceKey, fieldKey);
            }
            preferenceEditor.apply();
            showMsg(R.string.create_model_success, modelName);
        } else {
            showMsg(R.string.create_model_error);
        }
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

    private void showMsg(int msgResId, Object ...formatArgs) {
        Toast.makeText(MainActivity.this, getResources().getString(msgResId, formatArgs), Toast.LENGTH_LONG).show();
    }

    private void showQuantityMsg(int msgResId, int quantity, Object ...formatArgs) {
        Toast.makeText(MainActivity.this, getResources().getQuantityString(msgResId, quantity, formatArgs), Toast.LENGTH_LONG).show();
    }
}
