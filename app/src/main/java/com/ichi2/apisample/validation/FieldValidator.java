package com.ichi2.apisample.validation;

public interface FieldValidator extends Validator {
    boolean isValid(String value);
}
