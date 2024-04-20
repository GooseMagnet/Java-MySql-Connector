package com.goosemagnet.connectors.goose;

import com.goosemagnet.connectors.MySqlConnector;
import com.goosemagnet.connectors.goose.protocol.cs.message.HandshakeV10;
import com.goosemagnet.connectors.goose.protocol.cs.message.LoginRequest;
import lombok.SneakyThrows;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * <a href="https://dev.mysql.com/doc/dev/mysql-server/latest/page_protocol_connection_phase_packets_protocol_handshake_v10.html">...</a>
 */
public class GooseMySqlConnector implements MySqlConnector {

    @Override
    public List<String> getSchemas() {
        try (Socket socket = new Socket("localhost", 3306);
             DataInputStream is = new DataInputStream(socket.getInputStream());
             OutputStream os = socket.getOutputStream()) {

            HandshakeV10 handshakeV10 = HandshakeV10.fromInputStream(is);
            LoginRequest loginRequest = LoginRequest.fromHandshake(handshakeV10, "root", "password");
            os.write(loginRequest.toByteArray());

            int payloadLength2 = (readByte(is) & 0xFF) | (readByte(is) << 8) | (readByte(is) << 16);
            int sequenceId2 = is.readByte();
            byte[] bytes = fixedLengthString(payloadLength2, is);

            String query = "SHOW SCHEMAS";
            byte[] array = ByteBuffer.allocate(1 + query.getBytes(StandardCharsets.UTF_8).length)
                    .put((byte) 0x03)
                    .put(query.getBytes(StandardCharsets.UTF_8))
                    .array();

            byte LL = (byte) ((array.length) & 0xFF);
            byte M = (byte) (array.length & 0x0000FF00);
            byte MM = (byte) (array.length & 0x00FF0000);
            byte[] queryBytes = ByteBuffer.allocate(4 + array.length)
                    .put(LL)
                    .put(M)
                    .put(MM)
                    .put((byte) 0)
                    .put(array)
                    .array();

            os.write(queryBytes);

            int columnCountPacketLength = getSizeOfPacketInBytes(is);
            byte columnPacketNumber = readByte(is);
            byte numFields = readByte(is);

            int fieldPacketLength = getSizeOfPacketInBytes(is);
            byte fieldPacketNumber = readByte(is);
            byte lengthOfCatalog = readByte(is);
            byte[] catalog = fixedLengthString(lengthOfCatalog, is);

            byte schemaLength = readByte(is);
            byte[] schema = fixedLengthString(schemaLength, is);

            byte tableLength = readByte(is);
            byte[] table = fixedLengthString(tableLength, is);

            byte ogTableLength = readByte(is);
            byte[] ogTable = fixedLengthString(ogTableLength, is);

            byte nameLength = readByte(is);
            byte[] name = fixedLengthString(nameLength, is);

            byte ogNameLength = readByte(is);
            byte[] ogName = fixedLengthString(ogNameLength, is);

            byte lengthOfFixedLengthFields = readByte(is);

            int characterSet = (0xFF & readByte(is)) | (readByte(is) << 8);
            int maxLengthOfField = (readByte(is) & 0xFF) | (readByte(is) << 8) | (readByte(is) << 16) | (readByte(is) << 24);

            byte typeOfColumn = readByte(is);
            int flags = (0xFF & readByte(is)) | (readByte(is) << 8);

            /*
            0x00 for integers and static strings
            0x1f for dynamic strings, double, float
            0x00 to 0x51 for decimals
             */
            byte decimals = readByte(is);
            fixedLengthString(2, is);

            eof(is);

            List<String> results = new ArrayList<>();
            while (is.available() > 9) {
                int sizeOfPacket = getSizeOfPacketInBytes(is);
                int packetNum = readByte(is);
                int recordLength = readByte(is);
                byte[] record = fixedLengthString(recordLength, is);
                String recordAsString = new String(record);
//                System.out.println("Found Row " + recordAsString);
                results.add(recordAsString);
            }

            eof(is);

            return results;

        } catch (UnknownHostException uhe) {
            // Unknown Host
        } catch (IOException ioe) {
            throw new UncheckedIOException(ioe);
        }

        return List.of();
    }

    @SneakyThrows
    private static byte readByte(DataInputStream is) {
        return is.readByte();
    }

    private static byte[] nullTerminatedString(DataInputStream is) {
        List<Byte> byteList = Stream.generate(() -> readByte(is))
                .takeWhile(aByte -> aByte != 0)
                .toList();
        byte[] bytes = new byte[byteList.size()];
        for (int i = 0; i < byteList.size(); ++i) {
            bytes[i] = byteList.get(i);
        }
        return bytes;
    }

    private static byte[] fixedLengthString(int length, DataInputStream is) {
        List<Byte> byteList = Stream.generate(() -> readByte(is)).limit(length).toList();
        byte[] bytes = new byte[byteList.size()];
        for (int i = 0; i < byteList.size(); ++i) {
            bytes[i] = byteList.get(i);
        }
        return bytes;
    }
    private static int getSizeOfPacketInBytes(DataInputStream is) {
        return (readByte(is) & 0xFF) | (readByte(is) << 8) | (readByte(is) << 16);
    }

    private static void eof(DataInputStream is) {
        // EOF_PACKET
        int eofSize = getSizeOfPacketInBytes(is);
        byte eofPacketNumber = readByte(is);
        byte header = readByte(is); // 0xFE
        int warnings = (readByte(is) & 0xFF) | (readByte(is) << 8);
        int eofStatusFlags = (readByte(is) & 0xFF) | (readByte(is) << 8);
    }
}



