package com.ichi2.apisample.model;

import com.ichi2.apisample.helper.AnkiDroidHelper;
import com.ichi2.apisample.helper.MapUtil;
import com.ichi2.apisample.helper.equality.NoteEqualityChecker;
import com.ichi2.apisample.helper.search.SearchExpressionMaker;
import com.ichi2.apisample.validation.ValidationUtil;
import com.ichi2.apisample.validation.Validator;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

public abstract class RelatedIntervalSoundField {
    public static final String TAG_POINTING = "pointing";
    public static final String TAG_POINTED = "pointed";

    private final AnkiDroidHelper helper;
    private final MusInterval musInterval;

    private final String soundField;
    private final String startNoteField;
    private final String directionField;
    private final String timingField;
    private final String intervalField;
    private final String versionField;

    private RelatedIntervalSoundField reverse;

    private String relatedSoundField;
    private String relatedSoundAltField;
    private String reverseRelatedSoundField;
    private String reverseRelatedSoundAltField;

    public RelatedIntervalSoundField(AnkiDroidHelper helper, MusInterval musInterval) {
        this.helper = helper;
        this.musInterval = musInterval;

        soundField = musInterval.modelFields.getOrDefault(MusInterval.Fields.SOUND, MusInterval.Fields.SOUND);
        startNoteField = musInterval.modelFields.getOrDefault(MusInterval.Fields.START_NOTE, MusInterval.Fields.START_NOTE);
        directionField = musInterval.modelFields.getOrDefault(MusInterval.Fields.DIRECTION, MusInterval.Fields.DIRECTION);
        timingField = musInterval.modelFields.getOrDefault(MusInterval.Fields.TIMING, MusInterval.Fields.TIMING);
        intervalField = musInterval.modelFields.getOrDefault(MusInterval.Fields.INTERVAL, MusInterval.Fields.INTERVAL);
        versionField = musInterval.modelFields.getOrDefault(MusInterval.Fields.VERSION, MusInterval.Fields.VERSION);
    }

    public void setReverse(RelatedIntervalSoundField reverse) {
        this.reverse = reverse;

        String relatedSoundFieldKey = getFieldKey();
        relatedSoundField = musInterval.modelFields.getOrDefault(relatedSoundFieldKey, relatedSoundFieldKey);
        String relatedSoundAltFieldKey = getAltFieldKey();
        relatedSoundAltField = musInterval.modelFields.getOrDefault(relatedSoundAltFieldKey, relatedSoundAltFieldKey);
        String reverseRelatedSoundFieldKey = reverse.getFieldKey();
        reverseRelatedSoundField = musInterval.modelFields.getOrDefault(reverseRelatedSoundFieldKey, reverseRelatedSoundFieldKey);
        String reverseRelatedSoundAltFieldKey = reverse.getAltFieldKey();
        reverseRelatedSoundAltField = musInterval.modelFields.getOrDefault(reverseRelatedSoundAltFieldKey, reverseRelatedSoundAltFieldKey);
    }

    public boolean isSuspicious(Map<String, String> noteData, Map<String, Map<String, String>> soundDict, Map<String, Set<Map<String, String>>> suspiciousRelatedNotesData) {
        return isFieldSuspicious(noteData, relatedSoundField, soundDict, suspiciousRelatedNotesData);
    }

    public boolean isAltSuspicious(Map<String, String> noteData, Map<String, Map<String, String>> soundDict, Map<String, Set<Map<String, String>>> suspiciousRelatedNotesData) {
        return isFieldSuspicious(noteData, relatedSoundAltField, soundDict, suspiciousRelatedNotesData);
    }

    private boolean isFieldSuspicious(Map<String, String> noteData, String relatedSoundField,
                                      Map<String, Map<String, String>> soundDict,
                                      Map<String, Set<Map<String, String>>> suspiciousRelatedNotesData) {
        String interval = noteData.getOrDefault(intervalField, "");
        int intervalIdx = MusInterval.Fields.Interval.getIndex(interval);
        Map<String, String> keyData = getIntervalIdentityData(noteData);
        String relatedSound = noteData.getOrDefault(relatedSoundField, "");
        if (!relatedSound.isEmpty()) {
            Map<String, String> relatedNoteData = soundDict.getOrDefault(relatedSound, null);
            if (relatedNoteData != null) {
                String relatedInterval = relatedNoteData.getOrDefault(intervalField, "");
                Map<String, String> relatedNoteKeyData = getIntervalIdentityData(relatedNoteData);
                if (!isCorrectRelation(intervalIdx, relatedInterval) ||
                        !isEqualData(
                                keyData, relatedNoteKeyData,
                                relatedSoundField.equals(relatedSoundAltField), false)) {
                    Set<Map<String, String>> pointed = suspiciousRelatedNotesData.getOrDefault(relatedSoundField, new HashSet<Map<String, String>>());
                    pointed.add(relatedNoteData);
                    suspiciousRelatedNotesData.put(relatedSoundField, pointed);
                    return true;
                }
            } else {
                return true;
            }
        }
        return false;
    }

