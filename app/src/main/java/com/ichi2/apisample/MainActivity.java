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

// @todo Radiobuttons for direction/timing instead of input field
// @todo Save last entered values on close and restore next time
// @todo Save instruments, tempos ... and supply in input field
// @todo Implement "select file" function (see feature/select-file-dialog branch)

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

        configureSelectFileButton();
        configureCheckExistenceButton();
        configureAddToAnkiButton();

        mAnkiDroid = new AnkiDroidHelper(this);
    }

    private void configureSelectFileButton() {
        final Button actionSelectFile = findViewById(R.id.actionSelectFile);
        actionSelectFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // @fixme Dummy
                inputFilename.setText("/path/to/dummy/file.m4a");
            }
        });
    }

    private void configureCheckExistenceButton() {
        final AlertDialog.Builder markNoteDialog = new AlertDialog.Builder(MainActivity.this);
        markNoteDialog
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        try {
                            int count = getMusInterval().markExistingNotes();
                            showMsg(getResources().getQuantityString(R.plurals.mi_marked, count, count));
                        } catch (MusInterval.NoteNotExistsException e) {
                            showMsg(R.string.mi_not_exists);
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

                try {
                    int count = getMusInterval().getExistingNotesCount();

                    if (count > 0) {
                        markNoteDialog.setMessage(getResources().getQuantityString(R.plurals.mi_exists_ask_mark, count, count))
                                .show();
                    } else {
                        showMsg(R.string.mi_not_exists);
                    }
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

                final MusInterval musInterval = getMusInterval();
                String message;

                // @todo Make all fields mandatory on adding
                if (allFieldsEmpty()) {
                    message = getResources().getString(R.string.all_fields_empty);
                } else {
                    try {
                        musInterval.addToAnki();
                        message = getResources().getString(R.string.item_added);
                    } catch (MusInterval.NoSuchModelException e) {
                        message = getResources().getString(R.string.model_not_found, musInterval.modelName);
                    } catch (MusInterval.CreateDeckException e) {
                        message = getResources().getString(R.string.create_deck_error, musInterval.deckName);
                    } catch (MusInterval.AddToAnkiException e) {
                        message = getResources().getString(R.string.add_card_error);
                    }
                }

                Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
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

}
