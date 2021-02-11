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

public class SettingsFragment extends PreferenceFragmentCompat {
    private static final String FIELDS_PREFERENCE_CATEGORY_KEY = "fields";

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        Context context = getPreferenceManager().getContext();
        PreferenceScreen preferenceScreen = getPreferenceManager().createPreferenceScreen(context);

        AnkiDroidHelper helper = new AnkiDroidHelper(context);

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

        setPreferenceScreen(preferenceScreen);
    }
}