    private boolean isCorrectRelation(int intervalIdx, String relatedInterval) {
        return isRelationPossible(intervalIdx) && relatedInterval.equalsIgnoreCase(getRelatedInterval(intervalIdx));
    }

    public int autoFill(Map<String, String> noteData, boolean updateReverse) throws AnkiDroidHelper.InvalidAnkiDatabaseException {
        final String startNote = noteData.getOrDefault(startNoteField, "");
        final String interval = noteData.getOrDefault(intervalField, "");
        final String direction = noteData.getOrDefault(directionField, "");
        final String endNote = MusInterval.Fields.StartNote.getEndNote(startNote, direction, interval);
        final String timing = noteData.getOrDefault(timingField, "");
        String relatedSound = noteData.containsKey(relatedSoundField) ? noteData.remove(relatedSoundField) : "";
        String relatedSoundAlt = noteData.containsKey(relatedSoundAltField) ? noteData.remove(relatedSoundAltField) : "";
        final String reverseRelatedSound = noteData.containsKey(reverseRelatedSoundField) ? noteData.remove(reverseRelatedSoundField) : "'";
        final String reverseRelatedSoundAlt = noteData.containsKey(reverseRelatedSoundAltField) ? noteData.remove(reverseRelatedSoundAltField) : "";
        final String sound = noteData.containsKey(soundField) ? noteData.remove(soundField) : "";
        final String version = noteData.containsKey(versionField) ? noteData.remove(versionField) : "";

        int updatedLinks = 0;
        final int intervalIdx = MusInterval.Fields.Interval.getIndex(interval);
        if (isRelationPossible(intervalIdx)) {
            String relatedInterval = getRelatedInterval(intervalIdx);
            noteData.put(intervalField, relatedInterval);
            boolean isUnison = MusInterval.Fields.Interval.VALUE_UNISON.equalsIgnoreCase(interval);
            boolean isRelatedUnison = MusInterval.Fields.Interval.VALUE_UNISON.equals(relatedInterval);
            boolean isHarmonic = timing.equalsIgnoreCase(MusInterval.Fields.Timing.HARMONIC);
            if (isHarmonic && isRelatedUnison) {
                noteData.put(directionField, "");
            }
            LinkedList<Map<String, String>> relatedNotesData = helper.findNotes(
                    musInterval.modelId,
                    noteData,
                    musInterval.defaultValues,
                    musInterval.relativesSearchExpressionMakers,
                    musInterval.equalityCheckers
            );
            noteData.put(directionField, direction);
            if (isHarmonic || isUnison || isRelatedUnison) {
                Map<String, String> altNoteData = new HashMap<>(noteData);
                String oppositeDirection = isHarmonic && isRelatedUnison ? "" :
                        direction.equalsIgnoreCase(MusInterval.Fields.Direction.ASC) ?
                                MusInterval.Fields.Direction.DESC : MusInterval.Fields.Direction.ASC;
                if (isHarmonic) {
                    altNoteData.put(startNoteField, endNote);
                }
                altNoteData.put(directionField, oppositeDirection);
                LinkedList<Map<String, String>> relatedAltNotesData = helper.findNotes(
                        musInterval.modelId,
                        altNoteData,
                        musInterval.defaultValues,
                        musInterval.relativesSearchExpressionMakers,
                        musInterval.equalityCheckers
                );
                relatedNotesData.addAll(relatedAltNotesData);
            }

            Iterator<Map<String, String>> iterator = relatedNotesData.iterator();
            outer:
            while (iterator.hasNext()) {
                Map<String, String> relatedData = iterator.next();
                long relatedId = Long.parseLong(relatedData.get(AnkiDroidHelper.KEY_ID));
                for (Map.Entry<String, SearchExpressionMaker> relativesMakers :
                        musInterval.relativesSearchExpressionMakers.entrySet()) {
                    String modelField = relativesMakers.getKey();
                    String fieldKey = MapUtil.getKeyByValue(musInterval.modelFields, modelField);
                    Validator[] validators = MusInterval.Fields.VALIDATORS.getOrDefault(fieldKey, new Validator[]{});
                    for (Validator validator : validators) {
                        boolean isValid = ValidationUtil.isValid(
                                validator,
                                musInterval.modelId,
                                relatedId,
                                relatedData,
                                fieldKey,
                                musInterval.modelFields,
                                helper,
                                true
                        );
                        if (!isValid) {
                            iterator.remove();
                            continue outer;
                        }
                    }
                }
            }

            if (updateReverse) {
                updatedLinks += updateReverse(
                        noteData, relatedNotesData, relatedInterval,
                        sound, startNote, direction, endNote,
                        isHarmonic, isUnison, isRelatedUnison
                );
            }

            LinkedList<Map<String, String>> relatedAltNotesData = new LinkedList<>();
            if (isHarmonic || isUnison || isRelatedUnison) {
                iterator = relatedNotesData.iterator();
                while (iterator.hasNext()) {
                    Map<String, String> relatedData = iterator.next();
                    String relatedStartNote = relatedData.getOrDefault(startNoteField, "");
                    String relatedDirection = relatedData.getOrDefault(directionField, "");
                    String relatedEndNote = MusInterval.Fields.StartNote.getEndNote(relatedStartNote, relatedDirection, relatedInterval);
                    if (!startNote.equalsIgnoreCase(relatedStartNote) && !startNote.equalsIgnoreCase(relatedEndNote) ||
                            !isHarmonic && isUnison && !direction.equalsIgnoreCase(relatedDirection) ||
                            isHarmonic && isUnison &&
                                    (startNote.equalsIgnoreCase(relatedStartNote) && !direction.equalsIgnoreCase(relatedDirection) ||
                                            startNote.equalsIgnoreCase(relatedEndNote) && direction.equalsIgnoreCase(relatedDirection))) {
                        iterator.remove();
                        relatedAltNotesData.add(relatedData);
                    }
                }
            }

            for (int i = 0; i < musInterval.relativesPriorityComparators.length; i++) {
                RelativesPriorityComparator comparator = musInterval.relativesPriorityComparators[i];
                comparator.setTargetValueFromData(noteData);

                relatedNotesData = comparator.getLeadingRelatives(relatedNotesData);
                relatedAltNotesData = comparator.getLeadingRelatives(relatedAltNotesData);
            }

            String newRelatedSound = getValue(relatedNotesData, relatedSound);
            if (!relatedSound.equals(newRelatedSound)) {
                relatedSound = newRelatedSound;
                updatedLinks++;
            }
            String newRelatedSoundAlt = getValue(relatedAltNotesData, relatedSoundAlt);
            if (!relatedSoundAlt.equals(newRelatedSoundAlt)) {
                relatedSoundAlt = newRelatedSoundAlt;
                updatedLinks++;
            }
        }

        noteData.put(intervalField, interval);
        noteData.put(directionField, direction);
        noteData.put(relatedSoundField, relatedSound);
        noteData.put(relatedSoundAltField, relatedSoundAlt);
        noteData.put(reverseRelatedSoundField, reverseRelatedSound);
        noteData.put(reverseRelatedSoundAltField, reverseRelatedSoundAlt);
        noteData.put(soundField, sound);
        noteData.put(versionField, version);
        return updatedLinks;
    }

