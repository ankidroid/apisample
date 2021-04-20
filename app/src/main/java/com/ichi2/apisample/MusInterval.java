package com.ichi2.apisample;

import java.util.ArrayList;
import java.util.Arrays;
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
        public static final String VERSION = "MI2A_version";

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

        public static String[] getSignature(boolean versionField) {
            ArrayList<String> signature = new ArrayList<String>() {{
                add(SOUND);
                add(SOUND_SMALLER);
                add(SOUND_LARGER);
                add(START_NOTE);
                add(DIRECTION);
                add(TIMING);
                add(INTERVAL);
                add(TEMPO);
                add(INSTRUMENT);
            }};
            if (versionField) {
                signature.add(VERSION);
            }
            return signature.toArray(new String[0]);
        }
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
            put(Fields.VERSION, Fields.VERSION);
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
        private String mVersion = "";

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

        public Builder version(String vs) {
            mVersion = vs;
            return this;
        }
    }

    abstract static class Exception extends Throwable {}
    public static class CreateDeckException extends Exception {}
    public static class AddToAnkiException extends Exception {}
    public static class NoteNotExistsException extends Exception {}
    public static class MandatoryFieldEmptyException extends Exception {
        private String field;

        public MandatoryFieldEmptyException(String field) {
            super();
            this.field = field;
        }

        public String getField() {
            return field;
        }
    }
    public static class SoundAlreadyAddedException extends Exception {}
    public static class AddSoundFileException extends Exception {}

    public static class ValidationException extends Exception {}
    public static class StartNoteSyntaxException extends ValidationException {}
    public static class TempoValueException extends ValidationException {}
    public static class ModelValidationException extends ValidationException {
        private String modelName;

        public ModelValidationException(String modelName) {
            super();
            this.modelName = modelName;
        }

        public String getModelName() {
            return modelName;
        }
    }
    public static class ModelDoesNotExistException extends ModelValidationException {
        public ModelDoesNotExistException(String modelName) { super(modelName); }
    }
    public static class NotEnoughFieldsException extends ModelValidationException {
        public NotEnoughFieldsException(String modelName) { super(modelName); }
    }
    public static class ModelNotConfiguredException extends ModelValidationException {
        public ModelNotConfiguredException(String modelName) { super(modelName); }
    }

    private final AnkiDroidHelper helper;

    public final String modelName;
    private final Map<String, String> modelFields;
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
    public final String version;

    /**
     * Construct an object using builder class.
     */
    public MusInterval(Builder builder) throws ValidationException {
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
        version = builder.mVersion;

        validateFields();
    }

    protected void validateFields() throws ValidationException {
        String[] signature = Fields.getSignature(!version.isEmpty());

        if (modelId == null) {
            throw new ModelDoesNotExistException(modelName);
        }
        final ArrayList<String> modelOwnFields = new ArrayList<>(Arrays.asList(helper.getFieldList(modelId)));
        if (modelOwnFields.size() < signature.length) {
            throw new NotEnoughFieldsException(modelName);
        }
        ArrayList<String> takenFields = new ArrayList<>();
        for (String fieldKey : signature) {
            if (modelFields.containsKey(fieldKey)) {
                String field = modelFields.get(fieldKey);
                if (!modelOwnFields.contains(field) || takenFields.contains(field)) {
                    throw new ModelNotConfiguredException(modelName);
                }
                takenFields.add(field);
            } else {
                throw new ModelNotConfiguredException(modelName);
            }
        }

        if (!startNote.isEmpty() && !startNote.matches("[A-Ga-g]#?[0-8]")) {
            throw new StartNoteSyntaxException();
        }

        if (!tempo.isEmpty()) {
            int tempoInt = Integer.parseInt(tempo);
            if (tempoInt < Fields.Tempo.MIN_VALUE || tempoInt > Fields.Tempo.MAX_VALUE) {
                throw new TempoValueException();
            }
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
            data.remove(MusInterval.Fields.SOUND_SMALLER);
            data.remove(MusInterval.Fields.SOUND_LARGER);
            data.remove(modelFields.get(Fields.VERSION));

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
        return tagExistingNotes("marked");
    }

    private int tagExistingNotes(String tag) throws NoteNotExistsException, AnkiDroidHelper.InvalidAnkiDatabaseException {
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

            if (!tags.contains(String.format(" %s ", tag))) {
                tags = tags + String.format("%s ", tag);

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
    public MusInterval addToAnki(DuplicateAddingPrompter prompter)
            throws  CreateDeckException, AddToAnkiException, MandatoryFieldEmptyException,
            SoundAlreadyAddedException, AddSoundFileException, ValidationException,
            AnkiDroidHelper.InvalidAnkiDatabaseException {

        if (deckId == null) {
            deckId = helper.addNewDeck(deckName);
            if (deckId == null) {
                throw new CreateDeckException();
            }
            helper.storeDeckReference(deckName, deckId);
        }

        final Map<String, String> fields = new HashMap<String, String>() {{
            put(Fields.SOUND, sound);
            put(Fields.START_NOTE, startNote);
            put(Fields.DIRECTION, direction);
            put(Fields.TIMING, timing);
            put(Fields.INTERVAL, interval);
            put(Fields.TEMPO, tempo);
            put(Fields.INSTRUMENT, instrument);
        }};
        for (Map.Entry<String, String> field : fields.entrySet()) {
            if (field.getValue().isEmpty()) {
                throw new MandatoryFieldEmptyException(field.getKey());
            }
        }
        if (sound.startsWith("[sound:")) {
            throw new SoundAlreadyAddedException();
        }

        final LinkedList<Map<String, String>> existingNotesData = getExistingNotes();
        if (existingNotesData.size() > 0) {
            prompter.promptAddDuplicate(existingNotesData, new DuplicateAddingHandler() {
                @Override
                public MusInterval add() throws AddSoundFileException, AddToAnkiException,
                        AnkiDroidHelper.InvalidAnkiDatabaseException, ValidationException {
                    return addToAnki();
                }

                @Override
                public MusInterval replace() throws AnkiDroidHelper.InvalidAnkiDatabaseException,
                        AddSoundFileException, ValidationException {
                    if (existingNotesData.size() != 1) {
                        throw new IllegalStateException("Replacing more than 1 existing notes is not supported.");
                    }
                    Map<String, String> existingData = existingNotesData.getFirst();

                    String newSound = helper.addFileToAnkiMedia(sound);
                    if (newSound == null || newSound.isEmpty()) {
                        throw new AddSoundFileException();
                    }

                    Map<String, String> data = getCollectedData(newSound);
                    data = fillSimilarIntervals(data);

                    helper.updateNote(modelId, Long.parseLong(existingData.get("id")), data);

                    return getMusIntervalFromData(data);
                }

                @Override
                public int mark() throws NoteNotExistsException, AnkiDroidHelper.InvalidAnkiDatabaseException {
                    return markExistingNotes();
                }

                @Override
                public int tag(String tag) throws NoteNotExistsException, AnkiDroidHelper.InvalidAnkiDatabaseException {
                    return tagExistingNotes(tag);
                }
            });
            return null;
        }
        return addToAnki();
    }

    private MusInterval addToAnki() throws AddSoundFileException,
            AddToAnkiException, AnkiDroidHelper.InvalidAnkiDatabaseException, ValidationException {
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

        return getMusIntervalFromData(data);
    }

    private MusInterval getMusIntervalFromData(Map<String, String> data) throws ValidationException {
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

    private Map<String, String> fillSimilarIntervals(Map<String, String> data) throws AnkiDroidHelper.InvalidAnkiDatabaseException {
        Map<String, String> newData = new HashMap<>(data);
        final String soundField = modelFields.get(Fields.SOUND);
        final String soundSmallerField = modelFields.get(Fields.SOUND_SMALLER);
        final String soundLargerField = modelFields.get(Fields.SOUND_LARGER);
        final String intervalField = modelFields.get(Fields.INTERVAL);
        final String versionField = modelFields.get(Fields.VERSION);
        final String sound = newData.remove(soundField);
        newData.remove(soundSmallerField);
        newData.remove(soundLargerField);
        final String interval = newData.get(intervalField);
        final String version = newData.remove(versionField);
        int intervalIdx = 0;
        for (int i = 1; i < Fields.Interval.VALUES.length; i++) {
            if (Fields.Interval.VALUES[i].equals(interval)) {
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
                    smallerIntervalData.put(soundLargerField, sound);
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
                    largerIntervalData.put(soundSmallerField, sound);
                    helper.updateNote(modelId, id, largerIntervalData);
                }
                soundLarger = largerIntervals.get(maxIdIdx).get(soundField);
            }
        }
        newData.put(soundField, sound);
        newData.put(soundSmallerField, soundSmaller);
        newData.put(soundLargerField, soundLarger);
        newData.put(intervalField, interval);
        newData.put(versionField, version);
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
        if (!version.isEmpty()) {
            data.put(modelFields.get(Fields.VERSION), version);
        }
        return data;
    }
}
