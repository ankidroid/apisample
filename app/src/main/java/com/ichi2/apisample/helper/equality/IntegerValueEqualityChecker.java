package com.ichi2.apisample.helper.equality;

public class IntegerValueEqualityChecker implements ValueEqualityChecker {
    @Override
    public boolean areEqual(String v1, String v2) {
        try {
            return Integer.parseInt(v1.trim()) == Integer.parseInt(v2.trim());
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
