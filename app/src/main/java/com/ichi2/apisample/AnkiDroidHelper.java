package com.ichi2.apisample;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.ichi2.anki.FlashCardsContract;
import com.ichi2.anki.api.AddContentApi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static com.ichi2.anki.api.AddContentApi.READ_WRITE_PERMISSION;

public class AnkiDroidHelper {
    private static final String DECK_REF_DB = "com.ichi2.anki.api.decks";
    private static final String MODEL_REF_DB = "com.ichi2.anki.api.models";

    private final Context mContext;
    final ContentResolver mResolver;
    private final AddContentApi mApi;

    public AnkiDroidHelper(Context context) {
        mContext = context.getApplicationContext();
        mResolver = mContext.getContentResolver();
        mApi = new AddContentApi(mContext);
    }

    public AddContentApi getApi() {
        return mApi;
    }

    /**
     * Whether or not the API is available to use.
     * The API could be unavailable if AnkiDroid is not installed or the user explicitly disabled the API
     * @return true if the API is available to use
     */
    public static boolean isApiAvailable(Context context) {
        return AddContentApi.getAnkiDroidPackageName(context) != null;
    }

    /**
     * Whether or not we should request full access to the AnkiDroid API
     */
    public boolean shouldRequestPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return false;
        }
        return ContextCompat.checkSelfPermission(mContext, READ_WRITE_PERMISSION) != PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Request permission from the user to access the AnkiDroid API (for SDK 23+)
     * @param callbackActivity An Activity which implements onRequestPermissionsResult()
     * @param callbackCode The callback code to be used in onRequestPermissionsResult()
     */
    public void requestPermission(Activity callbackActivity, int callbackCode) {
        ActivityCompat.requestPermissions(callbackActivity, new String[]{READ_WRITE_PERMISSION}, callbackCode);
    }

    /**
     * Save a mapping from deckName to getDeckId in the SharedPreferences
     */
    public void storeDeckReference(String deckName, long deckId) {
        final SharedPreferences decksDb = mContext.getSharedPreferences(DECK_REF_DB, Context.MODE_PRIVATE);
        decksDb.edit().putLong(deckName, deckId).apply();
    }

    /**
     * Try to find the given model by name, accounting for renaming of the model:
     * If there's a model with this modelName that is known to have previously been created (by this app)
     *   and the corresponding model ID exists and has the required number of fields
     *   then return that ID (even though it may have since been renamed)
     * If there's a model from #getModelList with modelName and required number of fields then return its ID
     * Otherwise return null
     * @param modelName the name of the model to find
     * @param numFields the minimum number of fields the model is required to have
     * @return the model ID or null if something went wrong
     */
    public Long findModelIdByName(String modelName, int numFields) {
        SharedPreferences modelsDb = mContext.getSharedPreferences(MODEL_REF_DB, Context.MODE_PRIVATE);
        long prefsModelId = modelsDb.getLong(modelName, -1L);
        // if we have a reference saved to modelName and it exists and has at least numFields then return it
        if ((prefsModelId != -1L)
                && (mApi.getModelName(prefsModelId) != null)
                && (mApi.getFieldList(prefsModelId) != null)
                && (mApi.getFieldList(prefsModelId).length >= numFields)) { // could potentially have been renamed
            return prefsModelId;
        }
        Map<Long, String> modelList = mApi.getModelList(numFields);
        if (modelList != null) {
            for (Map.Entry<Long, String> entry : modelList.entrySet()) {
                if (entry.getValue().equals(modelName)) {
                    return entry.getKey(); // first model wins
                }
            }
        }
        // model no longer exists (by name nor old id), the number of fields was reduced, or API error
        return null;
    }

    public Long findModelIdByName(String modelName) {
        return findModelIdByName(modelName, 1);
    }

    /**
     * Try to find the given deck by name, accounting for potential renaming of the deck by the user as follows:
     * If there's a deck with deckName then return it's ID
     * If there's no deck with deckName, but a ref to deckName is stored in SharedPreferences, and that deck exist in
     * AnkiDroid (i.e. it was renamed), then use that deck.Note: this deck will not be found if your app is re-installed
     * If there's no reference to deckName anywhere then return null
     * @param deckName the name of the deck to find
     * @return the did of the deck in Anki
     */
    public Long findDeckIdByName(String deckName) {
        SharedPreferences decksDb = mContext.getSharedPreferences(DECK_REF_DB, Context.MODE_PRIVATE);
        // Look for deckName in the deck list
        Long did = getDeckId(deckName);
        if (did != null) {
            // If the deck was found then return it's id
            return did;
        } else {
            // Otherwise try to check if we have a reference to a deck that was renamed and return that
            did = decksDb.getLong(deckName, -1);
            if (did != -1 && mApi.getDeckName(did) != null) {
                return did;
            } else {
                // If the deck really doesn't exist then return null
                return null;
            }
        }
    }

    /**
     * Get the ID of the deck which matches the name
     * @param deckName Exact name of deck (note: deck names are unique in Anki)
     * @return the ID of the deck that has given name, or null if no deck was found or API error
     */
    private Long getDeckId(String deckName) {
        Map<Long, String> deckList = mApi.getDeckList();
        if (deckList != null) {
            for (Map.Entry<Long, String> entry : deckList.entrySet()) {
                if (entry.getValue().equalsIgnoreCase(deckName)) {
                    return entry.getKey();
                }
            }
        }
        return null;
    }

    public Long addNewDeck(String deckName) {
        return getApi().addNewDeck(deckName);
    }

    public String[] getFieldList(long modelId) {
        return getApi().getFieldList(modelId);
    }

    /**
     * Add note to Anki.
     *
     * Transforms Map into simple array of strings.
     */
    public Long addNote(long modelId, long deckId, Map<String, String> data, Set<String> tags) {
        String[] fieldNames = getFieldList(modelId);
        List<String> fields = new ArrayList<>();

        for (String fieldName : fieldNames) {
            String value = data.containsKey(fieldName) ? data.get(fieldName) : "";
            fields.add(value);
        }

        String[] result = new String[fields.size()];
        fields.toArray(result);

        return getApi().addNote(modelId, deckId, result, tags);
    }

    /**
     * Get all the notes, related to the passed model id.
     *
     * @param modelId Needed model
     * @return List of note ids
     */
    public LinkedList<Map<String, String>> getNotes(long modelId) throws InvalidAnkiDatabaseException {
        LinkedList<Map<String, String>> result = new LinkedList<>();

        String selection = String.format(Locale.US, "%s=%d", FlashCardsContract.Note.MID, modelId);
        String[] projection = new String[] { FlashCardsContract.Note._ID, FlashCardsContract.Note.FLDS, FlashCardsContract.Note.TAGS };
        Cursor notesTableCursor = mResolver.query(FlashCardsContract.Note.CONTENT_URI_V2, projection, selection, null, null);

        if (notesTableCursor == null) {
            // nothing found
            return result;
        }

        String[] fieldNames = getFieldList(modelId);

        try {
            while (notesTableCursor.moveToNext()) {
                int idIndex = notesTableCursor.getColumnIndexOrThrow(FlashCardsContract.Note._ID);
                int fldsIndex = notesTableCursor.getColumnIndexOrThrow(FlashCardsContract.Note.FLDS);
                int tagsIndex = notesTableCursor.getColumnIndexOrThrow(FlashCardsContract.Note.TAGS);

                String flds = notesTableCursor.getString(fldsIndex);

                if (flds != null) {
                    String[] fields = flds.split("\\x1f", -1);
                    if (fields.length != fieldNames.length) {
                        throw new InvalidAnkiDatabase_fieldAndFieldNameCountMismatchException();
                    }

                    Map<String, String> item = new HashMap<>();
                    item.put("id", Long.toString(notesTableCursor.getLong(idIndex)));
                    item.put("tags", notesTableCursor.getString(tagsIndex));

                    for (int i = 0; i < fieldNames.length; ++i) {
                        item.put(fieldNames[i], fields[i]);
                    }

                    result.add(item);
                }
            }
        }
        finally {
            notesTableCursor.close();
        }

        return result;
    }

    public int addTagToNote(long noteId, String tags) {
        ContentValues values = new ContentValues();
        values.put(FlashCardsContract.Note.TAGS, tags);

        Uri cardUri = Uri.withAppendedPath(FlashCardsContract.Note.CONTENT_URI, Long.toString(noteId));
        return mResolver.update(cardUri, values, null, null);
    }

    abstract static class InvalidAnkiDatabaseException extends Throwable {
    }

    static class InvalidAnkiDatabase_fieldAndFieldNameCountMismatchException extends InvalidAnkiDatabaseException {
    }
}
