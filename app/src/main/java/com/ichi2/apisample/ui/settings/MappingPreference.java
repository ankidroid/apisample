package com.ichi2.apisample.ui.settings;

import android.content.Context;

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
        Set<String> entries = new HashSet<>();
        for (Map.Entry<String, String> entry : mapping.entrySet()) {
            entries.add(entry.getKey() + SEPARATOR + entry.getValue());
        }
        persistStringSet(entries);
        notifyChanged();
    }
}
