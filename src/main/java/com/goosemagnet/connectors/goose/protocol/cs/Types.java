package com.goosemagnet.connectors.goose.protocol.cs;

import lombok.Value;

import java.math.BigInteger;

public interface Types {

    @Value
    class Int1 {
        long value;
    }

    @Value
    class Int2 {
        long value;
    }

    @Value
    class Int3 {
        long value;
    }

    @Value
    class Int4 {
        long value;
    }

    @Value
    class LengthEncodedInteger {
        BigInteger value;
    }

    @Value
    class NullTerminatedString {
        String value;
    }
}
