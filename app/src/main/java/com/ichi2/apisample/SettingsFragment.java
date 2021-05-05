package com.ichi2.apisample;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.preference.DropDownPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class SettingsFragment extends PreferenceFragmentCompat {
    public static final String KEY_DECK_PREFERENCE = "preference_deck";
    public static final String KEY_MODEL_PREFERENCE = "preference_model";
    public static final String KEY_VERSION_FIELD_SWITCH = "preference_version_field_switch";
    public static final String KEY_TAG_DUPLICATES_SWITCH = "preference_tag_duplicates_switch";
    private static final String KEY_FIELDS_PREFERENCE_CATEGORY = "preference_fields";

    private static final String TEMPLATE_KEY_FIELD_PREFERENCE = "preference_%s_field";
    private static final String TEMPLATE_KEY_MODEL_FIELD_PREFERENCE = "%s_%s_model";

    public static final boolean DEFAULT_VERSION_FIELD_SWITCH = true;
    public static final boolean DEFAULT_TAG_DUPLICATES_SWITCH = true;

    private static final Map<String, Integer> FIELD_PREFERENCE_LABEL_STRING_IDS = new HashMap<String, Integer>() {{
        put(MusInterval.Fields.SOUND, R.string.sound_field_list_preference_title);
        put(MusInterval.Fields.SOUND_SMALLER, R.string.sound_smaller_field_list_preference_title);
        put(MusInterval.Fields.SOUND_LARGER, R.string.sound_larger_field_list_preference_title);
        put(MusInterval.Fields.START_NOTE, R.string.start_note_field_list_preference_title);
        put(MusInterval.Fields.DIRECTION, R.string.direction_field_list_preference_title);
        put(MusInterval.Fields.TIMING, R.string.timing_field_list_preference_title);
        put(MusInterval.Fields.INTERVAL, R.string.interval_field_list_preference_title);
        put(MusInterval.Fields.TEMPO, R.string.tempo_field_list_preference_title);
        put(MusInterval.Fields.INSTRUMENT, R.string.instrument_field_list_preference_title);
        put(MusInterval.Fields.VERSION, R.string.version_field_list_preference_title);
    }};
    static {
        if (!FIELD_PREFERENCE_LABEL_STRING_IDS.keySet().equals(new HashSet<>(Arrays.asList(MusInterval.Fields.getSignature(true))))) {
            throw new AssertionError();
        }
    }

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
                final boolean versionField = preferences.getBoolean(KEY_VERSION_FIELD_SWITCH, DEFAULT_VERSION_FIELD_SWITCH);
                final String[] signature = MusInterval.Fields.getSignature(versionField);
                Long modelId = helper.findModelIdByName(currModel);
                if (modelId != null) {
                    SharedPreferences.Editor preferencesEditor = preferences.edit();
                    for (String fieldKey : signature) {
                        String fieldPreferenceKey = getFieldPreferenceKey(fieldKey);
                        String fieldPreference = preferences.getString(fieldPreferenceKey, "");
                        String modelFieldPreferenceKey = getModelFieldPreferenceKey(modelId, fieldPreferenceKey);
                        preferencesEditor.putString(modelFieldPreferenceKey, fieldPreference);
                    }
                    preferencesEditor.apply();
                }
                updateFieldsPreferenceEntries((String) newValue, signature, true);
                return true;
            }
        });
        preferenceScreen.addPreference(modelListPreference);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        final boolean versionField = preferences.getBoolean(KEY_VERSION_FIELD_SWITCH, DEFAULT_VERSION_FIELD_SWITCH);
        final String[] signature = MusInterval.Fields.getSignature(true);
        PreferenceCategory fieldsPreferenceCategory = new PreferenceCategory(context);
        fieldsPreferenceCategory.setKey(KEY_FIELDS_PREFERENCE_CATEGORY);
        fieldsPreferenceCategory.setTitle(R.string.fields_preference_category_title);
        fieldsPreferenceCategory.setInitialExpandedChildrenCount(0);
        preferenceScreen.addPreference(fieldsPreferenceCategory);
        for (String fieldKey : signature) {
            ListPreference fieldListPreference = new DropDownPreference(context);
            fieldListPreference.setKey(getFieldPreferenceKey(fieldKey));
            fieldListPreference.setTitle(getFieldPreferenceLabelString(fieldKey, context));
            fieldListPreference.setSummaryProvider(ListPreference.SimpleSummaryProvider.getInstance());
            fieldListPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    for (String fieldKey : signature) {
                        String fieldPreferenceKey = getFieldPreferenceKey(fieldKey);
                        ListPreference fieldListPreference = preferenceScreen.findPreference(fieldPreferenceKey);
                        if (fieldListPreference != null && fieldListPreference.getValue().equals(newValue)) {
                            fieldListPreference.setValue("");
                        }
                    }
                    return true;
                }
            });
            fieldsPreferenceCategory.addPreference(fieldListPreference);
        }
        if (!versionField) {
            String versionFieldPreferenceKey = getFieldPreferenceKey(MusInterval.Fields.VERSION);
            ListPreference versionFieldListPreference = preferenceScreen.findPreference(versionFieldPreferenceKey);
            if (versionFieldListPreference != null) {
                versionFieldListPreference.setVisible(false);
            }
        }
        updateFieldsPreferenceEntries(modelListPreference.getValue(), signature, false);

        SwitchPreference versionFieldSwitchPreference = new SwitchPreference(context);
        versionFieldSwitchPreference.setKey(KEY_VERSION_FIELD_SWITCH);
        versionFieldSwitchPreference.setTitle(R.string.version_field_switch_preference_title);
        versionFieldSwitchPreference.setSummary(R.string.version_field_switch_preference_summary);
        versionFieldSwitchPreference.setDefaultValue(DEFAULT_VERSION_FIELD_SWITCH);
        versionFieldSwitchPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                String versionFieldPreferenceKey = getFieldPreferenceKey(MusInterval.Fields.VERSION);
                ListPreference versionFieldListPreference = preferenceScreen.findPreference(versionFieldPreferenceKey);
                if (versionFieldListPreference != null) {
                    final boolean versionField = (boolean) newValue;
                    versionFieldListPreference.setVisible(versionField);
                    if (versionField) {
                        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
                        String model = preferences.getString(KEY_MODEL_PREFERENCE, "");
                        final String[] signature = MusInterval.Fields.getSignature(true);
                        updateFieldsPreferenceEntries(model, signature, false);
                    }
                }
                return true;
            }
        });
        preferenceScreen.addPreference(versionFieldSwitchPreference);

        SwitchPreference tagDuplicatesSwitchPreference = new SwitchPreference(context);
        tagDuplicatesSwitchPreference.setKey(KEY_TAG_DUPLICATES_SWITCH);
        tagDuplicatesSwitchPreference.setTitle(R.string.tag_duplicates_preference_title);
        tagDuplicatesSwitchPreference.setSummary(R.string.tag_duplicates_preference_summary);
        tagDuplicatesSwitchPreference.setDefaultValue(DEFAULT_TAG_DUPLICATES_SWITCH);
        preferenceScreen.addPreference(tagDuplicatesSwitchPreference);

        setPreferenceScreen(preferenceScreen);
    }

    public static String getFieldPreferenceKey(String fieldKey) {
        return String.format(TEMPLATE_KEY_FIELD_PREFERENCE, fieldKey);
    }

    public static String getModelFieldPreferenceKey(long modelId, String fieldPreferenceKey) {
        return String.format(TEMPLATE_KEY_MODEL_FIELD_PREFERENCE, fieldPreferenceKey, modelId);
    }

    public static String getFieldPreferenceLabelString(String fieldKey, Context context) {
        Integer resId = FIELD_PREFERENCE_LABEL_STRING_IDS.get(fieldKey);
        return resId != null ? context.getResources().getString(resId) : "";
    }

    private void updateFieldsPreferenceEntries(String modelName, String[] signature, boolean modelChanged) {
        Long modelId = helper.findModelIdByName(modelName);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String[] fieldList = modelId != null ? helper.getFieldList(modelId) : new String[]{};
        String[] entries = new String[fieldList.length + 1];
        entries[0] = "";
        System.arraycopy(fieldList, 0, entries, 1, fieldList.length);
        for (String fieldKey : signature) {
            String fieldPreferenceKey = getFieldPreferenceKey(fieldKey);
            String value = "";
            if (modelChanged) {
                if (modelId != null) {
                    String modelFieldPreferenceKey = getModelFieldPreferenceKey(modelId, fieldPreferenceKey);
                    value = preferences.getString(modelFieldPreferenceKey, "");
                }
            } else {
                value = preferences.getString(fieldPreferenceKey, "");
            }
            ListPreference fieldListPreference = preferenceScreen.findPreference(fieldPreferenceKey);
            if (fieldListPreference != null) {
                fieldListPreference.setEntries(entries);
                fieldListPreference.setEntryValues(entries);
                fieldListPreference.setValue(value);
            }
        }
    }
}
