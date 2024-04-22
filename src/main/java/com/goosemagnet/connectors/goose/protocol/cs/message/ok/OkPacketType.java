package com.goosemagnet.connectors.goose.protocol.cs.message.ok;

import com.goosemagnet.connectors.goose.protocol.cs.Types;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum OkPacketType {
    OK(new Types.Int1(0x00)),
    EOF(new Types.Int1(0xFE));

    private final Types.Int1 headerBytes;

    public static OkPacketType fromByte(byte value) {
        return fromInt1(new Types.Int1(value));
    }

    public static OkPacketType fromInt1(Types.Int1 value) {
        return Arrays.stream(values())
                .filter(okPacketType -> okPacketType.getHeaderBytes().equals(value))
                .findAny()
                .orElseThrow(() -> new RuntimeException("Unknown header type: " + value));
    }
}
