package com.ichi2.apisample;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
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
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Spinner;
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
    private static final int AD_PERM_REQUEST_VALID = 2;

    private static final int ACTION_SELECT_FILE = 10;

    private static final String STATE_REF_DB = "com.ichi2.apisample.uistate";

    private final Map<String, String> fieldLabels = new HashMap<>();

    private TextView labelFilename;
    private Button actionSelectFile;
    private LinearLayout layoutFilenames;
    private CheckBox[] checkNotes;
    private CheckBox[] checkOctaves;
    private RadioGroup radioGroupDirection;
    private RadioGroup radioGroupTiming;
    private Spinner selectInterval;
    private SeekBar seekTempo;
    private AutoCompleteTextView inputInstrument;

    private final int[] checkNoteIds = new int[]{
            R.id.checkC, R.id.checkCSharp,
            R.id.checkD, R.id.checkDSharp,
            R.id.checkE,
            R.id.checkF, R.id.checkFSharp,
            R.id.checkG, R.id.checkGSharp,
            R.id.checkA, R.id.checkASharp,
            R.id.checkB
    };
    private final int[] checkOctaveIds = new int[]{
            R.id.checkOctave1,
            R.id.checkOctave2,
            R.id.checkOctave3,
            R.id.checkOctave4,
            R.id.checkOctave5,
            R.id.checkOctave6
    };

    private HashSet<String> savedInstruments = new HashSet<>();

    private AnkiDroidHelper mAnkiDroid;

    @Override
    protected synchronized void onCreate(Bundle savedInstanceState) {
        fieldLabels.put(MusInterval.Fields.DIRECTION, getResources().getString(R.string.direction));
        fieldLabels.put(MusInterval.Fields.TIMING, getResources().getString(R.string.timing));
        fieldLabels.put(MusInterval.Fields.INTERVAL, getResources().getString(R.string.interval));
        fieldLabels.put(MusInterval.Fields.TEMPO, getResources().getString(R.string.tempo));
        fieldLabels.put(MusInterval.Fields.INSTRUMENT, getResources().getString(R.string.instrument));

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar main_toolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(main_toolbar);

        labelFilename = findViewById(R.id.labelFilename);
        actionSelectFile = findViewById(R.id.actionSelectFile);
        layoutFilenames = findViewById(R.id.layoutFilenames);
        checkNotes = new CheckBox[checkNoteIds.length];
        for (int i = 0; i < checkNoteIds.length; i++) {
            checkNotes[i] = findViewById(checkNoteIds[i]);
        }
        checkOctaves = new CheckBox[checkOctaveIds.length];
        for (int i = 0; i < checkOctaveIds.length; i++) {
            checkOctaves[i] = findViewById(checkOctaveIds[i]);
        }
        radioGroupDirection = findViewById(R.id.radioGroupDirection);
        radioGroupTiming = findViewById(R.id.radioGroupTiming);
        selectInterval = findViewById(R.id.selectInterval);
        seekTempo = findViewById(R.id.seekTempo);
        inputInstrument = findViewById(R.id.inputInstrument);

        for (CheckBox checkNote : checkNotes) {
            checkNote.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    clearAddedFilenames();
                    refreshPermutations();
                }
            });
        }
        for (CheckBox checkOctave : checkOctaves) {
            checkOctave.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    clearAddedFilenames();
                    refreshPermutations();
                }
            });
        }
        radioGroupDirection.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                clearAddedFilenames();
            }
        });
        radioGroupTiming.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                clearAddedFilenames();
            }
        });
        selectInterval.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                clearAddedFilenames();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) { }
        });
        seekTempo.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                TextView label = findViewById(R.id.labelTempoValue);
                label.setText(Integer.toString(seekBar.getProgress()));
                clearAddedFilenames();
            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) { }
            @Override public void onStopTrackingTouch(SeekBar seekBar) { }
        });
        inputInstrument.addTextChangedListener(new FieldInputTextWatcher());

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

        restoreUiState();

        mAnkiDroid = new AnkiDroidHelper(this);

        if (mAnkiDroid.shouldRequestPermission()) {
            mAnkiDroid.requestPermission(this, AD_PERM_REQUEST_VALID);
        } else if (!doesModelExist() || !doesModelHaveEnoughFields() || !doesModelHaveStoredFields()) {
            validateModel();
        }

        refreshPermutations();
    }

    private void clearAddedFilenames() {
        if (layoutFilenames.getChildCount() == 0) {
            return;
        }
        String firstFilename = ((TextView)layoutFilenames.getChildAt(0)).getText().toString();
        // wack
        if (firstFilename.length() > 0 && firstFilename.startsWith("[sound:")) {
            layoutFilenames.removeAllViews();
        }
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
            labelFilename.setText(res.getQuantityString(R.plurals.label_filename, permutationsNumber));
            String selectFileText;
            if (permutationsNumber == 1) {
                selectFileText = res.getQuantityString(R.plurals.select_file, permutationsNumber);
            } else {
                selectFileText = res.getQuantityString(R.plurals.select_file, permutationsNumber, permutationsNumber);
            }
            actionSelectFile.setText(selectFileText);
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
                layoutFilenames.removeAllViews();
                for (CheckBox checkNote : checkNotes) {
                    checkNote.setChecked(false);
                }
                for (CheckBox checkOctave : checkOctaves) {
                    checkOctave.setChecked(false);
                }
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
                        .putExtra(Intent.EXTRA_LOCAL_ONLY, true)
                        .putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);

                startActivityForResult(Intent.createChooser(intent, actionSelectFile.getText().toString()),
                        ACTION_SELECT_FILE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ACTION_SELECT_FILE && resultCode == RESULT_OK) {
            layoutFilenames.removeAllViews();
            ArrayList<Uri> selectedFiles = new ArrayList<>();
            if (data != null) {
                ClipData clipData = data.getClipData();
                if (clipData != null) {
                    for (int i = 0; i < clipData.getItemCount(); i++) {
                        selectedFiles.add(clipData.getItemAt(i).getUri());
                    }
                } else {
                    selectedFiles.add(data.getData());
                }
            }
            for (Uri file : selectedFiles) {
                TextView labelName = new TextView(this);
                labelName.setText(file.toString());
                layoutFilenames.addView(labelName);
            }
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
                    MusInterval mi = getMusInterval();
                    final int nMis = mi.getPermutationsNumber();
                    MusInterval newMi = mi.addToAnki();
                    layoutFilenames.removeAllViews();
                    for (String sound : newMi.sounds) {
                        TextView labelAdded = new TextView(MainActivity.this);
                        labelAdded.setText(sound);
                        layoutFilenames.addView(labelAdded);
                    }
                    savedInstruments.add(newMi.instrument);
                    String msg;
                    if (nMis == 1) {
                        msg = getResources().getQuantityString(R.plurals.mi_added, nMis);
                    } else {
                        msg = getResources().getQuantityString(R.plurals.mi_added, nMis, nMis);
                    }
                    showMsg(msg);
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
        final SharedPreferences.Editor uiDbEditor = getSharedPreferences(STATE_REF_DB, Context.MODE_PRIVATE).edit();

        final int nFiles = layoutFilenames.getChildCount();
        Set<String> fileNames = new HashSet<>(nFiles);
        for (int i = 0; i < nFiles; i++) {
            fileNames.add(((TextView) layoutFilenames.getChildAt(i)).getText().toString());
        }
        uiDbEditor.putStringSet("layoutFilenames", fileNames);
        for (int i = 0; i < checkNoteIds.length; i++) {
            uiDbEditor.putBoolean(String.valueOf(checkNoteIds[i]), checkNotes[i].isChecked());
        }
        for (int i = 0; i < checkOctaveIds.length; i++) {
            uiDbEditor.putBoolean(String.valueOf(checkOctaveIds[i]), checkOctaves[i].isChecked());
        }
        uiDbEditor.putInt("radioGroupDirection", radioGroupDirection.getCheckedRadioButtonId());
        uiDbEditor.putInt("radioGroupTiming", radioGroupTiming.getCheckedRadioButtonId());
        uiDbEditor.putInt("selectInterval", selectInterval.getSelectedItemPosition());
        uiDbEditor.putString("inputTempo", Integer.toString(seekTempo.getProgress()));
        uiDbEditor.putString("inputInstrument", inputInstrument.getText().toString());
        uiDbEditor.putStringSet("savedInstruments", savedInstruments);
        uiDbEditor.apply();

        super.onPause();
    }

    protected void restoreUiState() {
        final SharedPreferences uiDb = getSharedPreferences(STATE_REF_DB, Context.MODE_PRIVATE);
        Set<String> fileNames = uiDb.getStringSet("layoutFilenames", new HashSet<String>());
        for (String name : fileNames) {
            TextView labelName = new TextView(this);
            labelName.setText(name);
            layoutFilenames.addView(labelName);
        }
        for (int i = 0; i < checkNoteIds.length; i++) {
            checkNotes[i].setChecked(uiDb.getBoolean(String.valueOf(checkNoteIds[i]), false));
        }
        for (int i = 0; i < checkOctaveIds.length; i++) {
            checkOctaves[i].setChecked(uiDb.getBoolean(String.valueOf(checkOctaveIds[i]), false));
        }
        radioGroupDirection.check(uiDb.getInt("radioGroupDirection", findViewById(R.id.radioDirectionAny).getId()));
        radioGroupTiming.check(uiDb.getInt("radioGroupTiming", findViewById(R.id.radioTimingAny).getId()));
        selectInterval.setSelection(uiDb.getInt("selectInterval", 0));
        seekTempo.setProgress(Integer.parseInt(uiDb.getString("inputTempo", "0")));
        inputInstrument.setText(uiDb.getString("inputInstrument", ""));

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
                if (grantResults.length > 0) {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
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

        String[] sounds = new String[layoutFilenames.getChildCount()];
        for (int i = 0; i < sounds.length; i++) {
            sounds[i] = ((TextView)layoutFilenames.getChildAt(i)).getText().toString();
        }

        final ArrayList<String> noteList = new ArrayList<>();
        for (CheckBox checkNote : checkNotes) {
            if (checkNote.isChecked()) {
                noteList.add(checkNote.getText().toString());
            }
        }
        final ArrayList<String> octaveList = new ArrayList<>();
        for (CheckBox checkOctave : checkOctaves) {
            if (checkOctave.isChecked()) {
                octaveList.add(checkOctave.getText().toString());
            }
        }

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
                .sounds(sounds)
                .notes(noteList.toArray(new String[0]))
                .octaves(octaveList.toArray(new String[0]))
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
        } catch (MusInterval.NoteNotSelectedException e) {
            showMsg(R.string.note_not_selected);
        } catch (MusInterval.OctaveNotSelectedException e) {
            showMsg(R.string.octave_not_selected);
        } catch (MusInterval.UnexpectedSoundsAmountException e) {
            int expected = e.getExpectedAmount();
            Resources res = getResources();
            String msg;
            if (expected == 1) {
                msg = res.getQuantityString(R.plurals.unexpected_sounds_amount, expected);
            } else {
                msg = res.getQuantityString(R.plurals.unexpected_sounds_amount, expected, expected, e.getProvidedAmount());
            }
            showMsg(msg);
        } catch (MusInterval.NoteNotExistsException e) {
            showMsg(R.string.mi_not_exists);
        } catch (MusInterval.CreateDeckException e) {
            showMsg(R.string.create_deck_error);
        } catch (MusInterval.AddToAnkiException e) {
            showMsg(R.string.add_card_error);
        } catch (MusInterval.MandatoryFieldEmptyException e) {
            showMsg(String.format(getResources().getString(R.string.mandatory_field_empty), fieldLabels.get(e.getField())));
        } catch (MusInterval.SoundAlreadyAddedException e) {
            showMsg(R.string.already_added);
        } catch (MusInterval.AddSoundFileException e) {
            showMsg(R.string.add_file_error);
        } catch (MusInterval.Exception e) {
            showMsg(R.string.unknown_adding_error);
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
