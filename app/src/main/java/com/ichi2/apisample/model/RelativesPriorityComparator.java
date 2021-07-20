package com.ichi2.apisample.model;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.Map;

public abstract class RelativesPriorityComparator implements Comparator<Map<String, String>> {
    protected final String fieldKey;
    protected Map<String, String> modelFields;
    protected String targetValue;

    public RelativesPriorityComparator(String fieldKey) {
        this.fieldKey = fieldKey;
    }

    public void setModelFields(Map<String, String> modelFields) {
        this.modelFields = modelFields;
    }

    public void setTargetValueFromData(Map<String, String> targetData) {
        String modelField = modelFields.getOrDefault(fieldKey, fieldKey);
        targetValue = targetData.getOrDefault(modelField, "");
    }

    public LinkedList<Map<String, String>> getLeadingRelatives(LinkedList<Map<String, String>> relatedNotesData) {
        relatedNotesData = new LinkedList<>(relatedNotesData);
        if (relatedNotesData.isEmpty()) {
            return relatedNotesData;
        }
        relatedNotesData.sort(this);
        Collections.reverse(relatedNotesData);
        Map<String, String> maxData = relatedNotesData.getFirst();
        for (int j = relatedNotesData.size() - 1; j > 0; j--) {
            Map<String, String> relatedData = relatedNotesData.get(j);
            if (compare(maxData, relatedData) != 0) {
                relatedNotesData.remove(j);
            }
        }
        return relatedNotesData;
    }
}
