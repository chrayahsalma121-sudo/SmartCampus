package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * DatabaseConnection — provides a new JDBC Connection on each call.
 *
 * Each repository opens and closes its own connection via try-with-resources,
 * which is safe and simple for an MVP with no concurrent load concerns.
 *
 * Configuration: update the constants below to match your local MySQL setup.
 */
public class DatabaseConnection {

    private static final String URL      = "jdbc:mysql://localhost:3306/smart_campus?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private static final String DB_USER  = "root";
    private static final String PASSWORD = "root";

    private DatabaseConnection() {}

    /**
     * Opens and returns a new JDBC connection.
     * Callers MUST close it (use try-with-resources).
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, DB_USER, PASSWORD);
    }
}
