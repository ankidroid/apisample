package com.ichi2.apisample.helper.search;

public interface SearchExpressionMaker {
    String getExpression(String value);
    boolean isDefinitive();
}