    private int updateReverse(Map<String, String> data, LinkedList<Map<String, String>> relatedNotesData,
                              String relatedInterval, String sound, String startNote, String direction, String endNote,
                              boolean isHarmonic, boolean isUnison, boolean isRelatedUnison)
            throws AnkiDroidHelper.InvalidAnkiDatabaseException {
        int updatedLinks = 0;
        outer:
        for (Map<String, String> relatedData : relatedNotesData) {
            boolean alt = false;
            if (isHarmonic || isUnison || isRelatedUnison) {
                String relatedStartNote = relatedData.getOrDefault(startNoteField, "");
                String relatedDirection = relatedData.getOrDefault(directionField, "");
                if (!relatedStartNote.equalsIgnoreCase(startNote) && !relatedStartNote.equals(endNote) ||
                        !isHarmonic && isRelatedUnison && !direction.equalsIgnoreCase(relatedDirection) ||
                        isHarmonic && isRelatedUnison && (
                                relatedStartNote.equalsIgnoreCase(startNote) && !relatedDirection.equalsIgnoreCase(direction) ||
                                        relatedStartNote.equalsIgnoreCase(endNote) && relatedDirection.equalsIgnoreCase(direction))) {
                    alt = true;
                }
            }
            String reverseRelatedSoundField = alt ? reverseRelatedSoundAltField : this.reverseRelatedSoundField;
            final String relatedReverseSound = relatedData.getOrDefault(reverseRelatedSoundField, "");
            if (!relatedReverseSound.isEmpty()) {
                Map<String, String> searchData = new HashMap<String, String>() {{
                    put(soundField, relatedReverseSound);
                }};
                LinkedList<Map<String, String>> currentReverseSearchResult = helper.findNotes(
                        musInterval.modelId,
                        searchData,
                        musInterval.defaultValues,
                        musInterval.searchExpressionMakers,
                        musInterval.equalityCheckers
                );
                if (currentReverseSearchResult.size() != 1) {
                    continue;
                }

                Map<String, String> currentReverseData = currentReverseSearchResult.getFirst();
                long currentReverseId = Long.parseLong(currentReverseData.get(AnkiDroidHelper.KEY_ID));
                for (Map.Entry<String, Validator[]> fieldValidators : MusInterval.Fields.VALIDATORS.entrySet()) {
                    String fieldKey = fieldValidators.getKey();
                    Validator[] validators = fieldValidators.getValue();
                    for (Validator validator : validators) {
                        boolean isValid = ValidationUtil.isValid(
                                validator,
                                musInterval.modelId,
                                currentReverseId,
                                currentReverseData,
                                fieldKey,
                                musInterval.modelFields,
                                helper,
                                true
                        );
                        if (!isValid) {
                            continue outer;
                        }
                    }
                }

                int relatedIntervalIdx = MusInterval.Fields.Interval.getIndex(relatedInterval);
                String currentReverseInterval = currentReverseData.getOrDefault(intervalField, "");
                if (!reverse.isCorrectRelation(relatedIntervalIdx, currentReverseInterval) ||
                        !isEqualData(
                                getIntervalIdentityData(relatedData),
                                getIntervalIdentityData(currentReverseData),
                                alt, true)) {
                    continue;
                }

                for (int j = 0; j < musInterval.relativesPriorityComparators.length - 1; j++) {
                    RelativesPriorityComparator comparator = musInterval.relativesPriorityComparators[j];
                    comparator.setTargetValueFromData(relatedData);
                    if (comparator.compare(data, currentReverseData) < 0) {
                        continue outer;
                    }
                }
            }
            relatedData.put(reverseRelatedSoundField, sound);
            long relatedId = Long.parseLong(relatedData.get(AnkiDroidHelper.KEY_ID));
            helper.updateNote(musInterval.modelId, relatedId, relatedData);
            updatedLinks++;
        }
        return updatedLinks;
    }

