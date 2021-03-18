package com.ichi2.apisample;

import java.util.ArrayList;
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

        checkMandatoryFields(getCollectedData());

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

    public IntegritySummary checkIntegrity() throws AnkiDroidHelper.InvalidAnkiDatabaseException {
        final String soundField = modelFields.get(Fields.SOUND);
        Map<String, Map<String, String>> soundDict = new HashMap<>();
        LinkedList<Map<String, String>> allNotesData = helper.findNotes(modelId, new HashMap<String, String>());
        for (Map<String, String> noteData : allNotesData) {
            soundDict.put(noteData.getOrDefault(soundField, ""), noteData);
        }

        final String soundSmallerField = modelFields.get(MusInterval.Fields.SOUND_SMALLER);
        final String soundLargerField = modelFields.get(MusInterval.Fields.SOUND_LARGER);
        final String intervalField = modelFields.get(MusInterval.Fields.INTERVAL);

        Map<String, String> searchData = getCollectedData();
        searchData.remove(soundField);

        ArrayList<Map<String, String>> invalidNotesData = new ArrayList<>();
        ArrayList<Map<String, String>> validNotesData = new ArrayList<>();

        Map<String, Integer> invalidFieldsCount = new HashMap<>();
        Map<String, Integer> emptyFieldsCount = new HashMap<>();

        LinkedList<Map<String, String>> searchResult = helper.findNotes(modelId, searchData);

        for (final Map<String, String> noteData : searchResult) {
            boolean valid = true;
            try {
                try {
                    new MusInterval.Builder(helper)
                            .sound(noteData.get(soundField))
                            .sound_smaller(noteData.get(soundSmallerField))
                            .sound_larger(noteData.get(soundLargerField))
                            .start_note(noteData.get(modelFields.get(MusInterval.Fields.START_NOTE)))
                            .direction(noteData.get(modelFields.get(MusInterval.Fields.DIRECTION)))
                            .timing(noteData.get(modelFields.get(MusInterval.Fields.TIMING)))
                            .interval(noteData.get(modelFields.get(MusInterval.Fields.INTERVAL)))
                            .tempo(noteData.get(modelFields.get(MusInterval.Fields.TEMPO)))
                            .instrument(noteData.get(modelFields.get(MusInterval.Fields.INSTRUMENT)))
                            .build();
                } catch (MusInterval.InvalidFieldsException e) {
                    for (String field : e.getFields()) {
                        int currCount = invalidFieldsCount.getOrDefault(field, 0);
                        invalidFieldsCount.put(field, currCount + 1);
                    }
                    valid = false;
                }
                checkMandatoryFields(noteData);
            } catch (MusInterval.MandatoryFieldsEmptyException e) {
                LinkedList<String> emptyFields = e.getFields();
                for (String field : emptyFields) {
                    int currCount = emptyFieldsCount.getOrDefault(field, 0);
                    emptyFieldsCount.put(field, currCount + 1);
                }
                valid = false;
            }

            //final long noteId = Long.parseLong(noteData.get(AnkiDroidHelper.KEY_ID));
            if (!valid) {
                invalidNotesData.add(noteData);
                // mAnkiDroid.addTagToNote(noteId, String.format(" %s ", invalidTag));
                continue;
            }
            validNotesData.add(noteData);
        }

        ArrayList<Map<String, String>> susNotesData = new ArrayList<>();

        for (Map<String, String> noteData : validNotesData) {
            String interval = noteData.get(intervalField);
            int intervalIdx = 0;
            for (int i = 1; i < MusInterval.Fields.Interval.VALUES.length; i++) {
                if (MusInterval.Fields.Interval.VALUES[i].equals(interval)) {
                    intervalIdx = i;
                    break;
                }
            }
            Map<String, String> keyData = new HashMap<String, String>(noteData) {{
                remove(soundField);
                remove(soundSmallerField);
                remove(soundLargerField);
                remove(intervalField);
                remove(AnkiDroidHelper.KEY_ID);
                remove(AnkiDroidHelper.KEY_TAGS);
            }};
            boolean sus = false;
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
                        remove(AnkiDroidHelper.KEY_ID);
                        remove(AnkiDroidHelper.KEY_TAGS);
                    }};
                    if (!keyData.equals(smallerNoteKeyData) || intervalIdx <= 1 ||
                            !MusInterval.Fields.Interval.VALUES[intervalIdx - 1].equalsIgnoreCase(smallerInterval)) {
                        if (!susNotesData.contains(smallerNoteData)) {
                            susNotesData.add(smallerNoteData);
                        }
                        sus = true;
                    }
                } else {
                    sus = true;
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
                        remove(AnkiDroidHelper.KEY_ID);
                        remove(AnkiDroidHelper.KEY_TAGS);
                    }};
                    if (!keyData.equals(largerNoteKeyData) || intervalIdx >= MusInterval.Fields.Interval.VALUES.length - 1 ||
                            !MusInterval.Fields.Interval.VALUES[intervalIdx + 1].equalsIgnoreCase(largerInterval)) {
                        if (!susNotesData.contains(largerNoteData)) {
                            susNotesData.add(largerNoteData);
                        }
                        sus = true;
                    }
                } else {
                    sus = true;
                }
            }
            if (sus) {
                if (!susNotesData.contains(noteData)) {
                    susNotesData.add(noteData);
                }
            }
        }

        int fixedLinksCount = 0;
        ArrayList<Map<String, String>> correctNotesData = new ArrayList<>();
        for (Map<String, String> noteData : validNotesData) {
            if (!susNotesData.contains(noteData)) {
                correctNotesData.add(noteData);
                long noteId = Long.parseLong((noteData.get(AnkiDroidHelper.KEY_ID)));
                Map<String, String> updatedNoteData = fillSimilarIntervals(noteData);
                boolean updatedSmaller = !updatedNoteData.get(soundSmallerField).equals(noteData.get(soundSmallerField));
                boolean updatedLarger = !updatedNoteData.get(soundLargerField).equals(noteData.get(soundLargerField));
                if (!updatedSmaller && !updatedLarger) {
                    continue;
                }
                if (updatedSmaller && updatedLarger) {
                    fixedLinksCount += 2;
                } else {
                    fixedLinksCount++;
                }
                helper.updateNote(modelId, noteId, updatedNoteData);
            }
        }

        return new IntegritySummary(invalidNotesData, susNotesData, invalidFieldsCount, emptyFieldsCount, fixedLinksCount);
    }

    private void checkMandatoryFields(Map<String, String> data) throws MandatoryFieldsEmptyException {
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
        for (String mandatoryField : mandatoryFields) {
            String modelField = modelFields.get(mandatoryField);
            if (data.getOrDefault(modelField, "").isEmpty()) {
                emptyFields.add(mandatoryField);
            }
        }
        if (!emptyFields.isEmpty()) {
            throw new MandatoryFieldsEmptyException(emptyFields);
        }
    }

    public Map<String, String> fillSimilarIntervals(Map<String, String> data) throws AnkiDroidHelper.InvalidAnkiDatabaseException {
        Map<String, String> newData = new HashMap<>(data);
        String soundField = modelFields.get(Fields.SOUND);
        String soundSmallerField = modelFields.get(Fields.SOUND_SMALLER);
        String soundLargerField = modelFields.get(Fields.SOUND_LARGER);
        String sound = newData.remove(soundField);
        newData.remove(soundSmallerField);
        newData.remove(soundLargerField);
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
                    long id = Long.parseLong(smallerIntervalData.get(AnkiDroidHelper.KEY_ID));
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
                    long id = Long.parseLong(largerIntervalData.get(AnkiDroidHelper.KEY_ID));
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
        newData.put(intervalField, interval);
        newData.put(soundSmallerField, soundSmaller);
        newData.put(soundLargerField, soundLarger);
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

    public class IntegritySummary {
        private ArrayList<Map<String, String>> invalidNotesData;
        private ArrayList<Map<String, String>> suspiciousNotesData;

        private Map<String, Integer> invalidFieldsCount;
        private Map<String, Integer> emptyFieldsCount;
        private int filledLinksCound;

        public IntegritySummary(ArrayList<Map<String, String>> invalidNotesData, ArrayList<Map<String, String>> suspiciousNotesData, Map<String, Integer> invalidFieldsCount, Map<String, Integer> emptyFieldsCount, int filledLinksCound) {
            this.invalidNotesData = invalidNotesData;
            this.suspiciousNotesData = suspiciousNotesData;
            this.invalidFieldsCount = invalidFieldsCount;
            this.emptyFieldsCount = emptyFieldsCount;
            this.filledLinksCound = filledLinksCound;
        }

        public ArrayList<Map<String, String>> getInvalidNotesData() {
            return invalidNotesData;
        }

        public ArrayList<Map<String, String>> getSuspiciousNotesData() {
            return suspiciousNotesData;
        }

        public Map<String, Integer> getInvalidFieldsCount() {
            return invalidFieldsCount;
        }

        public Map<String, Integer> getEmptyFieldsCount() {
            return emptyFieldsCount;
        }

        public int getFilledLinksCount() {
            return filledLinksCound;
        }
    }
}
