package com.ordersystem.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Utility class responsible for providing a JDBC Connection to the SQLite database.
 * The URL defaults to "jdbc:sqlite:orders.db" and can be overridden with the
 * system property "db.url" (used by tests to point at a throwaway database).
 */
public class DbConnection {

    private static final String DB_URL = "jdbc:sqlite:orders.db";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(System.getProperty("db.url", DB_URL));
    }

    public static void initializeSchema() {
        // TODO: read resources/schema.sql and execute it if tables don't exist
    }
}
