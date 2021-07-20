package com.ichi2.apisample.model;

import com.ichi2.apisample.R;
import com.ichi2.apisample.helper.AnkiDroidHelper;
import com.ichi2.apisample.helper.equality.NoteEqualityChecker;
import com.ichi2.apisample.validation.ValidationUtil;
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
    private final String duplicateTag;

    private final ProgressIndicator progressIndicator;

    private int notesCount;

    private int corruptedNotesCount;
    private final Map<String, Integer> corruptedFieldCounts = new HashMap<>();
    private int fixedCorruptedFieldsCount;

    private int suspiciousNotesCount;
    private final Map<String, Integer> suspiciousFieldCounts = new HashMap<>();
    private int fixedSuspiciousRelations;
    private int autoFilledRelationsCount;

    private int duplicateNotesCount;

    private final Map<String, Set<Map<String, String>>> fieldSuspiciousPointed = new HashMap<>();
    private final Map<String, Set<Map<String, String>>> fieldSuspiciousPointing = new HashMap<>();

    public NotesIntegrity(AnkiDroidHelper helper, MusInterval musInterval, String corruptedTag, String suspiciousTag, String duplicateTag, ProgressIndicator progressIndicator) {
        this.helper = helper;
        this.musInterval = musInterval;

        this.corruptedTag = corruptedTag;
        this.suspiciousTag = suspiciousTag;
        this.duplicateTag = duplicateTag;

        this.progressIndicator = progressIndicator;
    }

    public Summary check() throws AnkiDroidHelper.InvalidAnkiDatabaseException {
        final String soundField = musInterval.modelFields.get(MusInterval.Fields.SOUND);

        progressIndicator.setMessage(R.string.integrity_searching);
        LinkedList<Map<String, String>> searchResult = musInterval.getExistingNotes();
        notesCount = searchResult.size();

        ArrayList<Map<String, String>> correctNotesData = checkCorrectness(searchResult);
        corruptedNotesCount = searchResult.size() - correctNotesData.size();

        progressIndicator.setMessage(R.string.integrity_finding_duplicates);
        countDuplicates(correctNotesData);

        LinkedList<Map<String, String>> allNotesData = helper.findNotes(
                musInterval.modelId,
                new HashMap<String, String>(),
                musInterval.defaultValues,
                musInterval.searchExpressionMakers,
                musInterval.equalityCheckers
        );
        Map<String, Map<String, String>> soundDict = new HashMap<>();
        for (Map<String, String> noteData : allNotesData) {
            soundDict.put(noteData.getOrDefault(soundField, ""), noteData);
        }
        checkRelations(correctNotesData, soundDict);

        int correctNotesCount = correctNotesData.size();
        for (int i = 0; i < correctNotesCount; i++) {
            progressIndicator.setMessage(R.string.integrity_processing_relations, i, correctNotesCount);
            final Map<String, String> noteData = correctNotesData.get(i);
            long noteId = Long.parseLong((noteData.get(AnkiDroidHelper.KEY_ID)));
            String noteTags = noteData.get(AnkiDroidHelper.KEY_TAGS).toLowerCase();
            boolean suspicious = false;
            for (RelatedIntervalSoundField relatedSoundField : musInterval.relatedSoundFields) {
                String relationFieldKey = relatedSoundField.getFieldKey();
                boolean relationSuspicious = isRelationSuspicious(relationFieldKey, noteData, noteId, noteTags);
                String relationAltFieldKey = relatedSoundField.getAltFieldKey();
                boolean relationAltSuspicious = isRelationSuspicious(relationAltFieldKey, noteData, noteId, noteTags);
                suspicious |= relationSuspicious || relationAltSuspicious;
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

    private boolean isRelationSuspicious(String fieldKey, Map<String, String> noteData, long noteId, String noteTags) {
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
                noteData,
                suspiciousPointingTag,
                noteTags,
                fieldSuspiciousPointing.getOrDefault(fieldKey, new HashSet<Map<String, String>>())
        );
        if (suspiciousPointing) {
            int current = suspiciousFieldCounts.getOrDefault(fieldKey, 0);
            suspiciousFieldCounts.put(fieldKey, current + 1);
        }

        String suspiciousPointedTag = (
                suspiciousBaseTag
                        + AnkiDroidHelper.HIERARCHICAL_TAG_SEPARATOR
                        + RelatedIntervalSoundField.TAG_POINTED
        ).toLowerCase();
        boolean suspiciousPointed = processRelation(
                noteId,
                noteData,
                suspiciousPointedTag,
                noteTags,
                fieldSuspiciousPointed.getOrDefault(fieldKey, new HashSet<Map<String, String>>())
        );

        return suspiciousPointing || suspiciousPointed;
    }

    private ArrayList<Map<String, String>> checkCorrectness(LinkedList<Map<String, String>> notesData) {
        ArrayList<Map<String, String>> correctNotesData = new ArrayList<>();
        int notesCount = notesData.size();
        for (int i = 0; i < notesCount; i++) {
            progressIndicator.setMessage(R.string.integrity_validating, i, notesCount);
            final Map<String, String> noteData = notesData.get(i);

            long noteId = Long.parseLong(noteData.get(AnkiDroidHelper.KEY_ID));
            String noteTags = noteData.get(AnkiDroidHelper.KEY_TAGS).toLowerCase();

            boolean noteValid = true;
            for (Map.Entry<String, Validator[]> validators : MusInterval.Fields.VALIDATORS.entrySet()) {
                String fieldKey = validators.getKey();
                for (Validator validator : validators.getValue()) {
                    final String errorTag = (
                            corruptedTag
                                    + AnkiDroidHelper.HIERARCHICAL_TAG_SEPARATOR
                                    + fieldKey
                                    + AnkiDroidHelper.HIERARCHICAL_TAG_SEPARATOR
                                    + validator.getErrorTag()
                    ).toLowerCase();
                    final String errorTagCheckStr = String.format(" %s ", errorTag);
                    boolean hasErrorTag = noteTags.contains(errorTagCheckStr);

                    boolean isValid = ValidationUtil.isValid(
                            validator,
                            musInterval.modelId,
                            noteId,
                            noteData,
                            fieldKey,
                            musInterval.modelFields,
                            helper,
                            true
                    );

                    if (!isValid) {
                        int currentCount = corruptedFieldCounts.getOrDefault(fieldKey, 0);
                        corruptedFieldCounts.put(fieldKey, currentCount + 1);
                        if (!hasErrorTag) {
                            final String errorTagAddStr = String.format("%s", errorTag);
                            helper.addTagToNote(noteId, noteTags + errorTagAddStr);
                        }
                        noteValid = false;
                        break;
                    } else if (hasErrorTag) {
                        helper.updateNoteTags(noteId, noteTags.replace(errorTagCheckStr, " "));
                        fixedCorruptedFieldsCount++;
                    }
                }
            }

            if (noteValid) {
                correctNotesData.add(noteData);
            }
        }
        return correctNotesData;
    }

    private void countDuplicates(ArrayList<Map<String, String>> notesData) {
        Map<Map<String, String>, LinkedList<Map<String, String>>> keysDataNotes = new HashMap<>();
        for (final Map<String, String> noteData : notesData) {
            Map<String, String> keyData = new HashMap<String, String>(noteData) {{
                remove(musInterval.modelFields.get(MusInterval.Fields.SOUND));
                remove(musInterval.modelFields.get(MusInterval.Fields.SOUND_SMALLER));
                remove(musInterval.modelFields.get(MusInterval.Fields.SOUND_SMALLER_ALT));
                remove(musInterval.modelFields.get(MusInterval.Fields.SOUND_LARGER));
                remove(musInterval.modelFields.get(MusInterval.Fields.SOUND_LARGER_ALT));
                remove(musInterval.modelFields.get(MusInterval.Fields.VERSION));
                remove(AnkiDroidHelper.KEY_ID);
                remove(AnkiDroidHelper.KEY_TAGS);
            }};
            boolean match = false;
            outer:
            for (Map.Entry<Map<String, String>, LinkedList<Map<String, String>>> keyDataNotes : keysDataNotes.entrySet()) {
                Map<String, String> countedKeyData = keyDataNotes.getKey();
                LinkedList<Map<String, String>> countedNotesData = keyDataNotes.getValue();
                Map<String, String> countedData = countedNotesData.getFirst();
                for (final String key : countedKeyData.keySet()) {
                    if (!NoteEqualityChecker.areEqual(noteData, countedData, key,
                            musInterval.equalityCheckers, musInterval.defaultValues)) {
                        continue outer;
                    }
                }
                match = true;
                countedNotesData.add(noteData);
                break;
            }
            if (!match) {
                keysDataNotes.put(keyData, new LinkedList<Map<String, String>>() {{
                    add(noteData);
                }});
            }
        }

        final String duplicateTagCheckStr = String.format(" %s ", duplicateTag);
        for (Map.Entry<Map<String, String>, LinkedList<Map<String, String>>> keyDataNotes : keysDataNotes.entrySet()) {
            LinkedList<Map<String, String>> notes = keyDataNotes.getValue();
            int notesCount = notes.size();
            if (notesCount > 1) {
                duplicateNotesCount += notesCount;
                if (duplicateTag == null) {
                    continue;
                }
                for (Map<String, String> note : notes) {
                    String noteTags = note.get(AnkiDroidHelper.KEY_TAGS);
                    if (!noteTags.contains(duplicateTagCheckStr)) {
                        long noteId = Long.parseLong(note.get(AnkiDroidHelper.KEY_ID));
                        helper.addTagToNote(noteId, String.format("%s ", duplicateTag));
                    }
                }
            } else if (notesCount == 1 && corruptedTag != null) {
                Map<String, String> note = notes.getFirst();
                String noteTags = note.get(AnkiDroidHelper.KEY_TAGS);
                if (noteTags.contains(duplicateTagCheckStr)) {
                    long noteId = Long.parseLong(note.get(AnkiDroidHelper.KEY_ID));
                    helper.updateNoteTags(noteId, noteTags.replace(duplicateTagCheckStr, ""));
                }
            }
        }
    }

    private void checkRelations(ArrayList<Map<String, String>> correctNotesData, Map<String, Map<String, String>> soundDict) {
        int correctNotesCount = correctNotesData.size();
        for (int i = 0; i < correctNotesCount; i++) {
            Map<String, String> noteData = correctNotesData.get(i);
            progressIndicator.setMessage(R.string.integrity_verifying_relations, i, correctNotesCount);
            for (RelatedIntervalSoundField relatedSoundField : musInterval.relatedSoundFields) {
                if (relatedSoundField.isSuspicious(noteData, soundDict, fieldSuspiciousPointed)) {
                    addSuspiciousPointing(noteData, relatedSoundField.getFieldKey());
                }
                if (relatedSoundField.isAltSuspicious(noteData, soundDict, fieldSuspiciousPointed)) {
                    addSuspiciousPointing(noteData, relatedSoundField.getAltFieldKey());
                }
            }
        }
    }

    private void addSuspiciousPointing(Map<String, String> noteData, String fieldKey) {
        Set<Map<String, String>> fieldPointing = fieldSuspiciousPointing.getOrDefault(fieldKey, new HashSet<Map<String, String>>());
        fieldPointing.add(noteData);
        fieldSuspiciousPointing.put(fieldKey, fieldPointing);
    }

    private boolean processRelation(long noteId, Map<String, String> noteData, String tag, String noteTags, Set<Map<String, String>> suspiciousData) {
        final String tagCheckStr = String.format(" %s ", tag);
        boolean hasTag = noteTags.contains(tagCheckStr);
        if (!suspiciousData.contains(noteData)) {
            if (hasTag) {
                helper.updateNoteTags(noteId, noteTags.replace(tagCheckStr, " "));
                fixedSuspiciousRelations++;
            }
        } else {
            if (!hasTag) {
                helper.addTagToNote(noteId, String.format("%s ", tag));
            }
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

        public String getDuplicateTag() {
            return duplicateTag;
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

        public int getFixedSuspiciousRelationsCount() {
            return fixedSuspiciousRelations;
        }

        public int getAutoFilledRelationsCount() {
            return autoFilledRelationsCount;
        }

        public int getDuplicateNotesCount() {
            return duplicateNotesCount;
        }
    }
}
