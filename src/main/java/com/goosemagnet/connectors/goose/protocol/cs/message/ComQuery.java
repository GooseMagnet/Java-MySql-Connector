package com.goosemagnet.connectors.goose.protocol.cs.message;

import com.goosemagnet.connectors.goose.protocol.cs.ByteArrayBuilder;
import lombok.Builder;
import lombok.Value;

import java.nio.charset.StandardCharsets;

import static com.goosemagnet.connectors.goose.protocol.cs.Types.Int1;
import static com.goosemagnet.connectors.goose.protocol.cs.Types.Int3;

@Value
@Builder(setterPrefix = "with")
public class ComQuery implements Message {

    private static final Int1 PACKET_NUMBER = new Int1(0);

    Int1 command = new Int1(0x03);
    String query;

    public static ComQuery fromQuery(String query) {
        return ComQuery.builder()
                .withQuery(query.trim())
                .build();
    }

    public byte[] toByteArray() {
        byte[] queryBytes = new ByteArrayBuilder()
                .putInt1(command)
                .putBytes(query.getBytes(StandardCharsets.UTF_8))
                .toByteArray();

        return new ByteArrayBuilder()
                .putInt3(new Int3(queryBytes.length))
                .putInt1(PACKET_NUMBER)
                .putBytes(queryBytes)
                .toByteArray();
    }
}
