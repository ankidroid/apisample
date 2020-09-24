package com.ichi2.apisample;

import android.content.pm.PackageManager;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;


public class MainActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback {
    private static final int AD_PERM_REQUEST = 0;

    private EditText inputFilename;
    private EditText inputIntervalDescription;
    private EditText inputStartNote;
    private EditText inputAscendingDescending;
    private EditText inputMelodicHarmonic;
    private EditText inputInterval;
    private EditText inputTempo;
    private EditText inputInstrument;

    private AnkiDroidHelper mAnkiDroid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        inputFilename = findViewById(R.id.inputFilename);
        inputIntervalDescription = findViewById(R.id.inputIntervalDescription);
        inputStartNote = findViewById(R.id.inputStartNote);
        inputAscendingDescending = findViewById(R.id.inputAscendingDescending);
        inputMelodicHarmonic = findViewById(R.id.inputMelodicHarmonic);
        inputInterval = findViewById(R.id.inputInterval);
        inputTempo = findViewById(R.id.inputTempo);
        inputInstrument = findViewById(R.id.inputInstrument);

        Button actionSelectFile = findViewById(R.id.actionSelectFile);
        actionSelectFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //showFileChooser();
            }
        });

        Button actionCheckExistence = findViewById(R.id.actionCheckExistence);
        actionCheckExistence.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mAnkiDroid.shouldRequestPermission()) {
                    mAnkiDroid.requestPermission(MainActivity.this, AD_PERM_REQUEST);
                    return;
                }
                checkIfDataAlreadyExists(getEnteredData());
            }
        });

        Button actionAddToAnki = findViewById(R.id.actionAddToAnki);
        actionAddToAnki.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mAnkiDroid.shouldRequestPermission()) {
                    mAnkiDroid.requestPermission(MainActivity.this, AD_PERM_REQUEST);
                    return;
                }
                if (!checkIfDataAlreadyExists(getEnteredData())) {
                    addCardToAnkiDroid(getEnteredData());
                }
            }
        });

        // @todo Disable buttons if AnkiDroid is unavailable
        //AnkiDroidHelper.isApiAvailable(this);

        // Create instance of helper class
        mAnkiDroid = new AnkiDroidHelper(this);
    }

    public void setAnkiDroidHelper(AnkiDroidHelper helper) {
        mAnkiDroid = helper;
    }

    public void onRequestPermissionsResult (int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == AD_PERM_REQUEST && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            addCardToAnkiDroid(getEnteredData());
        } else {
            Toast.makeText(MainActivity.this, R.string.permission_denied, Toast.LENGTH_LONG).show();
        }
    }

    Map<String, String> getEnteredData() {
        Map<String, String> data = new HashMap<String, String>();
        data.put(AnkiDroidConfig.FIELDS[0], inputFilename.getText().toString());
        data.put(AnkiDroidConfig.FIELDS[1], inputIntervalDescription.getText().toString());
        data.put(AnkiDroidConfig.FIELDS[2], inputStartNote.getText().toString());
        data.put(AnkiDroidConfig.FIELDS[3], inputAscendingDescending.getText().toString());
        data.put(AnkiDroidConfig.FIELDS[4], inputMelodicHarmonic.getText().toString());
        data.put(AnkiDroidConfig.FIELDS[5], inputInterval.getText().toString());
        data.put(AnkiDroidConfig.FIELDS[6], inputTempo.getText().toString());
        data.put(AnkiDroidConfig.FIELDS[7], inputInstrument.getText().toString());
        return data;
    }

    /**
     * get the deck id
     * @return might be null if there was a problem
     */
    private Long getDeckId() {
        Long did = mAnkiDroid.findDeckIdByName(AnkiDroidConfig.DECK_NAME);
        if (did == null) {
            did = mAnkiDroid.addNewDeck(AnkiDroidConfig.DECK_NAME);
            if (did != null) {
                mAnkiDroid.storeDeckReference(AnkiDroidConfig.DECK_NAME, did);
            }
        }
        return did;
    }

    /**
     * get model id
     * @return might be null if there was an error
     */
    private Long getModelId() {
        Long mid = mAnkiDroid.findModelIdByName(AnkiDroidConfig.MODEL_NAME, AnkiDroidConfig.FIELDS.length);
        if (mid == null) {
            mid = mAnkiDroid.addNewCustomModel(AnkiDroidConfig.MODEL_NAME, AnkiDroidConfig.FIELDS,
                    AnkiDroidConfig.CARD_NAMES, AnkiDroidConfig.QFMT, null, AnkiDroidConfig.CSS, getDeckId(), null);
            if (mid != null) {
                mAnkiDroid.storeModelReference(AnkiDroidConfig.MODEL_NAME, mid);
            }
        }
        return mid;
    }

    private boolean checkIfDataAlreadyExists(final Map<String, String> data) {
        Long deckId = getDeckId();
        Long modelId = getModelId();

        if ((deckId == null) || (modelId == null)) {
            // we had an API error, report failure and return
            Toast.makeText(MainActivity.this, getResources().getString(R.string.card_add_fail), Toast.LENGTH_LONG).show();
            return true;
        }

        String[] fieldNames = mAnkiDroid.getFieldList(modelId);
        if (fieldNames == null) {
            // we had an API error, report failure and return
            Toast.makeText(MainActivity.this, getResources().getString(R.string.card_add_fail), Toast.LENGTH_LONG).show();
            return true;
        }

        // Build list of fields and tags
        LinkedList<String []> fields = new LinkedList<>();
        LinkedList<Set<String>> tags = new LinkedList<>();

        String[] flds = new String[fieldNames.length];
        for (int i = 0; i < flds.length; i++) {
            if (i < AnkiDroidConfig.FIELDS.length) {
                flds[i] = data.get(AnkiDroidConfig.FIELDS[i]);
            }
        }

        fields.add(flds);
        tags.add(AnkiDroidConfig.TAGS);

        mAnkiDroid.removeDuplicates(fields, tags, modelId);

        return fields.isEmpty();
    }

    /**
     * Use the instant-add API to add flashcards directly to AnkiDroid.
     * @param data HashMap of field name / field value pairs
     */
    private void addCardToAnkiDroid(final Map<String, String> data) {
        Long deckId = getDeckId();
        Long modelId = getModelId();

        if ((deckId == null) || (modelId == null)) {
            // we had an API error, report failure and return
            Toast.makeText(MainActivity.this, getResources().getString(R.string.card_add_fail), Toast.LENGTH_LONG).show();
            return;
        }

        String[] fieldNames = mAnkiDroid.getFieldList(modelId);
        if (fieldNames == null) {
            // we had an API error, report failure and return
            Toast.makeText(MainActivity.this, getResources().getString(R.string.card_add_fail), Toast.LENGTH_LONG).show();
            return;
        }

        // Build list of fields and tags
        LinkedList<String []> fields = new LinkedList<>();
        LinkedList<Set<String>> tags = new LinkedList<>();

        // Build a field map accounting for the fact that the user could have changed the fields in the model
        String[] flds = new String[fieldNames.length];
        for (int i = 0; i < flds.length; i++) {
            // Fill up the fields one-by-one until either all fields are filled or we run out of fields to send
            if (i < AnkiDroidConfig.FIELDS.length) {
                flds[i] = data.get(AnkiDroidConfig.FIELDS[i]);
            }
        }

        fields.add(flds);
        tags.add(AnkiDroidConfig.TAGS);

        // Remove any duplicates from the LinkedLists and then add over the API
        mAnkiDroid.removeDuplicates(fields, tags, modelId);

        if (fields.isEmpty()) {
            Toast.makeText(MainActivity.this, getResources().getString(R.string.card_exists), Toast.LENGTH_LONG).show();
        } else {
            int added = mAnkiDroid.addNotes(modelId, deckId, fields, tags);

            if (added != 0) {
                Toast.makeText(MainActivity.this, getResources().getString(R.string.item_added), Toast.LENGTH_LONG).show();
            } else {
                // API indicates that a 0 return value is an error
                Toast.makeText(MainActivity.this, getResources().getString(R.string.card_add_fail), Toast.LENGTH_LONG).show();
            }
        }
    }
}
