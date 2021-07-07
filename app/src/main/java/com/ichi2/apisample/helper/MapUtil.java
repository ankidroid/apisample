package com.ichi2.apisample.helper;

import java.util.Map;

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
}
