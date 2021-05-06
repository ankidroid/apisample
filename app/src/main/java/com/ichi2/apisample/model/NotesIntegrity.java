package com.ichi2.apisample.model;

import com.ichi2.apisample.helper.AnkiDroidHelper;
import com.ichi2.apisample.validation.Validator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

public class NotesIntegrity {
    private final AnkiDroidHelper helper;
    private final MusInterval musInterval;

    private final String corruptedTag;
    private final String suspiciousTag;

    private int notesCount;

    private int corruptedNotesCount;
    private final Map<String, Integer> corruptedFieldCounts = new HashMap<>();
    private int fixedCorruptedFieldsCount;

    private int suspiciousNotesCount;
    private final Map<String, Integer> suspiciousFieldCounts = new HashMap<>();
    private int fixedSuspiciousFieldsCount;
    private int autoFilledRelationsCount;

    private int duplicatesCount;

    private final Map<String, Set<Map<String, String>>> fieldSuspiciousPointed = new HashMap<>();
    private final Map<String, Set<Map<String, String>>> fieldSuspiciousPointing = new HashMap<>();

    public NotesIntegrity(AnkiDroidHelper helper, MusInterval musInterval, String corruptedTag, String suspiciousTag) {
        this.helper = helper;
        this.musInterval = musInterval;

        this.corruptedTag = corruptedTag;
        this.suspiciousTag = suspiciousTag;
    }

    public Summary check() throws AnkiDroidHelper.InvalidAnkiDatabaseException {
        final String soundField = musInterval.modelFields.get(MusInterval.Fields.SOUND);

        LinkedList<Map<String, String>> allNotesData = helper.findNotes(musInterval.modelId, new HashMap<String, String>());
        Map<String, Map<String, String>> soundDict = new HashMap<>();
        for (Map<String, String> noteData : allNotesData) {
            soundDict.put(noteData.getOrDefault(soundField, ""), noteData);
        }

        LinkedList<Map<String, String>> searchResult = musInterval.getExistingNotes();
        notesCount = searchResult.size();

        ArrayList<Map<String, String>> correctNotesData = checkCorrectness(searchResult);
        corruptedNotesCount = searchResult.size() - correctNotesData.size();

        countDuplicates(correctNotesData);
        checkRelations(correctNotesData, soundDict);

        for (Map<String, String> noteData : correctNotesData) {
            long noteId = Long.parseLong((noteData.get(AnkiDroidHelper.KEY_ID)));
            String noteTags = noteData.get(AnkiDroidHelper.KEY_TAGS).toLowerCase();
            boolean suspicious = false;
            for (RelatedIntervalSoundField relatedSoundField : musInterval.relatedSoundFields) {
                String fieldKey = relatedSoundField.getFieldKey();
                final String suspiciousBaseTag =
                        suspiciousTag
                                + AnkiDroidHelper.HIERARCHICAL_TAG_SEPARATOR
                                + fieldKey;

                String suspiciousPointingTag = (
                        suspiciousBaseTag
                                + AnkiDroidHelper.HIERARCHICAL_TAG_SEPARATOR
                                + RelatedIntervalSoundField.TAG_POINTING
                ).toLowerCase();
                boolean suspiciousPointing = processRelation(
                        noteId,
                        fieldKey, noteData,
                        suspiciousPointingTag,
                        noteTags,
                        fieldSuspiciousPointing.getOrDefault(fieldKey, new HashSet<Map<String, String>>())
                );

                String suspiciousPointedTag = (
                        suspiciousBaseTag
                                + AnkiDroidHelper.HIERARCHICAL_TAG_SEPARATOR
                                + RelatedIntervalSoundField.TAG_POINTED
                ).toLowerCase();
                boolean suspiciousPointed = processRelation(
                        noteId,
                        fieldKey, noteData,
                        suspiciousPointedTag,
                        noteTags,
                        fieldSuspiciousPointed.getOrDefault(fieldKey, new HashSet<Map<String, String>>())
                );

                suspicious = suspicious || suspiciousPointing || suspiciousPointed;
            }

            if (!suspicious) {
                Map<String, String> noteFieldsData = new HashMap<String, String>(noteData) {{
                    remove(AnkiDroidHelper.KEY_ID);
                    remove(AnkiDroidHelper.KEY_TAGS);
                }};
                int autoFilledFields = 0;
                for (RelatedIntervalSoundField relatedIntervalSoundField : musInterval.relatedSoundFields) {
                    autoFilledFields += relatedIntervalSoundField.autoFill(noteFieldsData, false);
                }
                if (autoFilledFields > 0) {
                    helper.updateNote(musInterval.modelId, noteId, noteFieldsData);
                    autoFilledRelationsCount += autoFilledFields;
                }
            } else {
                suspiciousNotesCount++;
            }
        }

        return new Summary();
    }

