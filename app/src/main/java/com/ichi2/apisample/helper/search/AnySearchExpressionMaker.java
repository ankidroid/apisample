package com.ichi2.apisample.helper.search;

public class AnySearchExpressionMaker implements SearchExpressionMaker {
    @Override
    public String getExpression(String value) {
        return "%";
    }

    @Override
    public boolean isDefinitive() {
        return true;
    }
}
