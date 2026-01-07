import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private final String JDBC_URL = EnvConfig.get("JDBC_URL");
    private final String USER = EnvConfig.get("JDBC_USER");
    private final String PASSWORD = EnvConfig.get("JDBC_PASSWORD");

    public Connection getDBConnection(){
        try {
            Connection connection = DriverManager.getConnection(JDBC_URL, USER, PASSWORD);
            System.out.println("Connected to PostgreSQL");
            return connection;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
