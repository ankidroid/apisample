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


public class MainActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback {

    private static final int AD_PERM_REQUEST = 0;

    private EditText inputFilename;
    private EditText inputStartNote;
    private EditText inputDirection;
    private EditText inputScale;
    private EditText inputInterval;
    private EditText inputTempo;

    private AnkiDroidHelper mAnkiDroid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        inputFilename = findViewById(R.id.inputFilename);
        inputStartNote = findViewById(R.id.inputStartNote);
        inputDirection = findViewById(R.id.inputDirection);
        inputScale = findViewById(R.id.inputScale);
        inputInterval = findViewById(R.id.inputInterval);
        inputTempo = findViewById(R.id.inputTempo);

        final Button actionSelectFile = findViewById(R.id.actionSelectFile);
        actionSelectFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // @fixme Dummy
                inputFilename.setText("/path/to/dummy-file.m4a");
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

                final MusInterval musInterval = getMusInterval();
                String message;

                if (allFieldsEmpty()) {
                    message = getResources().getString(R.string.all_fields_empty);
                } else {
                    if (musInterval.existsInAnki()) {
                        message = getResources().getString(R.string.mi_exists);
                    } else {
                        message = getResources().getString(R.string.mi_not_exists);
                    }
                }

                Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
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
                && inputScale.getText().toString().isEmpty()
                && inputInterval.getText().toString().isEmpty()
                && inputTempo.getText().toString().isEmpty();
    }

    private MusInterval getMusInterval() {
        return new MusInterval(mAnkiDroid, inputFilename.getText().toString(), inputStartNote.getText().toString(),
                inputDirection.getText().toString(), inputScale.getText().toString(), inputInterval.getText().toString(),
                inputTempo.getText().toString());
    }

}
