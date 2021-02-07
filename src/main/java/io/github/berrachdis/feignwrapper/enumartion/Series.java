package io.github.berrachdis.feignwrapper.enumartion;

import java.util.Arrays;

public enum Series {
    INFORMATIONAL(1),
    SUCCESSFUL(2),
    REDIRECTION(3),
    CLIENT_ERROR(4),
    SERVER_ERROR(5);

    private final int value;

    Series(int value) {
        this.value = value;
    }

    public int value() {
        return this.value;
    }

    public static Series valueOf(int status) {
        int seriesCode = status / 100;
        return Arrays.stream(values())
                .filter(series -> series.value == seriesCode)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No matching constant for [" + status + "]"));
    }
}