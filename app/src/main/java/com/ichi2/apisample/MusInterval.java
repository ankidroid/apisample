package com.ichi2.apisample;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;


public class MusInterval {

    /**
     * Pre-defined field names and values, used in the target AnkiDroid model.
     */
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

        public static class Tempo {
            public static final int MIN_VALUE = 0;
            public static final int MAX_VALUE = 200;
        }

        public static final String[] SIGNATURE = new String[] {
                MusInterval.Fields.SOUND,
                MusInterval.Fields.START_NOTE,
                MusInterval.Fields.DIRECTION,
                MusInterval.Fields.TIMING,
                MusInterval.Fields.INTERVAL,
                MusInterval.Fields.TEMPO,
                MusInterval.Fields.INSTRUMENT
        };
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

        public MusInterval build() throws ValidationException {
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
    public static class CreateDeckException extends Exception {}
    public static class AddToAnkiException extends Exception {}
    public static class NoteNotExistsException extends Exception {}
    public static class MandatoryFieldEmptyException extends Exception {}
    public static class SoundAlreadyAddedException extends Exception {}
    public static class AddSoundFileException extends Exception {}

    public static class ValidationException extends Exception {}
    public static class StartNoteSyntaxException extends ValidationException {}
    public static class TempoValueException extends ValidationException {}
    abstract static class ModelValidationException extends ValidationException {
        private String modelName;
        protected ModelValidationException(String modelName) {
            super();
            this.modelName = modelName;
        }
        public String getModelName() {
            return modelName;
        }
    }
    public static class NoSuchModelException extends ModelValidationException {
        public NoSuchModelException(String modelName) {
            super(modelName);
        }
    }
    public static class InvalidModelException extends ModelValidationException {
        public InvalidModelException(String modelName) {
            super(modelName);
        }
    }

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

    /**
     * Construct an object using builder class.
     */
    public MusInterval(Builder builder) throws ValidationException {
        helper = builder.mHelper;

        modelName = builder.mModelName;
        modelId = helper.findModelIdByName(builder.mModelName);
        deckName = builder.mDeckName;
        deckId = helper.findDeckIdByName(builder.mDeckName);

        sound = builder.mSound.trim();
        startNote = builder.mStartNote.trim();
        direction = builder.mDirection.trim().toLowerCase();
        timing = builder.mTiming.trim().toLowerCase();
        interval = builder.mInterval.trim();
        tempo = builder.mTempo.trim();
        instrument = builder.mInstrument.trim();

        validateFields();
    }

    protected void validateFields() throws ValidationException {
        if (!startNote.isEmpty() && !startNote.matches("[A-Ga-g]#?[0-8]")) {
            throw new StartNoteSyntaxException();
        }

        if (!tempo.isEmpty()) {
            int tempoInt = Integer.parseInt(tempo);
            if (tempoInt < Fields.Tempo.MIN_VALUE || tempoInt > Fields.Tempo.MAX_VALUE) {
                throw new TempoValueException();
            }
        }

        if (modelId == null) {
            throw new NoSuchModelException(modelName);
        }
        if (!helper.isModelValid(modelId, Fields.SIGNATURE)) {
            throw new InvalidModelException(modelName);
        }
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
     * Count, how many similar or equal notes exists in AnkiDroid.
     */
    public int getExistingMarkedNotesCount() throws AnkiDroidHelper.InvalidAnkiDatabaseException {
        int result = 0;

        for (Map<String, String> note : getExistingNotes()) {
            if (note.get("tags") != null && note.get("tags").contains(" marked ")) {
                ++result;
            }
        }

        return result;
    }

    /**
     * Get list of existing (similar or equal) notes. Each note consists of main model fields, id field and tags.
     */
    private LinkedList<Map<String, String>> getExistingNotes() throws AnkiDroidHelper.InvalidAnkiDatabaseException {
        if (modelId != null) {
            Map<String, String> data = getCollectedData();
            data.remove(Fields.SOUND); // sound filed should not be compared in existing data

            return helper.findNotes(modelId, data);
        } else {
            return new LinkedList<>();
        }
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
     * @return New MusInterval instance (with some of the fields updated)
     */
    public MusInterval addToAnki()
            throws  CreateDeckException, AddToAnkiException, MandatoryFieldEmptyException,
            SoundAlreadyAddedException, AddSoundFileException, ValidationException {

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

        if (newSound == null || newSound.isEmpty()) {
            throw new AddSoundFileException();
        }

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
     * Check if all the fields are not empty (needed for adding to AnkiDroid).
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

    /**
     * Get the music interval data in one map.
     */
    public Map<String, String> getCollectedData() {
        return getCollectedData(this.sound);
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
