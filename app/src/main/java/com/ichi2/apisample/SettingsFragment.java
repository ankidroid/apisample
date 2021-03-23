package com.ichi2.apisample;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;

import androidx.preference.DropDownPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.PreferenceScreen;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SettingsFragment extends PreferenceFragmentCompat {
    public static final String KEY_DECK_PREFERENCE = "preference_deck";
    public static final String KEY_MODEL_PREFERENCE = "preference_model";

    private static final String KEY_FIELDS_PREFERENCE_CATEGORY = "preference_fields";

    private static final String TEMPLATE_KEY_FIELD_PREFERENCE = "preference_%s_field";
    private static final String TEMPLATE_KEY_MODEL_FIELD_PREFERENCE = "%s_%s_model";

    private Context context;
    private PreferenceScreen preferenceScreen;

    private AnkiDroidHelper helper;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        context = getPreferenceManager().getContext();
        preferenceScreen = getPreferenceManager().createPreferenceScreen(context);

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
        String[] deckEntries = deckEntriesList.toArray(new String[0]);
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
        String[] modelEntries = modelEntriesList.toArray(new String[0]);
        modelListPreference.setEntries(modelEntries);
        modelListPreference.setEntryValues(modelEntries);
        modelListPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
                String currModel = preferences.getString(KEY_MODEL_PREFERENCE, "");
                Long modelId = helper.findModelIdByName(currModel);
                if (modelId != null) {
                    SharedPreferences.Editor preferencesEditor = preferences.edit();
                    for (int i = 0; i < MusInterval.Fields.SIGNATURE.length; i++) {
                        String fieldKey = MusInterval.Fields.SIGNATURE[i];
                        String fieldPreferenceKey = getFieldPreferenceKey(fieldKey);
                        String fieldPreference = preferences.getString(fieldPreferenceKey, "");
                        String modelFieldPreferenceKey = getModelFieldPreferenceKey(modelId, fieldPreferenceKey);
                        preferencesEditor.putString(modelFieldPreferenceKey, fieldPreference);
                    }
                    preferencesEditor.apply();
                }
                updateFieldsPreferenceEntries((String) newValue);
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
        PreferenceCategory fieldsPreferenceCategory = new PreferenceCategory(context);
        fieldsPreferenceCategory.setKey(KEY_FIELDS_PREFERENCE_CATEGORY);
        fieldsPreferenceCategory.setTitle(R.string.fields_preference_category_title);
        fieldsPreferenceCategory.setInitialExpandedChildrenCount(0);
        preferenceScreen.addPreference(fieldsPreferenceCategory);
        for (int i = 0; i < MusInterval.Fields.SIGNATURE.length; i++) {
            ListPreference fieldListPreference = new DropDownPreference(context);
            fieldListPreference.setKey(getFieldPreferenceKey(MusInterval.Fields.SIGNATURE[i]));
            fieldListPreference.setTitle(fieldTitles[i]);
            fieldListPreference.setSummaryProvider(ListPreference.SimpleSummaryProvider.getInstance());
            fieldsPreferenceCategory.addPreference(fieldListPreference);
        }
        updateFieldsPreferenceEntries(modelListPreference.getValue());

        setPreferenceScreen(preferenceScreen);
    }

    public static String getFieldPreferenceKey(String fieldKey) {
        return String.format(TEMPLATE_KEY_FIELD_PREFERENCE, fieldKey);
    }

    public static String getModelFieldPreferenceKey(long modelId, String fieldPreferenceKey) {
        return String.format(TEMPLATE_KEY_MODEL_FIELD_PREFERENCE, fieldPreferenceKey, modelId);
    }

    private void updateFieldsPreferenceEntries(String modelName) {
        Long modelId = helper.findModelIdByName(modelName);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String[] fieldList = modelId != null ? helper.getFieldList(modelId) : new String[]{};
        String[] entries = new String[fieldList.length + 1];
        entries[0] = "";
        System.arraycopy(fieldList, 0, entries, 1, fieldList.length);
        for (int i = 0; i < MusInterval.Fields.SIGNATURE.length; i++) {
            String fieldKey = MusInterval.Fields.SIGNATURE[i];
            String fieldPreferenceKey = getFieldPreferenceKey(fieldKey);
            String modelFieldPreference = "";
            if (modelId != null) {
                String modelFieldPreferenceKey = getModelFieldPreferenceKey(modelId, fieldPreferenceKey);
                modelFieldPreference = preferences.getString(modelFieldPreferenceKey, "");
            }
            ListPreference fieldListPreference = preferenceScreen.findPreference(fieldPreferenceKey);
            if (fieldListPreference != null) {
                fieldListPreference.setEntries(entries);
                fieldListPreference.setEntryValues(entries);
                fieldListPreference.setValue(modelFieldPreference);
            }
        }
    }
}
