package com.goosemagnet.connectors.goose.protocol;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.io.InputStream;

import static com.goosemagnet.connectors.goose.protocol.IntegerTypes.int1;
import static com.goosemagnet.connectors.goose.protocol.IntegerTypes.int3;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MysqlPacket<T extends ConnectionLifecycle> {
    int3 payloadLength;
    int1 sequenceId;
    T payload;

    public static MysqlPacket<HandshakePacket> handshake(InputStream is) {
        int3 payloadLength = int3(is);
        int1 sequenceId = int1(is);
        HandshakePacket handshakePacket = HandshakePacket.create(is);
        return new MysqlPacket<>(payloadLength, sequenceId, handshakePacket);
    }
}
