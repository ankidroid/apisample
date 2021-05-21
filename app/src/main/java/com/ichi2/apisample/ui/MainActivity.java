package com.ichi2.apisample.ui;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.provider.OpenableColumns;
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

import com.ichi2.apisample.BuildConfig;
import com.ichi2.apisample.helper.StringUtil;
import com.ichi2.apisample.model.AddingHandler;
import com.ichi2.apisample.model.AddingPrompter;
import com.ichi2.apisample.model.NotesIntegrity;
import com.ichi2.apisample.model.MusInterval;
import com.ichi2.apisample.R;
import com.ichi2.apisample.helper.AnkiDroidHelper;
import com.ichi2.apisample.model.ProgressIndicator;
import com.ichi2.apisample.service.AudioCaptureService;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public class MainActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback, AddingPrompter, ProgressIndicator {

    private static final int AD_PERM_REQUEST = 0;
    private static final int PERMISSIONS_REQUEST_EXTERNAL_STORAGE = 1;
    private static final int PERMISSIONS_REQUEST_RECORD_AUDIO = 2;

    private static final int ACTION_SELECT_FILE = 10;
    private static final int ACTION_SCREEN_CAPTURE = 11;

    private static final String TAG_APPLICATION = "mi2a";
    private static final String TAG_DUPLICATE = "duplicate";
    private static final String TAG_CORRUPTED = "corrupted";
    private static final String TAG_SUSPICIOUS = "suspicious";

    private static final String REF_DB_STATE = "com.ichi2.apisample.uistate";
    private static final String REF_DB_SWITCH_BATCH = "switchBatch";
    private static final String REF_DB_SELECTED_FILENAMES = "selectedFilenames";
    private static final String REF_DB_CHECK_NOTE_ANY = "checkNoteAny";
    private static final String REF_DB_CHECK_OCTAVE_ANY = "checkOctaveAny";
    private static final String REF_DB_RADIO_GROUP_DIRECTION = "radioGroupDirection";
    private static final String REF_DB_RADIO_GROUP_TIMING = "radioGroupTiming";
    private static final String REF_DB_CHECK_INTERVAL_ANY = "checkIntervalAny";
    private static final String REF_DB_INPUT_TEMPO = "inputTempo";
    private static final String REF_DB_INPUT_INSTRUMENT = "inputInstrument";
    private static final String REF_DB_SAVED_INSTRUMENTS = "savedInstruments";
    private static final String REF_DB_BATCH_ADDING_NOTICE_SEEN = "batchAddingNoticeSeen";
    private static final String REF_DB_AFTER_ADDING = "afterAdding";
    private static final String REF_DB_NOTE_KEYS = "noteKeys";
    private static final String REF_DB_OCTAVE_KEYS = "octaveKeys";
    private static final String REF_DB_INTERVAL_KEYS = "intervalKeys";
    private static final String DB_STRING_ARRAY_SEPARATOR = ",";

    private final static Map<String, Integer> FIELD_LABEL_STRING_IDS_SINGULAR = new HashMap<String, Integer>() {{
        put(MusInterval.Fields.DIRECTION, R.string.direction);
        put(MusInterval.Fields.TIMING, R.string.timing);
        put(MusInterval.Fields.TEMPO, R.string.tempo);
        put(MusInterval.Fields.INSTRUMENT, R.string.instrument);
    }};
    static {
        if (!FIELD_LABEL_STRING_IDS_SINGULAR.keySet().equals(MusInterval.Builder.ADDING_MANDATORY_SINGULAR_KEYS)) {
            throw new AssertionError();
        }
    }

    private final static Map<String, Integer> FIELD_LABEL_STRING_IDS_SELECTION = new HashMap<String, Integer>() {{
        put(MusInterval.Builder.KEY_NOTES, R.string.start_note);
        put(MusInterval.Builder.KEY_OCTAVES, R.string.octave);
        put(MusInterval.Builder.KEY_INTERVALS, R.string.interval);
    }};
    static {
        if (!FIELD_LABEL_STRING_IDS_SELECTION.keySet().equals(MusInterval.Builder.ADDING_MANDATORY_SELECTION_KEYS)) {
            throw new AssertionError();
        }
    }

    private SwitchCompat switchBatch;
    private TextView textFilename;
    private Button actionPlay;
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

    private ProgressDialog progressDialog;

    private Handler mHandler;

    private final static int[] CHECK_NOTE_IDS = new int[]{
            R.id.checkNoteC, R.id.checkNoteCSharp,
            R.id.checkNoteD, R.id.checkNoteDSharp,
            R.id.checkNoteE,
            R.id.checkNoteF, R.id.checkNoteFSharp,
            R.id.checkNoteG, R.id.checkNoteGSharp,
            R.id.checkNoteA, R.id.checkNoteASharp,
            R.id.checkNoteB
    };
    private final static int[] CHECK_OCTAVE_IDS = new int[]{
            R.id.checkOctave1,
            R.id.checkOctave2,
            R.id.checkOctave3,
            R.id.checkOctave4,
            R.id.checkOctave5,
            R.id.checkOctave6
    };
    private final static int[] CHECK_INTERVAL_IDS = new int[]{
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

    private final static Map<Integer, String> CHECK_INTERVAL_ID_VALUES = new HashMap<>();
    static {
        //noinspection ConstantConditions
        if (CHECK_INTERVAL_IDS.length != MusInterval.Fields.Interval.VALUES.length) {
            throw new AssertionError();
        }
        for (int i = 0; i < MusInterval.Fields.Interval.VALUES.length; i++) {
            CHECK_INTERVAL_ID_VALUES.put(CHECK_INTERVAL_IDS[i], MusInterval.Fields.Interval.VALUES[i]);
        }
    }

    private String[] filenames = new String[]{};

    private SoundPlayer soundPlayer;

    private Integer permutationsNumber;

    private HashSet<String> savedInstruments = new HashSet<>();

    private AnkiDroidHelper mAnkiDroid;

    private boolean afterAdding;

    private String[] noteKeys = new String[]{};
    private String[] octaveKeys = new String[]{};
    private String[] intervalKeys = new String[]{};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar mainToolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(mainToolbar);

        switchBatch = findViewById(R.id.switchBatch);
        textFilename = findViewById(R.id.textFilename);
        actionPlay = findViewById(R.id.actionPlay);
        actionSelectFile = findViewById(R.id.actionSelectFile);
        checkNoteAny = findViewById(R.id.checkNoteAny);
        checkNotes = new CheckBox[CHECK_NOTE_IDS.length];
        for (int i = 0; i < CHECK_NOTE_IDS.length; i++) {
            checkNotes[i] = findViewById(CHECK_NOTE_IDS[i]);
        }
        checkOctaveAny = findViewById(R.id.checkOctaveAny);
        checkOctaves = new CheckBox[CHECK_OCTAVE_IDS.length];
        for (int i = 0; i < CHECK_OCTAVE_IDS.length; i++) {
            checkOctaves[i] = findViewById(CHECK_OCTAVE_IDS[i]);
        }
        radioGroupDirection = findViewById(R.id.radioGroupDirection);
        radioGroupTiming = findViewById(R.id.radioGroupTiming);
        checkIntervalAny = findViewById(R.id.checkIntervalAny);
        checkIntervals = new CheckBox[CHECK_INTERVAL_IDS.length];
        for (int i = 0; i < CHECK_INTERVAL_IDS.length; i++) {
            checkIntervals[i] = findViewById(CHECK_INTERVAL_IDS[i]);
        }
        inputTempo = findViewById(R.id.inputTempo);
        inputInstrument = findViewById(R.id.inputInstrument);
        labelExisting = findViewById(R.id.labelExisting);
        actionMarkExisting = findViewById(R.id.actionMarkExisting);

        restoreUiState();

        boolean enableMultiple = switchBatch.isChecked();
        final OnFieldCheckChangeListener onNoteCheckChangeListener = new OnFieldCheckChangeListener(this, checkNotes, checkNoteAny, enableMultiple);
        checkNoteAny.setOnCheckedChangeListener(onNoteCheckChangeListener);
        for (CheckBox checkNote : checkNotes) {
            checkNote.setOnCheckedChangeListener(onNoteCheckChangeListener);
        }
        final OnFieldCheckChangeListener onOctaveCheckChangeListener = new OnFieldCheckChangeListener(this, checkOctaves, checkOctaveAny, enableMultiple);
        checkOctaveAny.setOnCheckedChangeListener(onOctaveCheckChangeListener);
        for (CheckBox checkOctave : checkOctaves) {
            checkOctave.setOnCheckedChangeListener(onOctaveCheckChangeListener);
        }
        radioGroupDirection.setOnCheckedChangeListener(new OnFieldRadioChangeListener(this));
        radioGroupTiming.setOnCheckedChangeListener(new OnFieldRadioChangeListener(this));
        final OnFieldCheckChangeListener onIntervalCheckChangeListener = new OnFieldCheckChangeListener(this, checkIntervals, checkIntervalAny, enableMultiple);
        checkIntervalAny.setOnCheckedChangeListener(onIntervalCheckChangeListener);
        for (CheckBox checkInterval : checkIntervals) {
            checkInterval.setOnCheckedChangeListener(onIntervalCheckChangeListener);
        }
        inputTempo.addTextChangedListener(new FieldInputTextWatcher(this));
        inputInstrument.addTextChangedListener(new FieldInputTextWatcher(this));
        switchBatch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                onNoteCheckChangeListener.setEnableMultiple(b);
                onOctaveCheckChangeListener.setEnableMultiple(b);
                onIntervalCheckChangeListener.setEnableMultiple(b);
            }
        });

        mHandler = new Handler();

        configureRecordingButtons();
        configureClearAllButton();
        configureSelectFileButton();
        configureMarkExistingButton();
        configureAddToAnkiButton();
        configureSettingsButton();
        configureCheckIntegrityButton();

        soundPlayer = new SoundPlayer(this);

        mAnkiDroid = new AnkiDroidHelper(this);
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
        } catch (MusInterval.ModelValidationException e) {
            processMusIntervalException(e);
        } catch (MusInterval.ValidationException e) {
            // ignore other validation errors aside from model
        } catch (Throwable e) {
            processUnknownException(e);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshExisting();
        refreshPermutations();
        refreshFilenames();
    }

    void refreshPermutations() {
        if (mAnkiDroid.shouldRequestPermission()) {
            return;
        }
        int permutationsNumber = 1;
        try {
            permutationsNumber = getMusInterval().getPermutationsNumber();

        } catch (Throwable e) {
            // probably best to ignore exceptions here as this function is called silently
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

    void refreshExisting() {
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

    void clearAddedFilenames() {
        if (afterAdding) {
            refreshKeys();
            filenames = new String[]{};
            refreshFilenames();
            afterAdding = false;
        }
    }

    void refreshKeys() {
        String[] checkedNotes = getCheckedValues(checkNotes);
        String[] checkedOctaves = getCheckedValues(checkOctaves);
        String[] checkedIntervals = getCheckedValues(checkIntervals, CHECK_INTERVAL_ID_VALUES);
        int permutations = checkedNotes.length * checkedOctaves.length * checkedIntervals.length;
        noteKeys = new String[permutations];
        octaveKeys = new String[permutations];
        intervalKeys = new String[permutations];
        for (int i = 0; i < permutations; i++) {
            int octaveIdx = i / (checkedNotes.length * checkedIntervals.length);
            octaveKeys[i] = checkedOctaves[octaveIdx];
            int noteIdx = (i / checkedIntervals.length) % checkedNotes.length;
            noteKeys[i] = checkedNotes[noteIdx];
            int intervalIdx = i % checkedIntervals.length;
            intervalKeys[i] = checkedIntervals[intervalIdx];
        }
    }

    private void refreshFilenames() {
        StringBuilder text = new StringBuilder();
        if (filenames.length > 0) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            String ankiDir = preferences.getString(SettingsFragment.KEY_ANKI_DIR_PREFERENCE, SettingsFragment.DEFAULT_ANKI_DIR);

            final FilenameAdapter.UriPathName[] uriPathNames = new FilenameAdapter.UriPathName[filenames.length];
            for (int i = 0; i < uriPathNames.length; i++) {
                String filename = filenames[i];
                String name;
                String path = null;
                Uri uri;
                if (filename.startsWith("[sound:")) {
                    name = filename;
                    path = ankiDir + AnkiDroidHelper.DIR_MEDIA
                            + filename.substring(7, filename.length() - 1);
                    File file = new File(path);
                    if (!file.exists()) {
                        uri = null;
                    } else {
                        uri = Uri.fromFile(file);
                    }
                } else {
                    uri = Uri.parse(filename);
                    Cursor cursor = getContentResolver().query(uri, null, null, null, null);
                    int nameIdx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    cursor.moveToFirst();
                    name = cursor.getString(nameIdx);
                    cursor.close();
                }
                uriPathNames[i] = new FilenameAdapter.UriPathName(uri, path, name);
            }
            final FilenameAdapter.UriPathName uriFirst = uriPathNames[0];

            text.append(uriFirst.getName());

            actionPlay.setEnabled(true);
            if (filenames.length > 1) {
                text.append(getString(R.string.additional_filenames, filenames.length - 1));

                actionPlay.setText(R.string.view_all);
                actionPlay.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        FilenameAdapter.UriPathName[] filenames = new FilenameAdapter.UriPathName[uriPathNames.length];
                        for (int i = 0; i < uriPathNames.length; i++) {
                            FilenameAdapter.UriPathName uriPathName = uriPathNames[i];
                            String startNote = i < noteKeys.length || i < octaveKeys.length ? noteKeys[i] + octaveKeys[i] : getString(R.string.unassigned);
                            String interval = i < intervalKeys.length ? intervalKeys[i] : getString(R.string.unassigned);
                            String label = getString(
                                    R.string.filename_with_key,
                                    i + 1,
                                    uriPathName.getName(),
                                    startNote,
                                    interval);
                            filenames[i] = new FilenameAdapter.UriPathName(uriPathName.getUri(), uriPathName.getPath(), label);
                        }
                        openFilenamesDialog(filenames);
                    }
                });
            } else {
                actionPlay.setText(R.string.play);
                actionPlay.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        soundPlayer.play(uriFirst.getUri(), uriFirst.getPath());
                    }
                });
            }
        } else {
            resetPlayButton();
        }
        textFilename.setText(text);
    }

    public void openFilenamesDialog(FilenameAdapter.UriPathName[] uriPathNames) {
        ViewGroup viewGroup = findViewById(R.id.content);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_filenames, viewGroup, false);
        RecyclerView recyclerView = dialogView.findViewById(R.id.recyclerView);
        recyclerView.setAdapter(new FilenameAdapter(uriPathNames, soundPlayer));
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        new AlertDialog.Builder(this)
                .setView(dialogView)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .show();
    }

    private void configureRecordingButtons() {
        // @todo: request file permission aswell
        Button actionRecordingStart = findViewById(R.id.actionRecordingStart);
        actionRecordingStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{
                                    Manifest.permission.RECORD_AUDIO},
                            PERMISSIONS_REQUEST_RECORD_AUDIO
                    );
                    return;
                }
                MediaProjectionManager mediaProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
                Intent intent = mediaProjectionManager.createScreenCaptureIntent();
                startActivityForResult(intent, ACTION_SCREEN_CAPTURE);
            }
        });

        Button actionRecordingStop = findViewById(R.id.actionRecordingStop);
        actionRecordingStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, AudioCaptureService.class);
                intent.setAction(AudioCaptureService.ACTION_STOP);
                startService(intent);
            }
        });
    }

    private void configureClearAllButton() {
        final Button actionClearAll = findViewById(R.id.actionClearAll);
        actionClearAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filenames = new String[]{};
                resetPlayButton();
                textFilename.setText("");
                checkNoteAny.setChecked(true);
                checkOctaveAny.setChecked(true);
                radioGroupDirection.check(findViewById(R.id.radioDirectionAny).getId());
                radioGroupTiming.check(findViewById(R.id.radioTimingAny).getId());
                checkIntervalAny.setChecked(true);
                inputTempo.setText("");
                inputInstrument.setText("");
            }
        });
    }

    private void resetPlayButton() {
        actionPlay.setText(R.string.play);
        actionPlay.setOnClickListener(null);
        actionPlay.setEnabled(false);
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
        boolean batchNoticeSeen = preferences.getBoolean(REF_DB_BATCH_ADDING_NOTICE_SEEN, false);
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
                    editor.putBoolean(REF_DB_BATCH_ADDING_NOTICE_SEEN, true);
                    editor.apply();
                }
                openChooser();
            }
        }).show();
    }

    private void openChooser() {
        Intent intent = new Intent()
                .setAction(Intent.ACTION_OPEN_DOCUMENT)
                .setType("*/*")
                .addCategory(Intent.CATEGORY_OPENABLE)
                .putExtra(Intent.EXTRA_LOCAL_ONLY, true)
                .putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                .putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"audio/*", "video/*"});

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
            refreshKeys();
            afterAdding = false;
            refreshFilenames();
            if (filenames.length > 1) {
                actionPlay.callOnClick();
            }
        }

        if (requestCode == ACTION_SCREEN_CAPTURE && resultCode == RESULT_OK) {
            Intent intent = new Intent(this, AudioCaptureService.class);
            intent.setAction(AudioCaptureService.ACTION_START);
            intent.putExtra(AudioCaptureService.EXTRA_RESULT_DATA, data);
            startForegroundService(intent);
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
                } catch (Throwable e) {
                    handleError(e);
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

                progressDialog = new ProgressDialog(MainActivity.this);
                progressDialog.setTitle(R.string.batch_adding_title);
                progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progressDialog.setCancelable(false);
                progressDialog.show();

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            getMusInterval().addToAnki(MainActivity.this, MainActivity.this);
                        } catch (final Throwable t) {
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    progressDialog.dismiss();
                                    handleError(t);
                                }
                            });

                        }
                    }
                }).start();
            }
        });
    }

    @Override
    public void promptAddDuplicate(final MusInterval[] existingMis, final AddingHandler handler) {
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        final boolean tagDuplicates = sharedPreferences.getBoolean(SettingsFragment.KEY_TAG_DUPLICATES_SWITCH, SettingsFragment.DEFAULT_TAG_DUPLICATES_SWITCH);
        final String duplicateTag = TAG_APPLICATION + AnkiDroidHelper.HIERARCHICAL_TAG_SEPARATOR + TAG_DUPLICATE;

        mHandler.post(new DuplicatePromptWorker(this, mHandler, tagDuplicates, duplicateTag, existingMis, handler));
    }

    @Override
    public void addingFinished(final MusInterval newMi) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                progressDialog.dismiss();
                filenames = newMi.sounds;
                noteKeys = newMi.notes;
                octaveKeys = newMi.octaves;
                intervalKeys = newMi.intervals;
                afterAdding = true;
                refreshFilenames();
                savedInstruments.add(newMi.instrument);
                refreshExisting();
                final int nAdded = newMi.sounds.length;
                if (nAdded == 1) {
                    showQuantityMsg(R.plurals.mi_added, nAdded);
                } else if (nAdded > 1) {
                    showQuantityMsg(R.plurals.mi_added, nAdded, nAdded);
                }
            }
        });
    }

    @Override
    public void processException(final Throwable t) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                handleError(t);
            }
        });
    }

    void handleInsertion(MusInterval newMi) {
        String[] tempFilenames = new String[filenames.length + 1];
        System.arraycopy(filenames, 0, tempFilenames, 0, filenames.length);
        tempFilenames[tempFilenames.length - 1] = newMi.sounds[0];
        filenames = tempFilenames;
        refreshFilenames();
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
                    // handle IllegalStateException on unconfirmed permissions in AnkiDroid
                    if (!(e instanceof MusInterval.ValidationException)) {
                        handleError(e);
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
                    MusInterval mi = getMusInterval();

                    final String corruptedTag = TAG_APPLICATION + AnkiDroidHelper.HIERARCHICAL_TAG_SEPARATOR + TAG_CORRUPTED;
                    final String suspiciousTag = TAG_APPLICATION + AnkiDroidHelper.HIERARCHICAL_TAG_SEPARATOR + TAG_SUSPICIOUS;
                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                    boolean tagDuplicates = preferences.getBoolean(SettingsFragment.KEY_TAG_DUPLICATES_SWITCH, SettingsFragment.DEFAULT_TAG_DUPLICATES_SWITCH);
                    final String duplicateTag = !tagDuplicates ? null : TAG_APPLICATION + AnkiDroidHelper.HIERARCHICAL_TAG_SEPARATOR + TAG_DUPLICATE;

                    final NotesIntegrity integrity = new NotesIntegrity(mAnkiDroid, mi, corruptedTag, suspiciousTag, duplicateTag, MainActivity.this);

                    progressDialog = new ProgressDialog(MainActivity.this);
                    progressDialog.setTitle(R.string.integrity_progress_title);
                    progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    progressDialog.setCancelable(false);
                    progressDialog.show();

                    new Thread(new IntegrityCheckWorker(integrity, MainActivity.this, progressDialog, mHandler)).start();
                } catch (Throwable e) {
                    handleError(e);
                }
            }
        });
    }

    @Override
    public void setMessage(final int resId, final Object ...formatArgs) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                progressDialog.setMessage(getString(resId, formatArgs));
            }
        });
    }

    @Override
    protected void onPause() {
        final SharedPreferences.Editor uiDbEditor = getSharedPreferences(REF_DB_STATE, Context.MODE_PRIVATE).edit();

        uiDbEditor.putBoolean(REF_DB_SWITCH_BATCH, switchBatch.isChecked());
        uiDbEditor.putStringSet(REF_DB_SELECTED_FILENAMES, new HashSet<>(Arrays.asList(filenames)));
        uiDbEditor.putBoolean(REF_DB_CHECK_NOTE_ANY, checkNoteAny.isChecked());
        for (int i = 0; i < CHECK_NOTE_IDS.length; i++) {
            uiDbEditor.putBoolean(String.valueOf(CHECK_NOTE_IDS[i]), checkNotes[i].isChecked());
        }
        uiDbEditor.putBoolean(REF_DB_CHECK_OCTAVE_ANY, checkOctaveAny.isChecked());
        for (int i = 0; i < CHECK_OCTAVE_IDS.length; i++) {
            uiDbEditor.putBoolean(String.valueOf(CHECK_OCTAVE_IDS[i]), checkOctaves[i].isChecked());
        }
        uiDbEditor.putInt(REF_DB_RADIO_GROUP_DIRECTION, radioGroupDirection.getCheckedRadioButtonId());
        uiDbEditor.putInt(REF_DB_RADIO_GROUP_TIMING, radioGroupTiming.getCheckedRadioButtonId());
        uiDbEditor.putBoolean(REF_DB_CHECK_INTERVAL_ANY, checkIntervalAny.isChecked());
        for (int i = 0; i < CHECK_INTERVAL_IDS.length; i++) {
            uiDbEditor.putBoolean(String.valueOf(CHECK_INTERVAL_IDS[i]), checkIntervals[i].isChecked());
        }
        uiDbEditor.putString(REF_DB_INPUT_TEMPO, inputTempo.getText().toString());
        uiDbEditor.putString(REF_DB_INPUT_INSTRUMENT, inputInstrument.getText().toString());
        uiDbEditor.putStringSet(REF_DB_SAVED_INSTRUMENTS, savedInstruments);

        uiDbEditor.putBoolean(REF_DB_AFTER_ADDING, afterAdding);
        uiDbEditor.putString(REF_DB_NOTE_KEYS, StringUtil.joinStrings(DB_STRING_ARRAY_SEPARATOR, noteKeys));
        uiDbEditor.putString(REF_DB_OCTAVE_KEYS, StringUtil.joinStrings(DB_STRING_ARRAY_SEPARATOR, octaveKeys));
        uiDbEditor.putString(REF_DB_INTERVAL_KEYS, StringUtil.joinStrings(DB_STRING_ARRAY_SEPARATOR, intervalKeys));

        uiDbEditor.apply();

        super.onPause();
    }

    protected void restoreUiState() {
        final SharedPreferences uiDb = getSharedPreferences(REF_DB_STATE, Context.MODE_PRIVATE);
        switchBatch.setChecked(uiDb.getBoolean(REF_DB_SWITCH_BATCH, false));
        Set<String> storedFilenames = uiDb.getStringSet(REF_DB_SELECTED_FILENAMES, new HashSet<String>());
        filenames = storedFilenames.toArray(new String[0]);
        refreshFilenames();
        checkNoteAny.setChecked(uiDb.getBoolean(REF_DB_CHECK_NOTE_ANY, true));
        for (int i = 0; i < CHECK_NOTE_IDS.length; i++) {
            checkNotes[i].setChecked(uiDb.getBoolean(String.valueOf(CHECK_NOTE_IDS[i]), false));
        }
        checkOctaveAny.setChecked(uiDb.getBoolean(REF_DB_CHECK_OCTAVE_ANY, true));
        for (int i = 0; i < CHECK_OCTAVE_IDS.length; i++) {
            checkOctaves[i].setChecked(uiDb.getBoolean(String.valueOf(CHECK_OCTAVE_IDS[i]), false));
        }
        radioGroupDirection.check(uiDb.getInt(REF_DB_RADIO_GROUP_DIRECTION, findViewById(R.id.radioDirectionAny).getId()));
        radioGroupTiming.check(uiDb.getInt(REF_DB_RADIO_GROUP_TIMING, findViewById(R.id.radioTimingAny).getId()));
        checkIntervalAny.setChecked(uiDb.getBoolean(REF_DB_CHECK_INTERVAL_ANY, true));
        for (int i = 0; i < CHECK_INTERVAL_IDS.length; i++) {
            checkIntervals[i].setChecked(uiDb.getBoolean(String.valueOf(CHECK_INTERVAL_IDS[i]), false));
        }
        inputTempo.setText(uiDb.getString(REF_DB_INPUT_TEMPO, ""));
        inputInstrument.setText(uiDb.getString(REF_DB_INPUT_INSTRUMENT, ""));
        savedInstruments = (HashSet<String>) uiDb.getStringSet(REF_DB_SAVED_INSTRUMENTS, new HashSet<String>());
        inputInstrument.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, savedInstruments.toArray(new String[0])));

        afterAdding = uiDb.getBoolean(REF_DB_AFTER_ADDING, false);
        noteKeys = StringUtil.splitStrings(DB_STRING_ARRAY_SEPARATOR, uiDb.getString(REF_DB_NOTE_KEYS, ""));
        octaveKeys = StringUtil.splitStrings(DB_STRING_ARRAY_SEPARATOR, uiDb.getString(REF_DB_OCTAVE_KEYS, ""));
        intervalKeys = StringUtil.splitStrings(DB_STRING_ARRAY_SEPARATOR, uiDb.getString(REF_DB_INTERVAL_KEYS, ""));

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
            case PERMISSIONS_REQUEST_RECORD_AUDIO:
                if (grantResults.length > 0) {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        // @todo: add callback
                    } else {
                        showMsg(R.string.recording_permission_denied);
                    }
                }
                break;
        }
    }

    private MusInterval getMusInterval() throws MusInterval.ModelValidationException, MusInterval.TempoNotInRangeException {
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
        final String storedDeck = sharedPreferences.getString(SettingsFragment.KEY_DECK_PREFERENCE, MusInterval.Builder.DEFAULT_DECK_NAME);
        final String storedModel = sharedPreferences.getString(SettingsFragment.KEY_MODEL_PREFERENCE, MusInterval.Builder.DEFAULT_MODEL_NAME);

        String[] notes = !checkNoteAny.isChecked() ? getCheckedValues(checkNotes) : null;
        String[] octaves = !checkOctaveAny.isChecked() ? getCheckedValues(checkOctaves) : null;
        String[] intervals = !checkIntervalAny.isChecked() ? getCheckedValues(checkIntervals, CHECK_INTERVAL_ID_VALUES) : null;

        MusInterval.Builder builder = new MusInterval.Builder(mAnkiDroid)
                .deck(storedDeck)
                .model(storedModel)
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

        final boolean useDefaultModel = sharedPreferences.getBoolean(SettingsFragment.KEY_USE_DEFAULT_MODEL_CHECK, SettingsFragment.DEFAULT_USE_DEFAULT_MODEL_CHECK);
        if (useDefaultModel) {
            Resources res = getResources();
            final String[] fields = res.getStringArray(R.array.fields);
            final String[] cardNames = res.getStringArray(R.array.card_names);
            final String[] qfmt = res.getStringArray(R.array.qfmt);
            final String[] afmt = res.getStringArray(R.array.afmt);
            final String css = res.getString(R.string.css);
            builder.default_model(true)
                    .fields(fields)
                    .cards(cardNames)
                    .qfmt(qfmt)
                    .afmt(afmt)
                    .css(css);
            Long modelId = mAnkiDroid.findModelIdByName(MusInterval.Builder.DEFAULT_MODEL_NAME);
            if (modelId != null) {
                updateModelPreferences(modelId);
            }
        }

        String[] signature = MusInterval.Fields.getSignature(versionField);
        final Map<String, String> storedFields = new HashMap<>();
        for (String fieldKey : signature) {
            String fieldPreferenceKey = SettingsFragment.getFieldPreferenceKey(fieldKey);
            storedFields.put(fieldKey, sharedPreferences.getString(fieldPreferenceKey, ""));
        }
        builder.model_fields(storedFields);

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

    void handleError(Throwable err) {
        try {
            throw err;
        } catch (MusInterval.Exception e) {
            processMusIntervalException(e);
        } catch (AnkiDroidHelper.InvalidAnkiDatabaseException e) {
            processInvalidAnkiDatabase(e);
        } catch (Throwable e) {
            processUnknownException(e);
        }
    }

    private void processMusIntervalException(MusInterval.Exception miException) {
        try {
            throw miException;
        } catch (MusInterval.MandatorySelectionEmptyException e) {
            showMsg(R.string.empty_mandatory_selection, getString(FIELD_LABEL_STRING_IDS_SELECTION.get(e.getField())));
        } catch (MusInterval.UnexpectedSoundsAmountException e) {
            final int expected = e.getExpectedAmount();
            final int provided = e.getProvidedAmount();
            final boolean expectedSingle = expected == 1;
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
                            handleCreateDefaultModel();
                        }
                    })
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    })
                    .show();
        } catch (final MusInterval.DefaultModelOutdatedException e) {
            final String modelName = e.getModelName();
            new AlertDialog.Builder(this)
                    .setMessage(String.format(
                            getResources().getString(R.string.update_model),
                            modelName))
                    .setPositiveButton(R.string.update, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Long updatedModelId = mAnkiDroid.updateCustomModel(
                                    mAnkiDroid.findModelIdByName(modelName),
                                    e.getFields(),
                                    e.getCards(),
                                    e.getQfmt(),
                                    e.getAfmt(),
                                    e.getCss()
                            );
                            if (updatedModelId != null) {
                                updateModelPreferences(updatedModelId);
                                showMsg(R.string.update_model_success, MusInterval.Builder.DEFAULT_MODEL_NAME);
                            } else {
                                showMsg(R.string.update_model_error);
                            }
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
            showMsg(R.string.mandatory_field_empty, getString(FIELD_LABEL_STRING_IDS_SINGULAR.get(e.getField())));
        } catch (MusInterval.SoundAlreadyAddedException e) {
            showMsg(R.string.already_added);
        } catch (MusInterval.AddSoundFileException e) {
            showMsg(R.string.add_file_error);
        } catch (MusInterval.TempoNotInRangeException e) {
            showMsg(R.string.tempo_not_in_range, MusInterval.Fields.Tempo.MIN_VALUE, MusInterval.Fields.Tempo.MAX_VALUE);
        } catch (MusInterval.Exception e) {
            showMsg(R.string.unknown_adding_error);
        }
    }

    private void handleCreateDefaultModel() {
        Resources res = getResources();
        String[] fields = res.getStringArray(R.array.fields);
        String[] cardNames = res.getStringArray(R.array.card_names);
        String[] qfmt = res.getStringArray(R.array.qfmt);
        String[] afmt = res.getStringArray(R.array.afmt);
        String css = res.getString(R.string.css);

        final String modelName = MusInterval.Builder.DEFAULT_MODEL_NAME;
        final Long newModelId = mAnkiDroid.addNewCustomModel(
                modelName,
                fields,
                cardNames,
                qfmt,
                afmt,
                css
        );
        if (newModelId != null) {
            updateModelPreferences(newModelId);
            refreshExisting();
            refreshPermutations();
            showMsg(R.string.create_model_success, modelName);
        } else {
            showMsg(R.string.create_model_error);
        }
    }

    private void updateModelPreferences(long modelId) {
        String[] mainSignature = MusInterval.Fields.getSignature(false);
        SharedPreferences.Editor preferenceEditor = PreferenceManager.getDefaultSharedPreferences(MainActivity.this).edit();
        for (String fieldKey : mainSignature) {
            String fieldPreferenceKey = SettingsFragment.getFieldPreferenceKey(fieldKey);
            preferenceEditor.putString(fieldPreferenceKey, fieldKey);
            String modelFieldPreferenceKey = SettingsFragment.getModelFieldPreferenceKey(modelId, fieldPreferenceKey);
            preferenceEditor.putString(modelFieldPreferenceKey, fieldKey);
        }
        preferenceEditor.apply();
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

    void showMsg(int msgResId, Object ...formatArgs) {
        Toast.makeText(MainActivity.this, getResources().getString(msgResId, formatArgs), Toast.LENGTH_LONG).show();
    }

    void showQuantityMsg(int msgResId, int quantity, Object ...formatArgs) {
        Toast.makeText(MainActivity.this, getResources().getQuantityString(msgResId, quantity, formatArgs), Toast.LENGTH_LONG).show();
    }
}
