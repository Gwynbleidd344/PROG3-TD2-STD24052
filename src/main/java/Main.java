import java.sql.Connection;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args) throws SQLException {
        DataRetriever dataRetriever = new DataRetriever();
//        System.out.println(dataRetriever.findDishById(1));
        dataRetriever.findIngredients(1, 2).forEach(System.out::println);
    }
}
