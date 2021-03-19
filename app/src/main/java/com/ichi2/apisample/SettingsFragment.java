package com.ichi2.apisample;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;

import androidx.preference.DropDownPreference;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.PreferenceScreen;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class SettingsFragment extends PreferenceFragmentCompat {
    public static final String KEY_CORRUPTED_TAG_PREFERENCE = "invalid_tag";
    public static final String KEY_SUSPICIOUS_TAG_PREFERENCE = "suspicipus_tag";
    private static final String KEY_FIELDS_PREFERENCE_CATEGORY = "fields";

    public static final String DEFAULT_CORRUPTED_TAG = "mi2a_invalid";
    public static final String DEFAULT_SUSPICIOUS_TAG = "mi2a_suspicious";

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        final Context context = getPreferenceManager().getContext();
        PreferenceScreen preferenceScreen = getPreferenceManager().createPreferenceScreen(context);

        final AnkiDroidHelper helper = new AnkiDroidHelper(context);

        Long modelId = helper.findModelIdByName(MusInterval.Builder.DEFAULT_MODEL_NAME);
        if (modelId == null) {
            getActivity().finish();
            return;
        }

        Preference deckPreference = new Preference(context);
        deckPreference.setTitle(R.string.deck_preference_title);
        deckPreference.setSummary(MusInterval.Builder.DEFAULT_DECK_NAME);
        deckPreference.setPersistent(false); // @todo: add deck configuration
        preferenceScreen.addPreference(deckPreference);

        Preference modelPreference = new Preference(context);
        modelPreference.setTitle(R.string.model_preference_title);
        modelPreference.setSummary(MusInterval.Builder.DEFAULT_MODEL_NAME);
        deckPreference.setPersistent(false); // @todo: add model configuration
        preferenceScreen.addPreference(modelPreference);

        String[] keys = MusInterval.Fields.SIGNATURE;
        Resources res = getResources();
        String[] titles = new String[]{
                res.getString(R.string.sound_field_list_preference_title),
                res.getString(R.string.sound_smaller_field_list_preference_title),
                res.getString(R.string.sound_larger_field_list_preference_title),
                res.getString(R.string.start_note_field_list_preference_title),
                res.getString(R.string.direction_field_list_preference_title),
                res.getString(R.string.timing_field_list_preference_title),
                res.getString(R.string.interval_field_list_preference_title),
                res.getString(R.string.tempo_field_list_preference_title),
                res.getString(R.string.instrument_field_list_preference_title),
        };
        String[] entryValues = helper.getFieldList(modelId);
        String[] entries = new String[entryValues.length + 1];
        entries[0] = "";
        System.arraycopy(entryValues, 0, entries, 1, entryValues.length);
        PreferenceCategory fieldsPreferenceCategory = new PreferenceCategory(context);
        fieldsPreferenceCategory.setKey(KEY_FIELDS_PREFERENCE_CATEGORY);
        fieldsPreferenceCategory.setTitle(R.string.fields_preference_category_title);
        fieldsPreferenceCategory.setInitialExpandedChildrenCount(0);
        preferenceScreen.addPreference(fieldsPreferenceCategory);
        for (int i = 0; i < keys.length; i++) {
            ListPreference fieldListPreference = new DropDownPreference(context);
            fieldListPreference.setKey(keys[i]);
            fieldListPreference.setTitle(titles[i]);
            fieldListPreference.setDefaultValue(keys[i]);
            fieldListPreference.setSummaryProvider(ListPreference.SimpleSummaryProvider.getInstance());
            fieldListPreference.setEntries(entries);
            fieldListPreference.setEntryValues(entries);
            fieldsPreferenceCategory.addPreference(fieldListPreference);
        }

        EditTextPreference corruptedTagPreference = new EditTextPreference(context);
        corruptedTagPreference.setKey(KEY_CORRUPTED_TAG_PREFERENCE);
        corruptedTagPreference.setDefaultValue(DEFAULT_CORRUPTED_TAG);
        corruptedTagPreference.setTitle(R.string.corrupted_tag_edit_text_preference_title);
        corruptedTagPreference.setSummaryProvider(EditTextPreference.SimpleSummaryProvider.getInstance());
        corruptedTagPreference.setDialogTitle(R.string.corrupted_tag_edit_text_preference_dialog_title);
        corruptedTagPreference.setOnPreferenceChangeListener(new TagPreferenceChangeListener(context, helper, KEY_CORRUPTED_TAG_PREFERENCE, DEFAULT_CORRUPTED_TAG));
        preferenceScreen.addPreference(corruptedTagPreference);

        EditTextPreference suspiciousTagPreference = new EditTextPreference(context);
        suspiciousTagPreference.setKey(KEY_SUSPICIOUS_TAG_PREFERENCE);
        suspiciousTagPreference.setDefaultValue(DEFAULT_SUSPICIOUS_TAG);
        suspiciousTagPreference.setTitle(R.string.suspicious_tag_edit_text_preference_title);
        suspiciousTagPreference.setSummaryProvider(EditTextPreference.SimpleSummaryProvider.getInstance());
        suspiciousTagPreference.setDialogTitle(R.string.suspicious_tag_edit_text_preference_dialog_title);
        suspiciousTagPreference.setOnPreferenceChangeListener(new TagPreferenceChangeListener(context, helper, KEY_SUSPICIOUS_TAG_PREFERENCE, DEFAULT_SUSPICIOUS_TAG));
        preferenceScreen.addPreference(suspiciousTagPreference);

        setPreferenceScreen(preferenceScreen);
    }

    private class TagPreferenceChangeListener implements Preference.OnPreferenceChangeListener {
        private final Context context;
        private final AnkiDroidHelper helper;
        private final String key;
        private final String defaultValue;

        public TagPreferenceChangeListener(Context context, AnkiDroidHelper helper, String key, String defaultValue) {
            super();
            this.context = context;
            this.helper = helper;
            this.key = key;
            this.defaultValue = defaultValue;
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            try {
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
                final long modelId = helper.findModelIdByName(MusInterval.Builder.DEFAULT_MODEL_NAME);
                LinkedList<Map<String, String>> notesData = helper.findNotes(modelId, new HashMap<String, String>());
                final String currValue = sharedPreferences.getString(key, defaultValue);
                for (Map<String, String> noteData : notesData) {
                    String tags = noteData.get(AnkiDroidHelper.KEY_TAGS);
                    if (tags.contains(String.format(" %s ", currValue))) {
                        long id = Long.parseLong(noteData.get(AnkiDroidHelper.KEY_ID));
                        helper.updateNoteTags(id, tags.replace(currValue, (String) newValue));
                    }
                }
            } catch (AnkiDroidHelper.InvalidAnkiDatabaseException e) {
                return false;
            }
            return true;
        }
    }
}
