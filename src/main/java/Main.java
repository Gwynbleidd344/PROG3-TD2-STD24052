import java.sql.Connection;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args) throws SQLException {
        DBConnection dbConnection = new DBConnection();

        Connection connection = dbConnection.getDBConnection();
    }
}
