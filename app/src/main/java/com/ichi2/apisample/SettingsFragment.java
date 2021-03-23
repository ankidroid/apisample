package com.ichi2.apisample;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;

import androidx.preference.DropDownPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SettingsFragment extends PreferenceFragmentCompat {
    public static final String KEY_DECK_PREFERENCE = "preference_deck";
    public static final String KEY_MODEL_PREFERENCE = "preference_model";

    private static final String KEY_FIELDS_PREFERENCE_CATEGORY = "preference_fields";

    private AnkiDroidHelper helper;

    private PreferenceCategory fieldsPreferenceCategory;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        Context context = getPreferenceManager().getContext();
        PreferenceScreen preferenceScreen = getPreferenceManager().createPreferenceScreen(context);

        helper = new AnkiDroidHelper(context);

        ListPreference deckListPreference = new ListPreference(context);
        deckListPreference.setKey(KEY_DECK_PREFERENCE);
        deckListPreference.setTitle(R.string.deck_preference_title);
        deckListPreference.setSummaryProvider(ListPreference.SimpleSummaryProvider.getInstance());
        Map<Long, String> deckList = helper.getDeckList();
        List<String> deckEntriesList = new ArrayList<>(deckList.values());
        if (!deckEntriesList.contains(MusInterval.Builder.DEFAULT_DECK_NAME)) {
            deckEntriesList.add(MusInterval.Builder.DEFAULT_DECK_NAME);
        }
        deckListPreference.setDefaultValue(MusInterval.Builder.DEFAULT_DECK_NAME);
        String[] deckEntries = deckEntriesList.toArray(new String[deckEntriesList.size()]);
        deckListPreference.setEntries(deckEntries);
        deckListPreference.setEntryValues(deckEntries);
        preferenceScreen.addPreference(deckListPreference);

        ListPreference modelListPreference = new ListPreference(context);
        modelListPreference.setKey(KEY_MODEL_PREFERENCE);
        modelListPreference.setTitle(R.string.model_preference_title);
        modelListPreference.setSummaryProvider(ListPreference.SimpleSummaryProvider.getInstance());
        Map<Long, String> modelList = helper.getModelList();
        List<String> modelEntriesList = new ArrayList<>(modelList.values());
        if (!modelEntriesList.contains(MusInterval.Builder.DEFAULT_MODEL_NAME)) {
            modelEntriesList.add(MusInterval.Builder.DEFAULT_MODEL_NAME);
        }
        modelListPreference.setDefaultValue(MusInterval.Builder.DEFAULT_MODEL_NAME);
        String[] modelEntries = modelEntriesList.toArray(new String[modelEntriesList.size()]);
        modelListPreference.setEntries(modelEntries);
        modelListPreference.setEntryValues(modelEntries);
        modelListPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Long newModelId = helper.findModelIdByName((String) newValue);
                refreshFieldsPreferenceEntries(newModelId);
                return true;
            }
        });
        preferenceScreen.addPreference(modelListPreference);

        Resources res = getResources();
        String[] fieldTitles = new String[]{
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
        fieldsPreferenceCategory = new PreferenceCategory(context);
        fieldsPreferenceCategory.setKey(KEY_FIELDS_PREFERENCE_CATEGORY);
        fieldsPreferenceCategory.setTitle(R.string.fields_preference_category_title);
        fieldsPreferenceCategory.setInitialExpandedChildrenCount(0);
        preferenceScreen.addPreference(fieldsPreferenceCategory);
        for (int i = 0; i < MusInterval.Fields.SIGNATURE.length; i++) {
            ListPreference fieldListPreference = new DropDownPreference(context);
            fieldListPreference.setKey(MusInterval.Fields.SIGNATURE[i]);
            fieldListPreference.setTitle(fieldTitles[i]);
            fieldListPreference.setDefaultValue(MusInterval.Fields.SIGNATURE[i]);
            fieldListPreference.setSummaryProvider(ListPreference.SimpleSummaryProvider.getInstance());
            fieldsPreferenceCategory.addPreference(fieldListPreference);
        }
        Long modelId = helper.findModelIdByName(modelListPreference.getValue(), MusInterval.Fields.SIGNATURE.length);
        refreshFieldsPreferenceEntries(modelId);

        setPreferenceScreen(preferenceScreen);
    }

    private void refreshFieldsPreferenceEntries(Long modelId) {
        String[] fieldList = modelId != null ? helper.getFieldList(modelId) : new String[]{};
        boolean enabled = fieldList.length > 0;
        for (int i = 0; i < MusInterval.Fields.SIGNATURE.length; i++) {
            ListPreference fieldListPreference = (ListPreference) fieldsPreferenceCategory.getPreference(i);
            fieldListPreference.setEntries(fieldList);
            fieldListPreference.setEntryValues(fieldList);
            fieldListPreference.setEnabled(enabled);
        }
    }
}
