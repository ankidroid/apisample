package com.ichi2.apisample.helper.equality;

public class AnyEqualityChecker implements ValueEqualityChecker {
    @Override
    public boolean areEqual(String v1, String v2) {
        return true;
    }
}
