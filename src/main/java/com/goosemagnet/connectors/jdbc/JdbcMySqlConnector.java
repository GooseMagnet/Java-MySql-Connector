package com.goosemagnet.connectors.jdbc;

import com.goosemagnet.connectors.MySqlConnector;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JdbcMySqlConnector implements MySqlConnector {

    private static final String URL = "jdbc:mysql://localhost:3306/db?serverTimezone=UTC";
    private static final String SHOW_SCHEMAS = "SHOW SCHEMAS";
    private static final String COLUMN = "Database";

    private final String username;
    private final String password;

    public JdbcMySqlConnector(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public JdbcMySqlConnector() {
        this("user", "password");
    }

    @Override
    public List<String> getSchemas() {
        try (Connection connection = DriverManager.getConnection(URL, username, password);
             PreparedStatement ps = connection.prepareStatement(SHOW_SCHEMAS);
             ResultSet rs = ps.executeQuery()) {

            List<String> schemas = new ArrayList<>();
            while (rs.next()) {
                schemas.add(rs.getString(COLUMN));
            }
            return schemas;
        } catch (SQLException sqle) {
            throw new RuntimeException(sqle);
        }
    }
}
