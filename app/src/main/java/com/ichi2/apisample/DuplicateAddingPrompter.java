package com.ichi2.apisample;

public interface DuplicateAddingPrompter {
    void promptAddDuplicate(MusInterval[] existingMis, DuplicateAddingHandler handler);
}
