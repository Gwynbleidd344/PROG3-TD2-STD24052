import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        DataRetriever dataRetriever = new DataRetriever();
        Dish dish1 = dataRetriever.findDishById(1);
        DishOrder dishOrder1 = new DishOrder(1, dish1, 5);
        List<DishOrder> dishOrderList = List.of(dishOrder1);


        Order order = new Order(6, "ORD104", Instant.now(),dishOrderList, 3, LocalDateTime.now().minus(Duration.ofMinutes(10)), LocalDateTime.now());
        dataRetriever.saveOrder(order);
    }
}