package com.ichi2.apisample;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback {

    private static final int AD_PERM_REQUEST = 0;

    private EditText inputFilename;
    private EditText inputStartNote;
    private EditText inputDirection;
    private EditText inputTiming;
    private EditText inputInterval;
    private EditText inputTempo;
    private EditText inputInstrument;

    private AnkiDroidHelper mAnkiDroid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        inputFilename = findViewById(R.id.inputFilename);
        inputStartNote = findViewById(R.id.inputStartNote);
        inputDirection = findViewById(R.id.inputDirection);
        inputTiming = findViewById(R.id.inputTiming);
        inputInterval = findViewById(R.id.inputInterval);
        inputTempo = findViewById(R.id.inputTempo);
        inputInstrument = findViewById(R.id.inputInstrument);

        final Button actionSelectFile = findViewById(R.id.actionSelectFile);
        actionSelectFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // @fixme Dummy
                inputFilename.setText("/path/to/dummy-file.m4a");
            }
        });

        final AlertDialog.Builder markNoteDialog = new AlertDialog.Builder(MainActivity.this);
        markNoteDialog.setMessage(R.string.mi_exists_ask_mark)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        try {
                            getMusInterval().markExistingNote();
                        } catch (MusInterval.NoteNotExistsException e) {
                            Toast.makeText(MainActivity.this, getResources().getString(R.string.mi_not_exists), Toast.LENGTH_LONG).show();
                        } catch (MusInterval.AddTagException e) {
                            Toast.makeText(MainActivity.this, getResources().getString(R.string.mark_note_error), Toast.LENGTH_LONG).show();
                        } catch (AnkiDroidHelper.InvalidAnkiDatabaseException e) {
                            processInvalidAnkiDatabase(e);
                        }
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
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

                if (allFieldsEmpty()) {
                    Toast.makeText(MainActivity.this, getResources().getString(R.string.all_fields_empty), Toast.LENGTH_LONG).show();
                } else {
                    try {
                        if (getMusInterval().existsInAnki()) {
                            markNoteDialog.show();
                        } else {
                            // @todo: Move to separate method
                            Toast.makeText(MainActivity.this, getResources().getString(R.string.mi_not_exists), Toast.LENGTH_LONG).show();
                        }
                    } catch (AnkiDroidHelper.InvalidAnkiDatabaseException e) {
                        processInvalidAnkiDatabase(e);
                    }
                }
            }
        });

        final Button actionAddToAnki = findViewById(R.id.actionAddToAnki);
        actionAddToAnki.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mAnkiDroid.shouldRequestPermission()) {
                    mAnkiDroid.requestPermission(MainActivity.this, AD_PERM_REQUEST);
                    return;
                }

                final MusInterval musInterval = getMusInterval();
                String message;

                if (allFieldsEmpty()) {
                    message = getResources().getString(R.string.all_fields_empty);
                } else {
                    try {
                        musInterval.addToAnki();
                        message = getResources().getString(R.string.item_added);
                    } catch (MusInterval.NoSuchModelException e) {
                        message = getResources().getString(R.string.model_not_found, musInterval.getModelName());
                    } catch (MusInterval.CreateDeckException e) {
                        message = getResources().getString(R.string.create_deck_error, musInterval.getDeckName());
                    } catch (MusInterval.AddToAnkiException e) {
                        message = getResources().getString(R.string.add_card_error);
                    }
                }

                Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });

        mAnkiDroid = new AnkiDroidHelper(this);
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case AD_PERM_REQUEST: {
                if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(MainActivity.this, R.string.anki_permission_denied, Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private boolean allFieldsEmpty() {
        return inputStartNote.getText().toString().isEmpty()
                && inputDirection.getText().toString().isEmpty()
                && inputTiming.getText().toString().isEmpty()
                && inputInterval.getText().toString().isEmpty()
                && inputTempo.getText().toString().isEmpty()
                && inputInstrument.getText().toString().isEmpty();
    }

    private MusInterval getMusInterval() {
        return new MusInterval.Builder(mAnkiDroid)
                .sound(inputFilename.getText().toString())
                .start_note(inputStartNote.getText().toString())
                .direction(inputDirection.getText().toString())
                .timing(inputTiming.getText().toString())
                .interval(inputInterval.getText().toString())
                .tempo(inputTempo.getText().toString())
                .instrument(inputInstrument.getText().toString())
                .build();
    }

    private void processInvalidAnkiDatabase(AnkiDroidHelper.InvalidAnkiDatabaseException invalidAnkiDatabaseException) {
        try {
            throw invalidAnkiDatabaseException;
        } catch (AnkiDroidHelper.InvalidAnkiDatabase_fieldAndFieldNameCountMismatchException e) {
            Toast.makeText(MainActivity.this, getResources().getString(R.string.InvalidAnkiDatabase_unknownError), Toast.LENGTH_LONG).show();
        } catch (AnkiDroidHelper.InvalidAnkiDatabaseException e) {
            Toast.makeText(MainActivity.this, getResources().getString(R.string.InvalidAnkiDatabase_fieldAndFieldNameCountMismatch), Toast.LENGTH_LONG).show();
        }
    }

}
