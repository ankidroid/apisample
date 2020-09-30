package com.ichi2.apisample;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;


public class MusInterval {

    public static class Fields {
        public static final String SOUND = "sound";
        public static final String START_NOTE = "start_note";
        public static final String ASC_DESC = "ascending_descending";

        public static class AscDesc {
            public static final String ASC = "ascending";
            public static final String DESC = "descending";
        }
    }

    public static class NoSuchModelException extends Throwable { }

    private static final String DEFAULT_DECK_NAME = "Music intervals";
    private static final String DEFAULT_MODEL_NAME = "Music.interval";

    private final AnkiDroidHelper mHelper;

    private String mModelName;
    private Long mModelId;
    private final String mDeckName;
    private Long mDeckId;

    private final String mSound;
    private final String mStartNote;
    private final String mAscDesc;

    /**
     * Construct MusInterval instance.
     */
    public MusInterval(final AnkiDroidHelper helper, final String sound, final String startNote,
                       final String ascDesc, final String modelName, final String deckName) {
        mHelper = helper;

        mModelName = modelName;
        mModelId = mHelper.findModelIdByName(modelName);
        mDeckName = deckName;
        mDeckId = mHelper.findDeckIdByName(deckName);

        mSound = sound;
        mStartNote = startNote;
        mAscDesc = ascDesc;
    }

    public MusInterval(AnkiDroidHelper mAnkiDroid, String sound, String startNote, String ascDesc) {
        this(mAnkiDroid, sound, startNote, ascDesc, DEFAULT_MODEL_NAME, DEFAULT_DECK_NAME);
    }

    /**
     * Check if such a data already exists in the AnkiDroid.
     *
     * @return True or false depending on a result
     */
    public boolean existsInAnki() {
        if (mModelId == null) {
            return false;
        }

        LinkedList<Map<String, String>> notes = mHelper.getNotes(mModelId);

        for (Map<String, String> note : notes) {
            if ((mStartNote.isEmpty() || mStartNote.equals(note.get(MusInterval.Fields.START_NOTE)))
                && (mAscDesc.isEmpty() || mAscDesc.equals(note.get(MusInterval.Fields.ASC_DESC)))) {
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
        data.put(Fields.SOUND, mSound);
        data.put(Fields.START_NOTE, mStartNote);
        data.put(Fields.ASC_DESC, mAscDesc);

        // @todo Throw exception on adding failure
        Long noteId = mHelper.addNote(mModelId, mDeckId, data, null);
        return noteId != null;
    }

    public String getModelName() {
        return mModelName;
    }
}
