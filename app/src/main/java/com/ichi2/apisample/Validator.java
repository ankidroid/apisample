package com.ichi2.apisample;

public interface Validator {
    boolean isValid(String value);

    String getErrorTag();
}

class EmptyValidator implements Validator {
    @Override
    public boolean isValid(String value) {
        return !value.isEmpty();
    }

    @Override
    public String getErrorTag() {
        return "empty";
    }
}

class PatternValidator implements Validator {
    private final String pattern;

    public PatternValidator(String pattern) {
        this.pattern = pattern;
    }

    @Override
    public boolean isValid(String value) {
        return value.matches(pattern);
    }

    @Override
    public String getErrorTag() {
        return "invalid";
    }
}

class IntegerRangeValidator implements Validator {
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