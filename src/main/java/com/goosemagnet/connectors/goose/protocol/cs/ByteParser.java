package com.goosemagnet.connectors.goose.protocol.cs;

import com.goosemagnet.connectors.goose.protocol.cs.Types.Int1;
import com.goosemagnet.connectors.goose.protocol.cs.Types.Int2;
import com.goosemagnet.connectors.goose.protocol.cs.Types.Int3;
import com.goosemagnet.connectors.goose.protocol.cs.Types.Int4;
import com.goosemagnet.connectors.goose.protocol.cs.Types.NullTerminatedString;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

import java.io.DataInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Stream;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ByteParser {

    public static Int1 parseInt1(InputStream is) {
        byte b = readByte(is);
        return new Int1(b & 0xFF);
    }

    public static Int2 parseInt2(InputStream is) {
        long value = fixedLengthInteger(2, is);
        return new Int2(value);
    }

    public static Int3 parseInt3(InputStream is) {
        long value = fixedLengthInteger(3, is);
        return new Int3(value);
    }

    public static Int4 parseInt4(InputStream is) {
        long value = fixedLengthInteger(4, is);
        return new Int4(value);
    }

    @SneakyThrows
    private static byte readByte(InputStream is) {
        return (byte) is.read();
    }

    private static long fixedLengthInteger(int length, InputStream is) {
        long res = 0;
        for (int i = 0; i < length; ++i) {
            byte cur = readByte(is);
            res |= ((long) cur << (i * 8));
        }
        return res;
    }

    public static NullTerminatedString nullTerminatedString(InputStream is) {
        List<Byte> byteList = Stream.generate(() -> readByte(is))
                .takeWhile(aByte -> aByte != 0)
                .toList();
        byte[] bytes = new byte[byteList.size()];
        for (int i = 0; i < bytes.length; ++i) {
            bytes[i] = byteList.get(i);
        }
        return new NullTerminatedString(new String(bytes));
    }

    public static byte[] bytes(int length, InputStream is) {
        byte[] bytes = new byte[length];
        for (int i = 0; i < length; ++i) {
            bytes[i] = readByte(is);
        }
        return bytes;
    }
}
