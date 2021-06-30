package com.ichi2.apisample.validation;

public class PositiveDecimalValidator implements FieldValidator {
    @Override
    public boolean isValid(String value) {
        return value.matches("^\\d*$|^(?=^\\d*\\.\\d*$)(?=^(?!\\.$).*$).*$");
    }

    @Override
    public String getErrorTag() {
        return "invalid";
    }
}
