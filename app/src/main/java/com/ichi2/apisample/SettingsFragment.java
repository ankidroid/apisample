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
import androidx.preference.SwitchPreference;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class SettingsFragment extends PreferenceFragmentCompat {
    public static final String KEY_INVALID_TAG_PREFERENCE = "invalid_tag";
    public static final String KEY_AUTO_FIX_LINKS_PREFERENCE = "auto_fix_links";
    private static final String KEY_FIELDS_PREFERENCE_CATEGORY = "fields";

    public static final String DEFAULT_INVALID_TAG = "mi2a_invalid";
    public static final boolean DEFAULT_AUTO_FIX_LINKS = false;

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

        SwitchPreference autoFixLinksPreference = new SwitchPreference(context);
        autoFixLinksPreference.setKey(KEY_AUTO_FIX_LINKS_PREFERENCE);
        autoFixLinksPreference.setDefaultValue(DEFAULT_AUTO_FIX_LINKS);
        autoFixLinksPreference.setTitle(R.string.auto_fix_links_switch_preference_title);
        autoFixLinksPreference.setSummary(R.string.auto_fix_links_switch_preference_summary);
        preferenceScreen.addPreference(autoFixLinksPreference);

        EditTextPreference invalidTagPreference = new EditTextPreference(context);
        invalidTagPreference.setKey(KEY_INVALID_TAG_PREFERENCE);
        invalidTagPreference.setDefaultValue(DEFAULT_INVALID_TAG);
        invalidTagPreference.setTitle(R.string.invalid_tag_edit_text_preference_title);
        invalidTagPreference.setSummaryProvider(EditTextPreference.SimpleSummaryProvider.getInstance());
        invalidTagPreference.setDialogTitle(R.string.invalid_tag_edit_text_preference_dialog_title);
        invalidTagPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                try {
                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
                    final long modelId = helper.findModelIdByName(MusInterval.Builder.DEFAULT_MODEL_NAME);
                    LinkedList<Map<String, String>> notesData = helper.findNotes(modelId, new HashMap<String, String>());
                    final String currValue = sharedPreferences.getString(KEY_INVALID_TAG_PREFERENCE, DEFAULT_INVALID_TAG);
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
        });
        preferenceScreen.addPreference(invalidTagPreference);

        setPreferenceScreen(preferenceScreen);
    }
}
