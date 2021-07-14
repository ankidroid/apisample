package com.ichi2.apisample.helper;

import java.util.Map;
import java.util.Objects;

public class MapUtil {
    public static <K, V> void putMissingKeys(Map<K, V> source, Map<K, V> target) {
        for (Map.Entry<K, V> entry : source.entrySet()) {
            K key = entry.getKey();
            if (!target.containsKey(key)) {
                target.put(key, entry.getValue());
            }
        }
    }

    public static <K, V> K getKeyByValue(Map<K, V> map, V value) {
        for (Map.Entry<K, V> entry : map.entrySet()) {
            if (Objects.equals(value, entry.getValue())) {
                return entry.getKey();
            }
        }
        return null;
    }
}
