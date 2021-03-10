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
    private static final String FIELDS_PREFERENCE_CATEGORY_KEY = "fields";
    public static final String INVALID_TAG_PREFERENCE_KEY = "invalid_tag";

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
        fieldsPreferenceCategory.setKey(FIELDS_PREFERENCE_CATEGORY_KEY);
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

        EditTextPreference invalidTagPreference = new EditTextPreference(context);
        invalidTagPreference.setKey(INVALID_TAG_PREFERENCE_KEY);
        invalidTagPreference.setTitle(R.string.invalid_tag_edit_text_preference_title);
        invalidTagPreference.setSummaryProvider(EditTextPreference.SimpleSummaryProvider.getInstance());
        invalidTagPreference.setDialogTitle(R.string.invalid_tag_edit_text_preference_dialog_title);
        invalidTagPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                try {
                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
                    final long modelId = helper.findModelIdByName(MusInterval.Builder.DEFAULT_MODEL_NAME);
                    LinkedList<Map<String, String>> recordsData = helper.findNotes(modelId, new HashMap<String, String>());
                    final String currValue = sharedPreferences.getString(SettingsFragment.INVALID_TAG_PREFERENCE_KEY, SettingsFragment.INVALID_TAG_PREFERENCE_KEY);
                    for (Map<String, String> recordData : recordsData) {
                        String tags = recordData.get(AnkiDroidHelper.KEY_TAGS);
                        if (tags.contains(String.format(" %s ", currValue))) {
                            long id = Long.parseLong(recordData.get(AnkiDroidHelper.KEY_ID));
                            helper.updateNoteTags(id, tags.replace(currValue, (String) newValue));
                        }
                    }
                } catch (AnkiDroidHelper.InvalidAnkiDatabaseException e) {
                    return false;
                }
                return true;
            }
        });
        preferenceScreen.addPreference(invalidTagPreference);

        setPreferenceScreen(preferenceScreen);
    }
}
