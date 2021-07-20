package com.ichi2.apisample.ui.settings;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;

import androidx.preference.CheckBoxPreference;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreference;

import com.ichi2.apisample.helper.MapUtil;
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
    public static final String ACTION_SHOW_FIELDS_MAPPING_DIALOG = "SettingsFragment:ShowFieldsMappingDialog";

    public static final String KEY_DECK_PREFERENCE = "preference_deck";
    public static final String KEY_MODEL_PREFERENCE = "preference_model";
    public static final String KEY_FIELDS_PREFERENCE = "preference_fields";
    public static final String KEY_USE_DEFAULT_MODEL_CHECK = "preference_use_default_model";
    public static final String KEY_VERSION_FIELD_SWITCH = "preference_version_field_switch";
    public static final String KEY_TAG_DUPLICATES_SWITCH = "preference_tag_duplicates_switch";
    public static final String KEY_ANKI_DIR_PREFERENCE = "preference_anki_dir";
    public static final String KEY_FILES_DELETION_PREFERENCE = "preference_files_deletion";

    public static final String VALUE_FILES_DELETION_DISABLED = "none";
    public static final String VALUE_FILES_DELETION_CREATED_ONLY = "created_only";
    public static final String VALUE_FILES_DELETION_ALL = "all";
    public static final String VALUE_FILES_DELETION_ALWAYS_ASK = "always_ask";
    public static final Map<String, Integer> FILES_DELETION_VALUE_ENTRIES = new HashMap<String, Integer>() {{
        put(VALUE_FILES_DELETION_DISABLED, R.string.files_deletion_entry_none);
        put(VALUE_FILES_DELETION_CREATED_ONLY, R.string.files_deletion_entry_created_only);
        put(VALUE_FILES_DELETION_ALL, R.string.files_deletion_entry_all);
        put(VALUE_FILES_DELETION_ALWAYS_ASK, R.string.files_deletion_entry_always_ask);
    }};

    public static final boolean DEFAULT_USE_DEFAULT_MODEL_CHECK = true;
    public static final boolean DEFAULT_VERSION_FIELD_SWITCH = true;
    public static final boolean DEFAULT_TAG_DUPLICATES_SWITCH = true;
    public static final String DEFAULT_ANKI_DIR = Environment.getExternalStorageDirectory().getPath() + "/AnkiDroid";
    public static final String DEFAULT_FILES_DELETION = VALUE_FILES_DELETION_ALWAYS_ASK;

    private Context context;

    private AnkiDroidHelper helper;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        context = getPreferenceManager().getContext();
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
                String newModel = (String) newValue;
                handleModelChange(currModel, newModel);
                return true;
            }
        });
        preferenceScreen.addPreference(modelListPreference);

        MappingPreference fieldsMappingPreference = new MappingPreference(context);
        fieldsMappingPreference.setKey(KEY_FIELDS_PREFERENCE);
        fieldsMappingPreference.setTitle(R.string.fields_mapping_preference_title);
        fieldsMappingPreference.setDialogLayoutResource(R.layout.dialog_mapping);
        preferenceScreen.addPreference(fieldsMappingPreference);

        final SwitchPreference versionFieldSwitchPreference = new SwitchPreference(context);
        versionFieldSwitchPreference.setKey(KEY_VERSION_FIELD_SWITCH);
        versionFieldSwitchPreference.setTitle(R.string.version_field_switch_preference_title);
        versionFieldSwitchPreference.setSummary(R.string.version_field_switch_preference_summary);
        versionFieldSwitchPreference.setDefaultValue(DEFAULT_VERSION_FIELD_SWITCH);

        final CheckBoxPreference useDefaultModelCheckPreference = new CheckBoxPreference(context);
        useDefaultModelCheckPreference.setKey(KEY_USE_DEFAULT_MODEL_CHECK);
        useDefaultModelCheckPreference.setTitle(R.string.use_default_model_check_preference_title);
        useDefaultModelCheckPreference.setSummary(R.string.use_default_model_check_preference_summary);
        useDefaultModelCheckPreference.setDefaultValue(DEFAULT_USE_DEFAULT_MODEL_CHECK);
        useDefaultModelCheckPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if ((boolean) newValue) {
                    versionFieldSwitchPreference.setChecked(true);
                    versionFieldSwitchPreference.setEnabled(false);

                    modelListPreference.setValue(MusInterval.Builder.DEFAULT_MODEL_NAME);
                    modelListPreference.setEnabled(false);

                    String currModel = modelListPreference.getValue();
                    handleModelChange(currModel, MusInterval.Builder.DEFAULT_MODEL_NAME);

                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
                    SharedPreferences.Editor preferencesEditor = preferences.edit();
                    Set<String> defaultFields = getDefaultFields(preferences);
                    preferencesEditor.putStringSet(KEY_FIELDS_PREFERENCE, defaultFields);
                    preferencesEditor.apply();
                } else {
                    versionFieldSwitchPreference.setEnabled(true);
                    modelListPreference.setEnabled(true);
                }
                return true;
            }
        });
        preferenceScreen.addPreference(useDefaultModelCheckPreference);
        preferenceScreen.addPreference(versionFieldSwitchPreference);
        versionFieldSwitchPreference.setEnabled(!useDefaultModelCheckPreference.isChecked());
        modelListPreference.setEnabled(!useDefaultModelCheckPreference.isChecked());

        ListPreference filesDeletionPreference = new ListPreference(context);
        filesDeletionPreference.setKey(KEY_FILES_DELETION_PREFERENCE);
        filesDeletionPreference.setTitle(R.string.files_deletion_preference_title);
        filesDeletionPreference.setSummaryProvider(ListPreference.SimpleSummaryProvider.getInstance());
        String[] filesDeletionValues = new String[]{
                VALUE_FILES_DELETION_DISABLED,
                VALUE_FILES_DELETION_CREATED_ONLY,
                VALUE_FILES_DELETION_ALL,
                VALUE_FILES_DELETION_ALWAYS_ASK
        };
        String[] filesDeletionEntries = new String[filesDeletionValues.length];
        String[] filesDeletionEntryValues = new String[filesDeletionValues.length];
        for (int i = 0; i < filesDeletionEntryValues.length; i++) {
            String value = filesDeletionValues[i];
            int resId = FILES_DELETION_VALUE_ENTRIES.get(value);
            filesDeletionEntries[i] = getResources().getString(resId);
            filesDeletionEntryValues[i] = value;
        }
        filesDeletionPreference.setEntries(filesDeletionEntries);
        filesDeletionPreference.setEntryValues(filesDeletionEntryValues);
        filesDeletionPreference.setDefaultValue(DEFAULT_FILES_DELETION);
        preferenceScreen.addPreference(filesDeletionPreference);

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

        Activity activity = getActivity();
        if (activity != null && ACTION_SHOW_FIELDS_MAPPING_DIALOG.equals(activity.getIntent().getAction())) {
            onDisplayPreferenceDialog(fieldsMappingPreference);
        }
    }

    public static Set<String> getDefaultFields(SharedPreferences preferences) {
        Set<String> storedFields = preferences.getStringSet(KEY_FIELDS_PREFERENCE, new HashSet<String>());
        Map<String, String> storedFieldsMapping = MappingPreference.toMapping(storedFields);
        Map<String, String> defaultFieldsMapping = new HashMap<>(MusInterval.Builder.DEFAULT_MODEL_FIELDS);
        MapUtil.putMissingKeys(storedFieldsMapping, defaultFieldsMapping);
        return MappingPreference.toEntries(defaultFieldsMapping);
    }

    private void handleModelChange(String currModel, String newModel) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor preferencesEditor = preferences.edit();

        Long oldModelId = helper.findModelIdByName(currModel);
        if (oldModelId != null) {
            String modelFieldsKey = getModelFieldsKey(oldModelId);
            Set<String> fields = preferences.getStringSet(KEY_FIELDS_PREFERENCE, new HashSet<String>());
            preferencesEditor.putStringSet(modelFieldsKey, fields);
        }

        Long newModelId = helper.findModelIdByName(newModel);
        if (newModelId != null) {
            String modelFieldsKey = getModelFieldsKey(newModelId);
            Set<String> modelFields = preferences.getStringSet(modelFieldsKey, new HashSet<String>());
            preferencesEditor.putStringSet(KEY_FIELDS_PREFERENCE, modelFields);
        }

        preferencesEditor.apply();
    }

    private static final String TEMPLATE_KEY_MODEL_FIELD_PREFERENCE = KEY_FIELDS_PREFERENCE + "_%s_model";

    public static String getModelFieldsKey(long modelId) {
        return String.format(TEMPLATE_KEY_MODEL_FIELD_PREFERENCE, modelId);
    }

    private static final String TAG_FIELDS_MAPPING_DIALOG = "FIELDS_MAPPING_DIALOG";

    @Override
    public void onDisplayPreferenceDialog(Preference preference) {
        String key = preference.getKey();
        switch (key) {
            case KEY_FIELDS_PREFERENCE:
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
                String modelName = preferences.getString(KEY_MODEL_PREFERENCE, MusInterval.Builder.DEFAULT_MODEL_NAME);
                Long modelId = helper.findModelIdByName(modelName);
                if (modelId == null) {
                    return;
                }
                boolean versionField = preferences.getBoolean(KEY_VERSION_FIELD_SWITCH, DEFAULT_VERSION_FIELD_SWITCH);
                String[] signature = MusInterval.Fields.getSignature(versionField);
                String[] modelFields = helper.getFieldList(modelId);
                boolean useDefaultModel = preferences.getBoolean(KEY_USE_DEFAULT_MODEL_CHECK, DEFAULT_USE_DEFAULT_MODEL_CHECK);
                Set<String> disabledFieldKeys = !useDefaultModel ? new HashSet<String>() :
                        new HashSet<>(Arrays.asList(MusInterval.Fields.getSignature(true)));
                MappingDialogFragment fragment = MappingDialogFragment.newInstance(key, signature, modelFields, disabledFieldKeys);
                fragment.setTargetFragment(this, 0);
                fragment.show(getParentFragmentManager(), TAG_FIELDS_MAPPING_DIALOG);
                break;
            default:
                super.onDisplayPreferenceDialog(preference);
        }
    }
}
