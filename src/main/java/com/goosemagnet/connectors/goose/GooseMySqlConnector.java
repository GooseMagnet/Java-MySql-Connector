package com.goosemagnet.connectors.goose;

import com.goosemagnet.connectors.MySqlConnector;
import com.goosemagnet.connectors.goose.protocol.HandshakePacket;
import com.goosemagnet.connectors.goose.protocol.MysqlPacket;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;

/**
 * <a href="https://mariadb.com/kb/en/0-packet/">...</a>
 * <a href="https://mariadb.com/kb/en/connection/#initial-handshake-packet">...</a>
 * <a href="https://dev.mysql.com/doc/dev/mysql-server/latest/page_protocol_connection_phase_packets_protocol_handshake_v10.html">...</a>
 */
public class GooseMySqlConnector implements MySqlConnector {

    @Override
    public List<String> getSchemas() {
        try (Socket socket = new Socket("localhost", 3306);
             DataInputStream is = new DataInputStream(socket.getInputStream());
             OutputStream os = socket.getOutputStream()) {

            MysqlPacket<HandshakePacket> handshakePacket = MysqlPacket.handshake(is);
            System.out.println(handshakePacket);

        } catch (UnknownHostException uhe) {
            // Unknown Host
        } catch (IOException ioe) {
            throw new UncheckedIOException(ioe);
        }

        return List.of();
    }
}
