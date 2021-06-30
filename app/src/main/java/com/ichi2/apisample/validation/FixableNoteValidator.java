package com.ichi2.apisample.validation;

import com.ichi2.apisample.helper.AnkiDroidHelper;

import java.util.Map;

public interface FixableNoteValidator extends NoteValidator {
    boolean fix(long modelId, long noteId, Map<String, String> data, Map<String, String> modelFields, AnkiDroidHelper helper);
}
