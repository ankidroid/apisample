package com.ichi2.apisample.validation;

import java.util.Map;

public interface NoteValidator extends Validator {
    boolean isValid(Map<String, String> data, Map<String, String> modelFields);
}
