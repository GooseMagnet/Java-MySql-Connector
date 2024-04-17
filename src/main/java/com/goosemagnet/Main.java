package com.goosemagnet;

import com.goosemagnet.connectors.MySqlConnector;
import com.goosemagnet.connectors.goose.GooseMySqlConnector;
import com.goosemagnet.connectors.jdbc.JdbcMySqlConnector;
import com.mysql.cj.protocol.Security;
import lombok.SneakyThrows;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;

public class Main {

    @SneakyThrows
    public static void main(String[] args) {
        MySqlConnector db = new GooseMySqlConnector();
//        MySqlConnector db = new JdbcMySqlConnector();
        db.getSchemas().forEach(System.out::println);
    }
}