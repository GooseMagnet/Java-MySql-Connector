package com.goosemagnet.connectors.goose.protocol;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Value;

import java.io.InputStream;

import static com.goosemagnet.connectors.goose.protocol.IntegerTypes.int1;
import static com.goosemagnet.connectors.goose.protocol.IntegerTypes.int2;
import static com.goosemagnet.connectors.goose.protocol.IntegerTypes.int4;
import static com.goosemagnet.connectors.goose.protocol.MySqlString.NullTerminatedString;
import static com.goosemagnet.connectors.goose.protocol.MySqlString.FixedLengthString;
import static com.goosemagnet.connectors.goose.protocol.MySqlString.nullTerminatedString;
import static com.goosemagnet.connectors.goose.protocol.MySqlString.fixedLengthString;

@Value
@Builder(setterPrefix = "with", access = AccessLevel.PACKAGE)
public class HandshakePacket implements ConnectionLifecycle {

    private static final int LEN_AUTH_PLUGIN_DATA = 8;

    int1 protocolVersion;
    NullTerminatedString serverVersion;
    int4 connectionId;
    FixedLengthString authPluginDataPart1;
    int1 filler;
    int2 capabilityFlags1;
    int1 charSet;
    int2 statusFlags;
    int2 capabilityFlags2;
    int1 authPluginDataLen;
    FixedLengthString reserved;
    FixedLengthString authPluginDataPart2;
    NullTerminatedString authPluginName;

    static HandshakePacket create(InputStream is) {
        HandshakePacketBuilder builder = HandshakePacket.builder()
                .withProtocolVersion(int1(is))
                .withServerVersion(nullTerminatedString(is))
                .withConnectionId(int4(is))
                .withAuthPluginDataPart1(fixedLengthString(LEN_AUTH_PLUGIN_DATA, is))
                .withFiller(int1(is))
                .withCapabilityFlags1(int2(is))
                .withCharSet(int1(is))
                .withStatusFlags(int2(is))
                .withCapabilityFlags2(int2(is))
                .withAuthPluginDataLen(int1(is))
                .withReserved(fixedLengthString(10, is));

        builder.withAuthPluginDataPart2(fixedLengthString(Math.max(13, builder.authPluginDataLen.asInt() - 8), is))
                .withAuthPluginName(nullTerminatedString(is));

        return builder.build();
    }
}
