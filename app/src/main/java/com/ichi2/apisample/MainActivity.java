package com.ichi2.apisample;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
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
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;


public class MainActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback, DuplicateAddingPrompter {

    private static final int AD_PERM_REQUEST = 0;
    private static final int PERMISSIONS_REQUEST_EXTERNAL_STORAGE = 1;

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
    private TextView labelExisting;
    private Button actionMarkExisting;

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
        labelExisting = findViewById(R.id.labelExisting);
        actionMarkExisting = findViewById(R.id.actionMarkExisting);

        inputStartNote.addTextChangedListener(new FieldInputTextWatcher());
        radioGroupDirection.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                clearAddedInputFilename();
                refreshExisting();
            }
        });
        radioGroupTiming.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                clearAddedInputFilename();
                refreshExisting();
            }
        });
        selectInterval.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                clearAddedInputFilename();
                refreshExisting();
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
                clearAddedInputFilename();
                refreshExisting();
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
        configureMarkExistingButton();
        configureAddToAnkiButton();
        configureSettingsButton();

        restoreUiState();

        mAnkiDroid = new AnkiDroidHelper(this);
    }

    private void clearAddedInputFilename() {
        String filename = MainActivity.this.inputFilename.getText().toString();
        if (filename.length() > 0 && filename.startsWith("[sound:")) {
            inputFilename.setText("");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshExisting();
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
                clearAddedInputFilename();
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
                    MusInterval newMi = getMusInterval().addToAnki(MainActivity.this);
                    if (newMi != null) {
                        handleAddToAnki(newMi);
                    }
                } catch (MusInterval.Exception e) {
                    processMusIntervalException(e);
                } catch (AnkiDroidHelper.InvalidAnkiDatabaseException e) {
                    processInvalidAnkiDatabase(e);
                }
            }
        });
    }

    @Override
    public void promptAddDuplicate(LinkedList<Map<String, String>> existingNotesData, final DuplicateAddingHandler handler) {
        Resources res = getResources();
        String msg;
        int existingCount = existingNotesData.size();
        if (existingCount == 1) {
            msg = res.getQuantityString(R.plurals.duplicate_warning, existingCount);
        } else {
            msg = res.getQuantityString(R.plurals.duplicate_warning, existingCount, existingCount);
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this)
                .setMessage(msg)
                .setPositiveButton(R.string.add_anyway, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        try {
                            MusInterval newMi = handler.add();
                            final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                            final boolean tagDuplicates = sharedPreferences.getBoolean(SettingsFragment.KEY_TAG_DUPLICATES_SWITCH, SettingsFragment.DEFAULT_TAG_DUPLICATES_SWITCH);
                            if (tagDuplicates) {
                                final String duplicateTag = sharedPreferences.getString(SettingsFragment.KEY_DUPLICATE_TAG_PREFERENCE, SettingsFragment.DEFAULT_DUPLICATE_TAG);
                                newMi.tagExistingNotes(duplicateTag);
                            }
                            handleAddToAnki(newMi);
                        } catch (MusInterval.Exception e) {
                            processMusIntervalException(e);
                        } catch (AnkiDroidHelper.InvalidAnkiDatabaseException e) {
                            processInvalidAnkiDatabase(e);
                        }
                    }
                })
                .setNeutralButton(R.string.mark_existing, new DialogInterface.OnClickListener() {
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
        if (existingCount == 1) {
            builder.setNegativeButton(R.string.replace_existing, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    try {
                        MusInterval newMi = handler.replace();
                        inputFilename.setText(newMi.sound);
                        inputStartNote.setText(newMi.startNote);

                        savedStartNotes.add(newMi.startNote);
                        savedInstruments.add(newMi.instrument);

                        refreshExisting();

                        showMsg(R.string.item_replaced);
                    } catch (MusInterval.Exception e) {
                        processMusIntervalException(e);
                    } catch (AnkiDroidHelper.InvalidAnkiDatabaseException e) {
                        processInvalidAnkiDatabase(e);
                    }
                }
            });
        }
        builder.show();
    }

    private void handleAddToAnki(MusInterval newMi) {
        inputFilename.setText(newMi.sound);
        inputStartNote.setText(newMi.startNote);

        savedStartNotes.add(newMi.startNote);
        savedInstruments.add(newMi.instrument);

        refreshExisting();

        showMsg(R.string.item_added);
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
            case AD_PERM_REQUEST: {
                if (grantResults.length > 0) {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        refreshExisting();
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
                .sound(inputFilename.getText().toString())
                .start_note(inputStartNote.getText().toString())
                .direction(!directionStr.equals(anyStr) ? directionStr : "")
                .timing(!timingStr.equals(anyStr) ? timingStr : "")
                .interval(selectInterval.getSelectedItem().toString())
                .tempo(seekTempo.getProgress() > 0 ? Integer.toString(seekTempo.getProgress()) : "")
                .instrument(inputInstrument.getText().toString());
        if (versionField) {
            builder.version(BuildConfig.VERSION_NAME);
        }
        return builder.build();
    }

    private void processMusIntervalException(MusInterval.Exception miException) {
        try {
            throw miException;
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
        } catch (MusInterval.StartNoteSyntaxException e) {
            showMsg(R.string.invalid_start_note);
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
