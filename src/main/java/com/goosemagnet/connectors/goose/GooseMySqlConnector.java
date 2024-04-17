package com.goosemagnet.connectors.goose;

import com.goosemagnet.connectors.MySqlConnector;
import com.mysql.cj.exceptions.AssertionFailedException;
import lombok.SneakyThrows;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * <a href="https://dev.mysql.com/doc/dev/mysql-server/latest/page_protocol_connection_phase_packets_protocol_handshake_v10.html">...</a>
 */
public class GooseMySqlConnector implements MySqlConnector {

    private static final int CLIENT_PROTOCOL_41 = 1 << 9;
    private static final int CLIENT_PLUGIN_AUTH = 1 << 19;
    private static final int CACHING_SHA2_DIGEST_LENGTH = 32;

    @Override
    public List<String> getSchemas() {
        try (Socket socket = new Socket("localhost", 3306);
             DataInputStream is = new DataInputStream(socket.getInputStream());
             OutputStream os = socket.getOutputStream()) {

            // Server Greeting
            int payloadLength = (is.readByte() & 0xFF) | (is.readByte() << 8) | (is.readByte() << 16);
            int sequenceId = is.readByte();
            int protocolVersion = is.readByte();
            byte[] serverVersion = nullTerminatedString(is);
            int connectionId = (readByte(is) & 0xFF) | (readByte(is) << 8) | (readByte(is) << 16) | (readByte(is) << 24);
            byte[] salt1 = nullTerminatedString(is);
            int capabilities1 = (readByte(is) & 0xFF) | (readByte(is) << 8);
            byte charSet = readByte(is);
            int serverStatus = (readByte(is) & 0xFF) | (readByte(is) << 8);
            int capabilities2 = (readByte(is) & 0xFF) | (readByte(is) << 8);
            int saltLength = readByte(is);
            IntStream.range(0, 10).forEach(ignored -> readByte(is));
            byte[] salt2 = nullTerminatedString(is);
            byte[] authPluginName = nullTerminatedString(is);

            // Login Request
            int clientCapabilities = 1 | 2 | 4 | 1 << 15 | CLIENT_PROTOCOL_41 | CLIENT_PLUGIN_AUTH;
            int maxPacketSize = 16777215;
            byte[] username = "root".getBytes(StandardCharsets.UTF_8);

            byte[] salt = new byte[salt1.length + salt2.length];
            System.arraycopy(salt1, 0, salt, 0, salt1.length);
            System.arraycopy(salt2, 0, salt, salt1.length, salt2.length);
            byte[] encryptedPasswordBytes = scramble411("password".getBytes(StandardCharsets.UTF_8), salt);

            ByteBuffer bb = ByteBuffer.allocate(32 + username.length + 1 + 1 + encryptedPasswordBytes.length + authPluginName.length + 1)
                    .order(ByteOrder.LITTLE_ENDIAN)
                    .putInt(clientCapabilities)
                    .putInt(maxPacketSize)
                    .put(charSet)
                    .put(new byte[23])
                    .put(username)
                    .put((byte) 0)
                    .put((byte) encryptedPasswordBytes.length)
                    .put(encryptedPasswordBytes)
                    .put(authPluginName)
                    .put((byte) 0);

            byte[] loginRequest = bb.array();

            byte least = (byte) ((loginRequest.length) & 0xFF);
            byte mid = (byte) (loginRequest.length & 0x0000FF00);
            byte most = (byte) (loginRequest.length & 0x00FF0000);

            byte[] loginPacket = ByteBuffer.allocate(4 + loginRequest.length)
                    .put(least)
                    .put(mid)
                    .put(most)
                    .put((byte) 1)
                    .put(loginRequest)
                    .array();

            os.write(loginPacket);

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

    /**
     * Scrambling for caching_sha2_password plugin.
     *
     * <pre>
     * Scramble = XOR(SHA2(password), SHA2(SHA2(SHA2(password)), Nonce))
     * </pre>
     *
     * @param password password
     * @param seed     seed
     * @return bytes
     */
    @SneakyThrows
    public static byte[] scrambleCachingSha2(byte[] password, byte[] seed) {
        /*
         * Server does it in 4 steps (see sql/auth/sha2_password_common.cc Generate_scramble::scramble method):
         *
         * SHA2(src) => digest_stage1
         * SHA2(digest_stage1) => digest_stage2
         * SHA2(digest_stage2, m_rnd) => scramble_stage1
         * XOR(digest_stage1, scramble_stage1) => scramble
         */
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException ex) {
            throw new AssertionFailedException(ex);
        }

        byte[] dig1 = new byte[CACHING_SHA2_DIGEST_LENGTH];
        byte[] dig2 = new byte[CACHING_SHA2_DIGEST_LENGTH];
        byte[] scramble1 = new byte[CACHING_SHA2_DIGEST_LENGTH];

        // SHA2(src) => digest_stage1
        md.update(password, 0, password.length);
        md.digest(dig1, 0, CACHING_SHA2_DIGEST_LENGTH);
        md.reset();

        // SHA2(digest_stage1) => digest_stage2
        md.update(dig1, 0, dig1.length);
        md.digest(dig2, 0, CACHING_SHA2_DIGEST_LENGTH);
        md.reset();

        // SHA2(digest_stage2, m_rnd) => scramble_stage1
        md.update(dig2, 0, dig1.length);
        md.update(seed, 0, seed.length);
        md.digest(scramble1, 0, CACHING_SHA2_DIGEST_LENGTH);

        // XOR(digest_stage1, scramble_stage1) => scramble
        byte[] mysqlScrambleBuff = new byte[CACHING_SHA2_DIGEST_LENGTH];
        xorString(dig1, mysqlScrambleBuff, scramble1, CACHING_SHA2_DIGEST_LENGTH);

        return mysqlScrambleBuff;
    }

    /**
     * Encrypt/Decrypt function used for password encryption in authentication
     * <p>
     * Simple XOR is used here but it is OK as we encrypt random strings
     *
     * @param from     IN Data for encryption
     * @param to       OUT Encrypt data to the buffer (may be the same)
     * @param scramble IN Scramble used for encryption
     * @param length   IN Length of data to encrypt
     */
    public static void xorString(byte[] from, byte[] to, byte[] scramble, int length) {
        int pos = 0;
        int scrambleLength = scramble.length;

        while (pos < length) {
            to[pos] = (byte) (from[pos] ^ scramble[pos % scrambleLength]);
            pos++;
        }
    }

    /**
     * Hashing for MySQL-4.1 authentication. Algorithm is as follows (c.f. <i>sql/auth/password.c</i>):
     *
     * <pre>
     * SERVER: public_seed=create_random_string()
     * send(public_seed)
     *
     * CLIENT: recv(public_seed)
     * hash_stage1=sha1("password")
     * hash_stage2=sha1(hash_stage1)
     * reply=xor(hash_stage1, sha1(public_seed,hash_stage2))
     * send(reply)
     * </pre>
     *
     * @param password password
     * @param seed     seed
     * @return bytes
     */
    public static byte[] scramble411(byte[] password, byte[] seed) {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException ex) {
            throw new AssertionFailedException(ex);
        }

        byte[] passwordHashStage1 = md.digest(password);
        md.reset();

        byte[] passwordHashStage2 = md.digest(passwordHashStage1);
        md.reset();

        md.update(seed);
        md.update(passwordHashStage2);

        byte[] toBeXord = md.digest();

        int numToXor = toBeXord.length;

        for (int i = 0; i < numToXor; i++) {
            toBeXord[i] = (byte) (toBeXord[i] ^ passwordHashStage1[i]);
        }

        return toBeXord;
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



