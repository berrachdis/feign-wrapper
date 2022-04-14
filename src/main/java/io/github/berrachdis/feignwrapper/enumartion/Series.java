package io.github.berrachdis.feignwrapper.enumartion;

import java.util.Arrays;

public enum Series {
    INFORMATIONAL(1, "INFORMATIONAL"),
    SUCCESSFUL(2, "SUCCESSFUL"),
    REDIRECTION(3, "REDIRECTION"),
    CLIENT_ERROR(4, "Client error occurred"),
    SERVER_ERROR(5, "Server error occurred");

    private final int value;
    private final String message;

    Series(int value, String message) {
        this.value = value;
        this.message = message;
    }

    public String message() { return this.message; }

    public static Series valueOf(int status) {
        int seriesCode = status / 100;
        return Arrays.stream(values())
                .filter(series -> series.value == seriesCode)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No matching constant for [" + status + "]"));
    }
}