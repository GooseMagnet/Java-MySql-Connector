package com.goosemagnet.connectors.goose.protocol;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

import java.io.InputStream;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ProtocolUtils {

    @SneakyThrows
    public static byte readByte(InputStream is) {
        return (byte) is.read();
    }
}
