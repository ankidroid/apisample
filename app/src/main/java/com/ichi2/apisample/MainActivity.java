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

        inputStartNote.addTextChangedListener(new FieldInputTextWatcher());
        radioGroupDirection.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                clearAddedInputFilename();
            }
        });
        radioGroupTiming.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                clearAddedInputFilename();
            }
        });
        selectInterval.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                clearAddedInputFilename();
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
            mAnkiDroid.requestPermission(MainActivity.this, AD_PERM_REQUEST_VALID);
        } else {
            validateModel();
        }
    }

    private void clearAddedInputFilename() {
        String filename = MainActivity.this.inputFilename.getText().toString();
        if (filename.length() > 0 && filename.startsWith("[sound:")) {
            inputFilename.setText("");
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
            case AD_PERM_REQUEST_VALID: {
                if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    showMsg(R.string.anki_permission_denied);
                } else {
                    validateModel();
                }
            }
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

    private MusInterval getMusInterval() throws MusInterval.ValidationException {
        final String anyStr = getResources().getString(R.string.radio_any);

        final int radioDirectionId = radioGroupDirection.getCheckedRadioButtonId();
        final View radioDirection = findViewById(radioDirectionId);
        final String directionStr = radioDirection instanceof RadioButton && radioDirectionId != -1 ?
                ((RadioButton) radioDirection).getText().toString() : anyStr;
        // @todo: fix underlying cause of crash on unchecked groups
        final int radioTimingId = radioGroupTiming.getCheckedRadioButtonId();
        final View radioTiming = findViewById(radioTimingId);
        final String timingStr = radioTiming instanceof RadioButton && radioTimingId != -1 ?
                ((RadioButton) radioTiming).getText().toString() : anyStr;

        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        final Map<String, String> storedFields = new HashMap<>();
        for (String field : MusInterval.Fields.SIGNATURE) {
            storedFields.put(field, sharedPreferences.getString(field, field));
        }
        final String storedDeck = sharedPreferences.getString(SettingsFragment.KEY_DECK_PREFERENCE, MusInterval.Builder.DEFAULT_DECK_NAME);
        final String storedModel = sharedPreferences.getString(SettingsFragment.KEY_MODEL_PREFERENCE, MusInterval.Builder.DEFAULT_MODEL_NAME);

        return new MusInterval.Builder(mAnkiDroid)
                .deck(storedDeck)
                .model(storedModel)
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

    private void validateModel() {
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        final Map<String, String> storedFields = new HashMap<>();
        for (String field : MusInterval.Fields.SIGNATURE) {
            storedFields.put(field, sharedPreferences.getString(field, field));
        }
        final String storedModel = sharedPreferences.getString(SettingsFragment.KEY_MODEL_PREFERENCE, MusInterval.Builder.DEFAULT_MODEL_NAME);
        try {
            new MusInterval.Builder(mAnkiDroid)
                    .model(storedModel)
                    .model_fields(storedFields)
                    .build();
        } catch (MusInterval.ValidationException e) {
            processMusIntervalException(e);
        }
    }

    private void processMusIntervalException(MusInterval.Exception miException) {
        try {
            throw miException;
        } catch (MusInterval.ModelDoesNotExistException e) {
            DialogFragment f = new CreateModelDialogFragment();
            Bundle args = new Bundle();
            args.putString(SettingsFragment.KEY_MODEL_PREFERENCE, e.getModelName());
            f.setArguments(args);
            f.show(getFragmentManager(), "createModelDialog");
        } catch (MusInterval.NotEnoughFieldsException e) {
            showMsg(String.format(getResources().getString(R.string.invalid_model), e.getModelName()));
        } catch (MusInterval.ModelNotConfiguredException e) {
            DialogFragment f = new ConfigureModelDialogFragment();
            Bundle args = new Bundle();
            args.putString(SettingsFragment.KEY_MODEL_PREFERENCE, e.getModelName());
            f.setArguments(args);
            f.show(getFragmentManager(), "configureModelDialog");
        } catch (MusInterval.NoteNotExistsException e) {
            showMsg(R.string.mi_not_exists);
        } catch (MusInterval.StartNoteSyntaxException e) {
            showMsg(R.string.invalid_start_note);
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
            final String modelName = getArguments().getString(SettingsFragment.KEY_MODEL_PREFERENCE);
            return new AlertDialog.Builder(mainActivity)
                    .setMessage(String.format(
                            getResources().getString(R.string.create_model),
                            modelName))
                    .setPositiveButton(R.string.create, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            final Long newModelId = mainActivity.mAnkiDroid.addNewCustomModel(
                                    modelName,
                                    MusInterval.Fields.SIGNATURE,
                                    MusInterval.Builder.CARD_NAMES,
                                    MusInterval.Builder.QFMT,
                                    MusInterval.Builder.AFMT,
                                    MusInterval.Builder.CSS
                            );
                            if (newModelId != null) {
                                mainActivity.showMsg(
                                        String.format(
                                                getResources().getString(R.string.create_model_success),
                                                modelName
                                        )
                                );
                            } else {
                                mainActivity.showMsg(R.string.create_model_error);
                            }
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
                            getArguments().getString(SettingsFragment.KEY_MODEL_PREFERENCE)))
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

}
