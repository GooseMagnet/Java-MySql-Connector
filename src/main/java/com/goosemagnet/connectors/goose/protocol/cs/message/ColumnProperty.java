package com.goosemagnet.connectors.goose.protocol.cs.message;

import lombok.AllArgsConstructor;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

@AllArgsConstructor
public enum ColumnProperty {
    NOT_NULL_FLAG(1),
    PRI_KEY_FLAG(2),
    UNIQUE_KEY_FLAG(4),
    MULTIPLE_KEY_FLAG(8),
    BLOB_FLAG(16),
    UNSIGNED_FLAG(32),
    ZEROFILL_FLAG(64),
    BINARY_FLAG(128),
    ENUM_FLAG(256),
    AUTO_INCREMENT_FLAG(512),
    TIMESTAMP_FLAG(1024),
    SET_FLAG(2048),
    NO_DEFAULT_VALUE_FLAG(4096),
    ON_UPDATE_NOW_FLAG(8192),
    NUM_FLAG(32768),
    PART_KEY_FLAG(16384),
    GROUP_FLAG(32768),
    UNIQUE_FLAG(65536),
    BINCMP_FLAG(131072),
    GET_FIXED_FIELDS_FLAG(1 << 18),
    FIELD_IN_PART_FUNC_FLAG(1 << 19),
    FIELD_IN_ADD_INDEX(1 << 20),
    FIELD_IS_RENAMED(1 << 21),
    FIELD_FLAGS_STORAGE_MEDIA(1 << 22),
    FIELD_FLAGS_STORAGE_MEDIA_MASK(3 << FIELD_FLAGS_STORAGE_MEDIA.value),
    FIELD_FLAGS_COLUMN_FORMAT(24),
    FIELD_FLAGS_COLUMN_FORMAT_MASK(3 << FIELD_FLAGS_COLUMN_FORMAT.value),
    FIELD_IS_DROPPED(1 << 26),
    EXPLICIT_NULL_FLAG(1 << 27),
    NOT_SECONDARY_FLAG(1 << 29),
    FIELD_IS_INVISIBLE(1 << 30);

    private final long value;

    public static Set<ColumnProperty> from(long value) {
        return Arrays.stream(values())
                .filter(flag -> (flag.value & value) > 0)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
