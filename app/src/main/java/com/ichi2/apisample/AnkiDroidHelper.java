package com.ichi2.apisample;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.ichi2.anki.FlashCardsContract;
import com.ichi2.anki.api.AddContentApi;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static com.ichi2.anki.api.AddContentApi.READ_WRITE_PERMISSION;

public class AnkiDroidHelper {
    private static final String DECK_REF_DB = "com.ichi2.anki.api.decks";
    private static final String MODEL_REF_DB = "com.ichi2.anki.api.models";
    private static final String FLDS_SEPARATOR = "\u001f";

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

    public Map<Long, String> getModelList() {
        return mApi.getModelList();
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
        // if we have a reference saved to modelName and it exists then return it
        if ((prefsModelId != -1L)
                && (mApi.getModelName(prefsModelId) != null)) {
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
        // model no longer exists (by name nor old id), or API error
        return null;
    }

    public Long findModelIdByName(String modelName) {
        return findModelIdByName(modelName, 1);
    }

    public Long addNewCustomModel(String modelName, String[] fields, String[] cards, String[] qfmt, String[] afmt, String css) {
        return getApi().addNewCustomModel(modelName, fields, cards, qfmt, afmt, css, null, null);
    }

    public Map<Long, String> getDeckList() {
        return mApi.getDeckList();
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

    // @todo: refactor once new version release of "com.ichi2.anki.api" is available
    public String addFileToAnkiMedia(String uriString) {
        ContentResolver cr = mContext.getContentResolver();
        ContentValues cv = new ContentValues();
        cv.put("file_uri", uriString);
        final String preferredName = "music_interval_" + (System.currentTimeMillis() / 1000L);
        cv.put("preferred_name", preferredName);
        final Uri fileUri = Uri.parse(uriString);
        mContext.grantUriPermission("com.ichi2.anki", fileUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
        try {
            Uri uri = cr.insert(Uri.withAppendedPath(FlashCardsContract.AUTHORITY_URI, "media"), cv);
            File insertedFile = new File(uri.getPath());
            String filePath =  insertedFile.toString();
            return filePath.substring(1); // get rid of the "/" at the beginning
        } catch (Exception e) {
            return null;
        } finally {
            mContext.revokeUriPermission(fileUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
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

    @SuppressWarnings("unchecked")
    public LinkedList<Map<String, String>> findNotes(long modelId, Map<String, String> data)
            throws InvalidAnkiDatabase_fieldAndFieldNameCountMismatchException {
        return findNotes(modelId, new Map[]{data});
    }

    public LinkedList<Map<String, String>> findNotes(long modelId, Map<String, String>[] dataSet)
            throws InvalidAnkiDatabase_fieldAndFieldNameCountMismatchException {
        if (dataSet.length == 0) {
            return new LinkedList<>();
        }

        String[] fieldNames = getFieldList(modelId);

        StringBuilder dataCondition = new StringBuilder();
        for (Map<String, String> data : dataSet) {
            if (dataCondition.length() > 0) {
                dataCondition.append(" or ");
            }
            StringBuilder fieldsAggregated = new StringBuilder();
            for (String fieldName : fieldNames) {
                if (fieldsAggregated.length() > 0) {
                    fieldsAggregated.append(FLDS_SEPARATOR);
                }
                final String value = data.containsKey(fieldName) && !data.get(fieldName).isEmpty()
                        ? data.get(fieldName)
                        : "%";
                fieldsAggregated.append(value);
            }
            dataCondition.append(String.format(Locale.US, "%s like \"%s\"", FlashCardsContract.Note.FLDS, fieldsAggregated.toString()));
        }

        String selection = String.format(Locale.US, "%s=%d and (%s)",
                FlashCardsContract.Note.MID, modelId, dataCondition.toString());

        String[] projection = new String[] {
                FlashCardsContract.Note._ID,
                FlashCardsContract.Note.FLDS,
                FlashCardsContract.Note.TAGS
        };

        LinkedList<Map<String, String>> result = new LinkedList<>();
        Cursor notesTableCursor = mResolver.query(FlashCardsContract.Note.CONTENT_URI_V2, projection, selection, null, null);

        if (notesTableCursor == null) {
            // nothing found
            return result;
        }

        try {
            while (notesTableCursor.moveToNext()) {
                int idIndex = notesTableCursor.getColumnIndexOrThrow(FlashCardsContract.Note._ID);
                int fldsIndex = notesTableCursor.getColumnIndexOrThrow(FlashCardsContract.Note.FLDS);
                int tagsIndex = notesTableCursor.getColumnIndexOrThrow(FlashCardsContract.Note.TAGS);

                String flds = notesTableCursor.getString(fldsIndex);

                if (flds != null) {
                    String[] fields = flds.split(FLDS_SEPARATOR, -1);
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

    public boolean updateNote(long modelId, long noteId, Map<String, String> data) {
        String[] fieldNames = getFieldList(modelId);
        String[] fieldValues = new String[fieldNames.length];
        for (int i = 0; i < fieldNames.length; i++) {
            fieldValues[i] = data.getOrDefault(fieldNames[i], "");
        }
        return mApi.updateNoteFields(noteId, fieldValues);
    }

    public boolean updateNoteTags(long noteId, String tagsField) {
        String[] tags = tagsField.split(" ");
        return mApi.updateNoteTags(noteId, new HashSet<>(Arrays.asList(tags)));
    }

    public int addTagToNote(long noteId, String tags) {
        ContentValues values = new ContentValues();
        values.put(FlashCardsContract.Note.TAGS, tags);

        final Uri cardUri = Uri.withAppendedPath(FlashCardsContract.Note.CONTENT_URI, Long.toString(noteId));
        return mResolver.update(cardUri, values, null, null);
    }

    abstract static class InvalidAnkiDatabaseException extends Throwable {}
    static class InvalidAnkiDatabase_fieldAndFieldNameCountMismatchException extends InvalidAnkiDatabaseException {}
}
