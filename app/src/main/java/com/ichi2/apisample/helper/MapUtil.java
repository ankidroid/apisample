package com.ichi2.apisample.helper;

import java.util.Map;

public class MapUtil {
    public static <K, V> void putMissingKeys(Map<K, V> source, Map<K, V> target) {
        for (Map.Entry<K, V> entry : source.entrySet()) {
            K key = entry.getKey();
            if (!target.containsKey(key)) {
                target.put(key, entry.getValue());
            }
        }
    }
}
