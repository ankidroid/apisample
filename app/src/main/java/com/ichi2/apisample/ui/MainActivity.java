package com.ichi2.apisample.ui;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.DocumentsContract;
import android.provider.OpenableColumns;
import android.provider.Settings;
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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.documentfile.provider.DocumentFile;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.PreferenceManager;

import com.ichi2.apisample.BuildConfig;
import com.ichi2.apisample.R;
import com.ichi2.apisample.helper.AnkiDroidHelper;
import com.ichi2.apisample.helper.StringUtil;
import com.ichi2.apisample.helper.UriUtil;
import com.ichi2.apisample.model.AddingHandler;
import com.ichi2.apisample.model.AddingPrompter;
import com.ichi2.apisample.model.MusInterval;
import com.ichi2.apisample.model.NotesIntegrity;
import com.ichi2.apisample.model.ProgressIndicator;
import com.ichi2.apisample.ui.settings.MappingPreference;
import com.ichi2.apisample.ui.settings.SettingsActivity;
import com.ichi2.apisample.ui.settings.SettingsFragment;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public class MainActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback, AddingPrompter, ProgressIndicator {
    public static final String ACTION_CLOSE_CAPTURING = "MainActivity:CloseCapturing";

    private static final int AD_PERM_REQUEST = 0;
    private static final int PERMISSIONS_REQUEST_EXTERNAL_STORAGE_CALLBACK_OPEN_CHOOSER = 1;
    private static final int PERMISSIONS_REQUEST_RECORD_AUDIO_CALLBACK_CAPTURE = 2;
    private static final int PERMISSIONS_REQUEST_EXTERNAL_STORAGE_CALLBACK_CAPTURE = 3;

    private static final int ACTION_SELECT_FILE = 10;
    private static final int ACTION_SCREEN_CAPTURE = 11;
    private static final int ACTION_PROMPT_OVERLAY_PERMISSION = 12;

    private static final String TAG_APPLICATION = "mi2a";
    private static final String TAG_DUPLICATE = "duplicate";
    private static final String TAG_CORRUPTED = "corrupted";
    private static final String TAG_SUSPICIOUS = "suspicious";

    static final String REF_DB_STATE = "com.ichi2.apisample.uistate";
    static final String REF_DB_SELECTED_FILENAMES = "selectedFilenamesArr";
    static final String REF_DB_MISMATCHING_SORTING = "mismatchingSorting";
    static final String REF_DB_INTERSECTING_NAMES = "intersectingNames";
    static final String REF_DB_SORT_BY_NAME = "sortByName";
    static final String REF_DB_INTERSECTING_DATES = "intersectingDates";
    static final String REF_DB_SORT_BY_DATE = "sortByDate";
    static final String REF_DB_AFTER_SELECTING = "afterSelecting";
    static final String REF_DB_AFTER_CAPTURING = "afterCapturing";
    static final String REF_DB_IS_CAPTURING = "isCapturing";
    static final String REF_DB_AFTER_ADDING = "afterAdding";
    private static final String REF_DB_SWITCH_BATCH = "switchBatch";
    private static final String REF_DB_CHECK_NOTE_ANY = "checkNoteAny";
    private static final String REF_DB_CHECK_OCTAVE_ANY = "checkOctaveAny";
    private static final String REF_DB_RADIO_GROUP_DIRECTION = "radioGroupDirection";
    private static final String REF_DB_RADIO_GROUP_TIMING = "radioGroupTiming";
    private static final String REF_DB_CHECK_INTERVAL_ANY = "checkIntervalAny";
    private static final String REF_DB_INPUT_TEMPO = "inputTempo";
    private static final String REF_DB_INPUT_INSTRUMENT = "inputInstrument";
    private static final String REF_DB_INPUT_FIRST_NOTE_DURATION_COEFFICIENT = "firstNoteDurationCoefficient";
    private static final String REF_DB_BATCH_ADDING_NOTICE_SEEN = "batchAddingNoticeSeen";
    private static final String REF_DB_NOTE_KEYS = "noteKeys";
    private static final String REF_DB_OCTAVE_KEYS = "octaveKeys";
    private static final String REF_DB_INTERVAL_KEYS = "intervalKeys";
    private static final String DB_STRING_ARRAY_SEPARATOR = ",";

    private final static Map<String, Integer> FIELD_LABEL_STRING_IDS_SINGULAR = new HashMap<String, Integer>() {{
        put(MusInterval.Fields.DIRECTION, R.string.direction);
        put(MusInterval.Fields.TIMING, R.string.timing);
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
    Button actionPlay;
    private Button actionCaptureAudio;
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
    private EditText inputFirstNoteDurationCoefficient;
    private TextView labelExisting;
    private Button actionMarkExisting;

    private Toast toast;
    private String lastToastText;

    private ProgressDialog progressDialog;

    Handler handler;

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

    final ArrayList<AlertDialog> activeOnStartDialogs = new ArrayList<>();
    final DialogInterface.OnDismissListener onStartDialogDismissListener = new DialogInterface.OnDismissListener() {
        @Override
        public void onDismiss(DialogInterface dialogInterface) {
            activeOnStartDialogs.remove(dialogInterface);
        }
    };

    String[] filenames = new String[]{};
    private String[] selectedFilenames;
    boolean mismatchingSorting;
    boolean intersectingNames;
    boolean sortByName;
    static final Comparator<String> COMPARATOR_FILE_NAME = new Comparator<String>() {
        @Override
        public int compare(String s, String t1) {
            return s.compareTo(t1);
        }
    };
    boolean intersectingDates;
    boolean sortByDate;
    static final Comparator<Long> COMPARATOR_FILE_DATE = new Comparator<Long>() {
        @Override
        public int compare(Long s, Long t1) {
            return Long.compare(s, t1);
        }
    };

    private boolean afterSelecting;
    private boolean afterCapturing;
    boolean isCapturing;

    SoundPlayer soundPlayer;

    private Integer permutationsNumber;

    private ArrayAdapter<String> instrumentsAdapter;

    private AnkiDroidHelper mAnkiDroid;

    private boolean afterAdding;

    String[] noteKeys = new String[]{};
    String[] octaveKeys = new String[]{};
    String[] intervalKeys = new String[]{};

    private final Map<String, BroadcastReceiver> actionReceivers = new HashMap<String, BroadcastReceiver>() {{
        put(AudioCaptureService.ACTION_FILES_UPDATED, new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String[] newFilenames;
                if (intent.hasExtra(AudioCaptureService.EXTRA_URI_STRING)) {
                    clearSelectedFilenames();
                    clearAddedFilenames();
                    String uriString = intent.getStringExtra(AudioCaptureService.EXTRA_URI_STRING);
                    newFilenames = new String[filenames.length + 1];
                    System.arraycopy(filenames, 0, newFilenames, 0, filenames.length);
                    newFilenames[filenames.length] = uriString;
                    afterCapturing = true;
                } else {
                    newFilenames = new String[filenames.length - 1];
                    System.arraycopy(filenames, 0, newFilenames, 0, filenames.length - 1);
                    if (newFilenames.length == 0) {
                        afterCapturing = false;
                    }
                }
                filenames = newFilenames;
                refreshFilenames();
            }
        });
        put(AudioCaptureService.ACTION_CLOSED, new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                isCapturing = false;
            }
        });
    }};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar mainToolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(mainToolbar);

        switchBatch = findViewById(R.id.switchBatch);
        textFilename = findViewById(R.id.textFilename);
        actionPlay = findViewById(R.id.actionPlay);
        actionCaptureAudio = findViewById(R.id.actionCaptureAudio);
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
        inputFirstNoteDurationCoefficient = findViewById(R.id.inputFirstNoteDurationCoefficient);
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
        inputFirstNoteDurationCoefficient.addTextChangedListener(new FieldInputTextWatcher(this));
        switchBatch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                onNoteCheckChangeListener.setEnableMultiple(b);
                onOctaveCheckChangeListener.setEnableMultiple(b);
                onIntervalCheckChangeListener.setEnableMultiple(b);
            }
        });

        handler = new Handler();

        configureClearAllButton();
        configureCaptureAudioButton();
        configureSelectFileButton();
        configureMarkExistingButton();
        configureAddToAnkiButton();
        configureSettingsButton();
        configureCheckIntegrityButton();

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
        } catch (MusInterval.ModelException e) {
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

        refreshPreferences();

        LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(this);
        for (Map.Entry<String, BroadcastReceiver> actionReceiver : actionReceivers.entrySet()) {
            broadcastManager.registerReceiver(actionReceiver.getValue(), new IntentFilter(actionReceiver.getKey()));
        }

        Set<String> instrumentOptions = new HashSet<>();
        try {
            MusInterval searchMi = getMusInterval(true);
            MusInterval[] mis = searchMi.getExisting();
            for (MusInterval mi : mis) {
                instrumentOptions.add(mi.instrument);
            }
        } catch (Throwable t) {
            // simply don't fill the options if there was an error
        }
        instrumentsAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, new ArrayList<>(instrumentOptions));
        inputInstrument.setAdapter(instrumentsAdapter);

        refreshExisting();
        refreshPermutations();
        boolean selected = selectedFilenames != null;
        if (selected) {
            afterCapturing = false;
            afterAdding = false;
            filenames = selectedFilenames;
            selectedFilenames = null;
        } else {
            filenames = getStoredFilenames(this);
        }
        refreshFilenames();
        if (selected && filenames.length > 1) {
            actionPlay.callOnClick();
            if (mismatchingSorting) {
                new AlertDialog.Builder(this)
                        .setMessage(intersectingNames ? R.string.intersecting_names : R.string.intersecting_dates)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        })
                        .show();
            }
        }

        soundPlayer = new SoundPlayer(this);
    }

    private void refreshPreferences() {
        if (mAnkiDroid.shouldRequestPermission()) {
            return;
        }
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean useDefaultModel = preferences.getBoolean(SettingsFragment.KEY_USE_DEFAULT_MODEL_CHECK, SettingsFragment.DEFAULT_USE_DEFAULT_MODEL_CHECK);
        if (useDefaultModel) {
            Long modelId = mAnkiDroid.findModelIdByName(MusInterval.Builder.DEFAULT_MODEL_NAME);
            if (modelId != null) {
                updateDefaultModelPreferences(modelId);
            }
        }
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

    private void clearSelectedFilenames() {
        if (afterSelecting) {
            filenames = new String[]{};
            refreshFilenames();
            afterSelecting = false;
            mismatchingSorting = false;
            intersectingNames = false;
            sortByName = false;
            intersectingDates = false;
            sortByDate = false;
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
        if (filenames.length > 0) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            String ankiDir = preferences.getString(SettingsFragment.KEY_ANKI_DIR_PREFERENCE, SettingsFragment.DEFAULT_ANKI_DIR);

            final ContentResolver resolver = getContentResolver();
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
                    uri = file.exists() ? Uri.fromFile(file) : null;
                } else {
                    uri = Uri.parse(filename);

                    boolean exists;
                    if ("file".equals(uri.getScheme())) {
                        File file = new File(uri.getPath());
                        exists = file.exists();
                    } else {
                        DocumentFile documentFile = DocumentFile.fromSingleUri(MainActivity.this, uri);
                        exists = documentFile != null && documentFile.exists();
                    }
                    if (!exists) {
                        filenames = new String[]{};
                        refreshFilenames();
                        showMsg(R.string.filenames_refreshing_error);
                        return;
                    }

                    Uri contentUri = UriUtil.getContentUri(this, uri);
                    Cursor cursor = resolver.query(contentUri, null, null, null, null);
                    int nameIdx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    cursor.moveToFirst();
                    name = cursor.getString(nameIdx);
                    cursor.close();
                }
                String label = getFilenameLabel(name, i);
                uriPathNames[i] = new FilenameAdapter.UriPathName(uri, path, name, label);
            }

            final FilenameAdapter.UriPathName uriFirst = uriPathNames[0];

            actionPlay.setEnabled(true);
            if (filenames.length > 1) {
                actionPlay.setText(R.string.view_all);
                actionPlay.setOnClickListener(new OnViewAllClickListener(this, uriPathNames));
            } else {
                actionPlay.setText(R.string.play);
                actionPlay.setOnClickListener(new OnPlayClickListener(this, uriFirst, actionPlay));
            }

            refreshFilenameText(uriFirst.getName());
        } else {
            resetPlayButton();
            refreshFilenameText("");
        }
    }

    void refreshFilenameText(String firstName) {
        String text = firstName;
        if (filenames != null && filenames.length > 1) {
            text += getString(R.string.additional_filenames, filenames.length - 1);
        }
        textFilename.setText(text);
    }

    String getFilenameLabel(String name, int pos) {
        String startNote = pos < noteKeys.length || pos < octaveKeys.length ? noteKeys[pos] + octaveKeys[pos] : getString(R.string.unassigned);
        String interval = pos < intervalKeys.length ? intervalKeys[pos] : getString(R.string.unassigned);
        return getString(
                R.string.filename_with_key,
                pos + 1,
                name,
                startNote,
                interval
        );
    }

    private void configureClearAllButton() {
        final Button actionClearAll = findViewById(R.id.actionClearAll);
        actionClearAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filenames = new String[]{};
                afterAdding = false;
                mismatchingSorting = false;
                intersectingNames = false;
                sortByName = false;
                intersectingDates = false;
                sortByDate = false;
                afterSelecting = false;
                afterCapturing = false;
                resetPlayButton();
                textFilename.setText("");
                checkNoteAny.setChecked(true);
                checkOctaveAny.setChecked(true);
                radioGroupDirection.check(findViewById(R.id.radioDirectionAny).getId());
                radioGroupTiming.check(findViewById(R.id.radioTimingAny).getId());
                checkIntervalAny.setChecked(true);
                inputTempo.setText("");
                inputInstrument.setText("");
                inputFirstNoteDurationCoefficient.setText("");
            }
        });
    }

    private void resetPlayButton() {
        actionPlay.setText(R.string.play);
        actionPlay.setOnClickListener(null);
        actionPlay.setEnabled(false);
    }

    private void configureCaptureAudioButton() {
        actionCaptureAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                    showMsg(R.string.recording_unsupported);
                    return;
                }
                if (isCapturing) {
                    closeCapturing();
                }
                handleCaptureAudio();
            }
        });
    }

    private void handleCaptureAudio () {
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{
                            Manifest.permission.RECORD_AUDIO},
                    PERMISSIONS_REQUEST_RECORD_AUDIO_CALLBACK_CAPTURE
            );
            return;
        }
        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{
                            Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSIONS_REQUEST_EXTERNAL_STORAGE_CALLBACK_CAPTURE
            );
            return;
        }
        if (!Settings.canDrawOverlays(MainActivity.this)) {
            Intent intent = new Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName())
            );
            startActivityForResult(intent, ACTION_PROMPT_OVERLAY_PERMISSION);
            return;
        }

        if (afterCapturing) {
            new AlertDialog.Builder(this)
                    .setMessage(getResources().getQuantityString(R.plurals.recordings_clearing_prompt, filenames.length, filenames.length))
                    .setPositiveButton(R.string.add_more, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int which) {
                            handleInitiateCapturing();
                        }
                    })
                    .setNegativeButton(R.string.clear, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            for (String filename : filenames) {
                                Uri uri = Uri.parse(filename);
                                String path = uri.getPath();
                                new File(path).delete();
                            }
                            filenames = new String[]{};
                            refreshFilenames();
                            afterCapturing = false;
                            handleInitiateCapturing();
                        }
                    })
                    .setNeutralButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    })
                    .show();
            return;
        }

        handleInitiateCapturing();
    }

    void closeCapturing() {
        LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(ACTION_CLOSE_CAPTURING));
        isCapturing = false;
    }

    private void handleInitiateCapturing() {
        MediaProjectionManager mediaProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        Intent intent = mediaProjectionManager.createScreenCaptureIntent();
        startActivityForResult(intent, ACTION_SCREEN_CAPTURE);
    }

    private void configureSelectFileButton() {
        final Button actionSelectFile = findViewById(R.id.actionSelectFile);
        actionSelectFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            PERMISSIONS_REQUEST_EXTERNAL_STORAGE_CALLBACK_OPEN_CHOOSER
                    );
                    return;
                }
                if (isCapturing) {
                    closeCapturing();
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
                })
                .setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        if (checkRemember.isChecked()) {
                            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(MainActivity.this).edit();
                            editor.putBoolean(REF_DB_BATCH_ADDING_NOTICE_SEEN, true);
                            editor.apply();
                        }
                        openChooser();
                    }
                })
                .show();
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
    @TargetApi(Build.VERSION_CODES.Q)
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case ACTION_SELECT_FILE:
                if (resultCode != RESULT_OK) {
                    return;
                }

                final ArrayList<Uri> uriList = new ArrayList<>();
                if (data != null) {
                    ClipData clipData = data.getClipData();
                    if (clipData != null) {
                        for (int i = 0; i < clipData.getItemCount(); i++) {
                            Uri uri = clipData.getItemAt(i).getUri();
                            uriList.add(uri);
                        }
                    } else {
                        Uri uri = data.getData();
                        uriList.add(uri);
                    }
                }

                ContentResolver resolver = getContentResolver();
                final ArrayList<String> names = new ArrayList<>(uriList.size());
                final ArrayList<Long> dates = new ArrayList<>(uriList.size());
                for (Uri uri : uriList) {
                    Cursor cursor = resolver.query(uri, null, null, null, null);
                    int nameIdx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    int dateIdx = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_LAST_MODIFIED);
                    cursor.moveToFirst();
                    names.add(cursor.getString(nameIdx));
                    dates.add(cursor.getLong(dateIdx));
                    cursor.close();
                }

                intersectingNames = new HashSet<>(names).size() != names.size();
                final ArrayList<String> namesSorted = new ArrayList<>(names);
                namesSorted.sort(COMPARATOR_FILE_NAME);
                intersectingDates = new HashSet<>(dates).size() != dates.size();
                final ArrayList<Long> datesSorted = new ArrayList<>(dates);
                datesSorted.sort(COMPARATOR_FILE_DATE);

                boolean areKeysUnique = !intersectingNames && !intersectingDates;

                String[] uriStrings;
                if (intersectingNames && intersectingDates) {
                    uriStrings = new String[]{};
                    new AlertDialog.Builder(this)
                            .setMessage(R.string.intersecting_sorting_keys)
                            .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                }
                            })
                            .show();
                } else {
                    uriStrings = new String[uriList.size()];
                    for (int i = 0; i < uriList.size(); i++) {
                        int sortedNameIdx = names.indexOf(namesSorted.get(i));
                        int sortedDateIdx = dates.indexOf(datesSorted.get(i));
                        if (areKeysUnique && sortedNameIdx != sortedDateIdx) {
                            mismatchingSorting = true;
                            selectedFilenames = new String[]{};
                            showMismatchingSortingDialog(uriList, names, namesSorted, dates, datesSorted);
                            return;
                        }
                        uriStrings[i] = uriList.get(!intersectingNames ? sortedNameIdx : sortedDateIdx).toString();
                    }
                }

                selectedFilenames = uriStrings;
                sortByName = !intersectingNames && intersectingDates;
                sortByDate = !intersectingDates && intersectingNames;
                mismatchingSorting = sortByName || sortByDate;
                afterSelecting = true;
                break;
            case ACTION_SCREEN_CAPTURE:
                if (resultCode != RESULT_OK) {
                    return;
                }
                Intent intent = new Intent(this, AudioCaptureService.class);
                intent.putExtra(AudioCaptureService.EXTRA_RESULT_DATA, data);
                if (afterCapturing) {
                    intent.putExtra(AudioCaptureService.EXTRA_RECORDINGS, filenames);
                }
                startForegroundService(intent);
                isCapturing = true;
                break;
            case ACTION_PROMPT_OVERLAY_PERMISSION:
                if (!Settings.canDrawOverlays(this)) {
                    showMsg(R.string.display_over_apps_permission_denied);
                } else {
                    handleCaptureAudio();
                }
        }
    }

    private void showMismatchingSortingDialog(final ArrayList<Uri> uriList, final ArrayList<String> names, final ArrayList<String> namesSorted, final ArrayList<Long> lastModifiedValues, final ArrayList<Long> lastModifiedSorted) {
        new AlertDialog.Builder(this)
                .setMessage(R.string.mismatching_sorting)
                .setPositiveButton(R.string.sort_by_date, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String[] uriStrings = new String[uriList.size()];
                        for (int j = 0; j < uriList.size(); j++) {
                            int sortedLastModifiedIdx = lastModifiedValues.indexOf(lastModifiedSorted.get(j));
                            uriStrings[j] = uriList.get(sortedLastModifiedIdx).toString();
                        }
                        sortByDate = true;
                        sortByName = false;
                        filenames = uriStrings;
                        afterSelecting = true;
                        refreshFilenames();
                        actionPlay.callOnClick();
                    }
                })
                .setNegativeButton(R.string.sort_by_name, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String[] uriStrings = new String[uriList.size()];
                        for (int j = 0; j < uriList.size(); j++) {
                            int sortedNameIdx = names.indexOf(namesSorted.get(j));
                            uriStrings[j] = uriList.get(sortedNameIdx).toString();
                        }
                        sortByName = true;
                        sortByDate = false;
                        filenames = uriStrings;
                        afterSelecting = true;
                        refreshFilenames();
                        actionPlay.callOnClick();
                    }
                })
                .show();
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
                if (isCapturing) {
                    closeCapturing();
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
                            handler.post(new Runnable() {
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

        this.handler.post(new DuplicatePromptWorker(this, tagDuplicates, duplicateTag, existingMis, handler));
    }

    @Override
    public void addingFinished(final MusInterval.AddingResult addingResult) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                progressDialog.dismiss();

                final String[] originalFilenames = addingResult.getOriginalSounds();
                MusInterval newMi = addingResult.getMusInterval();
                filenames = newMi.sounds;
                afterSelecting = false;
                afterCapturing = false;
                noteKeys = newMi.notes;
                octaveKeys = newMi.octaves;
                intervalKeys = newMi.intervals;
                afterAdding = true;
                mismatchingSorting = false;
                intersectingNames = false;
                sortByName = false;
                intersectingDates = false;
                sortByDate = false;
                refreshFilenames();
                String addedInstrument = newMi.instrument;
                if (instrumentsAdapter.getPosition(addedInstrument) == -1) {
                    instrumentsAdapter.add(addedInstrument);
                }
                refreshExisting();
                final int nAdded = newMi.sounds.length;
                if (nAdded == 1) {
                    showQuantityMsg(R.plurals.mi_added, nAdded);
                } else if (nAdded > 1) {
                    showQuantityMsg(R.plurals.mi_added, nAdded, nAdded);
                } else {
                    return;
                }

                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                String filesDeletion = preferences.getString(SettingsFragment.KEY_FILES_DELETION_PREFERENCE, SettingsFragment.DEFAULT_FILES_DELETION);
                switch (filesDeletion) {
                    case SettingsFragment.VALUE_FILES_DELETION_DISABLED:
                        break;
                    case SettingsFragment.VALUE_FILES_DELETION_CREATED_ONLY:
                        deleteCapturedFiles(originalFilenames);
                        break;
                    case SettingsFragment.VALUE_FILES_DELETION_ALL:
                        deleteAddedFiles(originalFilenames);
                        break;
                    default:
                    case SettingsFragment.VALUE_FILES_DELETION_ALWAYS_ASK:
                        new AlertDialog.Builder(MainActivity.this)
                                .setMessage(R.string.files_deletion_prompt)
                                .setPositiveButton(R.string.files_deletion_all, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        deleteAddedFiles(originalFilenames);
                                    }
                                })
                                .setNegativeButton(R.string.files_deletion_recorded, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        deleteCapturedFiles(originalFilenames);
                                    }
                                })
                                .setNeutralButton(R.string.files_deletion_none, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dialogInterface.dismiss();
                                    }
                                })
                                .show();
                        break;
                }
            }
        });
    }

    private void deleteCapturedFiles(String[] filenames) {
        for (String filename : filenames) {
            Uri uri = Uri.parse(filename);
            if ("file".equals(uri.getScheme())) {
                String pathname = uri.getPath();
                String parentDir = pathname.substring(0, pathname.lastIndexOf("/"));
                if (AudioCaptureService.CAPTURES_DIRECTORY.equals(parentDir)) {
                    File file = new File(pathname);
                    file.delete();
                }
            }
        }
    }

    private void deleteAddedFiles(String[] filenames) {
        for (String filename : filenames) {
            Uri uri = Uri.parse(filename);
            if ("file".equals(uri.getScheme())) {
                File file = new File(uri.getPath());
                file.delete();
            } else {
                DocumentFile documentFile = DocumentFile.fromSingleUri(MainActivity.this, uri);
                documentFile.delete();
            }
        }
    }

    @Override
    public void processException(final Throwable t) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                handleError(t);
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
                try {
                    getMusInterval();
                } catch (Throwable e) {
                    // handle IllegalStateException on unconfirmed permissions in AnkiDroid
                    if (!(e instanceof MusInterval.ValidationException)) {
                        handleError(e);
                        return;
                    }
                }
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
            }
        });
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

                    new Thread(new IntegrityCheckWorker(integrity, MainActivity.this, progressDialog)).start();
                } catch (Throwable e) {
                    handleError(e);
                }
            }
        });
    }

    @Override
    public void setMessage(final int resId, final Object ...formatArgs) {
        handler.post(new Runnable() {
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
        storeFilenames(this, filenames);
        uiDbEditor.putBoolean(REF_DB_AFTER_SELECTING, afterSelecting);
        uiDbEditor.putBoolean(REF_DB_AFTER_CAPTURING, afterCapturing);
        uiDbEditor.putBoolean(REF_DB_IS_CAPTURING, isCapturing);
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
        uiDbEditor.putString(REF_DB_INPUT_FIRST_NOTE_DURATION_COEFFICIENT, inputFirstNoteDurationCoefficient.getText().toString());
        uiDbEditor.putBoolean(REF_DB_AFTER_ADDING, afterAdding);
        uiDbEditor.putBoolean(REF_DB_MISMATCHING_SORTING, mismatchingSorting);
        uiDbEditor.putBoolean(REF_DB_INTERSECTING_NAMES, intersectingNames);
        uiDbEditor.putBoolean(REF_DB_SORT_BY_NAME, sortByName);
        uiDbEditor.putBoolean(REF_DB_INTERSECTING_DATES, intersectingDates);
        uiDbEditor.putBoolean(REF_DB_SORT_BY_DATE, sortByDate);
        uiDbEditor.putString(REF_DB_NOTE_KEYS, StringUtil.joinStrings(DB_STRING_ARRAY_SEPARATOR, noteKeys));
        uiDbEditor.putString(REF_DB_OCTAVE_KEYS, StringUtil.joinStrings(DB_STRING_ARRAY_SEPARATOR, octaveKeys));
        uiDbEditor.putString(REF_DB_INTERVAL_KEYS, StringUtil.joinStrings(DB_STRING_ARRAY_SEPARATOR, intervalKeys));
        uiDbEditor.apply();

        LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(this);
        for (Map.Entry<String, BroadcastReceiver> actionReceiver : actionReceivers.entrySet()) {
            broadcastManager.unregisterReceiver(actionReceiver.getValue());
        }

        soundPlayer.stop();
        soundPlayer.release();

        super.onPause();
    }

    protected void restoreUiState() {
        final SharedPreferences uiDb = getSharedPreferences(REF_DB_STATE, Context.MODE_PRIVATE);
        switchBatch.setChecked(uiDb.getBoolean(REF_DB_SWITCH_BATCH, false));
        filenames = getStoredFilenames(this);
        refreshFilenames();
        afterSelecting = uiDb.getBoolean(REF_DB_AFTER_SELECTING, false);
        afterCapturing = uiDb.getBoolean(REF_DB_AFTER_CAPTURING, false);
        isCapturing = uiDb.getBoolean(REF_DB_IS_CAPTURING, false);
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
        inputFirstNoteDurationCoefficient.setText(uiDb.getString(REF_DB_INPUT_FIRST_NOTE_DURATION_COEFFICIENT, ""));
        afterAdding = uiDb.getBoolean(REF_DB_AFTER_ADDING, false);
        mismatchingSorting = uiDb.getBoolean(REF_DB_MISMATCHING_SORTING, false);
        intersectingNames = uiDb.getBoolean(REF_DB_INTERSECTING_NAMES, false);
        sortByName = uiDb.getBoolean(REF_DB_SORT_BY_NAME, false);
        intersectingDates = uiDb.getBoolean(REF_DB_INTERSECTING_DATES, false);
        sortByDate = uiDb.getBoolean(REF_DB_SORT_BY_DATE, false);
        noteKeys = StringUtil.splitStrings(DB_STRING_ARRAY_SEPARATOR, uiDb.getString(REF_DB_NOTE_KEYS, ""));
        octaveKeys = StringUtil.splitStrings(DB_STRING_ARRAY_SEPARATOR, uiDb.getString(REF_DB_OCTAVE_KEYS, ""));
        intervalKeys = StringUtil.splitStrings(DB_STRING_ARRAY_SEPARATOR, uiDb.getString(REF_DB_INTERVAL_KEYS, ""));

        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }

    static String[] getStoredFilenames(Context context) {
        final SharedPreferences uiDb = context.getSharedPreferences(REF_DB_STATE, Context.MODE_PRIVATE);
        return StringUtil.splitStrings(DB_STRING_ARRAY_SEPARATOR, uiDb.getString(REF_DB_SELECTED_FILENAMES, ""));
    }

    static void storeFilenames(Context context, String[] filenames) {
        final SharedPreferences.Editor uiDbEditor = context.getSharedPreferences(REF_DB_STATE, Context.MODE_PRIVATE).edit();
        uiDbEditor.putString(REF_DB_SELECTED_FILENAMES, StringUtil.joinStrings(DB_STRING_ARRAY_SEPARATOR, filenames));
        uiDbEditor.apply();
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case AD_PERM_REQUEST:
                if (grantResults.length > 0) {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        validateModel();
                        refreshPreferences();
                        refreshExisting();
                        refreshPermutations();
                    } else {
                        showMsg(R.string.anki_permission_denied);
                    }
                }
                break;
            case PERMISSIONS_REQUEST_EXTERNAL_STORAGE_CALLBACK_OPEN_CHOOSER:
                if (grantResults.length > 0) {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        handleSelectFile();
                    } else {
                        showMsg(R.string.fs_permission_denied);
                    }
                }
                break;
            case PERMISSIONS_REQUEST_RECORD_AUDIO_CALLBACK_CAPTURE:
                if (grantResults.length > 0) {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        handleCaptureAudio();
                    } else {
                        showMsg(R.string.recording_permission_denied);
                    }
                }
                break;
            case PERMISSIONS_REQUEST_EXTERNAL_STORAGE_CALLBACK_CAPTURE:
                if (grantResults.length > 0) {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        handleCaptureAudio();
                    } else {
                        showMsg(R.string.fs_permission_denied);
                    }
                }
                break;
        }
    }

    private MusInterval getMusInterval() throws MusInterval.ValidationException {
        return getMusInterval(false);
    }

    private MusInterval getMusInterval(boolean isEmpty) throws MusInterval.ValidationException {
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        final boolean versionField = sharedPreferences.getBoolean(SettingsFragment.KEY_VERSION_FIELD_SWITCH, SettingsFragment.DEFAULT_VERSION_FIELD_SWITCH);
        final String storedDeck = sharedPreferences.getString(SettingsFragment.KEY_DECK_PREFERENCE, MusInterval.Builder.DEFAULT_DECK_NAME);
        final String storedModel = sharedPreferences.getString(SettingsFragment.KEY_MODEL_PREFERENCE, MusInterval.Builder.DEFAULT_MODEL_NAME);

        MusInterval.Builder builder = new MusInterval.Builder(mAnkiDroid)
                .deck(storedDeck)
                .model(storedModel)
                .notes(null)
                .octaves(null)
                .intervals(null);

        if (!isEmpty) {
            fillBuilderFromInputs(builder, versionField);
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
        }

        Set<String> storedFields = sharedPreferences.getStringSet(SettingsFragment.KEY_FIELDS_PREFERENCE, SettingsFragment.getDefaultFields(sharedPreferences));
        final Map<String, String> storedFieldsMapping = MappingPreference.toMapping(storedFields);
        builder.model_fields(storedFieldsMapping);

        return builder.build();
    }

    private void fillBuilderFromInputs(MusInterval.Builder builder, boolean versionField) {
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

        String[] notes = !checkNoteAny.isChecked() ? getCheckedValues(checkNotes) : null;
        String[] octaves = !checkOctaveAny.isChecked() ? getCheckedValues(checkOctaves) : null;
        String[] intervals = !checkIntervalAny.isChecked() ? getCheckedValues(checkIntervals, CHECK_INTERVAL_ID_VALUES) : null;

        builder.sounds(filenames)
                .notes(notes)
                .octaves(octaves)
                .direction(!directionStr.equals(anyStr) ? directionStr : "")
                .timing(!timingStr.equals(anyStr) ? timingStr : "")
                .intervals(intervals)
                .tempo(inputTempo.getText().toString())
                .instrument(inputInstrument.getText().toString())
                .first_note_duration_coefficient(inputFirstNoteDurationCoefficient.getText().toString());

        if (versionField) {
            builder.version(BuildConfig.VERSION_NAME);
        }
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
            AlertDialog dialog = new AlertDialog.Builder(this)
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
                    .create();
            activeOnStartDialogs.add(dialog);
            dialog.setOnDismissListener(onStartDialogDismissListener);
            dialog.show();
        } catch (final MusInterval.DefaultModelOutdatedException e) {
            final String modelName = e.getModelName();
            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setMessage(String.format(
                            getResources().getString(R.string.update_default_model),
                            modelName))
                    .setPositiveButton(R.string.update, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Long updatedModelId = mAnkiDroid.updateCustomModel(
                                    e.getModelId(),
                                    e.getFields(),
                                    e.getCards(),
                                    e.getQfmt(),
                                    e.getAfmt(),
                                    e.getCss()
                            );
                            if (updatedModelId != null) {
                                updateDefaultModelPreferences(updatedModelId);
                                showMsg(R.string.update_model_success, MusInterval.Builder.DEFAULT_MODEL_NAME);
                            } else {
                                new AlertDialog.Builder(MainActivity.this)
                                        .setMessage(R.string.update_model_error)
                                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                dialogInterface.dismiss();
                                            }
                                        })
                                        .show();
                            }
                        }
                    })
                    .setNegativeButton(R.string.use_custom_model, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            SharedPreferences.Editor preferenceEditor = PreferenceManager.getDefaultSharedPreferences(MainActivity.this).edit();
                            preferenceEditor.putBoolean(SettingsFragment.KEY_USE_DEFAULT_MODEL_CHECK, false);
                            preferenceEditor.apply();
                        }
                    })
                    .setNeutralButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    })
                    .create();
            activeOnStartDialogs.add(dialog);
            dialog.setOnDismissListener(onStartDialogDismissListener);
            dialog.show();
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
                fieldsStr.append(String.format("\"%s\"", field));
            }
            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setMessage(
                            getResources().getQuantityString(R.plurals.invalid_model_fields, invalidModelFields.size(), modelName, fieldsStr.toString()))
                    .setPositiveButton(R.string.configure, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                            intent.setAction(SettingsFragment.ACTION_SHOW_FIELDS_MAPPING_DIALOG);
                            startActivity(intent);
                        }
                    })
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    })
                    .create();
            activeOnStartDialogs.add(dialog);
            dialog.setOnDismissListener(onStartDialogDismissListener);
            dialog.show();
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
        } catch (MusInterval.InvalidFirstNoteDurationCoefficientException e) {
            showMsg(R.string.invalid_first_note_duration_coefficient);
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
            updateDefaultModelPreferences(newModelId);
            refreshExisting();
            refreshPermutations();
            showMsg(R.string.create_model_success, modelName);
        } else {
            showMsg(R.string.create_model_error);
        }
    }

    private void updateDefaultModelPreferences(long modelId) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor preferenceEditor = preferences.edit();
        Set<String> defaultFields = SettingsFragment.getDefaultFields(preferences);
        preferenceEditor.putStringSet(SettingsFragment.KEY_FIELDS_PREFERENCE, defaultFields);
        String modelFieldsKey = SettingsFragment.getModelFieldsKey(modelId);
        preferenceEditor.putStringSet(modelFieldsKey, defaultFields);
        preferenceEditor.apply();
    }

    @Override
    protected void onStop() {
        super.onStop();
        for (AlertDialog dialog : activeOnStartDialogs) {
            dialog.dismiss();
        }
        if (toast != null) {
            toast.cancel();
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
        e.printStackTrace();
        showMsg(R.string.unknown_error);
    }

    void showMsg(int msgResId, Object ...formatArgs) {
        displayToast(getResources().getString(msgResId, formatArgs));
    }

    void showQuantityMsg(int msgResId, int quantity, Object ...formatArgs) {
        displayToast(getResources().getQuantityString(msgResId, quantity, formatArgs));
    }

    private void displayToast(String text) {
        if (toast != null && text.equals(lastToastText)) {
            toast.cancel();
        }
        toast = Toast.makeText(this, text, Toast.LENGTH_LONG);
        lastToastText = text;
        toast.show();
    }
}
