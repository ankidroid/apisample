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

    abstract static class Exception extends Throwable {}
    public static class NoSuchModelException extends Exception {}
    public static class CreateDeckException extends Exception {}
    public static class AddToAnkiException extends Exception {}
    public static class NoteNotExistsException extends Exception {}
    public static class MandatoryFieldEmptyException extends Exception {}
    public static class SoundAlreadyAddedException extends Exception {}

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

        sound = builder.mSound.trim();
        startNote = builder.mStartNote.trim();
        direction = builder.mDirection.trim();
        timing = builder.mTiming.trim();
        interval = builder.mInterval.trim();
        tempo = builder.mTempo.trim();
        instrument = builder.mInstrument.trim();
    }

    /**
     * Check if such a data already exists in AnkiDroid.
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
                if ((startNote.isEmpty() || startNote.equalsIgnoreCase(note.get(Fields.START_NOTE)))
                        && (direction.isEmpty() || direction.equalsIgnoreCase(note.get(Fields.DIRECTION)))
                        && (timing.isEmpty() || timing.equalsIgnoreCase(note.get(Fields.TIMING)))
                        && (interval.isEmpty() || interval.equalsIgnoreCase(note.get(Fields.INTERVAL)))
                        && (tempo.isEmpty() || tempo.equalsIgnoreCase(note.get(Fields.TEMPO)))
                        && (instrument.isEmpty() || instrument.equalsIgnoreCase(note.get(Fields.INSTRUMENT)))) {
                    existingNotes.add(note);
                }
            }
        }

        return existingNotes;
    }

    /**
     * Add tag "marked" to the existing notes (similar or equal to this one).
     * Does not add the tag if it already exists in a note.
     */
    public int markExistingNotes() throws NoteNotExistsException, AnkiDroidHelper.InvalidAnkiDatabaseException {
        final LinkedList<Map<String, String>> notes = getExistingNotes();
        int updated = 0;

        if (notes.size() == 0) {
            throw new NoteNotExistsException();
        }

        for (Map<String, String> note : notes) {
            String tags = note.get("tags");

            if (tags == null) {
                tags = " ";
            }

            if (!tags.contains(" marked ")) {
                tags = tags + "marked ";

                if (note.get("id") != null) {
                    updated += helper.addTagToNote(Long.parseLong(note.get("id")), tags);
                }
            }
        }

        return updated;
    }

    /**
     * Insert the data into AnkiDroid via API.
     * Also creates a deck if not yet created, but fails if model not found.
     * @return New MusInterval instance with updated "sound" field
     */
    public MusInterval addToAnki() throws NoSuchModelException, CreateDeckException, AddToAnkiException, MandatoryFieldEmptyException, SoundAlreadyAddedException {
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

        if (!areMandatoryFieldsFilled()) {
            throw new MandatoryFieldEmptyException();
        }

        if (sound.startsWith("[sound:")) {
            throw new SoundAlreadyAddedException();
        }

        String newSound = helper.addFileToAnkiMedia(sound);
        Map<String, String> data = getCollectedData(newSound);

        Long noteId = helper.addNote(modelId, deckId, data, null);

        if (noteId == null) {
            throw new AddToAnkiException();
        }

        return new Builder(helper)
                .sound(data.get(Fields.SOUND))
                .start_note(data.get(Fields.START_NOTE))
                .direction(data.get(Fields.DIRECTION))
                .timing(data.get(Fields.TIMING))
                .interval(data.get(Fields.INTERVAL))
                .tempo(data.get(Fields.TEMPO))
                .instrument(data.get(Fields.INSTRUMENT))
                .build();
    }

    /**
     * Check if all the fields are not empty (needed for adding to AnkiDroid)
     */
    protected boolean areMandatoryFieldsFilled() {
        return !sound.isEmpty()
                && !startNote.isEmpty()
                && !direction.isEmpty()
                && !timing.isEmpty()
                && !interval.isEmpty()
                && !tempo.isEmpty()
                && !instrument.isEmpty();
    }

    public Map<String, String> getCollectedData(String sound) {
        Map<String, String> data = new HashMap<>();
        data.put(Fields.SOUND, "[sound:" + sound + "]");
        data.put(Fields.START_NOTE, startNote.toUpperCase());
        data.put(Fields.DIRECTION, direction);
        data.put(Fields.TIMING, timing);
        data.put(Fields.INTERVAL, interval);
        data.put(Fields.TEMPO, tempo);
        data.put(Fields.INSTRUMENT, instrument);
        return data;
    }
}
