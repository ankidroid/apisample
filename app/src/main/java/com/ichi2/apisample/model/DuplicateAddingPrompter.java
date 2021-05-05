package com.ichi2.apisample.model;

public interface DuplicateAddingPrompter {
    void promptAddDuplicate(MusInterval[] existingMis, DuplicateAddingHandler handler);
}
