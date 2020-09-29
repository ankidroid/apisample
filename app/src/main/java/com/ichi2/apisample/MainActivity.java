package com.ichi2.apisample;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
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
import java.util.Map;


public class MainActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback {
    private static final int AD_PERM_REQUEST = 0;
    private static final int PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1;

    private EditText inputFilename;
    private EditText inputStartNote;

    private AnkiDroidHelper mAnkiDroid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        inputFilename = findViewById(R.id.inputFilename);
        inputStartNote = findViewById(R.id.inputStartNote);

        final Button actionSelectFile = findViewById(R.id.actionSelectFile);
        actionSelectFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inputFilename.setText("/path/to/file.m4a");
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
                if (inputStartNote.getText().toString().isEmpty()) {
                    Toast.makeText(MainActivity.this, "Nothing to check", Toast.LENGTH_LONG).show();
                }
                if (getMusInterval().isExistsInAnki()) {
                    Toast.makeText(MainActivity.this, getResources().getString(R.string.card_exists), Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(MainActivity.this, getResources().getString(R.string.card_not_exists), Toast.LENGTH_LONG).show();
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
                if (getMusInterval().addToAnki()){
                    Toast.makeText(MainActivity.this, getResources().getString(R.string.item_added), Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(MainActivity.this, getResources().getString(R.string.card_add_fail), Toast.LENGTH_LONG).show();
                }
            }
        });

        mAnkiDroid = new AnkiDroidHelper(this);
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case AD_PERM_REQUEST: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getMusInterval().addToAnki();
                } else {
                    Toast.makeText(MainActivity.this, R.string.anki_permission_denied, Toast.LENGTH_LONG).show();
                }
            }
            case PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Intent intent = getIntent();
                    PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
                    AlarmManager am = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
                    am.set(AlarmManager.RTC, System.currentTimeMillis() + 100, pendingIntent);
                    System.exit(0);
                } else {
                    Toast.makeText(MainActivity.this, R.string.fs_permission_denied, Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    public MusInterval getMusInterval() {
        Map<String, String> data = new HashMap<>();
        data.put("sound", inputFilename.getText().toString());
        data.put("start_note", inputStartNote.getText().toString());

        return new MusInterval(mAnkiDroid, data);
    }

}
