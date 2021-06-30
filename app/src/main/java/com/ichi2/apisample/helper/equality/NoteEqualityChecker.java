package com.ichi2.apisample.helper.equality;

public abstract class NoteEqualityChecker implements EqualityChecker {
    protected String[] modelFields;

    public NoteEqualityChecker(String[] modelFields) {
        this.modelFields = modelFields;
    }

    public String[] getModelFields() {
        return modelFields;
    }

    public void setModelFields(String[] modelFields) {
        this.modelFields = modelFields;
    }
}
