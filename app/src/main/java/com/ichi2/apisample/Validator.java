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