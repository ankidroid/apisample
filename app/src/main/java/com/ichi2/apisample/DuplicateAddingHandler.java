package com.ichi2.apisample;

public interface DuplicateAddingHandler {
    MusInterval add() throws MusInterval.AddSoundFileException, MusInterval.AddToAnkiException,
            AnkiDroidHelper.InvalidAnkiDatabaseException, MusInterval.ValidationException;

    MusInterval replace() throws AnkiDroidHelper.InvalidAnkiDatabaseException,
            MusInterval.AddSoundFileException, MusInterval.ValidationException;

    int mark() throws MusInterval.NoteNotExistsException, AnkiDroidHelper.InvalidAnkiDatabaseException;

    int tag(String tag) throws MusInterval.NoteNotExistsException, AnkiDroidHelper.InvalidAnkiDatabaseException;
}
