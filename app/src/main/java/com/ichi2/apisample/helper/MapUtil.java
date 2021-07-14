package com.ichi2.apisample.helper;

import java.util.Map;
import java.util.Objects;

public class MapUtil<K, V> {
    private final Map<K, V> map;

    public MapUtil(Map<K, V> map) {
        this.map = map;
    }

    public void putMissingKeys(Map<? extends K, ? extends V> source) {
        for (Map.Entry<? extends K, ? extends V> entry : source.entrySet()) {
            K key = entry.getKey();
            if (!map.containsKey(key)) {
                map.put(key, entry.getValue());
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
