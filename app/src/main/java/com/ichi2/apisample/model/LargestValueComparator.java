package com.ichi2.apisample.model;

import java.util.Map;

public class LargestValueComparator extends RelativesPriorityComparator {
    public LargestValueComparator(String fieldKey) {
        super(fieldKey);
    }

    @Override
    public int compare(Map<String, String> stringStringMap, Map<String, String> t1) {
        String modelField = modelFields.getOrDefault(fieldKey, fieldKey);
        String v1 = stringStringMap.getOrDefault(modelField, "").trim();
        String v2 = t1.getOrDefault(modelField, "").trim();
        boolean v1Empty = v1.isEmpty();
        boolean v2Empty = v2.isEmpty();
        if (v1Empty || v2Empty) {
            return v1Empty && v2Empty ? 0 :
                    v1Empty ? -1 : 1;
        }
        return Integer.compare(Integer.parseInt(v1), Integer.parseInt(v2));
    }
}
