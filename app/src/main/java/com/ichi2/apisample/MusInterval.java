package com.ichi2.apisample;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;


public class MusInterval {

    public static class Fields {
        public static final String SOUND = "sound";
        public static final String START_NOTE = "start_note";
        public static final String DIRECTION = "ascending_descending";
        public static final String SCALE = "melodic_harmonic";
        public static final String INTERVAL = "interval";
        public static final String TEMPO = "tempo";
        public static final String INSTRUMENT = "instrument";

        public static class Direction {
            public static final String ASC = "ascending";
            public static final String DESC = "descending";
        }

        public static class Scale {
            public static final String MELODIC = "melodic";
            public static final String HARMONIC = "harmonic";
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

    // Data of model's fields
    private final String mSound;
    private final String mStartNote;
    private final String mDirection;
    private final String mScale;
    private final String mInterval;
    private final String mTempo;
    private final String mInstrument;

    /**
     * Construct MusInterval instance with specified model and deck names.
     */
    public MusInterval(final AnkiDroidHelper helper, final String sound, final String startNote,
                       final String direction, final String scale, final String interval,
                       final String tempo, final String instrument, final String modelName,
                       final String deckName) {
        mHelper = helper;

        mModelName = modelName;
        mModelId = mHelper.findModelIdByName(modelName);
        mDeckName = deckName;
        mDeckId = mHelper.findDeckIdByName(deckName);

        mSound = sound;
        mStartNote = startNote;
        mDirection = direction;
        mScale = scale;
        mInterval = interval;
        mTempo = tempo;
        mInstrument = instrument;
    }

    /**
     * Construct MusInterval instance with default model and deck names.
     */
    public MusInterval(final AnkiDroidHelper helper, final String sound, final String startNote,
                       final String direction, final String scale, final String interval,
                       final String tempo, final String instrument) {
        this(helper, sound, startNote, direction, scale, interval, tempo, instrument,
                DEFAULT_MODEL_NAME, DEFAULT_DECK_NAME);
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
                && (mDirection.isEmpty() || mDirection.equals(note.get(Fields.DIRECTION)))
                && (mScale.isEmpty() || mScale.equals(note.get(Fields.SCALE)))
                && (mInterval.isEmpty() || mInterval.equals(note.get(Fields.INTERVAL)))
                && (mTempo.isEmpty() || mTempo.equals(note.get(Fields.TEMPO)))
                && (mInstrument.isEmpty() || mInstrument.equals(note.get(Fields.INSTRUMENT)))) {
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
        data.put(Fields.DIRECTION, mDirection);
        data.put(Fields.SCALE, mScale);
        data.put(Fields.INTERVAL, mInterval);
        data.put(Fields.TEMPO, mTempo);
        data.put(Fields.INSTRUMENT, mInstrument);

        Long noteId = mHelper.addNote(mModelId, mDeckId, data, null);

        if (noteId == null) {
            throw new AddToAnkiException();
        }
    }
}
