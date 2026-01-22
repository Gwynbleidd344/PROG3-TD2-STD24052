import io.github.cdimascio.dotenv.Dotenv;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    private static final Dotenv dotenv = Dotenv.load();

    public Connection getDBConnection() {
        String jdbcUrl = dotenv.get("JDBC_URL");
        String username = dotenv.get("USERNAME");
        String password = dotenv.get("PASSWORD");

        if (jdbcUrl == null || username == null || password == null) {
            throw new RuntimeException("Environment variables JDBC_URL, USERNAME, or PASSWORD are not set in .env file");
        }

        try {
            Connection connection = DriverManager.getConnection(jdbcUrl, username, password);
            return connection;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to connect to database: " + e.getMessage(), e);
        }
    }

    public void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                throw new RuntimeException("Failed to close connection: " + e.getMessage(), e);
            }
        }
    }
}