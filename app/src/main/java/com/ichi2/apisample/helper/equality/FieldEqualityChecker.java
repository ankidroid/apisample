package com.ichi2.apisample.helper.equality;

import java.util.Map;

public class FieldEqualityChecker implements EqualityChecker {
    private String modelField;
    private ValueEqualityChecker valueEqualityChecker;

    public FieldEqualityChecker(String modelField, ValueEqualityChecker valueEqualityChecker) {
        this.modelField = modelField;
        this.valueEqualityChecker = valueEqualityChecker;
    }

    @Override
    public boolean areEqual(Map<String, String> data1, Map<String, String> data2) {
        String value1 = data1.getOrDefault(modelField, "");
        String value2 = data2.getOrDefault(modelField, "");
        return valueEqualityChecker.areEqual(value1, value2);
    }

    public void setField(String modelField) {
        this.modelField = modelField;
    }
}
