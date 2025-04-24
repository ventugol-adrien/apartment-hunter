package org.example;

public enum PublishedInterval {
    LAST_24_HOURS("now-1d/d"),
    LAST_3_DAYS("now-3d/d"),
    LAST_7_DAYS("now-7d/d"),
    LAST_14_DAYS("now-14d/d"),
    LAST_30_DAYS("now-30d/d");

    private final String value;

    PublishedInterval(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}

