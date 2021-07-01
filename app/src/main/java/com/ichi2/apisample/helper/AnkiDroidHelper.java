package com.ichi2.apisample.helper;

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
import android.os.Environment;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.ichi2.anki.FlashCardsContract;
import com.ichi2.anki.api.AddContentApi;
import com.ichi2.apisample.helper.equality.EqualityChecker;
import com.ichi2.apisample.helper.equality.FieldEqualityChecker;
import com.ichi2.apisample.helper.equality.ValueEqualityChecker;
import com.ichi2.apisample.helper.search.SearchExpressionMaker;

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
    public static final String KEY_ID = "id";
    public static final String KEY_TAGS = "tags";

    public static final String HIERARCHICAL_TAG_SEPARATOR = "::";

    public static final String DIR_MEDIA = "/collection.media/";

    private static final String PACKAGE_ANKI = "com.ichi2.anki";

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


    /**
     * Check the correspondence of the stored model's parameters to input values.
     * A template card amount and string values equality is used for comparison.
     * Array arguments except fields must retain order. The field array uses a subset condition.
     * @param modelId the id of the model that is being checked
     * @param fields: array of field names
     * @param cards: array of names for the card templates, not necessarily ordered
     * @param qfmt: array of formatting strings for the question side of each template in cards
     * @param afmt: array of formatting strings for the answer side of each template in cards
     * @param css: css styling information to be shared across all of the templates
     * @return true if no differences were found, false otherwise
     * @throws IllegalArgumentException if the model with the provided id does not exist
     */
    public boolean checkCustomModel(long modelId, String[] fields, String[] cards, String[] qfmt, String[] afmt, String css) {
        Uri modelUri = Uri.withAppendedPath(FlashCardsContract.Model.CONTENT_URI, String.valueOf(modelId));
        Cursor cursor;
        try {
            cursor = mResolver.query(modelUri, null, null, null, null);
        } catch (Exception e) {
            throw new IllegalArgumentException();
        }
        cursor.moveToNext();
        String existingCss = cursor.getString(cursor.getColumnIndex(FlashCardsContract.Model.CSS));
        String existingNumCards = cursor.getString(cursor.getColumnIndex(FlashCardsContract.Model.NUM_CARDS));
        if (!StringUtil.strip(existingCss).equals(css) || Integer.parseInt(existingNumCards) != cards.length) {
            return false;
        }

        Set<String> existingFields = new HashSet<>(Arrays.asList(getFieldList(modelId)));
        for (String field : fields) {
            if (!existingFields.contains(field)) {
                return false;
            }
        }

        cursor = mResolver.query(Uri.withAppendedPath(modelUri, "templates"), null, null, null, null);
        final int colIdxName = cursor.getColumnIndex(FlashCardsContract.CardTemplate.NAME);
        final int colIdxQfmt = cursor.getColumnIndex(FlashCardsContract.CardTemplate.QUESTION_FORMAT);
        final int colIdxAfmt = cursor.getColumnIndex(FlashCardsContract.CardTemplate.ANSWER_FORMAT);
        for (int i = 0; i < cards.length; i++) {
            cursor.moveToNext();
            String existingName = cursor.getString(colIdxName);
            String existingQfmt = cursor.getString(colIdxQfmt);
            String existingAfmt = cursor.getString(colIdxAfmt);
            if (!StringUtil.strip(existingName).equals(cards[i]) ||
                    !StringUtil.strip(existingQfmt).equals(qfmt[i]) ||
                    !StringUtil.strip(existingAfmt).equals(afmt[i])) {
                return false;
            }
        }
        cursor.close();
        return true;
    }

    /**
     * Updates the stored model's parameters with input values.
     * Appends missing fields to the end of the existing ones (order is not retained).
     * For card templates, the existing ones are updated first, new ones are inserted if needed, order is retained.
     * @param modelId the id of the model that is being checked
     * @param fields: array of field names
     * @param cards: array of names for the card templates, not necessarily ordered
     * @param qfmt: array of formatting strings for the question side of each template in cards
     * @param afmt: array of formatting strings for the answer side of each template in cards
     * @param css: css styling information to be shared across all of the templates
     * @return updated model id if the operation finished successfully, null otherwise
     * @throws IllegalArgumentException if the model with the provided id does not exist
     */
    public Long updateCustomModel(long modelId, String[] fields, String[] cards, String[] qfmt, String[] afmt, String css) {
        Uri modelUri = Uri.withAppendedPath(FlashCardsContract.Model.CONTENT_URI, String.valueOf(modelId));
        Uri fieldsUri = Uri.withAppendedPath(modelUri, "fields");
        Set<String> existingFields = new HashSet<>(Arrays.asList(getFieldList(modelId)));
        for (String field : fields) {
            if (existingFields.contains(field)) {
                continue;
            }
            ContentValues values = new ContentValues();
            values.put(FlashCardsContract.Model.FIELD_NAME, field);
            try {
                Uri fieldUri = mResolver.insert(fieldsUri, values);
                if (fieldUri == null) {
                    return null;
                }
            } catch (Exception e) {
                return null;
            }
        }

        ContentValues modelValues = new ContentValues();
        modelValues.put(FlashCardsContract.Model.CSS, css);
        modelValues.put(FlashCardsContract.Model.NUM_CARDS, cards.length);
        int updated;
        try {
            updated = mResolver.update(modelUri, modelValues, null, null);
        } catch (Exception e) {
            throw new IllegalArgumentException();
        }
        if (updated == 0) {
            return null;
        }

        Uri templatesUri = Uri.withAppendedPath(modelUri, "templates");
        Cursor cursor = mResolver.query(templatesUri, null, null, null, null);
        int templatesCount = cursor.getCount();
        cursor.close();
        boolean templatesToAdd = cards.length - templatesCount > 0;
        int last = templatesToAdd ? templatesCount : cards.length;
        int i = 0;
        for (; i < last; i++) {
            Uri templateUri = Uri.withAppendedPath(templatesUri, Integer.toString(i));
            ContentValues values = new ContentValues();
            values.put(FlashCardsContract.CardTemplate.NAME, cards[i]);
            values.put(FlashCardsContract.CardTemplate.QUESTION_FORMAT, qfmt[i]);
            values.put(FlashCardsContract.CardTemplate.ANSWER_FORMAT, afmt[i]);
            updated = mResolver.update(templateUri, values, null, null);
            if (updated == 0) {
                return null;
            }
        }
        if (templatesToAdd) {
            for (; i < cards.length; i++) {
                ContentValues values = new ContentValues();
                values.put(FlashCardsContract.CardTemplate.NAME, cards[i]);
                values.put(FlashCardsContract.CardTemplate.QUESTION_FORMAT, qfmt[i]);
                values.put(FlashCardsContract.CardTemplate.ANSWER_FORMAT, afmt[i]);
                Uri templateUri = mResolver.insert(templatesUri, values);
                if (templateUri == null) {
                    return null;
                }
            }
        }

        return modelId;
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
        Uri uri = Uri.parse(uriString);
        uri = UriUtil.getContentUri(mContext, uri);
        uriString = uri.toString();
        mContext.grantUriPermission(PACKAGE_ANKI, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
        String type = mResolver.getType(uri);
        final String tempAudioFilePath = Environment.getExternalStorageDirectory().getPath() + "/tempOutput.mp3";
        if (type.startsWith("video")) {
            Uri extractedAudioUri = AudioExtractionUtil.extract(mContext, uri, tempAudioFilePath);
            // @todo: separate concerns
            mContext.revokeUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            if (extractedAudioUri == null) {
                return null;
            } else {
                uri = extractedAudioUri;
                mContext.grantUriPermission(PACKAGE_ANKI, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                uriString = uri.toString();
            }
        }

        ContentValues cv = new ContentValues();
        cv.put("file_uri", uriString);
        final String preferredName = "music_interval_" + (System.currentTimeMillis() / 1000L);
        cv.put("preferred_name", preferredName);
        try {
            Uri insertedUri = mResolver.insert(Uri.withAppendedPath(FlashCardsContract.AUTHORITY_URI, "media"), cv);
            File insertedFile = new File(insertedUri.getPath());
            String filePath = insertedFile.toString();
            return filePath.substring(1); // get rid of the "/" at the beginning
        } catch (Exception e) {
            return null;
        } finally {
            mContext.revokeUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            File tempAudioFile = new File(tempAudioFilePath);
            if (tempAudioFile.exists()) {
                tempAudioFile.delete();
            }
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

    public static final SearchExpressionMaker DEFAULT_SEARCH_EXPRESSION_MAKER = new SearchExpressionMaker() {
        @Override
        public String getExpression(String value) {
            return !value.isEmpty() ? value : "%";
        }

        @Override
        public boolean isDefinitive() {
            return true;
        }
    };

    public static final ValueEqualityChecker DEFAULT_EQUALITY_CHECKER = new ValueEqualityChecker() {
        @Override
        public boolean areEqual(String v1, String v2) {
            return v1.equalsIgnoreCase(v2);
        }
    };

    public LinkedList<Map<String, String>> findNotes(long modelId, final Map<String, String> data,
                                                     Map<String, String> fieldDefaultValues,
                                                     Map<String, SearchExpressionMaker> fieldSearchExpressionMakers,
                                                     Map<String, EqualityChecker> equalityCheckers)
            throws InvalidAnkiDatabase_fieldAndFieldNameCountMismatchException {
        ArrayList<Map<String, String>> dataSet = new ArrayList<Map<String, String>>() {{
            add(data);
        }};
        return findNotes(modelId, dataSet, fieldDefaultValues, fieldSearchExpressionMakers, equalityCheckers);
    }

    public LinkedList<Map<String, String>> findNotes(long modelId, ArrayList<Map<String, String>> dataSet,
                                                     Map<String, String> fieldDefaultValues,
                                                     Map<String, SearchExpressionMaker> fieldSearchExpressionMakers,
                                                     Map<String, EqualityChecker> equalityCheckers)
            throws InvalidAnkiDatabase_fieldAndFieldNameCountMismatchException {
        if (dataSet.size() == 0) {
            return new LinkedList<>();
        }

        String[] fieldNames = getFieldList(modelId);

        StringBuilder dataCondition = new StringBuilder();
        for (Map<String, String> data : dataSet) {
            if (dataCondition.length() > 0) {
                dataCondition.append(" or ");
            }
            ArrayList<String> defaultFields = new ArrayList<>();
            for (String fieldName : fieldNames) {
                if (data.containsKey(fieldName)) {
                    String value = data.get(fieldName);
                    if (!value.isEmpty() && fieldDefaultValues.containsKey(fieldName)) {
                        EqualityChecker defaultEqualityChecker = new FieldEqualityChecker(fieldName, DEFAULT_EQUALITY_CHECKER);
                        EqualityChecker equalityChecker = equalityCheckers.getOrDefault(fieldName, defaultEqualityChecker);
                        String defaultValue = fieldDefaultValues.get(fieldName);
                        Map<String, String> defaultData = new HashMap<>(data);
                        defaultData.put(fieldName, defaultValue);
                        if (equalityChecker.areEqual(data, defaultData)) {
                            defaultFields.add(fieldName);
                        }
                    }
                }
            }

            // here we create two conditions for each field containing default value
            // to account for the case of the default value and empty value equality
            int n = (int) Math.pow(2, defaultFields.size());
            for (int i = 0; i < n; i++) {
                StringBuilder fieldsAggregated = new StringBuilder();
                if (i > 0) {
                    dataCondition.append(" or ");
                }
                for (String fieldName : fieldNames) {
                    if (fieldsAggregated.length() > 0) {
                        fieldsAggregated.append(FLDS_SEPARATOR);
                    }

                    String expression;
                    if (data.containsKey(fieldName)) {
                        String value = data.get(fieldName);
                        expression = fieldSearchExpressionMakers.getOrDefault(fieldName, DEFAULT_SEARCH_EXPRESSION_MAKER).getExpression(value);
                    } else {
                        expression = "%";
                    }

                    // decide whether or not this is the "second" condition, for which we substitute
                    // the value that is being searched for with an empty string
                    int idx = defaultFields.indexOf(fieldName);
                    if (idx != -1 && i % (n / (int) Math.pow(2, idx)) >= (n / Math.pow(2, idx + 1))) {
                        expression = "";
                    }
                    fieldsAggregated.append(expression);
                }
                dataCondition.append(String.format(Locale.US, "%s like \"%s\"", FlashCardsContract.Note.FLDS, fieldsAggregated.toString()));
            }

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
            rows:
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
                    item.put(KEY_ID, Long.toString(notesTableCursor.getLong(idIndex)));
                    item.put(KEY_TAGS, notesTableCursor.getString(tagsIndex));

                    for (int i = 0; i < fieldNames.length; ++i) {
                        String fieldName = fieldNames[i];
                        String resultValue = fields[i];
                        // additional filtering for non-definitive expressions
                        // can be computationally expensive
                        SearchExpressionMaker expressionMaker = fieldSearchExpressionMakers.getOrDefault(fieldName, DEFAULT_SEARCH_EXPRESSION_MAKER);
                        if (!expressionMaker.isDefinitive()) {
                            EqualityChecker defaultEqualityChecker = new FieldEqualityChecker(fieldName, DEFAULT_EQUALITY_CHECKER);
                            EqualityChecker equalityChecker = equalityCheckers.getOrDefault(fieldName, defaultEqualityChecker);
                            boolean matching = false;
                            for (Map<String, String> data : dataSet) {
                                String value = data.getOrDefault(fieldName, "");
                                boolean defaultEquality = false;
                                if (resultValue.isEmpty() && fieldDefaultValues.containsKey(fieldName)) {
                                    String defaultValue = fieldDefaultValues.get(fieldName);
                                    Map<String, String> defaultData = new HashMap<>(data);
                                    defaultData.put(fieldName, defaultValue);
                                    if (equalityChecker.areEqual(data, defaultData)) {
                                        defaultEquality = true;
                                    }
                                }
                                Map<String, String> resultData = new HashMap<>(data);
                                resultData.put(fieldName, resultValue);
                                if (value.isEmpty() || equalityChecker.areEqual(data, resultData) || defaultEquality) {
                                    matching = true;
                                    break;
                                }
                            }
                            if (!matching) {
                                continue rows;
                            }
                        }
                        item.put(fieldName, resultValue);
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

    public abstract static class InvalidAnkiDatabaseException extends Throwable {}
    public static class InvalidAnkiDatabase_fieldAndFieldNameCountMismatchException extends InvalidAnkiDatabaseException {}
}
