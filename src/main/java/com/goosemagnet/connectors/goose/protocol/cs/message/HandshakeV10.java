package com.goosemagnet.connectors.goose.protocol.cs.message;

import com.goosemagnet.connectors.goose.protocol.cs.ByteParser;
import com.goosemagnet.connectors.goose.protocol.cs.Capabilities;
import com.goosemagnet.connectors.goose.protocol.cs.CharacterSet;
import com.goosemagnet.connectors.goose.protocol.cs.ServerStatus;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.SneakyThrows;
import lombok.Value;

import java.io.InputStream;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.goosemagnet.connectors.goose.protocol.cs.Types.Int1;
import com.goosemagnet.connectors.goose.protocol.cs.Types.Int3;
import com.goosemagnet.connectors.goose.protocol.cs.Types.Int4;
import com.goosemagnet.connectors.goose.protocol.cs.Types.NullTerminatedString;

@Value
@Builder(setterPrefix = "with")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class HandshakeV10 implements Message {
    Int1 protocolVersion;
    NullTerminatedString serverVersion;
    Int4 connectionId;
    byte[] salt;
    Set<Capabilities> serverCapabilities;
    CharacterSet characterSet;
    Set<ServerStatus> serverStatus;
    NullTerminatedString authPluginName;

    @SneakyThrows
    public static HandshakeV10 fromInputStream(InputStream is) {
        // Server Greeting
        Int3 packetLength = ByteParser.parseInt3(is);
        Int1 packetId = ByteParser.parseInt1(is);
        Int1 protocolVersion = ByteParser.parseInt1(is);
        NullTerminatedString serverVersion = ByteParser.nullTerminatedString(is);
        Int4 connectionId = ByteParser.parseInt4(is);
        byte[] salt1 = ByteParser.bytes(8, is);
        ByteParser.parseInt1(is);
        Set<Capabilities> capabilities1 = Capabilities.from(ByteParser.parseInt2(is).getValue());
        CharacterSet charSet = CharacterSet.from(ByteParser.parseInt1(is).getValue());
        Set<ServerStatus> serverStatus = ServerStatus.from(ByteParser.parseInt2(is).getValue());
        Set<Capabilities> capabilities2 = Capabilities.from(ByteParser.parseInt2(is).getValue() << 8);
        Int1 saltLength = ByteParser.parseInt1(is);
        ByteParser.bytes(10, is);
        byte[] salt2 = ByteParser.bytes(Math.max(13, (int) saltLength.getValue() - salt1.length), is);
        NullTerminatedString authPluginName = ByteParser.nullTerminatedString(is);

        if (is.available() > 0) {
            throw new RuntimeException("Error during handshake...");
        }

        byte[] salt = new byte[salt1.length + salt2.length - 1];
        System.arraycopy(salt1, 0, salt, 0, salt1.length);
        System.arraycopy(salt2, 0, salt, salt1.length, salt2.length - 1);

        Set<Capabilities> capabilities = Stream.concat(capabilities1.stream(), capabilities2.stream())
                .collect(Collectors.toSet());

        return HandshakeV10.builder()
                .withProtocolVersion(protocolVersion)
                .withServerVersion(serverVersion)
                .withConnectionId(connectionId)
                .withSalt(salt)
                .withServerCapabilities(capabilities)
                .withCharacterSet(charSet)
                .withServerStatus(serverStatus)
                .withAuthPluginName(authPluginName)
                .build();
    }
}
