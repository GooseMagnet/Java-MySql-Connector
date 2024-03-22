package com.goosemagnet.connectors.goose;

import lombok.Builder;
import lombok.Value;

@Value
@Builder(setterPrefix = "with")
class HandshakePacket {
    long protocolVersion;
    String serverVersion;
    long connectionId;
    String scramble;
    long filler;
    long serverCapabilities1;
    long serverCapabilities2;
    long serverCapabilities3;
}
