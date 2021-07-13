package com.ichi2.apisample.helper.equality;

public class IntegerValueEqualityChecker implements ValueEqualityChecker {
    @Override
    public boolean areEqual(String v1, String v2) {
        v1 = v1.trim();
        v2 = v2.trim();
        if (v1.isEmpty() && v2.isEmpty()) {
            return true;
        }
        try {
            return Integer.parseInt(v1) == Integer.parseInt(v2);
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
