package com.goosemagnet.connectors.goose.protocol;

import lombok.*;

import java.io.InputStream;
import java.util.List;
import java.util.stream.Stream;

import static com.goosemagnet.connectors.goose.protocol.ProtocolUtils.readByte;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class IntegerTypes {

    private interface FixedLengthInteger {
        long getValue();

        default int asInt() {
            return (int) getValue();
        }
    }

    @Value
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    static class int1 implements FixedLengthInteger {
        long value;

        private static int1 of(long value) {
            return new int1(value);
        }

        public int asInt() {
            return (int) value;
        }
    }

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    static class int2 {
        long value;

        private static int2 of(long value) {
            return new int2(value);
        }
    }

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    static class int3 {
        long value;

        private static int3 of(long value) {
            return new int3(value);
        }
    }

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    static class int4 {
        long value;

        private static int4 of(long value) {
            return new int4(value);
        }
    }

    static class int6 {

    }

    static class int8 {

    }

    static int1 int1(InputStream is) {
        return int1.of(getFixedLengthInteger(1, is));
    }

    static int2 int2(InputStream is) {
        return int2.of(getFixedLengthInteger(2, is));
    }

    static int3 int3(InputStream is) {
        return int3.of(getFixedLengthInteger(3, is));
    }

    static int4 int4(InputStream is) {
        return int4.of(getFixedLengthInteger(4, is));
    }

    private static long getFixedLengthInteger(int length, InputStream is) {
        List<Byte> bytes = Stream.generate(() -> readByte(is)).limit(length).toList();
        long res = 0;
        for (int i = 0; i < length; ++i) {
            res |= (long) bytes.get(i) << (i * 8);
        }
        return res;
    }
}
