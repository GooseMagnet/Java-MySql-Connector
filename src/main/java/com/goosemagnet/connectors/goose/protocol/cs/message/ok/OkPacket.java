package com.goosemagnet.connectors.goose.protocol.cs.message.ok;

import com.goosemagnet.connectors.goose.protocol.cs.ByteParser;
import com.goosemagnet.connectors.goose.protocol.cs.ServerStatus;
import com.goosemagnet.connectors.goose.protocol.cs.Types;
import com.goosemagnet.connectors.goose.protocol.cs.message.Message;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.SneakyThrows;
import lombok.Value;

import java.io.InputStream;
import java.util.Set;

public sealed interface OkPacket extends Message permits OkPacket.OkPacketImpl, OkPacket.EofPacketImpl {

    OkPacketType getHeader();

    @SneakyThrows
    static OkPacket fromInputStream(InputStream is) {
        Types.Int3 packetLength = ByteParser.parseInt3(is);
        Types.Int1 packetId = ByteParser.parseInt1(is);

        OkPacketType type = OkPacketType.fromInt1(ByteParser.parseInt1(is));

        return switch (type) {
            case OK -> parseOkPacket(is);
            case EOF -> parseEofPacket(is);
        };
    }

    private static OkPacketImpl parseOkPacket(InputStream is) {
        return OkPacketImpl.builder()
                .withHeader(OkPacketType.OK)
                .withAffectedRows(ByteParser.parseLengthEncodedInteger(is))
                .withLastInsertId(ByteParser.parseLengthEncodedInteger(is))
                .withServerStatus(ServerStatus.from(ByteParser.parseInt2(is).getValue()))
                .withWarnings(ByteParser.parseInt2(is))
                .build();
    }

    private static EofPacketImpl parseEofPacket(InputStream is) {
        return EofPacketImpl.builder()
                .withHeader(OkPacketType.EOF)
                .withWarnings(ByteParser.parseInt2(is))
                .withServerStatus(ServerStatus.from(ByteParser.parseInt2(is).getValue()))
                .build();
    }

    @Value
    @Builder(setterPrefix = "with")
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    class OkPacketImpl implements OkPacket {
        OkPacketType header;
        Types.LengthEncodedInteger affectedRows;
        Types.LengthEncodedInteger lastInsertId;
        Set<ServerStatus> serverStatus;
        Types.Int2 warnings;
        String info;
        String sessionStateInfo;
    }

    @Value
    @Builder(setterPrefix = "with")
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    class EofPacketImpl implements OkPacket {
        OkPacketType header;
        Types.Int2 warnings;
        Set<ServerStatus> serverStatus;
    }
}
