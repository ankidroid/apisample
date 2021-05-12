package com.ichi2.apisample.validation;

public class EmptyValidator implements Validator {
    @Override
    public boolean isValid(String value) {
        return !value.isEmpty();
    }

    @Override
    public String getErrorTag() {
        return "empty";
    }
}