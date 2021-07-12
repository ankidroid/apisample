package com.ichi2.apisample.model;

import java.util.Comparator;
import java.util.Map;

public abstract class RelativesPriorityComparator implements Comparator<Map<String, String>> {
    protected final String fieldKey;
    protected Map<String, String> modelFields;
    protected String targetValue;

    public RelativesPriorityComparator(String fieldKey) {
        this.fieldKey = fieldKey;
    }

    public String getFieldKey() {
        return fieldKey;
    }

    public void setModelFields(Map<String, String> modelFields) {
        this.modelFields = modelFields;
    }

    public void setTargetValue(String targetValue) {
        this.targetValue = targetValue;
    }
}
