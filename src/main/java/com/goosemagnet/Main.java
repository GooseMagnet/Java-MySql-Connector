package com.goosemagnet;

import com.goosemagnet.connectors.MySqlConnector;
import com.goosemagnet.connectors.goose.GooseMySqlConnector;
import lombok.SneakyThrows;

public class Main {

    @SneakyThrows
    public static void main(String[] args) {
        MySqlConnector db = new GooseMySqlConnector();
//        MySqlConnector db = new JdbcMySqlConnector();
        db.getSchemas().forEach(System.out::println);
    }

    /*
    Both connectors give the same output
    1:hi
    2:yo
    3:sup
    4:hey
    5:hello
    6:greetings
    7:salutations
    8:shalom
    9:salaam
    10:privet
    11:nihao
    */
}