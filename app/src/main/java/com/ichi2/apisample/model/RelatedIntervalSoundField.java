package com.ichi2.apisample.model;

import com.ichi2.apisample.helper.AnkiDroidHelper;
import com.ichi2.apisample.helper.equality.EqualityChecker;

import java.util.HashMap;
import java.util.HashSet;
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
                if (!isEqualData(keyData, relatedNoteKeyData, musInterval.modelFieldsDefaultValues, musInterval.modelFieldsEqualityCheckers)
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
            noteData.put(intervalField, getRelatedInterval(intervalIdx));
            LinkedList<Map<String, String>> relatedNotesData = helper.findNotes(
                    musInterval.modelId,
                    noteData,
                    musInterval.modelFieldsDefaultValues,
                    musInterval.modelFieldsSearchExpressionMakers,
                    musInterval.modelFieldsEqualityCheckers
            );
            if (relatedNotesData != null && relatedNotesData.size() >= 1) {
                int maxIdIdx = 0;
                long maxId = -1;
                for (int i = 0; i < relatedNotesData.size(); i++) {
                    Map<String, String> relatedData = relatedNotesData.get(i);
                    long id = Long.parseLong(relatedData.get("id"));
                    if (id > maxId) {
                        maxId = id;
                        maxIdIdx = i;
                    }
                    if (updateReverse && !relatedData.getOrDefault(relatedSoundField, "").equals(sound)) {
                        relatedData.put(reverseRelatedSoundField, sound);
                        helper.updateNote(musInterval.modelId, id, relatedData);
                        updatedLinks++;
                    }
                }
                String newRelatedSound = relatedNotesData.get(maxIdIdx).get(soundField);
                if (!relatedSound.equals(newRelatedSound)) {
                    relatedSound = newRelatedSound;
                    updatedLinks++;
                }
            }
        }

        noteData.put(intervalField, interval);
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
            remove(musInterval.modelFields.get(MusInterval.Fields.INTERVAL));
            remove(musInterval.modelFields.get(MusInterval.Fields.VERSION));
            remove(AnkiDroidHelper.KEY_ID);
            remove(AnkiDroidHelper.KEY_TAGS);
        }};
    }

    private static boolean isEqualData(Map<String, String> data1, Map<String, String> data2,
                                       Map<String, String> modelFieldsDefaultValues,
                                       Map<String, EqualityChecker> modelFieldsEqualityCheckers) {
        Set<String> keySet = data1.keySet();
        if (!keySet.equals(data2.keySet())) {
            return false;
        }
        for (String key : keySet) {
            String defaultValue = modelFieldsDefaultValues.getOrDefault(key, "");
            String value1 = data1.getOrDefault(key, "");
            String value2 = data2.getOrDefault(key, "");
            boolean defaultEquality = !defaultValue.isEmpty() &&
                    ((value1.equalsIgnoreCase(defaultValue) && value2.isEmpty() || value1.isEmpty() && value2.equalsIgnoreCase(defaultValue))
                            || (value1.isEmpty() && value2.isEmpty()));
            EqualityChecker equalityChecker = modelFieldsEqualityCheckers.getOrDefault(key, AnkiDroidHelper.DEFAULT_EQUALITY_CHECKER);
            if (!equalityChecker.areEqual(value1, value2) && !defaultEquality) {
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