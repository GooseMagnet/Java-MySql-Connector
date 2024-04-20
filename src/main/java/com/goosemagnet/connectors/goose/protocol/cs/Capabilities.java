package com.goosemagnet.connectors.goose.protocol.cs;

import lombok.Getter;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
public enum Capabilities {
    CLIENT_LONG_PASSWORD(1),
    CLIENT_FOUND_ROWS(2),
    CLIENT_LONG_FLAG(4),
    CLIENT_CONNECT_WITH_DB(8),
    CLIENT_NO_SCHEMA(16),
    CLIENT_COMPRESS(32),
    CLIENT_ODBC(64),
    CLIENT_LOCAL_FILES(128),
    CLIENT_IGNORE_SPACE(256),
    CLIENT_PROTOCOL_41(512),
    CLIENT_INTERACTIVE(1024),
    CLIENT_SSL(2048),
    CLIENT_IGNORE_SIGPIPE(4096),
    CLIENT_TRANSACTIONS(8192),
    CLIENT_RESERVED(16384),
    CLIENT_RESERVED2(32768),
    CLIENT_MTI_STATEMENTS(1 << 16),
    CLIENT_MTI_RESTS(1 << 17),
    CLIENT_PS_MTI_RESTS(1 << 18),
    CLIENT_PLUGIN_AUTH(1 << 19),
    CLIENT_CONNECT_ATTRS(1 << 20),
    CLIENT_PLUGIN_AUTH_LENENC_CLIENT_DATA(1 << 21),
    CLIENT_CAN_HANDLE_EXPIRED_PASSWORDS(1 << 22),
    CLIENT_SESSION_TRACK(1 << 23),
    CLIENT_DEPRECATE_EOF(1 << 24),
    CLIENT_OPTIONAL_RESTSET_METADATA(1 << 25),
    CLIENT_ZSTD_COMPRESSION_ALGORITHM(1 << 26),
    CLIENT_QUERY_ATTRIBUTES(1 << 27),
    MULTI_FACTOR_AUTHENTICATION(1 << 28),
    CLIENT_CAPABILITY_EXTENSION(1 << 29),
    CLIENT_SSL_VERIFY_SERVER_CERT(1 << 30),
    CLIENT_REMEMBER_OPTIONS(1L << 31);

    private final long value;

    Capabilities(long value) {
        this.value = value;
    }

    public static Set<Capabilities> from(long value) {
        return Arrays.stream(values())
                .filter(capability -> (capability.value & value) > 0)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
