package com.ichi2.apisample;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;


public class MusInterval {

    public static class Fields {
        public static final String SOUND = "sound";
        public static final String START_NOTE = "start_note";
        public static final String DIRECTION = "ascending_descending";
        public static final String TIMING = "melodic_harmonic";
        public static final String INTERVAL = "interval";
        public static final String TEMPO = "tempo";
        public static final String INSTRUMENT = "instrument";

        public static class Direction {
            public static final String ASC = "ascending";
            public static final String DESC = "descending";
        }

        public static class Timing {
            public static final String MELODIC = "melodic";
            public static final String HARMONIC = "harmonic";
        }
    }

    public static class Builder {
        private static final String DEFAULT_DECK_NAME = "Music intervals";
        private static final String DEFAULT_MODEL_NAME = "Music.interval";

        private final AnkiDroidHelper mHelper;
        private String mModelName = DEFAULT_MODEL_NAME;
        private String mDeckName = DEFAULT_DECK_NAME;
        private String mSound = "";
        private String mStartNote = "";
        private String mDirection = "";
        private String mTiming = "";
        private String mInterval = "";
        private String mTempo = "";
        private String mInstrument = "";

        public Builder(final AnkiDroidHelper helper) {
            mHelper = helper;
        }

        public MusInterval build() {
            return new MusInterval(this);
        }

        public Builder model(String mn) {
            mModelName = mn;
            return this;
        }

        public Builder deck(String dn) {
            mDeckName = dn;
            return this;
        }

        public Builder sound(String sd) {
            mSound = sd;
            return this;
        }

        public Builder start_note(String sn) {
            mStartNote = sn;
            return this;
        }

        public Builder direction(String dr) {
            mDirection = dr;
            return this;
        }

        public Builder timing(String tm) {
            mTiming = tm;
            return this;
        }

        public Builder interval(String in) {
            mInterval = in;
            return this;
        }

        public Builder tempo(String tp) {
            mTempo = tp;
            return this;
        }

        public Builder instrument(String is) {
            mInstrument = is;
            return this;
        }
    }

    public static class NoSuchModelException extends Throwable {}
    public static class CreateDeckException extends Throwable {}
    public static class AddToAnkiException extends Throwable {}
    public static class NoteNotExistsException extends Throwable {}
    public static class AddTagException extends Throwable {}

    private final AnkiDroidHelper helper;

    public final String modelName;
    private final Long modelId;
    public final String deckName;
    private Long deckId;

    // Data of model's fields
    public final String sound;
    public final String startNote;
    public final String direction;
    public final String timing;
    public final String interval;
    public final String tempo;
    public final String instrument;

    public MusInterval(Builder builder) {
        helper = builder.mHelper;

        modelName = builder.mModelName;
        modelId = helper.findModelIdByName(builder.mModelName);
        deckName = builder.mDeckName;
        deckId = helper.findDeckIdByName(builder.mDeckName);

        sound = builder.mSound;
        startNote = builder.mStartNote;
        direction = builder.mDirection;
        timing = builder.mTiming;
        interval = builder.mInterval;
        tempo = builder.mTempo;
        instrument = builder.mInstrument;
    }

    public String getModelName() {
        return modelName;
    }

    public String getDeckName() {
        return deckName;
    }

    /**
     * Check if such a data already exists in the AnkiDroid.
     *
     * @return True or false depending on a result
     */
    public boolean existsInAnki() throws AnkiDroidHelper.InvalidAnkiDatabaseException {
        return getExistingNotesCount() > 0;
    }

    /**
     * Count, how many similar or equal notes exists in AnkiDroid.
     */
    public int getExistingNotesCount() throws AnkiDroidHelper.InvalidAnkiDatabaseException {
        return getExistingNotes().size();
    }

    /**
     * Get list of existing notes. Each note consists of main fields, id field and tags.
     */
    private LinkedList<Map<String, String>> getExistingNotes() throws AnkiDroidHelper.InvalidAnkiDatabaseException {
        LinkedList<Map<String, String>> existingNotes = new LinkedList<>();

        if (modelId != null) {
            final LinkedList<Map<String, String>> notes = helper.getNotes(modelId);

            for (Map<String, String> note : notes) {
                if ((startNote.isEmpty() || startNote.equals(note.get(Fields.START_NOTE)))
                        && (direction.isEmpty() || direction.equals(note.get(Fields.DIRECTION)))
                        && (timing.isEmpty() || timing.equals(note.get(Fields.TIMING)))
                        && (interval.isEmpty() || interval.equals(note.get(Fields.INTERVAL)))
                        && (tempo.isEmpty() || tempo.equals(note.get(Fields.TEMPO)))
                        && (instrument.isEmpty() || instrument.equals(note.get(Fields.INSTRUMENT)))) {
                    existingNotes.add(note);
                }
            }
        }

        return existingNotes;
    }

    public void markExistingNote() throws NoteNotExistsException, AddTagException, AnkiDroidHelper.InvalidAnkiDatabaseException {
        final Map<String, String> note = getExistingNote();

        if (notes.size() == 0) {
            throw new NoteNotExistsException();
        }

        for (Map<String, String> note : notes) {
            String tags = note.get("tags");

            if (tags == null) {
                tags = " ";
            }

            if (tags.contains(" marked ")) {
                continue;
            }

            tags = tags + "marked ";

            updated += helper.addTagToNote(Long.parseLong(note.get("id")), tags);
        }

        return updated;
    }

    /**
     * Insert the data into AnkiDroid via API.
     * Also created a model and a deck if not yet created.
     */
    public void addToAnki() throws NoSuchModelException, CreateDeckException, AddToAnkiException {
        if (modelId == null) {
            throw new NoSuchModelException();
        }

        if (deckId == null) {
            deckId = helper.addNewDeck(deckName);
            if (deckId == null) {
                throw new CreateDeckException();
            }
            helper.storeDeckReference(deckName, deckId);
        }

        Long noteId = helper.addNote(modelId, deckId, getCollectedData(), null);

        if (noteId == null) {
            throw new AddToAnkiException();
        }
    }

    public Map<String, String> getCollectedData() {
        Map<String, String> data = new HashMap<>();
        data.put(Fields.SOUND, sound);
        data.put(Fields.START_NOTE, startNote);
        data.put(Fields.DIRECTION, direction);
        data.put(Fields.TIMING, timing);
        data.put(Fields.INTERVAL, interval);
        data.put(Fields.TEMPO, tempo);
        data.put(Fields.INSTRUMENT, instrument);
        return data;
    }
}
