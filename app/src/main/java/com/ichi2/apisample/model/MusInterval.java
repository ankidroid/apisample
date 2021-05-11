package com.ichi2.apisample.model;

import com.ichi2.apisample.R;
import com.ichi2.apisample.helper.AnkiDroidHelper;
import com.ichi2.apisample.validation.EmptyValidator;
import com.ichi2.apisample.validation.IntegerRangeValidator;
import com.ichi2.apisample.validation.PatternValidator;
import com.ichi2.apisample.validation.Validator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
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
        public static final String DIRECTION = "direction";
        public static final String TIMING = "timing";
        public static final String INTERVAL = "interval";
        public static final String TEMPO = "tempo";
        public static final String INSTRUMENT = "instrument";
        public static final String VERSION = "mi2a_version";

        public static class StartNote {
            private static final String[] NOTES = new String[]{
                    "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"
            };

            private static final String[] OCTAVES = new String[]{
                    "1", "2", "3", "4", "5", "6"
            };

            public static final String[] VALUES = new String[NOTES.length * OCTAVES.length];
            static {
                for (int i = 0; i < NOTES.length; i++) {
                    for (int j = 0; j < OCTAVES.length; j++) {
                        VALUES[i * OCTAVES.length + j] = NOTES[i] + OCTAVES[j];
                    }
                }
            }

            public static String getValidationPattern() {
                return "[A-Ga-g]#?[1-6]";
            }
        }

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

            public static int getIndex(String value) {
                for (int i = 0; i < VALUES.length; i++) {
                    if (VALUES[i].equalsIgnoreCase(value)) {
                        return i;
                    }
                }
                return -1;
            }

            private static String getValidationPattern() {
                StringBuilder pattern = new StringBuilder();
                pattern.append("(?i)");
                for (int i = 0; i < VALUES.length; i++) {
                    if (i != 0) {
                        pattern.append("|");
                    }
                    pattern.append(VALUES[i]);
                }
                return pattern.toString();
            }
        }

        public static class Tempo {
            public static final int MIN_VALUE = 20;
            public static final int MAX_VALUE = 400;

            private static final Validator RANGE_VALIDATOR = new IntegerRangeValidator(Tempo.MIN_VALUE, Tempo.MAX_VALUE);
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

        private static final Validator VALIDATOR_EMPTY = new EmptyValidator();
        private static final Validator VALIDATOR_SOUND = new PatternValidator("^$|^\\[sound:.*\\]$");

        static final Map<String, Validator[]> VALIDATORS = new HashMap<String, Validator[]>() {{
            put(SOUND, new Validator[]{
                    VALIDATOR_EMPTY,
                    VALIDATOR_SOUND
            });
            put(SOUND_SMALLER, new Validator[]{
                    VALIDATOR_SOUND
            });
            put(SOUND_LARGER, new Validator[]{
                    VALIDATOR_SOUND
            });
            put(START_NOTE, new Validator[]{
                    VALIDATOR_EMPTY,
                    new PatternValidator(StartNote.getValidationPattern())
            });
            put(DIRECTION, new Validator[]{
                    VALIDATOR_EMPTY,
                    new PatternValidator(String.format("(?i)%s|%s", Direction.ASC, Direction.DESC))
            });
            put(TIMING, new Validator[]{
                    VALIDATOR_EMPTY,
                    new PatternValidator(String.format("(?i)%s|%s", Timing.MELODIC, Timing.HARMONIC))
            });
            put(INTERVAL, new Validator[]{
                    VALIDATOR_EMPTY,
                    new PatternValidator(Interval.getValidationPattern())
            });
            put(TEMPO, new Validator[]{
                    VALIDATOR_EMPTY,
                    new PatternValidator("^[0-9]*$"),
                    Tempo.RANGE_VALIDATOR
            });
            put(INSTRUMENT, new Validator[]{
                    VALIDATOR_EMPTY
            });
        }};
    }

    public static class Builder {
        public static final String DEFAULT_DECK_NAME = "Music intervals";
        public static final String DEFAULT_MODEL_NAME = "Music.interval";
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

        public MusInterval build() throws ModelValidationException, TempoNotInRangeException {
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

    public abstract static class Exception extends Throwable {}
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
        private final ArrayList<String> invalidModelFields;
        public ModelNotConfiguredException(String modelName, ArrayList<String> invalidModelFields) {
            super(modelName);
            this.invalidModelFields = invalidModelFields;
        }

        public ArrayList<String> getInvalidModelFields() {
            return invalidModelFields;
        }
    }
    public static class TempoNotInRangeException extends Exception { }

    private final AnkiDroidHelper helper;

    final RelatedIntervalSoundField[] relatedSoundFields;

    public final String modelName;
    public final Map<String, String> modelFields;
    public final Long modelId;
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

    // Lists of added sounds & automatically filled links
    private ArrayList<String> addedSounds;
    private ArrayList<String> addedSoundsSmaller;
    private ArrayList<String> addedSoundsLarger;

    /**
     * Construct an object using builder class.
     */
    public MusInterval(Builder builder) throws ModelValidationException, TempoNotInRangeException {
        helper = builder.mHelper;

        relatedSoundFields = new RelatedIntervalSoundField[]{
                new SmallerIntervalSoundField(helper, this),
                new LargerIntervalSoundField(helper, this)
        };

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

        validateModel();
    }

    protected void validateModel() throws ModelValidationException, TempoNotInRangeException {
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

        if (!tempo.isEmpty() && !Fields.Tempo.RANGE_VALIDATOR.isValid(tempo)) {
            throw new TempoNotInRangeException();
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
            String tags = note.get(AnkiDroidHelper.KEY_TAGS);
            if (tags != null && tags.toLowerCase().contains(" marked ")) {
                ++result;
            }
        }

        return result;
    }

    /**
     * Get list of existing (similar or equal) notes. Each note consists of main model fields, id field and tags.
     */
    private LinkedList<Map<String, String>> getExistingNotes(ArrayList<Map<String, String>> dataSet) throws AnkiDroidHelper.InvalidAnkiDatabaseException {
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

    LinkedList<Map<String, String>> getExistingNotes() throws AnkiDroidHelper.InvalidAnkiDatabaseException {
        return getExistingNotes(getCollectedDataSet());
    }

    private LinkedList<Map<String, String>> getExistingNotes(final Map<String, String> data) throws AnkiDroidHelper.InvalidAnkiDatabaseException {
        ArrayList<Map<String, String>> dataSet = new ArrayList<Map<String, String>>() {{
            add(new HashMap<>(data));
        }};
        return getExistingNotes(dataSet);
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

        tag = tag.toLowerCase();

        for (Map<String, String> note : notes) {
            String tags = note.get(AnkiDroidHelper.KEY_TAGS);

            if (tags == null) {
                tags = " ";
            }

            tags = tags.toLowerCase();

            if (!tags.contains(String.format(" %s ", tag))) {
                tags = tags + String.format("%s ", tag);

                String id = note.get(AnkiDroidHelper.KEY_ID);

                if (id != null) {
                    updated += helper.addTagToNote(Long.parseLong(id), tags);
                }
            }
        }

        return updated;
    }

    public void addToAnki(AddingPrompter prompter, ProgressIndicator progressIndicator)
            throws CreateDeckException, AddToAnkiException, UnexpectedSoundsAmountException,
            MandatoryFieldEmptyException, SoundAlreadyAddedException, AddSoundFileException,
            ModelValidationException, AnkiDroidHelper.InvalidAnkiDatabaseException, TempoNotInRangeException {
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

        ArrayList<Map<String, String>> miDataSet = getCollectedDataSet();

        addedSounds = new ArrayList<>();
        addedSoundsSmaller = new ArrayList<>();
        addedSoundsLarger = new ArrayList<>();

        addToAnki(0, miDataSet, prompter, progressIndicator);
    }

    private void addToAnki(int idx, final List<Map<String, String>> dataSet, final AddingPrompter prompter, final ProgressIndicator progressIndicator)
            throws AddToAnkiException, SoundAlreadyAddedException, AddSoundFileException, ModelValidationException,
            AnkiDroidHelper.InvalidAnkiDatabaseException, TempoNotInRangeException {

        final int dataCount = dataSet.size();
        if (idx >= dataCount) {
            Builder builder = new Builder(helper)
                    .deck(deckName)
                    .model(modelName)
                    .model_fields(modelFields)
                    .sounds(addedSounds.toArray(new String[0]))
                    .sounds_smaller(addedSoundsSmaller.toArray(new String[0]))
                    .sounds_larger(addedSoundsLarger.toArray(new String[0]))
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
            prompter.addingFinished(builder.build());
            return;
        }

        for (int i = idx; i < dataCount; i++) {
            final Map<String, String> miData = dataSet.get(i);

            String sound = miData.get(modelFields.get(Fields.SOUND));
            if (sound == null) {
                throw new IllegalStateException();
            }
            if (sound.startsWith("[sound:")) {
                throw new SoundAlreadyAddedException();
            }

            final LinkedList<Map<String, String>> existingNotesData = getExistingNotes(miData);
            if (existingNotesData.size() > 0) {
                MusInterval[] existingMis = new MusInterval[existingNotesData.size()];
                for (int j = 0; j < existingNotesData.size(); j++) {
                    existingMis[j] = getMusIntervalFromData(existingNotesData.get(j));
                }

                final int nextIdx = i + 1;

                prompter.promptAddDuplicate(existingMis, new AddingHandler() {
                    @Override
                    public MusInterval add() throws AddSoundFileException, AddToAnkiException,
                            AnkiDroidHelper.InvalidAnkiDatabaseException, ModelValidationException, TempoNotInRangeException {
                        return addToAnki(miData);
                    }

                    @Override
                    public MusInterval replace() throws AnkiDroidHelper.InvalidAnkiDatabaseException,
                            AddSoundFileException, ModelValidationException, TempoNotInRangeException {
                        if (existingNotesData.size() != 1) {
                            throw new IllegalStateException("Replacing more than 1 existing note is not supported.");
                        }
                        Map<String, String> existingData = existingNotesData.getFirst();

                        String sound = miData.get(modelFields.get(Fields.SOUND));
                        String newSound = helper.addFileToAnkiMedia(sound);
                        if (newSound == null || newSound.isEmpty()) {
                            throw new AddSoundFileException();
                        }
                        newSound = String.format("[sound:%s]", newSound);
                        miData.put(modelFields.get(Fields.SOUND), newSound);

                        Map<String, String> newData = new HashMap<>(miData);
                        for (RelatedIntervalSoundField relatedSoundField : relatedSoundFields) {
                            relatedSoundField.autoFill(newData, true);
                        }

                        helper.updateNote(modelId, Long.parseLong(existingData.get(AnkiDroidHelper.KEY_ID)), newData);

                        return getMusIntervalFromData(newData);
                    }

                    @Override
                    public int mark() throws NoteNotExistsException, ModelValidationException,
                            AnkiDroidHelper.InvalidAnkiDatabaseException, TempoNotInRangeException {
                        return getMusIntervalFromData(miData).markExistingNotes();
                    }

                    @Override
                    public int tag(String tag) throws NoteNotExistsException, ModelValidationException,
                            AnkiDroidHelper.InvalidAnkiDatabaseException, TempoNotInRangeException {
                        return getMusIntervalFromData(miData).tagExistingNotes(tag);
                    }

                    @Override
                    public void proceed() throws Throwable {
                        progressIndicator.setMessage(R.string.batch_adding, nextIdx, dataCount);
                        addToAnki(nextIdx, dataSet, prompter, progressIndicator);
                    }
                });
                return;
            }

            MusInterval newMi = addToAnki(miData);
            addedSounds.add(newMi.sounds[0]);
            if (newMi.soundsSmaller.length > 0) {
                addedSoundsSmaller.add(newMi.soundsSmaller[0]);
            }
            if (newMi.soundsLarger.length > 0) {
                addedSoundsLarger.add(newMi.soundsLarger[0]);
            }

            progressIndicator.setMessage(R.string.batch_adding, i + 1, dataCount);
        }

        Builder builder = new Builder(helper)
                .deck(deckName)
                .model(modelName)
                .model_fields(modelFields)
                .sounds(addedSounds.toArray(new String[0]))
                .sounds_smaller(addedSoundsSmaller.toArray(new String[0]))
                .sounds_larger(addedSoundsLarger.toArray(new String[0]))
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
        prompter.addingFinished(builder.build());
    }

    private MusInterval addToAnki(Map<String, String> data) throws AddSoundFileException,
            AddToAnkiException, AnkiDroidHelper.InvalidAnkiDatabaseException, ModelValidationException, TempoNotInRangeException {
        String sound = data.get(modelFields.get(Fields.SOUND));
        String newSound = helper.addFileToAnkiMedia(sound);
        if (newSound == null || newSound.isEmpty()) {
            throw new AddSoundFileException();
        }
        newSound = String.format("[sound:%s]", newSound);
        data.put(modelFields.get(Fields.SOUND), newSound);

        for (RelatedIntervalSoundField relatedSoundField : relatedSoundFields) {
            relatedSoundField.autoFill(data, true);
        }

        Long noteId = helper.addNote(modelId, deckId, data, null);
        if (noteId == null) {
            throw new AddToAnkiException();
        }

        return getMusIntervalFromData(data);
    }

    private MusInterval getMusIntervalFromData(Map<String, String> data) throws ModelValidationException, TempoNotInRangeException {
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

    public int getPermutationsNumber() {
        return (notes != null ? notes.length : 0)
                * (octaves != null ? octaves.length : 0)
                * (intervals != null ? intervals.length : 0);
    }

    public ArrayList<Map<String, String>> getCollectedDataSet() {
        final String[] octaves = this.octaves != null ? this.octaves : Builder.EMPTY_SELECTION;
        final String[] notes = this.notes != null ? this.notes : Builder.EMPTY_SELECTION;
        final String[] intervals = this.intervals != null ? this.intervals : Builder.EMPTY_SELECTION;
        ArrayList<Map<String, String>> miDataSet = new ArrayList<>(octaves.length * notes.length * intervals.length);
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
                    miDataSet.add(miData);
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
}
