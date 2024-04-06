package com.goosemagnet.connectors.goose.protocol;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

import java.io.InputStream;
import java.util.stream.Stream;

import static com.goosemagnet.connectors.goose.protocol.ProtocolUtils.readByte;

public interface MySqlString {

    String toString();

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    class FixedLengthString implements MySqlString {
        byte[] bytes;

        private static FixedLengthString of(byte[] bytes) {
            return new FixedLengthString(bytes);
        }

        @Override
        public String toString() {
            return new String(bytes);
        }
    }

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    class NullTerminatedString implements MySqlString {
        byte[] bytes;

        private static NullTerminatedString of(byte[] bytes) {
            return new NullTerminatedString(bytes);
        }

        public String toString() {
            return new String(bytes);
        }
    }

    static FixedLengthString fixedLengthString(int length, InputStream is) {
        Byte[] boxedBytes = Stream.generate(() -> readByte(is)).limit(length).toArray(Byte[]::new);
        byte[] bytes = convertToPrimitive(boxedBytes);
        return FixedLengthString.of(bytes);
    }

    static NullTerminatedString nullTerminatedString(InputStream is) {
        Byte[] boxedBytes = Stream.generate(() -> readByte(is)).takeWhile(b -> b != 0x0).toArray(Byte[]::new);
        byte[] bytes = convertToPrimitive(boxedBytes);
        return NullTerminatedString.of(bytes);
    }

    @SuppressWarnings("all")
    private static byte[] convertToPrimitive(Byte[] boxedBytes) {
        byte[] bytes = new byte[boxedBytes.length];
        for (int i = 0; i < boxedBytes.length; ++i) {
            bytes[i] = boxedBytes[i];
        }
        return bytes;
    }
}
