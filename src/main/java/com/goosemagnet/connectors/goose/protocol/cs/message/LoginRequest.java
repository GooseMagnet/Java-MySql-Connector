package com.goosemagnet.connectors.goose.protocol.cs.message;

import com.goosemagnet.connectors.goose.protocol.cs.ByteArrayBuilder;
import com.goosemagnet.connectors.goose.protocol.cs.Capabilities;
import com.goosemagnet.connectors.goose.protocol.cs.CharacterSet;
import com.mysql.cj.exceptions.AssertionFailedException;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Value;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Set;

import com.goosemagnet.connectors.goose.protocol.cs.Types.Int1;
import com.goosemagnet.connectors.goose.protocol.cs.Types.Int3;
import com.goosemagnet.connectors.goose.protocol.cs.Types.Int4;
import com.goosemagnet.connectors.goose.protocol.cs.Types.NullTerminatedString;

@Value
@Builder(setterPrefix = "with")
public class LoginRequest implements Message {

    private static final Int1 MYSQL_NATIVE_PASSWORD_LENGTH = new Int1(32);
    private static final Int4 MAX_PACKET_SIZE = new Int4(16777215);
    private static final Set<Capabilities> CLIENT_CAPABILITIES = Set.of(
            Capabilities.CLIENT_LONG_PASSWORD,
            Capabilities.CLIENT_FOUND_ROWS,
            Capabilities.CLIENT_LONG_FLAG,
            Capabilities.CLIENT_PROTOCOL_41,
            Capabilities.CLIENT_RESERVED2,
            Capabilities.CLIENT_PLUGIN_AUTH
    );

    Set<Capabilities> clientCapabilities;
    Int4 maxPacketSize;
    CharacterSet characterSet;
    NullTerminatedString username;
    String password;
    NullTerminatedString authPluginName;

    @Getter(AccessLevel.PRIVATE)
    HandshakeV10 handshake;

    public static LoginRequest fromHandshake(HandshakeV10 handshakeV10, String username, String password) {
        return LoginRequest.builder()
                .withClientCapabilities(CLIENT_CAPABILITIES)
                .withMaxPacketSize(MAX_PACKET_SIZE)
                .withCharacterSet(handshakeV10.getCharacterSet())
                .withUsername(new NullTerminatedString(username))
                .withPassword(password)
                .withAuthPluginName(handshakeV10.getAuthPluginName())
                .withHandshake(handshakeV10)
                .build();
    }

    public byte[] toByteArray() {
        long capabilities = clientCapabilities.stream()
                .mapToLong(Capabilities::getValue)
                .reduce(0L, (l1, l2) -> l1 | l2);

        byte[] encodedPassword = scramble411(password.getBytes(StandardCharsets.UTF_8), handshake.getSalt());

        byte[] loginRequest = new ByteArrayBuilder()
                .putInt4(new Int4(capabilities))
                .putInt4(maxPacketSize)
                .putInt1(new Int1(characterSet.getNumValue()))
                .putBytes(new byte[23])
                .putNullTerminatedString(username)
                .putInt1(new Int1(encodedPassword.length))
                .putBytes(encodedPassword)
                .putNullTerminatedString(authPluginName)
                .toByteArray();

        return new ByteArrayBuilder()
                .putInt3(new Int3(loginRequest.length))
                .putInt1(new Int1(1))
                .putBytes(loginRequest)
                .toByteArray();
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
}
