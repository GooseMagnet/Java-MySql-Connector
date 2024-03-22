package com.goosemagnet.connectors.goose;

import com.goosemagnet.connectors.MySqlConnector;
import lombok.SneakyThrows;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * https://mariadb.com/kb/en/0-packet/
 * https://mariadb.com/kb/en/connection/#initial-handshake-packet
 * https://dev.mysql.com/doc/dev/mysql-server/latest/page_protocol_connection_phase_packets_protocol_handshake_v10.html
 */
public class GooseMySqlConnector implements MySqlConnector {

    @Override
    public List<String> getSchemas() {
        try (Socket socket = new Socket("localhost", 3306);
             DataInputStream is = new DataInputStream(socket.getInputStream());
             OutputStream os = socket.getOutputStream()) {

            long payloadLength = payloadLength(is);
            long sequenceId = sequenceId(is);
            int protocolVersion = protocolVersion(is);
            String serverVersion = serverVersion(is);
            long connectionId = connectionId(is);
            String scramble = scramble(is);
            long filler = filler(is);
            long serverCapabilities1 = getFixedLengthInteger(2, is);

            HandshakePacket handshakePacket = HandshakePacket.builder()
                    .withProtocolVersion(protocolVersion)
                    .withServerVersion(serverVersion)
                    .withConnectionId(connectionId)
                    .withScramble(scramble)
                    .withFiller(filler)
                    .withServerCapabilities1(serverCapabilities1)
                    .build();

            System.out.println("Capabilities: " + Long.toString(serverCapabilities1, 2));

        } catch (UnknownHostException uhe) {
            System.err.println("Unknown host");
            throw new UncheckedIOException(uhe);
        } catch (IOException ioe) {
            System.err.println("Something else went wrong");
            throw new UncheckedIOException(ioe);
        }
        return List.of();
    }

    @SneakyThrows
    private int readByte(InputStream is) {
        int firstByte = is.read();
        return firstByte;
    }

    private long payloadLength(InputStream is) {
        long payloadLength = getFixedLengthInteger(3, is);
        System.out.printf("payload_length: %d%n", payloadLength);
        return payloadLength;
    }

    private long sequenceId(InputStream is) {
        long sequenceId = readByte(is);
        System.out.printf("sequence_id: %d%n", sequenceId);
        return sequenceId;
    }

    private int protocolVersion(InputStream is) {
        int protocolVersion = readByte(is);
        System.out.printf("protocol_version: %d%n", protocolVersion);
        return protocolVersion;
    }

    private String serverVersion(InputStream is) {
        String serverVersion = Stream.generate(() -> readByte(is))
                .takeWhile(i -> i != 0x0)
                .map(i -> (char) i.intValue())
                .map(String::valueOf)
                .collect(Collectors.joining());
        System.out.printf("server_version: %s%n", serverVersion);
        return serverVersion;
    }

    private long connectionId(InputStream is) {
        long connectionId = getFixedLengthInteger(3, is);
        System.out.printf("connection_id: %d%n", connectionId);
        return connectionId;
    }

    private String scramble(InputStream is) {
        String scramble = Stream.generate(() -> readByte(is))
                .limit(8)
                .map(i -> (char) i.intValue())
                .map(String::valueOf)
                .collect(Collectors.joining());
        System.out.printf("scramble: %s%n", scramble);
        return scramble;
    }

    private long filler(InputStream is) {
        return readByte(is);
    }

    private long getFixedLengthInteger(int length, InputStream is) {
        List<Integer> bytes = Stream.generate(() -> readByte(is)).limit(length).toList();
        long res = 0;
        for (int i = 0; i < length; ++i) {
            res |= (long) bytes.get(i) << (i * 8);
        }
        return res;
    }
}
