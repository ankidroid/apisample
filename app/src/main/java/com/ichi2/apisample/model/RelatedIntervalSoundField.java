package com.ichi2.apisample.model;

import com.ichi2.apisample.helper.AnkiDroidHelper;
import com.ichi2.apisample.helper.equality.EqualityChecker;
import com.ichi2.apisample.helper.equality.FieldEqualityChecker;
import com.ichi2.apisample.helper.search.SearchExpressionMaker;
import com.ichi2.apisample.validation.FieldValidator;
import com.ichi2.apisample.validation.FixableNoteValidator;
import com.ichi2.apisample.validation.NoteValidator;
import com.ichi2.apisample.validation.Validator;

import java.util.Collections;
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

    public RelatedIntervalSoundField(AnkiDroidHelper helper, MusInterval musInterval) {
        this.helper = helper;
        this.musInterval = musInterval;
    }

    public boolean isSuspicious(Map<String, String> noteData, Map<String, Map<String, String>> soundDict, Map<String, Set<Map<String, String>>> suspiciousRelatedNotesData) {
        final String intervalField = musInterval.modelFields.getOrDefault(MusInterval.Fields.INTERVAL, MusInterval.Fields.INTERVAL);
        final String interval = noteData.getOrDefault(intervalField, "");
        final int intervalIdx = MusInterval.Fields.Interval.getIndex(interval);

        final String relatedSoundFieldKey = getFieldKey();
        final String relatedSoundField = musInterval.modelFields.getOrDefault(relatedSoundFieldKey, relatedSoundFieldKey);
        final String relatedSound = noteData.getOrDefault(relatedSoundField, "");

        Map<String, String> keyData = getIntervalIdentityData(noteData);
        boolean suspicious = false;
        if (!relatedSound.isEmpty()) {
            Map<String, String> relatedNoteData = soundDict.getOrDefault(relatedSound, null);
            if (relatedNoteData != null) {
                String relatedInterval = relatedNoteData.getOrDefault(intervalField, "");
                Map<String, String> relatedNoteKeyData = getIntervalIdentityData(relatedNoteData);
                if (!isEqualData(keyData, relatedNoteKeyData, musInterval.defaultValues, musInterval.relativesEqualityCheckers, intervalField)
                        || !isCorrectRelation(intervalIdx, relatedInterval)) {
                    Set<Map<String, String>> pointed = suspiciousRelatedNotesData.getOrDefault(relatedSoundField, new HashSet<Map<String, String>>());
                    pointed.add(relatedNoteData);
                    suspiciousRelatedNotesData.put(relatedSoundField, pointed);
                    suspicious = true;
                }
            } else {
                suspicious = true;
            }
        }
        return suspicious;
    }

    private boolean isCorrectRelation(int intervalIdx, String relatedInterval) {
        return isRelationPossible(intervalIdx) && relatedInterval.equalsIgnoreCase(getRelatedInterval(intervalIdx));
    }

    public int autoFill(Map<String, String> noteData, boolean updateReverse) throws AnkiDroidHelper.InvalidAnkiDatabaseException {
        final String intervalField = musInterval.modelFields.getOrDefault(MusInterval.Fields.INTERVAL, MusInterval.Fields.INTERVAL);
        final String interval = noteData.getOrDefault(intervalField, "");
        final int intervalIdx = MusInterval.Fields.Interval.getIndex(interval);

        final String unisonInterval = MusInterval.Fields.Interval.VALUE_UNISON;
        final String directionField = musInterval.modelFields.getOrDefault(MusInterval.Fields.DIRECTION, MusInterval.Fields.DIRECTION);
        final String direction = noteData.getOrDefault(directionField, "");

        final String relatedSoundFieldKey = getFieldKey();
        final String relatedSoundField = musInterval.modelFields.getOrDefault(relatedSoundFieldKey, relatedSoundFieldKey);
        String relatedSound = noteData.remove(relatedSoundField);

        final String reverseRelatedSoundFieldKey = getReverseFieldKey();
        final String reverseRelatedSoundField = musInterval.modelFields.getOrDefault(reverseRelatedSoundFieldKey, reverseRelatedSoundFieldKey);
        final String reverseRelatedSound = noteData.remove(reverseRelatedSoundField);

        final String soundField = musInterval.modelFields.getOrDefault(MusInterval.Fields.SOUND, MusInterval.Fields.SOUND);
        final String sound = noteData.remove(soundField);

        final String versionField = musInterval.modelFields.getOrDefault(MusInterval.Fields.VERSION, MusInterval.Fields.VERSION);
        final String version = noteData.remove(versionField);

        int updatedLinks = 0;
        if (isRelationPossible(intervalIdx)) {
            String relatedInterval = getRelatedInterval(intervalIdx);
            noteData.put(intervalField, relatedInterval);
            if (unisonInterval.equalsIgnoreCase(interval) || unisonInterval.equals(relatedInterval)) {
                noteData.put(directionField, "");
            }
            LinkedList<Map<String, String>> relatedNotesData = helper.findNotes(
                    musInterval.modelId,
                    noteData,
                    musInterval.defaultValues,
                    musInterval.relativesSearchExpressionMakers,
                    musInterval.equalityCheckers
            );

            Iterator<Map<String, String>> iterator = relatedNotesData.iterator();
            outer:
            while (iterator.hasNext()) {
                Map<String, String> relatedData = iterator.next();
                long relatedId = Long.parseLong(relatedData.get(AnkiDroidHelper.KEY_ID));
                for (Map.Entry<String, SearchExpressionMaker> relativesMakers :
                        MusInterval.Fields.RELATIVES_SEARCH_EXPRESSION_MAKERS.entrySet()) {
                    String fieldKey = relativesMakers.getKey();
                    String modelField = musInterval.modelFields.getOrDefault(fieldKey, fieldKey);
                    Validator[] validators = MusInterval.Fields.VALIDATORS.getOrDefault(fieldKey, new Validator[]{});
                    for (Validator validator : validators) {
                        boolean isValid;
                        if (validator instanceof FieldValidator) {
                            String value = relatedData.getOrDefault(modelField, "");
                            isValid = ((FieldValidator) validator).isValid(value);
                        } else if (validator instanceof NoteValidator) {
                            isValid = ((NoteValidator) validator).isValid(relatedData, musInterval.modelFields);
                            if (!isValid && validator instanceof FixableNoteValidator) {
                                isValid = ((FixableNoteValidator) validator).fix(
                                        musInterval.modelId,
                                        relatedId,
                                        relatedData,
                                        musInterval.modelFields,
                                        helper
                                );
                            }
                        } else {
                            throw new IllegalStateException();
                        }
                        if (!isValid) {
                            iterator.remove();
                            continue outer;
                        }
                    }
                }
            }

            if (relatedNotesData != null && relatedNotesData.size() >= 1) {
                if (updateReverse) { // @todo
                    for (Map<String, String> relatedData : relatedNotesData) {
                        long id = Long.parseLong(relatedData.get(AnkiDroidHelper.KEY_ID));
                        if (!relatedData.getOrDefault(relatedSoundField, "").equals(sound)) {
                            relatedData.put(reverseRelatedSoundField, sound);
                            helper.updateNote(musInterval.modelId, id, relatedData);
                            updatedLinks++;
                        }
                    }
                }

                int priorityCount = 0;
                while (relatedNotesData.size() > 1) {
                    RelativesPriorityComparator comparator = musInterval.relativesPriorityComparators[priorityCount];

                    String fieldKey = comparator.getFieldKey();
                    String modelField = musInterval.modelFields.getOrDefault(fieldKey, fieldKey);
                    String targetValue = noteData.getOrDefault(modelField, "");
                    comparator.setTargetValue(targetValue);

                    relatedNotesData.sort(comparator);
                    Collections.reverse(relatedNotesData);

                    Map<String, String> maxData = relatedNotesData.getFirst();
                    for (int i = relatedNotesData.size() - 1; i > 0; i--) {
                        Map<String, String> relatedData = relatedNotesData.get(i);
                        if (comparator.compare(maxData, relatedData) != 0) {
                            relatedNotesData.remove(i);
                        }
                    }

                    priorityCount++;
                }

                String newRelatedSound = relatedNotesData.getFirst().get(soundField);
                if (!relatedSound.equals(newRelatedSound)) {
                    relatedSound = newRelatedSound;
                    updatedLinks++;
                }
            }
        }

        noteData.put(intervalField, interval);
        noteData.put(directionField, direction);
        noteData.put(relatedSoundField, relatedSound);
        noteData.put(reverseRelatedSoundField, reverseRelatedSound);
        noteData.put(soundField, sound);
        noteData.put(versionField, version);
        return updatedLinks;
    }

    protected abstract String getFieldKey();

    protected abstract String getReverseFieldKey();

    protected abstract boolean isRelationPossible(int intervalIdx);

    protected abstract String getRelatedInterval(int intervalIdx);

    private Map<String, String> getIntervalIdentityData(Map<String, String> data) {
        return new HashMap<String, String>(data) {{
            remove(musInterval.modelFields.get(MusInterval.Fields.SOUND));
            remove(musInterval.modelFields.get(MusInterval.Fields.SOUND_SMALLER));
            remove(musInterval.modelFields.get(MusInterval.Fields.SOUND_LARGER));
            remove(musInterval.modelFields.get(MusInterval.Fields.VERSION));
            remove(AnkiDroidHelper.KEY_ID);
            remove(AnkiDroidHelper.KEY_TAGS);
        }};
    }

    private static boolean isEqualData(Map<String, String> data1, Map<String, String> data2,
                                       Map<String, String> modelFieldsDefaultValues,
                                       Map<String, EqualityChecker> modelFieldsEqualityCheckers, String intervalField) {
        Set<String> keySet1 = new HashSet<>(data1.keySet());
        keySet1.remove(intervalField);
        Set<String> keySet2 = new HashSet<>(data2.keySet());
        keySet2.remove(intervalField);
        if (!keySet1.equals(keySet2)) {
            return false;
        }
        for (String key : keySet1) {
            String defaultValue = modelFieldsDefaultValues.getOrDefault(key, "");
            String value1 = data1.getOrDefault(key, "");
            String value2 = data2.getOrDefault(key, "");
            boolean defaultEquality = !defaultValue.isEmpty() &&
                    ((value1.equalsIgnoreCase(defaultValue) && value2.isEmpty() || value1.isEmpty() && value2.equalsIgnoreCase(defaultValue))
                            || (value1.isEmpty() && value2.isEmpty()));
            EqualityChecker defaultEqualityChecker = new FieldEqualityChecker(key, AnkiDroidHelper.DEFAULT_EQUALITY_CHECKER);
            EqualityChecker equalityChecker = modelFieldsEqualityCheckers.getOrDefault(key, defaultEqualityChecker);
            if (!equalityChecker.areEqual(data1, data2) && !defaultEquality) {
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
    protected String getReverseFieldKey() {
        return MusInterval.Fields.SOUND_LARGER;
    }

    @Override
    protected boolean isRelationPossible(int intervalIdx) {
        return intervalIdx > 0;
    }

    @Override
    protected String getRelatedInterval(int intervalIdx) {
        return isRelationPossible(intervalIdx) ? MusInterval.Fields.Interval.VALUES[intervalIdx - 1] : null;
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
    protected String getReverseFieldKey() {
        return MusInterval.Fields.SOUND_SMALLER;
    }

    @Override
    protected boolean isRelationPossible(int intervalIdx) {
        return intervalIdx < MusInterval.Fields.Interval.VALUES.length - 1;
    }

    @Override
    protected String getRelatedInterval(int intervalIdx) {
        return isRelationPossible(intervalIdx) ? MusInterval.Fields.Interval.VALUES[intervalIdx + 1] : null;
    }
}