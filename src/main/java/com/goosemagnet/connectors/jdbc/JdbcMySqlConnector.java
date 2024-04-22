package com.goosemagnet.connectors.jdbc;

import com.goosemagnet.connectors.MySqlConnector;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JdbcMySqlConnector implements MySqlConnector {

    private static final String URL = "jdbc:mysql://localhost:3306?serverTimezone=UTC&useSSL=false";
    private static final String SHOW_SCHEMAS = "Select * from db.hello";

    private final String username;
    private final String password;

    public JdbcMySqlConnector(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public JdbcMySqlConnector() {
        this("root", "password");
    }

    @Override
    public List<String> getSchemas() {
        try (Connection connection = DriverManager.getConnection(URL, username, password);
             PreparedStatement ps = connection.prepareStatement(SHOW_SCHEMAS);
             ResultSet rs = ps.executeQuery()
        ) {
            List<String> schemas = new ArrayList<>();
            while (rs.next()) {
                schemas.add(rs.getInt("id") + ":" + rs.getString("hello"));
            }
            return schemas;
        } catch (SQLException sqle) {
            throw new RuntimeException(sqle);
        }
    }
}
