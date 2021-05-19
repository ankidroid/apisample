package com.ichi2.apisample.model;

public interface AddingPrompter {
    void promptAddDuplicate(MusInterval[] existingMis, AddingHandler handler);

    void addingFinished(MusInterval newMi);

    void processException(Throwable t);
}
