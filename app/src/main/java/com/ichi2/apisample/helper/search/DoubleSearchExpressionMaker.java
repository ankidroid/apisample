package com.ichi2.apisample.helper.search;

import java.util.Locale;

public class DoubleSearchExpressionMaker implements SearchExpressionMaker {
    @Override
    public String getExpression(String value) {
        value = value.trim();
        if (value.isEmpty()) {
            return "%";
        }
        double number;
        try {
            number = Double.parseDouble(value);
            if (number % 1 == 0) {
                return String.format(Locale.US, "%%%d%%", (int) number);
            } else {
                return String.format(Locale.US, "%%%s%%", number);
            }
        } catch (NumberFormatException e) {
            return value;
        }
    }

    @Override
    public boolean isDefinitive() {
        return false;
    }
}
