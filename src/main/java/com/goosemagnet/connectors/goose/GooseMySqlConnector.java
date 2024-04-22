package com.goosemagnet.connectors.goose;

import com.goosemagnet.connectors.MySqlConnector;
import com.goosemagnet.connectors.goose.protocol.cs.message.ComQuery;
import com.goosemagnet.connectors.goose.protocol.cs.message.HandshakeV10;
import com.goosemagnet.connectors.goose.protocol.cs.message.LoginRequest;
import com.goosemagnet.connectors.goose.protocol.cs.message.TextResultSet;
import com.goosemagnet.connectors.goose.protocol.cs.message.ok.OkPacket;
import com.goosemagnet.connectors.goose.protocol.cs.message.ok.OkPacketType;
import lombok.AllArgsConstructor;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;

/**
 * <a href="https://dev.mysql.com/doc/dev/mysql-server/latest/page_protocol_connection_phase_packets_protocol_handshake_v10.html">...</a>
 */
@AllArgsConstructor
public class GooseMySqlConnector implements MySqlConnector {

    private final String username;
    private final String password;

    public GooseMySqlConnector() {
        this("root", "password");
    }

    @Override
    public List<String> getSchemas() {
        try (Socket socket = new Socket("localhost", 3306);
             DataInputStream is = new DataInputStream(socket.getInputStream());
             OutputStream os = socket.getOutputStream()) {

            HandshakeV10 handshakeV10 = HandshakeV10.fromInputStream(is);

            LoginRequest loginRequest = LoginRequest.fromHandshake(handshakeV10, username, password);
            os.write(loginRequest.toByteArray());

            OkPacket okPacket = OkPacket.fromInputStream(is);
            if (OkPacketType.EOF.equals(okPacket.getHeader())) {
                throw new RuntimeException("Unexpected EOF exception...");
            }

            ComQuery queryPacket = ComQuery.fromQuery("""
                    Select * from db.hello
                    """
            );
            os.write(queryPacket.toByteArray());

            TextResultSet resultSet = TextResultSet.fromInputStream(is);
            return resultSet.getRows().stream()
                    .map(s -> s[0] + ":" + s[1])
                    .toList();

        } catch (UnknownHostException uhe) {
            // Unknown Host
        } catch (IOException ioe) {
            throw new UncheckedIOException(ioe);
        }

        return List.of();
    }
}



