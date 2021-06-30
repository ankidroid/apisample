package com.ichi2.apisample.helper.equality;

public class DoubleValueEqualityChecker implements ValueEqualityChecker {
    @Override
    public boolean areEqual(String v1, String v2) {
        try {
            return Double.parseDouble(v1.trim()) == Double.parseDouble(v2.trim());
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
