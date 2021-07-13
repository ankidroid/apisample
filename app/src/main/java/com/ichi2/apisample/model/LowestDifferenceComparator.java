package com.ichi2.apisample.model;

import java.util.Map;

public class LowestDifferenceComparator extends RelativesPriorityComparator {
    public LowestDifferenceComparator(String fieldKey) {
        super(fieldKey);
    }

    @Override
    public int compare(Map<String, String> stringStringMap, Map<String, String> t1) {
        String modelField = modelFields.getOrDefault(fieldKey, fieldKey);
        String v1 = stringStringMap.getOrDefault(modelField, "").trim();
        String v2 = t1.getOrDefault(modelField, "").trim();
        boolean v1Empty = v1.isEmpty();
        boolean v2Empty = v2.isEmpty();
        if (v1Empty && v2Empty) {
            return 0;
        }
        String target = targetValue.trim();
        if (target.isEmpty()) {
            return v1Empty ? 1 :
                    v2Empty ? -1 :
                            Integer.compare(Integer.parseInt(v2), Integer.parseInt(v1));
        } else {
            int targetInt = Integer.parseInt(target);
            return v1Empty ? -1 :
                    v2Empty ? 1 :
                            Integer.compare(Math.abs(targetInt - Integer.parseInt(v2)), Math.abs(targetInt - Integer.parseInt(v1)));
        }
    }
}
