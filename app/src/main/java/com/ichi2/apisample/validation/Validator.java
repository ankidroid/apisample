package com.ichi2.apisample.validation;

public interface Validator {
    boolean isValid(String value);

    String getErrorTag();
}
