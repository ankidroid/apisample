package com.ichi2.apisample;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;


public class MusInterval {
    // Name of deck which will be created in AnkiDroid
    public static final String DEFAULT_DECK_NAME = "Music intervals";
    // Name of model which will be created in AnkiDroid
    public static final String DEFAULT_MODEL_NAME = "Music.interval";

    private final AnkiDroidHelper mHelper;

    private Long mModelId;
    private final String mDeckName;
    private Long mDeckId;

    private final String mSound;
    private final String mStartNote;

    /**
     * Construct MusInterval instance.
     *
     *
     */
    public MusInterval(AnkiDroidHelper helper, String sound, String startNote, String modelName, String deckName) {
        mHelper = helper;

        mModelId = mHelper.findModelIdByName(modelName);
        mDeckName = deckName;
        mDeckId = mHelper.findDeckIdByName(deckName);

        mSound = sound;
        mStartNote = startNote;
    }

    public MusInterval(AnkiDroidHelper mAnkiDroid, String sound, String startNote) {
        this(mAnkiDroid, sound, startNote, DEFAULT_MODEL_NAME, DEFAULT_DECK_NAME);
    }

    /**
     * Check if such a data already exists in the AnkiDroid.
     *
     * @return
     */
    public boolean existsInAnki() {
        if (mModelId == null) {
            return false;
        }

        LinkedList<Map<String, String>> notes = mHelper.getNotes(mModelId);

        for (Map<String, String> note : notes) {
            if (mStartNote.isEmpty() || mStartNote.equals(note.get("start_note")))
            {
                return true;
            }
        }

        return false;
    }

    /**
     * Insert the data into AnkiDroid via API.
     * Also created a model and a deck if not yet created.
     *
     * @return True in case of successful action
     */
    public boolean addToAnki() throws NoSuchModelException {
        if (mModelId == null) {
            throw new NoSuchModelException();
        }

        if (mDeckId == null) {
            mDeckId = mHelper.addNewDeck(mDeckName);
            if (mDeckId == null) {
                return false;  // @todo Probably throw exception ?
            }
            mHelper.storeDeckReference(mDeckName, mDeckId);
        }

        Map<String, String> data = new HashMap<>();
        data.put("sound", mSound);
        data.put("start_note", mStartNote);

        // @todo Throw exception on adding failure
        Long noteId = mHelper.addNote(mModelId, mDeckId, data, null);
        return noteId != null;
    }

    public class NoSuchModelException extends Throwable {
    }
}
