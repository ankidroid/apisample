package com.ichi2.apisample;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;


public class MusInterval {

    public static class Fields {
        public static final String SOUND = "sound";
        public static final String START_NOTE = "start_note";
        public static final String ASC_DESC = "ascending_descending";
        public static final String MEL_HAR = "melodic_harmonic";
        public static final String INTERVAL = "interval";

        public static class AscDesc {
            public static final String ASC = "ascending";
            public static final String DESC = "descending";
        }

        public static class MelHar {
            public static final String MEL = "melodic";
            public static final String HAR = "harmonic";
        }
    }

    public static class NoSuchModelException extends Throwable {}
    public static class CreateDeckException extends Throwable {}
    public static class AddToAnkiException extends Throwable {}

    private static final String DEFAULT_DECK_NAME = "Music intervals";
    private static final String DEFAULT_MODEL_NAME = "Music.interval";

    private final AnkiDroidHelper mHelper;

    private final String mModelName;
    private final Long mModelId;
    private final String mDeckName;
    private Long mDeckId;

    private final String mSound;
    private final String mStartNote;
    private final String mAscDesc;
    private final String mMelHar;
    private final String mInterval;

    /**
     * Construct MusInterval instance.
     */
    public MusInterval(final AnkiDroidHelper helper, final String sound, final String startNote,
                       final String ascDesc, final String melHar, final String interval,
                       final String modelName, final String deckName) {
        mHelper = helper;

        mModelName = modelName;
        mModelId = mHelper.findModelIdByName(modelName);
        mDeckName = deckName;
        mDeckId = mHelper.findDeckIdByName(deckName);

        mSound = sound;
        mStartNote = startNote;
        mAscDesc = ascDesc;
        mMelHar = melHar;
        mInterval = interval;
    }

    public MusInterval(AnkiDroidHelper mAnkiDroid, String sound, String startNote, String ascDesc, String melHar, String interval) {
        this(mAnkiDroid, sound, startNote, ascDesc, melHar, interval, DEFAULT_MODEL_NAME, DEFAULT_DECK_NAME);
    }

    public String getModelName() {
        return mModelName;
    }

    public String getDeckName() {
        return mDeckName;
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
            if ((mStartNote.isEmpty() || mStartNote.equals(note.get(Fields.START_NOTE)))
                && (mAscDesc.isEmpty() || mAscDesc.equals(note.get(Fields.ASC_DESC)))
                && (mMelHar.isEmpty() || mMelHar.equals(note.get(Fields.MEL_HAR)))
                && (mInterval.isEmpty() || mInterval.equals(note.get(Fields.INTERVAL)))) {
                return true;
            }
        }

        return false;
    }

    /**
     * Insert the data into AnkiDroid via API.
     * Also created a model and a deck if not yet created.
     */
    public void addToAnki() throws NoSuchModelException, CreateDeckException, AddToAnkiException {
        if (mModelId == null) {
            throw new NoSuchModelException();
        }

        if (mDeckId == null) {
            mDeckId = mHelper.addNewDeck(mDeckName);
            if (mDeckId == null) {
                throw new CreateDeckException();
            }
            mHelper.storeDeckReference(mDeckName, mDeckId);
        }

        Map<String, String> data = new HashMap<>();
        data.put(Fields.SOUND, mSound);
        data.put(Fields.START_NOTE, mStartNote);
        data.put(Fields.ASC_DESC, mAscDesc);
        data.put(Fields.MEL_HAR, mMelHar);
        data.put(Fields.INTERVAL, mInterval);

        Long noteId = mHelper.addNote(mModelId, mDeckId, data, null);

        if (noteId == null) {
            throw new AddToAnkiException();
        }
    }
}
