package com.goosemagnet;

import com.goosemagnet.connectors.goose.GooseMySqlConnector;
import com.goosemagnet.connectors.MySqlConnector;
import com.goosemagnet.connectors.jdbc.JdbcMySqlConnector;
import com.mysql.cj.NativeSession;

public class Main {
    public static void main(String[] args) {
        MySqlConnector db = new GooseMySqlConnector();
        db.getSchemas().forEach(System.out::println);
    }
}