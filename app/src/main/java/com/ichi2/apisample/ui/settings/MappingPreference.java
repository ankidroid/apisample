package com.ichi2.apisample.ui.settings;

import android.content.Context;

import androidx.annotation.Nullable;
import androidx.preference.DialogPreference;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MappingPreference extends DialogPreference {
    private static final String SEPARATOR = ":";

    public MappingPreference(Context context) {
        super(context);
    }

    public Map<String, String> getPersistedMapping() {
        Set<String> entries = getPersistedStringSet(new HashSet<String>());
        return toMapping(entries);
    }

    public static Map<String, String> toMapping(Set<String> entries) {
        if (entries == null) {
            return null;
        }
        Map<String, String> mapping = new HashMap<>();
        for (String entry : entries) {
            String[] split = entry.split(SEPARATOR);
            String key = split[0];
            String value = split.length > 1 ? split[1] : "";
            mapping.put(key, value);
        }
        return mapping;
    }

    public void persistMapping(Map<String, String> mapping) {
        Set<String> entries = toEntries(mapping);
        persistStringSet(entries);
        notifyChanged();
    }

    public static Set<String> toEntries(Map<String, String> mapping) {
        Set<String> entries = new HashSet<>();
        for (Map.Entry<String, String> entry : mapping.entrySet()) {
            entries.add(entry.getKey() + SEPARATOR + entry.getValue());
        }
        return entries;
    }

    @Override
    protected void onSetInitialValue(@Nullable Object defaultValue) {
        if (defaultValue == null) {
            defaultValue = new HashSet<>();
        }
        persistMapping(toMapping(getPersistedStringSet((Set<String>) defaultValue)));
    }
}