    private ArrayList<Map<String, String>> checkCorrectness(LinkedList<Map<String, String>> notesData) {
        ArrayList<Map<String, String>> correctNotesData = new ArrayList<>();
        for (final Map<String, String> noteData : notesData) {
            long noteId = Long.parseLong(noteData.get(AnkiDroidHelper.KEY_ID));
            String noteTags = noteData.get(AnkiDroidHelper.KEY_TAGS).toLowerCase();

            boolean valid = true;
            for (Map.Entry<String, Validator[]> fieldValidators : MusInterval.Fields.VALIDATORS.entrySet()) {
                String fieldKey = fieldValidators.getKey();
                String value = noteData.getOrDefault(musInterval.modelFields.getOrDefault(fieldKey, fieldKey), "");
                for (Validator validator : fieldValidators.getValue()) {
                    final String errorTag = (
                            corruptedTag
                                    + AnkiDroidHelper.HIERARCHICAL_TAG_SEPARATOR
                                    + fieldKey
                                    + AnkiDroidHelper.HIERARCHICAL_TAG_SEPARATOR
                                    + validator.getErrorTag()
                    ).toLowerCase();
                    final String errorTagCheckStr = String.format(" %s ", errorTag);
                    boolean hasErrorTag = noteTags.contains(errorTagCheckStr);
                    if (!validator.isValid(value)) {
                        int currentCount = corruptedFieldCounts.getOrDefault(fieldKey, 0);
                        corruptedFieldCounts.put(fieldKey, currentCount + 1);
                        if (!hasErrorTag) {
                            final String errorTagAddStr = String.format("%s", errorTag);
                            helper.addTagToNote(noteId, noteTags + errorTagAddStr);
                        }
                        valid = false;
                        break;
                    } else if (hasErrorTag) {
                        helper.updateNoteTags(noteId, noteTags.replace(errorTagCheckStr, " "));
                        fixedCorruptedFieldsCount++;
                    }
                }
            }

            if (valid) {
                correctNotesData.add(noteData);
            }
        }
        return correctNotesData;
    }

    private void countDuplicates(ArrayList<Map<String, String>> notesData) {
        Map<Map<String, String>, ArrayList<Long>> keysDataNoteIds = new HashMap<>();
        for (Map<String, String> noteData : notesData) {
            long id = Long.parseLong(noteData.get(AnkiDroidHelper.KEY_ID));
            Map<String, String> keyData = new HashMap<String, String>(noteData) {{
                remove(musInterval.modelFields.get(MusInterval.Fields.SOUND));
                remove(musInterval.modelFields.get(MusInterval.Fields.SOUND_SMALLER));
                remove(musInterval.modelFields.get(MusInterval.Fields.SOUND_LARGER));
                remove(musInterval.modelFields.get(MusInterval.Fields.VERSION));
                remove(AnkiDroidHelper.KEY_ID);
                remove(AnkiDroidHelper.KEY_TAGS);
            }};
            ArrayList<Long> current = keysDataNoteIds.getOrDefault(keyData, new ArrayList<Long>());
            current.add(id);
            keysDataNoteIds.put(keyData, current);
        }

        for (Map.Entry<Map<String, String>, ArrayList<Long>> keyDataNoteIds : keysDataNoteIds.entrySet()) {
            int noteIdsCount = keyDataNoteIds.getValue().size();
            if (noteIdsCount > 1) {
                duplicatesCount += noteIdsCount;
            }
        }
    }

    private void checkRelations(ArrayList<Map<String, String>> correctNotesData, Map<String, Map<String, String>> soundDict) {
        for (Map<String, String> noteData : correctNotesData) {
            for (RelatedIntervalSoundField relatedSoundField : musInterval.relatedSoundFields) {
                if (relatedSoundField.isSuspicious(noteData, soundDict, fieldSuspiciousPointed)) {
                    final String fieldKey = relatedSoundField.getFieldKey();
                    Set<Map<String, String>> fieldPointing = fieldSuspiciousPointing.getOrDefault(fieldKey, new HashSet<Map<String, String>>());
                    fieldPointing.add(noteData);
                    fieldSuspiciousPointing.put(fieldKey, fieldPointing);
                }
            }
        }
    }

    private boolean processRelation(long noteId, String fieldKey, Map<String, String> noteData, String tag, String noteTags, Set<Map<String, String>> suspiciousData) {
        final String tagCheckStr = String.format(" %s ", tag);
        boolean hasTag = noteTags.contains(tagCheckStr);
        if (!suspiciousData.contains(noteData)) {
            if (hasTag) {
                helper.updateNoteTags(noteId, noteTags.replace(tagCheckStr, " "));
                fixedSuspiciousFieldsCount++;
            }
        } else {
            if (!hasTag) {
                helper.addTagToNote(noteId, String.format("%s ", tag));
            }
            int cur = suspiciousFieldCounts.getOrDefault(fieldKey, 0);
            suspiciousFieldCounts.put(fieldKey, cur + 1);
            return true;
        }
        return false;
    }

    public class Summary {
        public MusInterval getMusInterval() {
            return musInterval;
        }

        public String getCorruptedTag() {
            return corruptedTag;
        }

        public String getSuspiciousTag() {
            return suspiciousTag;
        }

        public int getNotesCount() {
            return notesCount;
        }

        public int getCorruptedNotesCount() {
            return corruptedNotesCount;
        }

        public Map<String, Integer> getCorruptedFieldCounts() {
            return corruptedFieldCounts;
        }

        public int getFixedCorruptedFieldsCount() {
            return fixedCorruptedFieldsCount;
        }

        public int getSuspiciousNotesCount() {
            return suspiciousNotesCount;
        }

        public Map<String, Integer> getSuspiciousFieldCounts() {
            return suspiciousFieldCounts;
        }

        public int getFixedSuspiciousFieldsCount() {
            return fixedSuspiciousFieldsCount;
        }

        public int getAutoFilledRelationsCount() {
            return autoFilledRelationsCount;
        }

        public int getDuplicatesCount() {
            return duplicatesCount;
        }
    }
}
