package com.ichi2.apisample;

public interface DuplicateAddingHandler {
    MusInterval add() throws MusInterval.AddSoundFileException, MusInterval.AddToAnkiException,
            AnkiDroidHelper.InvalidAnkiDatabaseException, MusInterval.ValidationException;

    MusInterval replace() throws AnkiDroidHelper.InvalidAnkiDatabaseException,
            MusInterval.AddSoundFileException, MusInterval.ValidationException;

    int mark() throws MusInterval.NoteNotExistsException, MusInterval.ValidationException,
            AnkiDroidHelper.InvalidAnkiDatabaseException;

    int tag(String tag) throws MusInterval.NoteNotExistsException, MusInterval.ValidationException,
            AnkiDroidHelper.InvalidAnkiDatabaseException;
}
