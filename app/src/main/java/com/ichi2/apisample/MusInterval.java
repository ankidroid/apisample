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
        public static final String SOUND_SMALLER = "sound_smaller";
        public static final String SOUND_LARGER = "sound_larger";
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

        public static class Interval {
            // @todo: Make full list of intervals
            public static final String[] VALUES = new String[]{"", "min2", "Maj2", "min3", "Maj3"};
        }

        public static class Tempo {
            public static final int MIN_VALUE = 0;
            public static final int MAX_VALUE = 200;
        }

        public static final String[] SIGNATURE = new String[] {
                SOUND,
                SOUND_SMALLER,
                SOUND_LARGER,
                START_NOTE,
                DIRECTION,
                TIMING,
                INTERVAL,
                TEMPO,
                INSTRUMENT
        };
    }

    public static class Builder {
        public static final String DEFAULT_DECK_NAME = "Music intervals";
        public static final String DEFAULT_MODEL_NAME = "Music.interval";
        public static final String[] CARD_NAMES = {"Question > Answer"};
        // CSS to share between all the cards
        public static final String CSS = ".card {\n" +
                "  font-family: arial;\n" +
                "  font-size: 20px;\n" +
                "  text-align: center;\n" +
                "  color: black;\n" +
                "  background-color: white;\n" +
                "}\n" +
                "\n" +
                ".the_answer {\n" +
                "  font-size:40px;\n" +
                "  font-face:bold;\n" +
                "  color:green;\n" +
                "}";
        // Template for the question of each card
        static final String QFMT1 = "{{sound}}\n" +
                "Which interval is it?";
        public static final String[] QFMT = {QFMT1};
        static final String AFMT1 = "{{FrontSide}}\n" +
                "\n" +
                "<hr id=answer>\n" +
                "\n" +
                "<img src=\"_wils_{{start_note}}_{{ascending_descending}}_{{melodic_harmonic}}_{{interval}}.jpg\" onerror=\"this.style.display='none'\"/>\n" +
                "<img src=\"_wila_{{interval}}_.jpg\" onerror=\"this.style.display='none'\"/>\n" +
                "<div id=\"interval_longer_name\" class=\"the_answer\"></div>\n" +
                "{{start_note}}, {{ascending_descending}}, {{melodic_harmonic}}, <span id=\"interval_short_name\">{{interval}}</span>; {{tempo}}BPM, {{instrument}}\n" +
                "\n" +
                "<script>\n" +
                "function intervalLongerName(intervalShortName) {\n" +
                "  var longerName = {\n" +
                "    'min2': 'minor 2nd',\n" +
                "    'Maj2': 'Major 2nd'\n" +
                "  };\n" +
                "  return longerName[intervalShortName];\n" +
                "}\n" +
                "\n" +
                "document.getElementById(\"interval_longer_name\").innerText =\n" +
                "    intervalLongerName(document.getElementById(\"interval_short_name\").innerText);\n" +
                "\n" +
                "</script>\n" +
                "\n";
        public static final String[] AFMT = {AFMT1};
        private final AnkiDroidHelper mHelper;
        private String mModelName = DEFAULT_MODEL_NAME;
        private Map<String, String> mModelFields = new HashMap<String, String>() {{
            put(Fields.SOUND, Fields.SOUND);
            put(Fields.SOUND_SMALLER, Fields.SOUND_SMALLER);
            put(Fields.SOUND_LARGER, Fields.SOUND_LARGER);
            put(Fields.START_NOTE, Fields.START_NOTE);
            put(Fields.DIRECTION, Fields.DIRECTION);
            put(Fields.TIMING, Fields.TIMING);
            put(Fields.INTERVAL, Fields.INTERVAL);
            put(Fields.TEMPO, Fields.TEMPO);
            put(Fields.INSTRUMENT, Fields.INSTRUMENT);
        }};
        private String mDeckName = DEFAULT_DECK_NAME;
        private String mSound = "";
        private String mSoundSmaller = "";
        private String mSoundLarger = "";
        private String mStartNote = "";
        private String mDirection = "";
        private String mTiming = "";
        private String mInterval = "";
        private String mTempo = "";
        private String mInstrument = "";

        public Builder(final AnkiDroidHelper helper) {
            mHelper = helper;
        }

        public MusInterval build() throws InvalidFieldsException {
            return new MusInterval(this);
        }

        public Builder model(String mn) {
            mModelName = mn;
            return this;
        }

        public Builder model_fields(Map<String, String> mflds) {
            mModelFields = mflds;
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

        public Builder sound_smaller(String sds) {
            mSoundSmaller = sds;
            return this;
        }

        public Builder sound_larger(String sdl) {
            mSoundLarger = sdl;
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
    public static class SoundAlreadyAddedException extends Exception {}
    public static class AddSoundFileException extends Exception {}

    public static class FieldsValidationException extends Exception {
        private LinkedList<String> fields;

        public FieldsValidationException(LinkedList<String> fields) {
            super();
            this.fields = fields;
        }

        public LinkedList<String> getFields() {
            return fields;
        }
    }
    public static class InvalidFieldsException extends FieldsValidationException {
        public InvalidFieldsException(LinkedList<String> invalidFields) { super(invalidFields); }
    }
    public static class MandatoryFieldsEmptyException extends FieldsValidationException {
        public MandatoryFieldsEmptyException(LinkedList<String> emptyFields) {
            super(emptyFields);
        }
    }

    private final AnkiDroidHelper helper;

    public final String modelName;
    public final Map<String, String> modelFields;
    private final Long modelId;
    public final String deckName;
    private Long deckId;

    // Data of model's fields
    public final String sound;
    public final String soundSmaller;
    public final String soundLarger;
    public final String startNote;
    public final String direction;
    public final String timing;
    public final String interval;
    public final String tempo;
    public final String instrument;

    /**
     * Construct an object using builder class.
     */
    public MusInterval(Builder builder) throws InvalidFieldsException {
        helper = builder.mHelper;

        modelName = builder.mModelName;
        modelFields = builder.mModelFields;
        modelId = helper.findModelIdByName(builder.mModelName);
        deckName = builder.mDeckName;
        deckId = helper.findDeckIdByName(builder.mDeckName);

        sound = builder.mSound.trim();
        soundSmaller = builder.mSoundSmaller;
        soundLarger = builder.mSoundLarger;
        startNote = builder.mStartNote.trim();
        direction = builder.mDirection.trim().toLowerCase();
        timing = builder.mTiming.trim().toLowerCase();
        interval = builder.mInterval.trim();
        tempo = builder.mTempo.trim();
        instrument = builder.mInstrument.trim();

        validateFields();
    }

    protected void validateFields() throws InvalidFieldsException {
        LinkedList<String> invalidFields = new LinkedList<>();
        if (!startNote.isEmpty() && !startNote.matches("[A-Ga-g]#?[0-8]")) {
            invalidFields.add(Fields.START_NOTE);
        }

        if (!tempo.isEmpty()) {
            int tempoInt = Integer.parseInt(tempo);
            if (tempoInt < Fields.Tempo.MIN_VALUE || tempoInt > Fields.Tempo.MAX_VALUE) {
                invalidFields.add(Fields.TEMPO);
            }
        }

        if (!direction.isEmpty()
                && !direction.equalsIgnoreCase(Fields.Direction.ASC)
                && !direction.equalsIgnoreCase(Fields.Direction.DESC)) {
            invalidFields.add(Fields.DIRECTION);
        }

        if (!interval.isEmpty()) {
            boolean valid = false;
            for (int i = 1; i < Fields.Interval.VALUES.length; i++) {
                if (Fields.Interval.VALUES[i].equalsIgnoreCase(interval)) {
                    valid = true;
                    break;
                }
            }
            if (!valid) {
                invalidFields.add(Fields.INTERVAL);
            }
        }

        if (!timing.isEmpty()
                && !timing.equalsIgnoreCase(Fields.Timing.MELODIC)
                && !timing.equalsIgnoreCase(Fields.Timing.HARMONIC)) {
            invalidFields.add(Fields.TIMING);
        }

        if (!invalidFields.isEmpty()) {
            throw new InvalidFieldsException(invalidFields);
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
            data.remove(modelFields.get(Fields.SOUND)); // sound field should not be compared in existing data

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
            throws CreateDeckException, AddToAnkiException, FieldsValidationException,
            SoundAlreadyAddedException, AddSoundFileException, AnkiDroidHelper.InvalidAnkiDatabaseException {

        if (deckId == null) {
            deckId = helper.addNewDeck(deckName);
            if (deckId == null) {
                throw new CreateDeckException();
            }
            helper.storeDeckReference(deckName, deckId);
        }

        checkMandatoryFields(getDefaultKeyData(getCollectedData(), modelFields));

        if (sound.startsWith("[sound:")) {
            throw new SoundAlreadyAddedException();
        }

        String newSound = helper.addFileToAnkiMedia(sound);

        if (newSound == null || newSound.isEmpty()) {
            throw new AddSoundFileException();
        }

        Map<String, String> data = getCollectedData(newSound);

        data = fillSimilarIntervals(data);

        Long noteId = helper.addNote(modelId, deckId, data, null);

        if (noteId == null) {
            throw new AddToAnkiException();
        }

        return new Builder(helper)
                .sound(data.get(modelFields.get(Fields.SOUND)))
                .sound_smaller(data.get(modelFields.get(Fields.SOUND_SMALLER)))
                .sound_larger(data.get(modelFields.get(Fields.SOUND_LARGER)))
                .start_note(data.get(modelFields.get(Fields.START_NOTE)))
                .direction(data.get(modelFields.get(Fields.DIRECTION)))
                .timing(data.get(modelFields.get(Fields.TIMING)))
                .interval(data.get(modelFields.get(Fields.INTERVAL)))
                .tempo(data.get(modelFields.get(Fields.TEMPO)))
                .instrument(data.get(modelFields.get(Fields.INSTRUMENT)))
                .build();
    }

    public static void checkMandatoryFields(Map<String, String> fields) throws MandatoryFieldsEmptyException {
        final String[] mandatoryFields = new String[]{
                Fields.SOUND,
                Fields.START_NOTE,
                Fields.DIRECTION,
                Fields.TIMING,
                Fields.INTERVAL,
                Fields.TEMPO,
                Fields.INSTRUMENT
        };
        LinkedList<String> emptyFields = new LinkedList<>();
        for (String field : mandatoryFields) {
            if (fields.getOrDefault(field, "").isEmpty()) {
                emptyFields.add(field);
            }
        }
        if (!emptyFields.isEmpty()) {
            throw new MandatoryFieldsEmptyException(emptyFields);
        }
    }

    public static Map<String, String> getDefaultKeyData(Map<String, String> data, final Map<String, String> modelFields) {
        Map<String, String> defaultKeyData = new HashMap<>();
        for (String field : Fields.SIGNATURE) {
            String modelField = modelFields.getOrDefault(field, null);
            String value = data.getOrDefault(modelField, "");
            defaultKeyData.put(field, value);
        }
        return defaultKeyData;
    }

    public Map<String, String> fillSimilarIntervals(Map<String, String> data) throws AnkiDroidHelper.InvalidAnkiDatabaseException {
        Map<String, String> newData = new HashMap<>(data);
        String soundField = modelFields.get(Fields.SOUND);
        String sound = newData.remove(soundField);
        newData.remove(Fields.SOUND_SMALLER);
        newData.remove(Fields.SOUND_LARGER);
        String intervalField = modelFields.get(Fields.INTERVAL);
        String interval = newData.get(intervalField);
        int intervalIdx = 0;
        for (int i = 1; i < Fields.Interval.VALUES.length; i++) {
            if (Fields.Interval.VALUES[i].equalsIgnoreCase(interval)) {
                intervalIdx = i;
                break;
            }
        }
        String soundSmaller = "";
        if (intervalIdx > 1) {
            newData.put(intervalField, Fields.Interval.VALUES[intervalIdx - 1]);
            LinkedList<Map<String, String>> smallerIntervals = helper.findNotes(modelId, newData);
            if (smallerIntervals != null && smallerIntervals.size() >= 1) {
                int maxIdIdx = 0;
                long maxId = Long.MIN_VALUE;
                for (int i = 0; i < smallerIntervals.size(); i++) {
                    Map<String, String> smallerIntervalData = smallerIntervals.get(i);
                    long id = Long.parseLong(smallerIntervalData.get("id"));
                    if (id > maxId) {
                        maxId = id;
                        maxIdIdx = i;
                    }
                    smallerIntervalData.put(Fields.SOUND_LARGER, sound);
                    helper.updateNote(modelId, id, smallerIntervalData);
                }
                soundSmaller = smallerIntervals.get(maxIdIdx).get(soundField);
            }
        }
        String soundLarger = "";
        if (intervalIdx < Fields.Interval.VALUES.length - 1) {
            newData.put(intervalField, Fields.Interval.VALUES[intervalIdx + 1]);
            LinkedList<Map<String, String>> largerIntervals = helper.findNotes(modelId, newData);
            if (largerIntervals != null && largerIntervals.size() >= 1) {
                int maxIdIdx = 0;
                long maxId = Long.MIN_VALUE;
                for (int i = 0; i < largerIntervals.size(); i++) {
                    Map<String, String> largerIntervalData = largerIntervals.get(i);
                    long id = Long.parseLong(largerIntervalData.get("id"));
                    if (id > maxId) {
                        maxId = id;
                        maxIdIdx = i;
                    }
                    largerIntervalData.put(Fields.SOUND_SMALLER, sound);
                    helper.updateNote(modelId, id, largerIntervalData);
                }
                soundLarger = largerIntervals.get(maxIdIdx).get(soundField);
            }
        }
        newData.put(soundField, sound);
        newData.put(intervalField, interval);
        newData.put(modelFields.get(Fields.SOUND_SMALLER), soundSmaller);
        newData.put(modelFields.get(Fields.SOUND_LARGER), soundLarger);
        return newData;
    }

    /**
     * Get the music interval data in one map.
     */
    public Map<String, String> getCollectedData() {
        return getCollectedData(this.sound);
    }

    public Map<String, String> getCollectedData(String sound) {
        Map<String, String> data = new HashMap<>();
        data.put(modelFields.get(Fields.SOUND), "[sound:" + sound + "]");
        data.put(modelFields.get(Fields.SOUND_SMALLER), soundSmaller);
        data.put(modelFields.get(Fields.SOUND_LARGER), soundLarger);
        data.put(modelFields.get(Fields.START_NOTE), startNote.toUpperCase());
        data.put(modelFields.get(Fields.DIRECTION), direction);
        data.put(modelFields.get(Fields.TIMING), timing);
        data.put(modelFields.get(Fields.INTERVAL), interval);
        data.put(modelFields.get(Fields.TEMPO), tempo);
        data.put(modelFields.get(Fields.INSTRUMENT), instrument);
        return data;
    }
}
