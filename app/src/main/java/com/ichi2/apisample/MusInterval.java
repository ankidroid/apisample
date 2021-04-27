package com.ichi2.apisample;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;


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
            public static final String[] VALUES = new String[]{
                    "Uni",
                    "min2", "Maj2",
                    "min3", "Maj3",
                    "P4",
                    "Tri",
                    "P5",
                    "min6", "Maj6",
                    "min7", "Maj7",
                    "Oct"
            };
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

        private static Map<String, Validator> VALIDATORS = new HashMap<String, Validator>() {{
            put(SOUND, new SoundValidator());
            put(SOUND_SMALLER, new SoundValidator());
            put(SOUND_LARGER, new SoundValidator());
            put(START_NOTE, new Validator() {
                @Override
                public boolean isValid(String value) {
                    return value.matches("[A-Ga-g]#?[0-8]");
                }
            });
            put(DIRECTION, new Validator() {
                @Override
                public boolean isValid(String value) {
                    return value.equalsIgnoreCase(Fields.Direction.ASC)
                            || value.equalsIgnoreCase(Fields.Direction.DESC);
                }
            });
            put(TIMING, new Validator() {
                @Override
                public boolean isValid(String value) {
                    return value.equalsIgnoreCase(Fields.Timing.MELODIC)
                            || value.equalsIgnoreCase(Fields.Timing.HARMONIC);
                }
            });
            put(INTERVAL, new Validator() {
                @Override
                public boolean isValid(String value) {
                    boolean valid = false;
                    for (int i = 0; i < Fields.Interval.VALUES.length; i++) {
                        if (Fields.Interval.VALUES[i].equalsIgnoreCase(value)) {
                            valid = true;
                            break;
                        }
                    }
                    return valid;
                }
            });
            put(TEMPO, new Validator() {
                @Override
                public boolean isValid(String value) {
                    int intVal = Integer.parseInt(value);
                    return intVal >= Tempo.MIN_VALUE && intVal <= Tempo.MAX_VALUE;
                }
            });
        }};

        private interface Validator {
            boolean isValid(String value);
        }

        private static class SoundValidator implements Validator {
            @Override
            public boolean isValid(String value) {
                return value.isEmpty() || value.startsWith("[sound:") && value.endsWith("]");
            }
        }

        private static String[] MANDATORY = new String[]{
                SOUND,
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
        public static final Set<String> ADDING_MANDATORY_SINGULAR_KEYS = new HashSet<String>() {{
            add(Fields.DIRECTION);
            add(Fields.TIMING);
            add(Fields.TEMPO);
            add(Fields.INSTRUMENT);
        }};
        public static final String KEY_NOTES = "notes";
        public static final String KEY_OCTAVES = "octaves";
        public static final String KEY_INTERVALS = "intervals";
        public static final Set<String> ADDING_MANDATORY_SELECTION_KEYS = new HashSet<String>() {{
            add(KEY_NOTES);
            add(KEY_OCTAVES);
            add(KEY_INTERVALS);
        }};
        private static final String[] EMPTY_SELECTION = new String[]{"%"};
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
        private String[] mSounds = new String[]{};
        private String[] mSoundsSmaller = new String[]{};
        private String[] mSoundsLarger = new String[]{};
        private String[] mNotes = new String[]{};
        private String[] mOctaves = new String[]{};
        private String mDirection = "";
        private String mTiming = "";
        private String[] mIntervals = new String[]{};
        private String mTempo = "";
        private String mInstrument = "";
        private String mVersion = "";

        public Builder(final AnkiDroidHelper helper) {
            mHelper = helper;
        }

        public MusInterval build() throws ModelValidationException {
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

        public Builder sounds(String[] sds) {
            mSounds = sds;
            return this;
        }

        public Builder sounds_smaller(String[] sdss) {
            mSoundsSmaller = sdss;
            return this;
        }

        public Builder sounds_larger(String[] sdls) {
            mSoundsLarger = sdls;
            return this;
        }

        public Builder notes(String[] nts) {
            mNotes = nts;
            return this;
        }

        public Builder octaves(String[] ocs) {
            mOctaves = ocs;
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

        public Builder intervals(String[] ins) {
            mIntervals = ins;
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
    public static class UnexpectedSoundsAmountException extends Exception {
        private final int expectedAmount;
        private final int providedAmount;

        public UnexpectedSoundsAmountException(int expectedAmount, int providedAmount) {
            this.expectedAmount = expectedAmount;
            this.providedAmount = providedAmount;
        }

        public int getExpectedAmount() { return expectedAmount; }

        public int getProvidedAmount() { return providedAmount; }
    }
    public static class MandatoryFieldEmptyException extends Exception {
        private final String field;

        public MandatoryFieldEmptyException(String field) {
            super();
            this.field = field;
        }

        public String getField() {
            return field;
        }
    }
    public static class MandatorySelectionEmptyException extends MandatoryFieldEmptyException {
        public MandatorySelectionEmptyException(String field) { super(field); }
    }
    public static class SoundAlreadyAddedException extends Exception {}
    public static class AddSoundFileException extends Exception {}
    public static class ModelValidationException extends Exception {
        private final String modelName;

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
        private ArrayList<String> invalidModelFields;
        public ModelNotConfiguredException(String modelName, ArrayList<String> invalidModelFields) {
            super(modelName);
            this.invalidModelFields = invalidModelFields;
        }

        public ArrayList<String> getInvalidModelFields() {
            return invalidModelFields;
        }
    }

    private final AnkiDroidHelper helper;

    public final String modelName;
    public final Map<String, String> modelFields;
    private final Long modelId;
    public final String deckName;
    private Long deckId;

    // Data of model's fields
    public final String[] sounds;
    public final String[] soundsSmaller;
    public final String[] soundsLarger;
    public final String[] notes;
    public final String[] octaves;
    public final String direction;
    public final String timing;
    public final String[] intervals;
    public final String tempo;
    public final String instrument;
    public final String version;

    /**
     * Construct an object using builder class.
     */
    public MusInterval(Builder builder) throws ModelValidationException {
        helper = builder.mHelper;

        modelName = builder.mModelName;
        modelFields = builder.mModelFields;
        modelId = helper.findModelIdByName(builder.mModelName);
        deckName = builder.mDeckName;
        deckId = helper.findDeckIdByName(builder.mDeckName);

        sounds = builder.mSounds;
        soundsSmaller = builder.mSoundsSmaller;
        soundsLarger = builder.mSoundsLarger;
        notes = builder.mNotes;
        octaves = builder.mOctaves;
        direction = builder.mDirection.trim().toLowerCase();
        timing = builder.mTiming.trim().toLowerCase();
        intervals = builder.mIntervals;
        tempo = builder.mTempo.trim();
        instrument = builder.mInstrument.trim();
        version = builder.mVersion;

        validateFields();
    }

    protected void validateFields() throws ModelValidationException {
        String[] signature = Fields.getSignature(!version.isEmpty());

        if (modelId == null) {
            throw new ModelDoesNotExistException(modelName);
        }
        final ArrayList<String> modelOwnFields = new ArrayList<>(Arrays.asList(helper.getFieldList(modelId)));
        if (modelOwnFields.size() < signature.length) {
            throw new NotEnoughFieldsException(modelName);
        }
        ArrayList<String> invalidModelFields = new ArrayList<>();
        for (String fieldKey : signature) {
            if (modelFields.containsKey(fieldKey)) {
                String field = modelFields.get(fieldKey);
                if (!modelOwnFields.contains(field)) {
                    invalidModelFields.add(fieldKey);
                }
            } else {
                invalidModelFields.add(fieldKey);
            }
        }
        if (!invalidModelFields.isEmpty()) {
            throw new ModelNotConfiguredException(modelName, invalidModelFields);
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
            if (note.get(AnkiDroidHelper.KEY_TAGS) != null && note.get(AnkiDroidHelper.KEY_TAGS).contains(" marked ")) {
                ++result;
            }
        }

        return result;
    }

    /**
     * Get list of existing (similar or equal) notes. Each note consists of main model fields, id field and tags.
     */
    private LinkedList<Map<String, String>> getExistingNotes(Map<String, String>[] dataSet) throws AnkiDroidHelper.InvalidAnkiDatabaseException {
        if (modelId != null) {
            for (Map<String, String> data : dataSet) {
                data.remove(modelFields.get(Fields.SOUND));
                data.remove(modelFields.get(Fields.SOUND_SMALLER));
                data.remove(modelFields.get(Fields.SOUND_LARGER));
                data.remove(modelFields.get(Fields.VERSION));
            }
            return helper.findNotes(modelId, dataSet);
        } else {
            return new LinkedList<>();
        }
    }

    private LinkedList<Map<String, String>> getExistingNotes() throws AnkiDroidHelper.InvalidAnkiDatabaseException {
        return getExistingNotes(getCollectedDataSet());
    }

    @SuppressWarnings("unchecked")
    private LinkedList<Map<String, String>> getExistingNotes(Map<String, String> data) throws AnkiDroidHelper.InvalidAnkiDatabaseException {
        return getExistingNotes(new Map[]{new HashMap<>(data)});
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
            String tags = note.get(AnkiDroidHelper.KEY_TAGS);

            if (tags == null) {
                tags = " ";
            }

            if (!tags.contains(String.format(" %s ", tag))) {
                tags = tags + String.format("%s ", tag);

                if (note.get("id") != null) {
                    updated += helper.addTagToNote(Long.parseLong(note.get(AnkiDroidHelper.KEY_ID)), tags);
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
            throws CreateDeckException, AddToAnkiException, UnexpectedSoundsAmountException,
            MandatoryFieldEmptyException, SoundAlreadyAddedException, AddSoundFileException,
            ModelValidationException, AnkiDroidHelper.InvalidAnkiDatabaseException {
        if (deckId == null) {
            deckId = helper.addNewDeck(deckName);
            if (deckId == null) {
                throw new CreateDeckException();
            }
            helper.storeDeckReference(deckName, deckId);
        }

        final Map<String, String[]> selectionFieldValues = new HashMap<String, String[]>() {{
            put(Builder.KEY_NOTES, notes);
            put(Builder.KEY_OCTAVES, octaves);
            put(Builder.KEY_INTERVALS, intervals);
        }};
        if (!selectionFieldValues.keySet().equals(Builder.ADDING_MANDATORY_SELECTION_KEYS)) {
            throw new AssertionError();
        }
        for (Map.Entry<String, String[]> field : selectionFieldValues.entrySet()) {
            String[] value = field.getValue();
            if (value == null || value.length == 0) {
                throw new MandatorySelectionEmptyException(field.getKey());
            }
        }

        final int permutationsNumber = getPermutationsNumber();
        final boolean soundsProvided = sounds != null;
        if (!soundsProvided || sounds.length != permutationsNumber) {
            final int providedAmount = soundsProvided ? sounds.length : 0;
            throw new UnexpectedSoundsAmountException(permutationsNumber, providedAmount);
        }

        final Map<String, String> singularFieldValues = new HashMap<String, String>() {{
            put(Fields.DIRECTION, direction);
            put(Fields.TIMING, timing);
            put(Fields.TEMPO, tempo);
            put(Fields.INSTRUMENT, instrument);
        }};
        if (!singularFieldValues.keySet().equals(Builder.ADDING_MANDATORY_SINGULAR_KEYS)) {
            throw new AssertionError();
        }
        for (Map.Entry<String, String> field : singularFieldValues.entrySet()) {
            if (field.getValue().isEmpty()) {
                throw new MandatoryFieldEmptyException(field.getKey());
            }
        }

        Map<String, String>[] miDataSet = getCollectedDataSet();
        final String soundField = modelFields.get(Fields.SOUND);
        ArrayList<String> addedSounds = new ArrayList<>();
        ArrayList<String> soundsSmaller = new ArrayList<>();
        ArrayList<String> soundsLarger = new ArrayList<>();
        for (final Map<String, String> miData : miDataSet) {
            String sound = miData.get(soundField);
            if (sound == null) {
                throw new IllegalStateException();
            }
            if (sound.startsWith("[sound:")) {
                throw new SoundAlreadyAddedException();
            }

            final LinkedList<Map<String, String>> existingNotesData = getExistingNotes(miData);
            if (existingNotesData.size() > 0) {
                MusInterval[] existingMis = new MusInterval[existingNotesData.size()];
                for (int i = 0; i < existingNotesData.size(); i++) {
                    existingMis[i] = getMusIntervalFromData(existingNotesData.get(i));
                }
                prompter.promptAddDuplicate(existingMis, new DuplicateAddingHandler() {
                    @Override
                    public MusInterval add() throws AddSoundFileException, AddToAnkiException,
                            AnkiDroidHelper.InvalidAnkiDatabaseException, ModelValidationException {
                        return addToAnki(miData);
                    }

                    @Override
                    public MusInterval replace() throws AnkiDroidHelper.InvalidAnkiDatabaseException,
                            AddSoundFileException, ModelValidationException {
                        if (existingNotesData.size() != 1) {
                            throw new IllegalStateException("Replacing more than 1 existing note is not supported.");
                        }
                        Map<String, String> existingData = existingNotesData.getFirst();

                        String sound = miData.get(soundField);
                        String newSound = helper.addFileToAnkiMedia(sound);
                        if (newSound == null || newSound.isEmpty()) {
                            throw new AddSoundFileException();
                        }
                        newSound = String.format("[sound:%s]", newSound);
                        miData.put(soundField, newSound);

                        Map<String, String> newData = new HashMap<>(miData);
                        fillSimilarIntervals(newData, true);

                        helper.updateNote(modelId, Long.parseLong(existingData.get(AnkiDroidHelper.KEY_ID)), newData);

                        return getMusIntervalFromData(newData);
                    }

                    @Override
                    public int mark() throws NoteNotExistsException, ModelValidationException,
                            AnkiDroidHelper.InvalidAnkiDatabaseException {
                        return getMusIntervalFromData(miData).markExistingNotes();
                    }

                    @Override
                    public int tag(String tag) throws NoteNotExistsException, ModelValidationException,
                            AnkiDroidHelper.InvalidAnkiDatabaseException {
                        return getMusIntervalFromData(miData).tagExistingNotes(tag);
                    }
                });
                continue;
            }

            MusInterval newMi = addToAnki(miData);
            addedSounds.add(newMi.sounds[0]);
            if (newMi.soundsSmaller.length > 0) {
                soundsSmaller.add(newMi.soundsSmaller[0]);
            }
            if (newMi.soundsLarger.length > 0) {
                soundsLarger.add(newMi.soundsLarger[0]);
            }
        }

        Builder builder = new Builder(helper)
                .deck(deckName)
                .model(modelName)
                .model_fields(modelFields)
                .sounds(addedSounds.toArray(new String[0]))
                .sounds_smaller(soundsSmaller.toArray(new String[0]))
                .sounds_larger(soundsLarger.toArray(new String[0]))
                .notes(notes)
                .octaves(octaves)
                .direction(direction)
                .timing(timing)
                .intervals(intervals)
                .tempo(tempo)
                .instrument(instrument);
        if (!version.isEmpty()) {
            builder.version(version);
        }
        return builder.build();
    }

    private MusInterval addToAnki(Map<String, String> data) throws AddSoundFileException,
            AddToAnkiException, AnkiDroidHelper.InvalidAnkiDatabaseException, ModelValidationException {
        String sound = data.get(modelFields.get(Fields.SOUND));
        String newSound = helper.addFileToAnkiMedia(sound);
        if (newSound == null || newSound.isEmpty()) {
            throw new AddSoundFileException();
        }
        newSound = String.format("[sound:%s]", newSound);
        data.put(modelFields.get(Fields.SOUND), newSound);

        fillSimilarIntervals(data, true);

        Long noteId = helper.addNote(modelId, deckId, data, null);
        if (noteId == null) {
            throw new AddToAnkiException();
        }

        return getMusIntervalFromData(data);
    }

    private MusInterval getMusIntervalFromData(Map<String, String> data) throws ModelValidationException {
        String startNote = data.get(modelFields.get(Fields.START_NOTE));
        String note = null;
        String octave = null;
        if (startNote != null && startNote.length() >= 2) {
            note = startNote.substring(0, startNote.length() - 1);
            octave = startNote.substring(startNote.length() - 1);
        }
        Builder builder = new Builder(helper)
                .deck(deckName)
                .model(modelName)
                .model_fields(modelFields)
                .sounds(new String[]{data.get(modelFields.get(Fields.SOUND))})
                .sounds_smaller(new String[]{data.get(modelFields.get(Fields.SOUND_SMALLER))})
                .sounds_larger(new String[]{data.get(modelFields.get(Fields.SOUND_LARGER))})
                .notes(note != null ? new String[]{note} : new String[]{})
                .octaves(octave != null ? new String[]{octave} : new String[]{})
                .direction(data.get(modelFields.get(Fields.DIRECTION)))
                .timing(data.get(modelFields.get(Fields.TIMING)))
                .intervals(new String[]{data.get(modelFields.get(Fields.INTERVAL))})
                .tempo(data.get(modelFields.get(Fields.TEMPO)))
                .instrument(data.get(modelFields.get(Fields.INSTRUMENT)));
        if (!version.isEmpty()) {
            builder.version(version);
        }
        return builder.build();
    }

    public IntegritySummary checkIntegrity(String corruptedTag, String suspiciousTag) throws AnkiDroidHelper.InvalidAnkiDatabaseException {
        LinkedList<Map<String, String>> allNotesData = helper.findNotes(modelId, new HashMap<String, String>());

        final String soundField = modelFields.get(Fields.SOUND);
        Map<String, Map<String, String>> soundDict = new HashMap<>();
        for (Map<String, String> noteData : allNotesData) {
            soundDict.put(noteData.getOrDefault(soundField, ""), noteData);
        }

        final String soundSmallerField = modelFields.get(MusInterval.Fields.SOUND_SMALLER);
        final String soundLargerField = modelFields.get(MusInterval.Fields.SOUND_LARGER);

        ArrayList<Map<String, String>> correctNotesData = new ArrayList<>();
        ArrayList<Map<String, String>> corruptedNotesData = new ArrayList<>();
        Map<String, Integer> invalidFieldsCount = new HashMap<>();
        Map<String, Integer> emptyFieldsCount = new HashMap<>();

        LinkedList<Map<String, String>> searchResult = getExistingNotes();

        final String corruptedTagStr = String.format(" %s ", corruptedTag);

        int fixedCorruptedNotesCount = 0;
        int fixedSuspiciousNotesCount = 0;

        final String intervalField = modelFields.get(MusInterval.Fields.INTERVAL);
        final String versionField = modelFields.get(MusInterval.Fields.VERSION);

        for (final Map<String, String> noteData : searchResult) {
            boolean corrupted = false;

            ArrayList<String> emptyFields = new ArrayList<>();
            for (String fieldKey : Fields.MANDATORY) {
                String value = noteData.getOrDefault(modelFields.getOrDefault(fieldKey, ""), "");
                if (value.isEmpty()) {
                    int curr = emptyFieldsCount.getOrDefault(fieldKey, 0);
                    emptyFieldsCount.put(fieldKey, curr + 1);
                    emptyFields.add(fieldKey);
                }
            }

            for (Map.Entry<String, Fields.Validator> fieldValidator : Fields.VALIDATORS.entrySet()) {
                String fieldKey = fieldValidator.getKey();
                if (emptyFields.contains(fieldKey)) {
                    continue;
                }
                Fields.Validator validator = fieldValidator.getValue();
                String value = noteData.getOrDefault(modelFields.getOrDefault(fieldKey, ""), "");
                if (!validator.isValid(value)) {
                    int curr = invalidFieldsCount.getOrDefault(fieldKey, 0);
                    invalidFieldsCount.put(fieldKey, curr + 1);
                    corrupted = true;
                }
            }

            long noteId = Long.parseLong(noteData.get(AnkiDroidHelper.KEY_ID));
            String noteTags = noteData.get(AnkiDroidHelper.KEY_TAGS);
            boolean hasCorruptedTag = noteTags.contains(corruptedTagStr);
            if (corrupted) {
                if (!hasCorruptedTag) {
                    helper.addTagToNote(noteId, corruptedTagStr);
                }
                corruptedNotesData.add(noteData);
                continue;
            }
            if (hasCorruptedTag) {
                helper.updateNoteTags(noteId, noteTags.replace(corruptedTagStr, " "));
                fixedCorruptedNotesCount++;
            }
            correctNotesData.add(noteData);
        }

        ArrayList<Map<String, String>> suspiciousNotesData = new ArrayList<>();

        for (Map<String, String> noteData : correctNotesData) {
            String interval = noteData.get(intervalField);
            int intervalIdx = 0;
            for (int i = 0; i < Fields.Interval.VALUES.length; i++) {
                if (Fields.Interval.VALUES[i].equals(interval)) {
                    intervalIdx = i;
                    break;
                }
            }
            Map<String, String> keyData = new HashMap<String, String>(noteData) {{
                remove(soundField);
                remove(soundSmallerField);
                remove(soundLargerField);
                remove(intervalField);
                remove(versionField);
                remove(AnkiDroidHelper.KEY_ID);
                remove(AnkiDroidHelper.KEY_TAGS);
            }};
            boolean suspicious = false;
            String soundSmaller = noteData.getOrDefault(soundSmallerField, "");
            if (!soundSmaller.isEmpty()) {
                Map<String, String> smallerNoteData = soundDict.getOrDefault(soundSmaller, null);
                if (smallerNoteData != null) {
                    String smallerInterval = smallerNoteData.getOrDefault(intervalField, "");
                    Map<String, String> smallerNoteKeyData = new HashMap<String, String>(smallerNoteData) {{
                        remove(soundField);
                        remove(soundSmallerField);
                        remove(soundLargerField);
                        remove(intervalField);
                        remove(versionField);
                        remove(AnkiDroidHelper.KEY_ID);
                        remove(AnkiDroidHelper.KEY_TAGS);
                    }};
                    if (!keyData.equals(smallerNoteKeyData) || intervalIdx <= 0 ||
                            !Fields.Interval.VALUES[intervalIdx - 1].equalsIgnoreCase(smallerInterval)) {
                        if (!suspiciousNotesData.contains(smallerNoteData)) {
                            suspiciousNotesData.add(smallerNoteData);
                        }
                        suspicious = true;
                    }
                } else {
                    suspicious = true;
                }
            }
            String soundLarger = noteData.getOrDefault(soundLargerField, "");
            if (!soundLarger.isEmpty()) {
                Map<String, String> largerNoteData = soundDict.getOrDefault(soundLarger, null);
                if (largerNoteData != null) {
                    String largerInterval = largerNoteData.getOrDefault(intervalField, "");
                    Map<String, String> largerNoteKeyData = new HashMap<String, String>(largerNoteData) {{
                        remove(soundField);
                        remove(soundSmallerField);
                        remove(soundLargerField);
                        remove(intervalField);
                        remove(versionField);
                        remove(AnkiDroidHelper.KEY_ID);
                        remove(AnkiDroidHelper.KEY_TAGS);
                    }};
                    if (!keyData.equals(largerNoteKeyData) || intervalIdx >= Fields.Interval.VALUES.length - 1 ||
                            !Fields.Interval.VALUES[intervalIdx + 1].equalsIgnoreCase(largerInterval)) {
                        if (!suspiciousNotesData.contains(largerNoteData)) {
                            suspiciousNotesData.add(largerNoteData);
                        }
                        suspicious = true;
                    }
                } else {
                    suspicious = true;
                }
            }
            if (suspicious) {
                if (!suspiciousNotesData.contains(noteData)) {
                    suspiciousNotesData.add(noteData);
                }
            }
        }

        final String suspiciousTagStr = String.format(" %s ", suspiciousTag);

        int filledLinksCount = 0;
        for (Map<String, String> noteData : correctNotesData) {
            long noteId = Long.parseLong((noteData.get(AnkiDroidHelper.KEY_ID)));
            String noteTags = noteData.get(AnkiDroidHelper.KEY_TAGS);
            boolean hasSuspiciousTag = noteTags.contains(suspiciousTagStr);
            if (!suspiciousNotesData.contains(noteData)) {
                Map<String, String> noteFieldsData = new HashMap<String, String>(noteData) {{
                    remove(AnkiDroidHelper.KEY_ID);
                    remove(AnkiDroidHelper.KEY_TAGS);
                }};
                int updatedLinks = fillSimilarIntervals(noteFieldsData, false);
                if (updatedLinks > 0) {
                    helper.updateNote(modelId, noteId, noteFieldsData);
                    filledLinksCount += updatedLinks;
                }
                if (hasSuspiciousTag) {
                    helper.updateNoteTags(noteId, noteTags.replace(suspiciousTag, " "));
                    fixedSuspiciousNotesCount++;
                }
            } else {
                if (!hasSuspiciousTag) {
                    helper.addTagToNote(noteId, suspiciousTagStr);
                }
            }
        }

        return new IntegritySummary(
                searchResult.size(),
                corruptedNotesData.size(),
                invalidFieldsCount,
                emptyFieldsCount,
                fixedCorruptedNotesCount,
                suspiciousNotesData.size(),
                fixedSuspiciousNotesCount,
                filledLinksCount
        );
    }

    private int fillSimilarIntervals(Map<String, String> data, boolean updateReverse) throws AnkiDroidHelper.InvalidAnkiDatabaseException {
        final String soundField = modelFields.get(Fields.SOUND);
        final String soundSmallerField = modelFields.get(Fields.SOUND_SMALLER);
        final String soundLargerField = modelFields.get(Fields.SOUND_LARGER);
        final String intervalField = modelFields.get(Fields.INTERVAL);
        final String versionField = modelFields.get(Fields.VERSION);
        final String sound = data.remove(soundField);
        String soundSmaller = data.remove(soundSmallerField);
        String soundLarger = data.remove(soundLargerField);
        final String interval = data.get(intervalField);
        final String version = data.remove(versionField);
        int intervalIdx = -1;
        for (int i = 0; i < Fields.Interval.VALUES.length; i++) {
            if (Fields.Interval.VALUES[i].equals(interval)) {
                intervalIdx = i;
                break;
            }
        }
        int updatedLinks = 0;
        if (intervalIdx > 0) {
            data.put(intervalField, Fields.Interval.VALUES[intervalIdx - 1]);
            LinkedList<Map<String, String>> smallerIntervals = helper.findNotes(modelId, data);
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
                    if (updateReverse && !smallerIntervalData.getOrDefault(soundLargerField, "").equals(sound)) {
                        smallerIntervalData.put(soundLargerField, sound);
                        helper.updateNote(modelId, id, smallerIntervalData);
                        updatedLinks++;
                    }
                }
                String newSoundSmaller = smallerIntervals.get(maxIdIdx).get(soundField);
                if (!soundSmaller.equals(newSoundSmaller)) {
                    soundSmaller = newSoundSmaller;
                    updatedLinks++;
                }
            }
        }
        if (intervalIdx < Fields.Interval.VALUES.length - 1) {
            data.put(intervalField, Fields.Interval.VALUES[intervalIdx + 1]);
            LinkedList<Map<String, String>> largerIntervals = helper.findNotes(modelId, data);
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
                    if (updateReverse && !largerIntervalData.getOrDefault(soundSmallerField, "").equals(sound)) {
                        largerIntervalData.put(soundSmallerField, sound);
                        helper.updateNote(modelId, id, largerIntervalData);
                        updatedLinks++;
                    }
                }
                String newSoundLarger = largerIntervals.get(maxIdIdx).get(soundField);
                if (!soundLarger.equals(newSoundLarger)) {
                    soundLarger = newSoundLarger;
                    updatedLinks++;
                }
            }
        }
        data.put(soundField, sound);
        data.put(soundSmallerField, soundSmaller);
        data.put(soundLargerField, soundLarger);
        data.put(intervalField, interval);
        data.put(versionField, version);
        return updatedLinks;
    }

    public int getPermutationsNumber() {
        return (notes != null ? notes.length : 0)
                * (octaves != null ? octaves.length : 0)
                * (intervals != null ? intervals.length : 0);
    }

    @SuppressWarnings("unchecked")
    public Map<String, String>[] getCollectedDataSet() {
        final String[] octaves = this.octaves != null ? this.octaves : Builder.EMPTY_SELECTION;
        final String[] notes = this.notes != null ? this.notes : Builder.EMPTY_SELECTION;
        final String[] intervals = this.intervals != null ? this.intervals : Builder.EMPTY_SELECTION;
        Map<String, String>[] miDataSet = new Map[octaves.length * notes.length * intervals.length];
        int i = 0;
        final boolean soundsProvided = sounds != null;
        final boolean soundsSmallerProvided = soundsSmaller != null;
        final boolean soundsLargerProvided = soundsLarger != null;
        for (String octave : octaves) {
            for (String note : notes) {
                for (String interval : intervals) {
                    Map<String, String> miData = new HashMap<>();
                    String sound = soundsProvided && sounds.length > i ? sounds[i] : "";
                    miData.put(modelFields.get(Fields.SOUND), sound);
                    String soundSmaller = soundsSmallerProvided && soundsSmaller.length > i ? soundsSmaller[i] : "";
                    miData.put(modelFields.get(Fields.SOUND_SMALLER), soundSmaller);
                    String soundLarger = soundsLargerProvided && soundsLarger.length > i ? soundsLarger[i] : "";
                    miData.put(modelFields.get(Fields.SOUND_LARGER), soundLarger);
                    miData.put(modelFields.get(Fields.START_NOTE), note + octave);
                    miData.put(modelFields.get(Fields.DIRECTION), direction);
                    miData.put(modelFields.get(Fields.TIMING), timing);
                    miData.put(modelFields.get(Fields.INTERVAL), interval);
                    miData.put(modelFields.get(Fields.TEMPO), tempo);
                    miData.put(modelFields.get(Fields.INSTRUMENT), instrument);
                    miDataSet[i] = miData;
                    i++;
                }
            }
        }
        if (!version.isEmpty()) {
            for (Map<String, String> miData : miDataSet) {
                miData.put(modelFields.get(Fields.VERSION), version);
            }
        }
        return miDataSet;
    }

    public static class IntegritySummary {
        private final int notesCount;
        private final int corruptedNotesCount;
        private final Map<String, Integer> invalidFieldsCount;
        private final Map<String, Integer> emptyFieldsCount;
        private final int fixedCorruptedNotesCount;
        private final int suspiciousNotesCount;
        private final int fixedSuspiciousNotesCount;
        private final int filledLinksCount;

        public IntegritySummary(int notesCount, int corruptedNotesCount, Map<String, Integer> invalidFieldsCount, Map<String, Integer> emptyFieldsCount, int fixedCorruptedNotesCount, int suspiciousNotesCount, int fixedSuspiciousNotesCount, int filledLinksCount) {
            this.notesCount = notesCount;
            this.corruptedNotesCount = corruptedNotesCount;
            this.invalidFieldsCount = invalidFieldsCount;
            this.emptyFieldsCount = emptyFieldsCount;
            this.fixedCorruptedNotesCount = fixedCorruptedNotesCount;
            this.suspiciousNotesCount = suspiciousNotesCount;
            this.fixedSuspiciousNotesCount = fixedSuspiciousNotesCount;
            this.filledLinksCount = filledLinksCount;
        }

        public int getNotesCount() {
            return notesCount;
        }

        public int getCorruptedNotesCount() {
            return corruptedNotesCount;
        }

        public Map<String, Integer> getInvalidFieldsCount() {
            return invalidFieldsCount;
        }

        public Map<String, Integer> getEmptyFieldsCount() {
            return emptyFieldsCount;
        }

        public int getFixedCorruptedNotesCount() {
            return fixedCorruptedNotesCount;
        }

        public int getSuspiciousNotesCount() {
            return suspiciousNotesCount;
        }

        public int getFixedSuspiciousNotesCount() {
            return fixedSuspiciousNotesCount;
        }

        public int getFilledLinksCount() {
            return filledLinksCount;
        }
    }
}
