package com.fuzzysound.walrus.common;

public class TestUtils {
    public static String getRandomHexString() {
        return Long.toHexString(Math.round(Math.random() * Math.pow(10, 10)));
    }
}