    private String getValue(LinkedList<Map<String, String>> relatedNotesData, String relatedSound) {
        return !relatedNotesData.isEmpty() ? relatedNotesData.getFirst().get(soundField) : relatedSound;
    }

    protected abstract String getFieldKey();

    protected abstract String getAltFieldKey();

    protected abstract boolean isRelationPossible(int intervalIdx);

    protected String getRelatedInterval(int intervalIdx) {
        return isRelationPossible(intervalIdx) ? MusInterval.Fields.Interval.VALUES[intervalIdx + getDistance()] : null;
    }

    protected abstract int getDistance();

    private Map<String, String> getIntervalIdentityData(Map<String, String> data) {
        return new HashMap<String, String>(data) {{
            remove(musInterval.modelFields.get(MusInterval.Fields.SOUND));
            remove(musInterval.modelFields.get(MusInterval.Fields.SOUND_SMALLER));
            remove(musInterval.modelFields.get(MusInterval.Fields.SOUND_SMALLER_ALT));
            remove(musInterval.modelFields.get(MusInterval.Fields.SOUND_LARGER));
            remove(musInterval.modelFields.get(MusInterval.Fields.SOUND_LARGER_ALT));
            remove(musInterval.modelFields.get(MusInterval.Fields.VERSION));
            remove(AnkiDroidHelper.KEY_ID);
            remove(AnkiDroidHelper.KEY_TAGS);
        }};
    }

