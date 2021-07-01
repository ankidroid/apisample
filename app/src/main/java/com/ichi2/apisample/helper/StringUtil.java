package com.ichi2.apisample.helper;

public class StringUtil {
    public static String joinStrings(String separator, String[] values) {
        StringBuilder builder = new StringBuilder();
        for (String value : values) {
            if (builder.length() > 0) {
                builder.append(separator);
            }
            builder.append(value);
        }
        return builder.toString();
    }

    public static String[] splitStrings(String separator, String str) {
        return !str.isEmpty() ? str.split(separator) : new String[]{};
    }

    public static String strip(String str) {
        return str.replace('\n', ' ').trim().replaceAll(" +", " ");
    }
}
