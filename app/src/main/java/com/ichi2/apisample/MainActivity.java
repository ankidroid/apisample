package com.ichi2.apisample;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
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
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public class MainActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback, DuplicateAddingPrompter {

    private static final int AD_PERM_REQUEST = 0;
    private static final int PERMISSIONS_REQUEST_EXTERNAL_STORAGE = 1;

    private static final int ACTION_SELECT_FILE = 10;

    private static final String STATE_REF_DB = "com.ichi2.apisample.uistate";
    private static final String KEY_BATCH_ADDING_NOTICE_SEEN = "batchAddingNoticeSeen";

    private final Map<String, Integer> singularFieldLabelStringIds = new HashMap<String, Integer>() {{
        put(MusInterval.Fields.DIRECTION, R.string.direction);
        put(MusInterval.Fields.TIMING, R.string.timing);
        put(MusInterval.Fields.TEMPO, R.string.tempo);
        put(MusInterval.Fields.INSTRUMENT, R.string.instrument);
    }};
    {
        if (!singularFieldLabelStringIds.keySet().equals(MusInterval.Builder.ADDING_MANDATORY_SINGULAR_KEYS)) {
            throw new AssertionError();
        }
    }

    private final Map<String, Integer> selectionFieldLabelStringIds = new HashMap<String, Integer>() {{
        put(MusInterval.Builder.KEY_NOTES, R.string.start_note);
        put(MusInterval.Builder.KEY_OCTAVES, R.string.octave);
        put(MusInterval.Builder.KEY_INTERVALS, R.string.interval);
    }};
    {
        if (!selectionFieldLabelStringIds.keySet().equals(MusInterval.Builder.ADDING_MANDATORY_SELECTION_KEYS)) {
            throw new AssertionError();
        }
    }

    private SwitchCompat switchBatch;
    private TextView textFilename;
    private Button actionSelectFile;
    private CheckBox checkNoteAny;
    private CheckBox[] checkNotes;
    private CheckBox checkOctaveAny;
    private CheckBox[] checkOctaves;
    private RadioGroup radioGroupDirection;
    private RadioGroup radioGroupTiming;
    private CheckBox checkIntervalAny;
    private CheckBox[] checkIntervals;
    private EditText inputTempo;
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

    private final Map<Integer, String> checkIntervalIdValues = new HashMap<>();
    {
        if (checkIntervalIds.length != MusInterval.Fields.Interval.VALUES.length) {
            throw new AssertionError();
        }
        for (int i = 0; i < MusInterval.Fields.Interval.VALUES.length; i++) {
            checkIntervalIdValues.put(checkIntervalIds[i], MusInterval.Fields.Interval.VALUES[i]);
        }
    }

    private String[] filenames = new String[]{};
    private Integer permutationsNumber;

    private HashSet<String> savedInstruments = new HashSet<>();

    private AnkiDroidHelper mAnkiDroid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar main_toolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(main_toolbar);

        switchBatch = findViewById(R.id.switchBatch);
        textFilename = findViewById(R.id.textFilename);
        actionSelectFile = findViewById(R.id.actionSelectFile);
        checkNoteAny = findViewById(R.id.checkNoteAny);
        checkNotes = new CheckBox[checkNoteIds.length];
        for (int i = 0; i < checkNoteIds.length; i++) {
            checkNotes[i] = findViewById(checkNoteIds[i]);
        }
        checkOctaveAny = findViewById(R.id.checkOctaveAny);
        checkOctaves = new CheckBox[checkOctaveIds.length];
        for (int i = 0; i < checkOctaveIds.length; i++) {
            checkOctaves[i] = findViewById(checkOctaveIds[i]);
        }
        radioGroupDirection = findViewById(R.id.radioGroupDirection);
        radioGroupTiming = findViewById(R.id.radioGroupTiming);
        checkIntervalAny = findViewById(R.id.checkIntervalAny);
        checkIntervals = new CheckBox[checkIntervalIds.length];
        for (int i = 0; i < checkIntervalIds.length; i++) {
            checkIntervals[i] = findViewById(checkIntervalIds[i]);
        }
        inputTempo = findViewById(R.id.inputTempo);
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
                        // @todo: update filename field values signature
                        if (i < orderedPermutationKeys.length && !filenames[i].startsWith("[sound:")) {
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
        boolean enableMultiple = switchBatch.isChecked();
        final OnFieldCheckChangeListener onNoteCheckChangeListener = new OnFieldCheckChangeListener(checkNotes, checkNoteAny, enableMultiple);
        checkNoteAny.setOnCheckedChangeListener(onNoteCheckChangeListener);
        for (CheckBox checkNote : checkNotes) {
            checkNote.setOnCheckedChangeListener(onNoteCheckChangeListener);
        }
        final OnFieldCheckChangeListener onOctaveCheckChangeListener = new OnFieldCheckChangeListener(checkOctaves, checkOctaveAny, enableMultiple);
        checkOctaveAny.setOnCheckedChangeListener(onOctaveCheckChangeListener);
        for (CheckBox checkOctave : checkOctaves) {
            checkOctave.setOnCheckedChangeListener(onOctaveCheckChangeListener);
        }
        radioGroupDirection.setOnCheckedChangeListener(new OnFieldRadioChangeListener());
        radioGroupTiming.setOnCheckedChangeListener(new OnFieldRadioChangeListener());
        final OnFieldCheckChangeListener onIntervalCheckChangeListener = new OnFieldCheckChangeListener(checkIntervals, checkIntervalAny, enableMultiple);
        checkIntervalAny.setOnCheckedChangeListener(onIntervalCheckChangeListener);
        for (CheckBox checkInterval : checkIntervals) {
            checkInterval.setOnCheckedChangeListener(onIntervalCheckChangeListener);
        }
        inputTempo.addTextChangedListener(new FieldInputTextWatcher());
        inputInstrument.addTextChangedListener(new FieldInputTextWatcher());
        switchBatch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                onNoteCheckChangeListener.setEnableMultiple(b);
                onOctaveCheckChangeListener.setEnableMultiple(b);
                onIntervalCheckChangeListener.setEnableMultiple(b);
            }
        });

        configureClearAllButton();
        configureSelectFileButton();
        configureMarkExistingButton();
        configureAddToAnkiButton();
        configureSettingsButton();
        configureCheckIntegrityButton();

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

    @Override
    protected void onStart() {
        super.onStart();
        if (mAnkiDroid.shouldRequestPermission()) {
            mAnkiDroid.requestPermission(MainActivity.this, AD_PERM_REQUEST);
            return;
        }
        validateModel();
    }

    private void validateModel() {
        try {
            getMusInterval();
        } catch (MusInterval.Exception e) {
            processMusIntervalException(e);
        } catch (Throwable e) {
            processUnknownException(e);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshExisting();
        refreshPermutations();
    }

    private void refreshPermutations() {
        if (mAnkiDroid.shouldRequestPermission()) {
            return;
        }
        int permutationsNumber = 1;
        try {
            permutationsNumber = getMusInterval().getPermutationsNumber();

        } catch (Throwable e) {
        } finally {
            Resources res = getResources();
            String selectFileText;
            if (permutationsNumber <= 1) {
                permutationsNumber = 1;
                selectFileText = res.getQuantityString(R.plurals.select_file, permutationsNumber);
            } else {
                selectFileText = res.getQuantityString(R.plurals.select_file, permutationsNumber, permutationsNumber);
            }
            actionSelectFile.setText(selectFileText);
            this.permutationsNumber = permutationsNumber;
        }
    }

    private void refreshExisting() {
        if (mAnkiDroid.shouldRequestPermission()) {
            return;
        }
        String textExisting = "";
        int existingCount = 0;
        int markedCount = 0;
        try {
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
        } catch (Throwable e) {
            textExisting = ""; // might wanna set some error message here
        } finally {
            labelExisting.setText(textExisting);
            final int unmarkedCount = existingCount - markedCount;
            actionMarkExisting.setText(getString(R.string.action_mark_n, unmarkedCount));
            actionMarkExisting.setEnabled(unmarkedCount > 0);
        }
    }

    private class OnFieldCheckChangeListener implements CompoundButton.OnCheckedChangeListener {
        private final CheckBox[] checkBoxes;
        private final CheckBox checkBoxAny;
        private boolean enableMultiple;

        public OnFieldCheckChangeListener(CheckBox[] checkBoxes, CheckBox checkBoxAny, boolean enableMultiple) {
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
            clearAddedFilenames();
            refreshExisting();
            refreshPermutations();
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

    private class OnFieldRadioChangeListener implements RadioGroup.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(RadioGroup radioGroup, int i) {
            clearAddedFilenames();
            refreshExisting();
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
                inputTempo.setText("");
                inputInstrument.setText("");
            }
        });
    }

    private void configureSelectFileButton() {
        final Button actionSelectFile = findViewById(R.id.actionSelectFile);
        actionSelectFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            PERMISSIONS_REQUEST_EXTERNAL_STORAGE
                    );
                    return;
                }
                handleSelectFile();
            }
        });
    }

    private void handleSelectFile() {
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

    private void openChooser() {
        Intent intent = new Intent()
                .setAction(Intent.ACTION_OPEN_DOCUMENT)
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
                        Uri uri = clipData.getItemAt(i).getUri();
                        getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        filenamesList.add(uri.toString());
                    }
                } else {
                    Uri uri = data.getData();
                    getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    filenamesList.add(uri.toString());
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
                } catch (Throwable e) {
                    processUnknownException(e);
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
                    MusInterval newMi = getMusInterval().addToAnki(MainActivity.this);
                    filenames = newMi.sounds;
                    refreshFilenameText();
                    savedInstruments.add(newMi.instrument);
                    refreshExisting();
                    final int nAdded = newMi.sounds.length;
                    if (nAdded == 1) {
                        showQuantityMsg(R.plurals.mi_added, nAdded);
                    } else if (nAdded > 1) {
                        showQuantityMsg(R.plurals.mi_added, nAdded, nAdded);
                    }
                } catch (MusInterval.Exception e) {
                    processMusIntervalException(e);
                } catch (AnkiDroidHelper.InvalidAnkiDatabaseException e) {
                    processInvalidAnkiDatabase(e);
                } catch (Throwable e) {
                    processUnknownException(e);
                }
            }
        });
    }

    @Override
    public void promptAddDuplicate(MusInterval[] existingMis, final DuplicateAddingHandler handler) {
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        final boolean tagDuplicates = sharedPreferences.getBoolean(SettingsFragment.KEY_TAG_DUPLICATES_SWITCH, SettingsFragment.DEFAULT_TAG_DUPLICATES_SWITCH);
        final String duplicateTag = sharedPreferences.getString(SettingsFragment.KEY_DUPLICATE_TAG_PREFERENCE, SettingsFragment.DEFAULT_DUPLICATE_TAG);
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this)
                .setPositiveButton(R.string.add_anyway, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        try {
                            MusInterval newMi = handler.add();
                            if (tagDuplicates) {
                                handler.tag(duplicateTag);
                            }
                            handleInsertion(newMi);
                            showQuantityMsg(R.plurals.mi_added, 1);
                        } catch (MusInterval.Exception e) {
                            processMusIntervalException(e);
                        } catch (AnkiDroidHelper.InvalidAnkiDatabaseException e) {
                            processInvalidAnkiDatabase(e);
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
                            final int count = handler.mark();
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
        } catch (AnkiDroidHelper.InvalidAnkiDatabaseException e) {
        }
        Resources res = getResources();
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
                        MusInterval newMi = handler.replace();
                        handleInsertion(newMi);
                        showMsg(R.string.item_replaced);
                    } catch (MusInterval.Exception e) {
                        processMusIntervalException(e);
                    } catch (AnkiDroidHelper.InvalidAnkiDatabaseException e) {
                        processInvalidAnkiDatabase(e);
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
                    handler.tag(duplicateTag);
                } catch (MusInterval.Exception e) {
                    processMusIntervalException(e);
                } catch (AnkiDroidHelper.InvalidAnkiDatabaseException e) {
                    processInvalidAnkiDatabase(e);
                }
            }
        }
        builder.setMessage(msg);
        builder.show();
    }

    private void handleInsertion(MusInterval newMi) {
        String[] tempFilenames = new String[filenames.length + 1];
        System.arraycopy(filenames, 0, tempFilenames, 0, filenames.length);
        tempFilenames[tempFilenames.length - 1] = newMi.sounds[0];
        filenames = tempFilenames;
        refreshFilenameText();
        savedInstruments.add(newMi.instrument);
        refreshExisting();
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
                try {
                    getMusInterval();
                } catch (Throwable e) {
                    if (!(e instanceof MusInterval.ModelValidationException)) {
                        processUnknownException(e);
                        return;
                    }
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
                try {
                    final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                    final String corruptedTag = sharedPreferences.getString(SettingsFragment.KEY_CORRUPTED_TAG_PREFERENCE, SettingsFragment.DEFAULT_CORRUPTED_TAG);
                    final String suspiciousTag = sharedPreferences.getString(SettingsFragment.KEY_SUSPICIOUS_TAG_PREFERENCE, SettingsFragment.DEFAULT_SUSPICIOUS_TAG);

                    MusInterval mi = getMusInterval();
                    MusInterval.IntegritySummary integritySummary = mi.checkIntegrity(corruptedTag, suspiciousTag);

                    int notesCount = integritySummary.getNotesCount();
                    int corruptedNotesCount = integritySummary.getCorruptedNotesCount();
                    Map<String, Integer> invalidFieldsCount = integritySummary.getInvalidFieldsCount();
                    Map<String, Integer> emptyFieldsCount = integritySummary.getEmptyFieldsCount();
                    int fixedCorruptedNotesCount = integritySummary.getFixedCorruptedNotesCount();
                    int suspiciousNotesCount = integritySummary.getSuspiciousNotesCount();
                    int fixedSuspiciousNotesCount = integritySummary.getFixedSuspiciousNotesCount();
                    int filledLinksCount = integritySummary.getFilledLinksCount();

                    StringBuilder report = new StringBuilder();
                    Resources res = getResources();
                    report.append(res.getString(R.string.integrity_check_completed, notesCount));
                    if (corruptedNotesCount > 0) {
                        report.append("\n\n");
                        if (corruptedNotesCount == 1) {
                            report.append(res.getQuantityString(R.plurals.integrity_corrupted, corruptedNotesCount, corruptedTag));
                        } else {
                            report.append(res.getQuantityString(R.plurals.integrity_corrupted, corruptedNotesCount, corruptedNotesCount, corruptedTag));
                        }
                        report.append("\n");
                        for (String field : MusInterval.Fields.getSignature(false)) {
                            final int invalidCount = invalidFieldsCount.getOrDefault(field, 0);
                            final int emptyCount = emptyFieldsCount.getOrDefault(field, 0);
                            final int corruptedCount = invalidCount + emptyCount;
                            if (corruptedCount > 0) {
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
                    if (fixedCorruptedNotesCount > 0) {
                        report.append("\n\n");
                        if (fixedCorruptedNotesCount == 1) {
                            report.append(res.getQuantityString(R.plurals.integrity_corrupted_fixed, fixedCorruptedNotesCount));
                        } else {
                            report.append(res.getQuantityString(R.plurals.integrity_corrupted_fixed, fixedCorruptedNotesCount, fixedCorruptedNotesCount));
                        }
                    }
                    if (suspiciousNotesCount > 0) {
                        report.append("\n\n");
                        if (suspiciousNotesCount == 1) {
                            report.append(res.getQuantityString(R.plurals.integrity_suspicious, suspiciousNotesCount, suspiciousTag));
                        } else {
                            report.append(res.getQuantityString(R.plurals.integrity_suspicious, suspiciousNotesCount, suspiciousNotesCount, suspiciousTag));
                        }
                    }
                    if (filledLinksCount > 0) {
                        report.append("\n\n");
                        report.append(getResources().getQuantityString(R.plurals.integrity_links, filledLinksCount, filledLinksCount));
                    }
                    if (fixedSuspiciousNotesCount > 0) {
                        report.append("\n\n");
                        if (fixedSuspiciousNotesCount == 1) {
                            report.append(res.getQuantityString(R.plurals.integrity_suspicious_fixed, fixedSuspiciousNotesCount));
                        } else {
                            report.append(res.getQuantityString(R.plurals.integrity_suspicious_fixed, fixedSuspiciousNotesCount, fixedSuspiciousNotesCount));
                        }
                    }
                    if (corruptedNotesCount == 0 && suspiciousNotesCount == 0) {
                        report.append("\n\n");
                        report.append(res.getString(R.string.integrity_ok));
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

    @Override
    protected void onPause() {
        final SharedPreferences.Editor uiDbEditor = getSharedPreferences(STATE_REF_DB, Context.MODE_PRIVATE).edit();

        uiDbEditor.putBoolean("switchBatch", switchBatch.isChecked());
        uiDbEditor.putStringSet("selectedFilenames", new HashSet<>(Arrays.asList(filenames)));
        uiDbEditor.putBoolean("checkNoteAny", checkNoteAny.isChecked());
        for (int i = 0; i < checkNoteIds.length; i++) {
            uiDbEditor.putBoolean(String.valueOf(checkNoteIds[i]), checkNotes[i].isChecked());
        }
        uiDbEditor.putBoolean("checkOctaveAny", checkOctaveAny.isChecked());
        for (int i = 0; i < checkOctaveIds.length; i++) {
            uiDbEditor.putBoolean(String.valueOf(checkOctaveIds[i]), checkOctaves[i].isChecked());
        }
        uiDbEditor.putInt("radioGroupDirection", radioGroupDirection.getCheckedRadioButtonId());
        uiDbEditor.putInt("radioGroupTiming", radioGroupTiming.getCheckedRadioButtonId());
        uiDbEditor.putBoolean("checkIntervalAny", checkIntervalAny.isChecked());
        for (int i = 0; i < checkIntervalIds.length; i++) {
            uiDbEditor.putBoolean(String.valueOf(checkIntervalIds[i]), checkIntervals[i].isChecked());
        }
        uiDbEditor.putString("inputTempo", inputTempo.getText().toString());
        uiDbEditor.putString("inputInstrument", inputInstrument.getText().toString());
        uiDbEditor.putStringSet("savedInstruments", savedInstruments);
        uiDbEditor.apply();

        super.onPause();
    }

    protected void restoreUiState() {
        final SharedPreferences uiDb = getSharedPreferences(STATE_REF_DB, Context.MODE_PRIVATE);
        switchBatch.setChecked(uiDb.getBoolean("switchBatch", false));
        Set<String> storedFilenames = uiDb.getStringSet("selectedFilenames", new HashSet<String>());
        filenames = storedFilenames.toArray(new String[0]);
        refreshFilenameText();
        checkNoteAny.setChecked(uiDb.getBoolean("checkNoteAny", false));
        for (int i = 0; i < checkNoteIds.length; i++) {
            checkNotes[i].setChecked(uiDb.getBoolean(String.valueOf(checkNoteIds[i]), false));
        }
        checkOctaveAny.setChecked(uiDb.getBoolean("checkOctaveAny", false));
        for (int i = 0; i < checkOctaveIds.length; i++) {
            checkOctaves[i].setChecked(uiDb.getBoolean(String.valueOf(checkOctaveIds[i]), false));
        }
        radioGroupDirection.check(uiDb.getInt("radioGroupDirection", findViewById(R.id.radioDirectionAny).getId()));
        radioGroupTiming.check(uiDb.getInt("radioGroupTiming", findViewById(R.id.radioTimingAny).getId()));
        checkIntervalAny.setChecked(uiDb.getBoolean("checkIntervalAny", false));
        for (int i = 0; i < checkIntervalIds.length; i++) {
            checkIntervals[i].setChecked(uiDb.getBoolean(String.valueOf(checkIntervalIds[i]), false));
        }
        inputTempo.setText(uiDb.getString("inputTempo", ""));
        inputInstrument.setText(uiDb.getString("inputInstrument", ""));
        savedInstruments = (HashSet<String>) uiDb.getStringSet("savedInstruments", new HashSet<String>());
        inputInstrument.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, savedInstruments.toArray(new String[0])));

        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case AD_PERM_REQUEST:
                if (grantResults.length > 0) {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        validateModel();
                        refreshExisting();
                        refreshPermutations();
                    } else {
                        showMsg(R.string.anki_permission_denied);
                    }
                }
                break;
            case PERMISSIONS_REQUEST_EXTERNAL_STORAGE:
                if (grantResults.length > 0) {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        handleSelectFile();
                    } else {
                        showMsg(R.string.fs_permission_denied);
                    }
                }
                break;
        }
    }

    private MusInterval getMusInterval() throws MusInterval.ModelValidationException {
        final String anyStr = getResources().getString(R.string.any);

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

        String[] notes = !checkNoteAny.isChecked() ? getCheckedValues(checkNotes) : null;
        String[] octaves = !checkOctaveAny.isChecked() ? getCheckedValues(checkOctaves) : null;
        String[] intervals = !checkIntervalAny.isChecked() ? getCheckedValues(checkIntervals, checkIntervalIdValues) : null;

        MusInterval.Builder builder = new MusInterval.Builder(mAnkiDroid)
                .deck(storedDeck)
                .model(storedModel)
                .model_fields(storedFields)
                .sounds(filenames)
                .notes(notes)
                .octaves(octaves)
                .direction(!directionStr.equals(anyStr) ? directionStr : "")
                .timing(!timingStr.equals(anyStr) ? timingStr : "")
                .intervals(intervals)
                .tempo(inputTempo.getText().toString())
                .instrument(inputInstrument.getText().toString());
        if (versionField) {
            builder.version(BuildConfig.VERSION_NAME);
        }
        return builder.build();
    }

    private static String[] getCheckedValues(CheckBox[] checkBoxes) {
        return getCheckedValues(checkBoxes, null);
    }

    private static String[] getCheckedValues(CheckBox[] checkBoxes, Map<Integer, String> checkIdValues) {
        ArrayList<String> valuesList = new ArrayList<>();
        for (CheckBox checkBox : checkBoxes) {
            if (checkBox.isChecked()) {
                String value = checkBox.getText().toString();
                if (checkIdValues != null) {
                    value = checkIdValues.get(checkBox.getId());
                }
                valuesList.add(value);
            }
        }
        return valuesList.toArray(new String[0]);
    }

    private void processMusIntervalException(MusInterval.Exception miException) {
        try {
            throw miException;
        } catch (MusInterval.MandatorySelectionEmptyException e) {
            showMsg(R.string.empty_mandatory_selection, getString(selectionFieldLabelStringIds.get(e.getField())));
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
            StringBuilder fieldsStr = new StringBuilder();
            ArrayList<String> invalidModelFields = e.getInvalidModelFields();
            for (String field : invalidModelFields) {
                if (fieldsStr.length() != 0) {
                    fieldsStr.append(", ");
                }
                fieldsStr.append(String.format("\"%s\"", SettingsFragment.getFieldPreferenceLabelString(field, this)));
            }
            new AlertDialog.Builder(this)
                    .setMessage(
                            getResources().getQuantityString(R.plurals.invalid_model_fields, invalidModelFields.size(), modelName, fieldsStr.toString()))
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
            showMsg(R.string.mandatory_field_empty, getString(singularFieldLabelStringIds.get(e.getField())));
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
            refreshExisting();
            refreshPermutations();
            showMsg(R.string.create_model_success, modelName);
        } else {
            showMsg(R.string.create_model_error);
        }
    }

    private void processInvalidAnkiDatabase(AnkiDroidHelper.InvalidAnkiDatabaseException invalidAnkiDatabaseException) {
        try {
            throw invalidAnkiDatabaseException;
        } catch (AnkiDroidHelper.InvalidAnkiDatabase_fieldAndFieldNameCountMismatchException e) {
            showMsg(R.string.InvalidAnkiDatabase_fieldAndFieldNameCountMismatch);
        } catch (AnkiDroidHelper.InvalidAnkiDatabaseException e) {
            showMsg(R.string.InvalidAnkiDatabase_unknownError);
        }
    }

    private void processUnknownException(Throwable e) {
        showMsg(R.string.unknown_error);
    }

    private void showMsg(int msgResId, Object ...formatArgs) {
        Toast.makeText(MainActivity.this, getResources().getString(msgResId, formatArgs), Toast.LENGTH_LONG).show();
    }

    private void showQuantityMsg(int msgResId, int quantity, Object ...formatArgs) {
        Toast.makeText(MainActivity.this, getResources().getQuantityString(msgResId, quantity, formatArgs), Toast.LENGTH_LONG).show();
    }
}
