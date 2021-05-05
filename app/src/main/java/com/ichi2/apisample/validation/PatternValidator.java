package com.ichi2.apisample.validation;

public class PatternValidator implements Validator {
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
