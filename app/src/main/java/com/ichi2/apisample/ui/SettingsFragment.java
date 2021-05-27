package com.ichi2.apisample.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;

import androidx.preference.CheckBoxPreference;
import androidx.preference.DropDownPreference;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreference;

import com.ichi2.apisample.model.MusInterval;
import com.ichi2.apisample.R;
import com.ichi2.apisample.helper.AnkiDroidHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SettingsFragment extends PreferenceFragmentCompat {
    public static final String KEY_DECK_PREFERENCE = "preference_deck";
    public static final String KEY_USE_DEFAULT_MODEL_CHECK = "preference_use_default_model";
    public static final String KEY_MODEL_PREFERENCE = "preference_model";
    public static final String KEY_VERSION_FIELD_SWITCH = "preference_version_field_switch";
    public static final String KEY_TAG_DUPLICATES_SWITCH = "preference_tag_duplicates_switch";
    public static final String KEY_ANKI_DIR_PREFERENCE = "preference_anki_dir";

    private static final String KEY_FIELDS_PREFERENCE_CATEGORY = "preference_fields";

    private static final String TEMPLATE_KEY_FIELD_PREFERENCE = "preference_%s_field";
    private static final String TEMPLATE_KEY_MODEL_FIELD_PREFERENCE = "%s_%s_model";

    public static final boolean DEFAULT_USE_DEFAULT_MODEL_CHECK = true;
    public static final boolean DEFAULT_VERSION_FIELD_SWITCH = false;
    public static final boolean DEFAULT_TAG_DUPLICATES_SWITCH = true;
    public static final String DEFAULT_ANKI_DIR = Environment.getExternalStorageDirectory().getPath() + "/AnkiDroid";

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

        final ListPreference modelListPreference = new ListPreference(context);
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

        final String[] fullSignature = MusInterval.Fields.getSignature(true);
        PreferenceCategory fieldsPreferenceCategory = new PreferenceCategory(context);
        fieldsPreferenceCategory.setKey(KEY_FIELDS_PREFERENCE_CATEGORY);
        fieldsPreferenceCategory.setTitle(R.string.fields_preference_category_title);
        fieldsPreferenceCategory.setInitialExpandedChildrenCount(0);
        preferenceScreen.addPreference(fieldsPreferenceCategory);
        for (String fieldKey : fullSignature) {
            ListPreference fieldListPreference = new DropDownPreference(context);
            fieldListPreference.setKey(getFieldPreferenceKey(fieldKey));
            fieldListPreference.setTitle(getFieldPreferenceLabelString(fieldKey, context));
            fieldListPreference.setSummaryProvider(ListPreference.SimpleSummaryProvider.getInstance());
            fieldListPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    for (String fieldKey : fullSignature) {
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
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        final boolean versionField = preferences.getBoolean(KEY_VERSION_FIELD_SWITCH, DEFAULT_VERSION_FIELD_SWITCH);
        if (!versionField) {
            String versionFieldPreferenceKey = getFieldPreferenceKey(MusInterval.Fields.VERSION);
            ListPreference versionFieldListPreference = preferenceScreen.findPreference(versionFieldPreferenceKey);
            if (versionFieldListPreference != null) {
                versionFieldListPreference.setVisible(false);
            }
        }
        updateFieldsPreferenceEntries(modelListPreference.getValue(), fullSignature, false);

        final String[] mainSignature = MusInterval.Fields.getSignature(false);
        final CheckBoxPreference useDefaultModelCheckPreference = new CheckBoxPreference(context);
        useDefaultModelCheckPreference.setKey(KEY_USE_DEFAULT_MODEL_CHECK);
        useDefaultModelCheckPreference.setTitle(R.string.use_default_model_check_preference_title);
        useDefaultModelCheckPreference.setSummary(R.string.use_default_model_check_preference_summary);
        useDefaultModelCheckPreference.setDefaultValue(DEFAULT_USE_DEFAULT_MODEL_CHECK);
        useDefaultModelCheckPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                boolean newVal = (boolean) newValue;
                for (String fieldKey : mainSignature) {
                    String fieldPreferenceKey = getFieldPreferenceKey(fieldKey);
                    ListPreference fieldListPreference = preferenceScreen.findPreference(fieldPreferenceKey);
                    if (newVal) {
                        fieldListPreference.setValue(fieldKey);
                        fieldListPreference.setEnabled(false);
                    } else {
                        fieldListPreference.setEnabled(true);
                    }
                }

                final String defaultModel = MusInterval.Builder.DEFAULT_MODEL_NAME;
                if (newVal) {
                    final String currModel = modelListPreference.getValue();
                    if (!currModel.equals(defaultModel)) {
                        updateFieldsPreferenceEntries(defaultModel, fullSignature, true);
                    }
                    modelListPreference.setValue(defaultModel);
                    modelListPreference.setEnabled(false);

                    updateVersionFieldPreferenceEntries(true);
                } else {
                    modelListPreference.setEnabled(true);
                    updateFieldsPreferenceEntries(defaultModel, fullSignature, false);
                }
                return true;
            }
        });
        preferenceScreen.addPreference(useDefaultModelCheckPreference);
        modelListPreference.setEnabled(!useDefaultModelCheckPreference.isChecked());
        for (String fieldKey : mainSignature) {
            String fieldPreferenceKey = getFieldPreferenceKey(fieldKey);
            ListPreference fieldListPreference = preferenceScreen.findPreference(fieldPreferenceKey);
            if (useDefaultModelCheckPreference.isChecked()) {
                fieldListPreference.setEnabled(false);
                fieldListPreference.setValue(fieldKey);
            } else {
                fieldListPreference.setEnabled(true);
            }
        }
        if (useDefaultModelCheckPreference.isChecked()) {
            updateVersionFieldPreferenceEntries(true);
        }

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
                    if (versionField) {
                        versionFieldListPreference.setVisible(true);
                        updateVersionFieldPreferenceEntries(useDefaultModelCheckPreference.isChecked());
                    } else {
                        versionFieldListPreference.setVisible(false);
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

        EditTextPreference ankiDirPreference = new EditTextPreference(context);
        ankiDirPreference.setKey(KEY_ANKI_DIR_PREFERENCE);
        ankiDirPreference.setTitle(R.string.anki_dir_preference_title);
        ankiDirPreference.setSummaryProvider(EditTextPreference.SimpleSummaryProvider.getInstance());
        ankiDirPreference.setDialogTitle(R.string.anki_dir_preference_title);
        ankiDirPreference.setDefaultValue(DEFAULT_ANKI_DIR);
        preferenceScreen.addPreference(ankiDirPreference);

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

    private void updateVersionFieldPreferenceEntries(boolean excludeTaken) {
        String versionFieldPreferenceKey = getFieldPreferenceKey(MusInterval.Fields.VERSION);
        String value = PreferenceManager.getDefaultSharedPreferences(context).getString(versionFieldPreferenceKey, "");
        Long modelId = helper.findModelIdByName(MusInterval.Builder.DEFAULT_MODEL_NAME);
        ArrayList<String> fields = modelId != null ? new ArrayList<>(Arrays.asList(helper.getFieldList(modelId))) : new ArrayList<String>();
        ArrayList<String> availableFields = new ArrayList<>();
        if (excludeTaken) {
            Set<String> takenFields = new HashSet<>(Arrays.asList(MusInterval.Fields.getSignature(false)));
            for (String fieldKey : fields) {
                if (!takenFields.contains(fieldKey)) {
                    availableFields.add(fieldKey);
                }
            }
            if (takenFields.contains(value)) {
                value = "";
            }
        } else {
            availableFields = fields;
        }
        ListPreference versionFieldListPreference = preferenceScreen.findPreference(versionFieldPreferenceKey);
        availableFields.add(0, "");
        String[] versionEntries = availableFields.toArray(new String[0]);
        versionFieldListPreference.setEntries(versionEntries);
        versionFieldListPreference.setEntryValues(versionEntries);
        versionFieldListPreference.setValue(value);
    }
}
