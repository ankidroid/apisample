package com.ichi2.apisample.helper.equality;

import java.util.Map;

public interface EqualityChecker {
    boolean areEqual(Map<String, String> data1, Map<String, String> data2);
}
