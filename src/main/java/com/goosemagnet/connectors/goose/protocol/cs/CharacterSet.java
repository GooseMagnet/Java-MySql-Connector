package com.goosemagnet.connectors.goose.protocol.cs;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum CharacterSet {
    UTF8MB4(255, "utf8mb4", "UTF-8");

    private final long numValue;
    private final String value;
    private final String javaEquivalent;

    public static CharacterSet from(long value) {
        return Arrays.stream(values())
                .filter(characterSet -> characterSet.numValue == value)
                .findAny()
                .orElseThrow(() -> new RuntimeException("Unknown Character Set [%d]".formatted(value)));
    }

    public static CharacterSet from(String value) {
        return Arrays.stream(values())
                .filter(characterSet -> characterSet.value.equalsIgnoreCase(value))
                .findAny()
                .orElseThrow(() -> new RuntimeException("Unknown Character Set [%s]".formatted(value)));
    }
}
