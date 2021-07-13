package com.ichi2.apisample.validation;

import com.ichi2.apisample.helper.AnkiDroidHelper;

import java.util.Map;

public class ValidationUtil {
    public static boolean isValid(Validator validator, long modelId, long noteId, Map<String, String> noteData,
                                  String fieldKey, Map<String, String> modelFields, AnkiDroidHelper helper,
                                  boolean resolveFixable) {
        String modelField = modelFields.getOrDefault(fieldKey, fieldKey);
        boolean isValid;
        if (validator instanceof FieldValidator) {
            String value = noteData.getOrDefault(modelField, "");
            isValid = ((FieldValidator) validator).isValid(value);
        } else if (validator instanceof NoteValidator) {
            isValid = ((NoteValidator) validator).isValid(noteData, modelFields);
            if (!isValid && validator instanceof FixableNoteValidator && resolveFixable) {
                isValid = ((FixableNoteValidator) validator).fix(
                        modelId,
                        noteId,
                        noteData,
                        modelFields,
                        helper
                );
            }
        } else {
            throw new IllegalArgumentException();
        }
        return isValid;
    }
}
