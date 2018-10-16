package com.ichi2.apisample;

import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class StatsActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback {

    private static final int AD_PERM_REQUEST = 0;
    private AnkiDroidHelper mAnkiDroid;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAnkiDroid = new AnkiDroidHelper(this);

        setContentView(R.layout.activity_stats);
        if (!AnkiDroidHelper.isApiAvailable(this)) {
            Toast.makeText(this, R.string.api_not_available, Toast.LENGTH_LONG).show();
            finishActivity(-1);
            return;
        }

        if (mAnkiDroid.shouldRequestReadPermission()) {
            mAnkiDroid.requestReadPermission(this, AD_PERM_REQUEST);
        } else {
            displayCardCount();
        }
    }

    private void displayCardCount() {
        TextView matureText = findViewById(R.id.count_mature);
        TextView youngText = findViewById(R.id.count_young);
        TextView newCount = findViewById(R.id.count_new);
        TextView suspendedCount = findViewById(R.id.count_suspended);

        Cursor countCursor = fetchCardCount();

        matureText.setText(getString(R.string.mature_cards, countCursor.getInt(0)));
        youngText.setText(getString(R.string.young_cards, countCursor.getInt(1)));
        newCount.setText(getString(R.string.new_cards, countCursor.getInt(2)));
        suspendedCount.setText(getString(R.string.suspended_cards, countCursor.getInt(3)));
    }

    private Cursor fetchCardCount() {
        ContentResolver resolver = getApplicationContext().getContentResolver();
        Cursor countCursor = resolver.query(Uri.parse("content://com.ichi2.anki.stats/cardcount"), null, null, null, null);
        countCursor.moveToFirst();
        return countCursor;
    }


    public void onRequestPermissionsResult (int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if ((requestCode == AD_PERM_REQUEST) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
            displayCardCount();
        } else {
            Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_LONG).show();
            finishActivity(-1);
        }
    }
}
