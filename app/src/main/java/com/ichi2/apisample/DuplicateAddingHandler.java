package com.ichi2.apisample;

public interface DuplicateAddingHandler {
    MusInterval add() throws MusInterval.AddSoundFileException, MusInterval.AddToAnkiException,
            AnkiDroidHelper.InvalidAnkiDatabaseException, MusInterval.ModelValidationException, MusInterval.TempoNotInRangeException;

    MusInterval replace() throws AnkiDroidHelper.InvalidAnkiDatabaseException,
            MusInterval.AddSoundFileException, MusInterval.ModelValidationException, MusInterval.TempoNotInRangeException;

    int mark() throws MusInterval.NoteNotExistsException, MusInterval.ModelValidationException,
            AnkiDroidHelper.InvalidAnkiDatabaseException, MusInterval.TempoNotInRangeException;

    int tag(String tag) throws MusInterval.NoteNotExistsException, MusInterval.ModelValidationException,
            AnkiDroidHelper.InvalidAnkiDatabaseException, MusInterval.TempoNotInRangeException;
}
