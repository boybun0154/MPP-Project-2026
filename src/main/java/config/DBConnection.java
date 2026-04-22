package config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Manages JDBC database connection lifecycle using the configured environment URL.
 */
public class DBConnection {
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(AppConfig.getDatabaseUrl());
    }
}