    private boolean isEqualData(Map<String, String> data1, Map<String, String> data2, boolean alt, boolean reverse) {
        String interval1 = data1.getOrDefault(intervalField, "");
        int interval1Idx = MusInterval.Fields.Interval.getIndex(interval1);
        String interval2 = data2.getOrDefault(intervalField, "");
        int interval2Idx = MusInterval.Fields.Interval.getIndex(interval2);
        if (interval2Idx - interval1Idx != (reverse ? this.reverse.getDistance() : getDistance())) {
            return false;
        }
        Set<String> keySet1 = new HashSet<>(data1.keySet());
        keySet1.remove(intervalField);
        Set<String> keySet2 = new HashSet<>(data2.keySet());
        keySet2.remove(intervalField);
        if (!keySet1.equals(keySet2)) {
            return false;
        }
        boolean isUnison1 = MusInterval.Fields.Interval.VALUE_UNISON.equalsIgnoreCase(interval1);
        boolean isUnison2 = MusInterval.Fields.Interval.VALUE_UNISON.equalsIgnoreCase(interval2);
        String startNote1 = data1.getOrDefault(startNoteField, "");
        String direction1 = data1.getOrDefault(directionField, "");
        String endNote1 = MusInterval.Fields.StartNote.getEndNote(startNote1, direction1, interval1);
        String startNote2 = data2.getOrDefault(startNoteField, "");
        String direction2 = data2.getOrDefault(directionField, "");
        data2 = new HashMap<>(data2);
        String direction2Opposite = direction2.equalsIgnoreCase(MusInterval.Fields.Direction.ASC) ?
                MusInterval.Fields.Direction.DESC : MusInterval.Fields.Direction.ASC;
        if (alt) {
            if (isUnison1) {
                if (startNote1.equalsIgnoreCase(startNote2)) {
                    data2.put(directionField, direction2Opposite);
                } else {
                    int idx = MusInterval.Fields.StartNote.getIndex(startNote2);
                    int distance = Math.abs(getDistance());
                    if (direction1.equalsIgnoreCase(MusInterval.Fields.Direction.DESC)) {
                        distance = -distance;
                    }
                    if (reverse) {
                        distance = -distance;
                    }
                    data2.put(startNoteField, MusInterval.Fields.StartNote.VALUES[idx + distance]);
                }
            } else {
                String direction1Opposite = direction1.equalsIgnoreCase(MusInterval.Fields.Direction.ASC) ?
                        MusInterval.Fields.Direction.DESC : MusInterval.Fields.Direction.ASC;
                if (isUnison2) {
                    data2.put(directionField, direction1Opposite);
                }
                if (!startNote2.equalsIgnoreCase(endNote1)) {
                    int idx = MusInterval.Fields.StartNote.getIndex(startNote2);
                    int distance = !reverse ? getDistance() : this.reverse.getDistance();
                    data2.put(startNoteField, MusInterval.Fields.StartNote.VALUES[idx + distance]);
                } else {
                    data2.put(intervalField, interval1);
                }
            }
        } else {
            if (isUnison1) {
                if (!startNote1.equalsIgnoreCase(startNote2)) {
                    int idx = MusInterval.Fields.StartNote.getIndex(startNote2);
                    int distance = -Math.abs(getDistance());
                    if (direction1.equalsIgnoreCase(MusInterval.Fields.Direction.DESC)) {
                        distance = -distance;
                    }
                    if (reverse) {
                        distance = -distance;
                    }
                    data2.put(startNoteField, MusInterval.Fields.StartNote.VALUES[idx + distance]);
                    data2.put(directionField, direction2Opposite);
                }
            } else {
                if (isUnison2) {
                    data2.put(directionField, direction1);
                }
            }
        }

        for (final String key : keySet1) {
            if (!NoteEqualityChecker.areEqual(data1, data2, key,
                    musInterval.relativesEqualityCheckers, musInterval.defaultValues)) {
                return false;
            }
        }
        return true;
    }
}

class SmallerIntervalSoundField extends RelatedIntervalSoundField {
    public SmallerIntervalSoundField(AnkiDroidHelper helper, MusInterval musInterval) {
        super(helper, musInterval);
    }

    @Override
    protected String getFieldKey() {
        return MusInterval.Fields.SOUND_SMALLER;
    }

    @Override
    protected String getAltFieldKey() {
        return MusInterval.Fields.SOUND_SMALLER_ALT;
    }

    @Override
    protected boolean isRelationPossible(int intervalIdx) {
        return intervalIdx > 0;
    }

    @Override
    protected int getDistance() {
        return -1;
    }
}

class LargerIntervalSoundField extends RelatedIntervalSoundField {
    public LargerIntervalSoundField(AnkiDroidHelper helper, MusInterval musInterval) {
        super(helper, musInterval);
    }

    @Override
    protected String getFieldKey() {
        return MusInterval.Fields.SOUND_LARGER;
    }

    @Override
    protected String getAltFieldKey() {
        return MusInterval.Fields.SOUND_LARGER_ALT;
    }

    @Override
    protected boolean isRelationPossible(int intervalIdx) {
        return intervalIdx < MusInterval.Fields.Interval.VALUES.length - 1;
    }

    @Override
    protected int getDistance() {
        return 1;
    }
}