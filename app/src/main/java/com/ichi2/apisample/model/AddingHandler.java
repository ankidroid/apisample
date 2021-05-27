package com.ichi2.apisample.model;

import com.ichi2.apisample.helper.AnkiDroidHelper;

public interface AddingHandler {
    MusInterval add() throws MusInterval.AddSoundFileException, MusInterval.AddToAnkiException,
            AnkiDroidHelper.InvalidAnkiDatabaseException, MusInterval.ModelException, MusInterval.TempoNotInRangeException;

    MusInterval replace() throws AnkiDroidHelper.InvalidAnkiDatabaseException,
            MusInterval.AddSoundFileException, MusInterval.ModelException, MusInterval.TempoNotInRangeException;

    int mark() throws MusInterval.NoteNotExistsException, MusInterval.ModelException,
            AnkiDroidHelper.InvalidAnkiDatabaseException, MusInterval.TempoNotInRangeException;

    int tag(String tag) throws MusInterval.NoteNotExistsException, MusInterval.ModelException,
            AnkiDroidHelper.InvalidAnkiDatabaseException, MusInterval.TempoNotInRangeException;

    void proceed() throws Throwable;
}
