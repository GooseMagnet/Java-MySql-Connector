package com.goosemagnet.connectors.goose.protocol.cs;

import lombok.AllArgsConstructor;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

@AllArgsConstructor
public enum ServerStatus {
    SERVER_STATUS_IN_TRANS(1),
    SERVER_STATUS_AUTOCOMMIT(2),
    SERVER_MORE_RESULTS_EXISTS(4),
    SERVER_QUERY_NO_GOOD_INDEX_USED(8),
    SERVER_QUERY_NO_INDEX_USED(16),
    SERVER_STATUS_CURSOR_EXISTS(32),
    SERVER_STATUS_LAST_ROW_SENT(64),
    SERVER_STATUS_DB_DROPPED(128),
    SERVER_STATUS_NO_BACKSLASH_ESCAPES(256),
    SERVER_STATUS_METADATA_CHANGED(512),
    SERVER_QUERY_WAS_SLOW(1024),
    SERVER_PS_OUT_PARAMS(2048),
    SERVER_STATUS_IN_TRANS_READONLY(4096),
    SERVER_SESSION_STATE_CHANGED(8192);

    private final int value;

    public static Set<ServerStatus> from(long value) {
        return Arrays.stream(values())
                .filter(capability -> (capability.value & value) > 0)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
