package com.ichi2.apisample;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
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
    private static final int PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1;

    private static final int ACTION_SELECT_FILE = 10;

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
            Uri selectedFile = data.getData();
            String filePath = selectedFile.toString(); // @todo Transform to real path ?
            inputFilename.setText(filePath);
        }
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

                if (allFieldsEmpty()) {
                    message = getResources().getString(R.string.all_fields_empty);
                } else {
                    try {
                        MusInterval newMi = musInterval.addToAnki();
                        inputFilename.setText(newMi.sound);
                        message = getResources().getString(R.string.item_added);
                    } catch (MusInterval.NoSuchModelException e) {
                        message = getResources().getString(R.string.model_not_found, musInterval.modelName);
                    } catch (MusInterval.CreateDeckException e) {
                        message = getResources().getString(R.string.create_deck_error, musInterval.deckName);
                    } catch (MusInterval.AddToAnkiException e) {
                        message = getResources().getString(R.string.add_card_error);
                    } catch (MusInterval.MandatoryFieldEmptyException e) {
                        message = getResources().getString(R.string.mandatory_field_empty);
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
                    showMsg(R.string.anki_permission_denied);
                }
            }
            case PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE: {
                if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    showMsg(R.string.fs_permission_denied);
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
