package com.ichi2.apisample.validation;

public class IntegerRangeValidator implements FieldValidator {
    private final int minValue;
    private final int maxValue;

    private String errorTag;

    public IntegerRangeValidator(int minValue, int maxValue) {
        this.minValue = minValue;
        this.maxValue = maxValue;
    }

    @Override
    public boolean isValid(String value) {
        int intVal = Integer.parseInt(value);
        if (intVal < minValue) {
            errorTag = "below" + minValue;
            return false;
        }
        if (intVal > maxValue) {
            errorTag = "above" + maxValue;
            return false;
        }
        return true;
    }

    @Override
    public String getErrorTag() {
        return errorTag;
    }
}
