import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class RestaurantTable {
    private int id;
    private int number;
    private List<Order> orders = new ArrayList<>();

    public RestaurantTable() {
    }

    public RestaurantTable(int id, int number) {
        this.id = id;
        this.number = number;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public List<Order> getOrders() {
        return orders;
    }

    public void setOrders(List<Order> orders) {
        this.orders = orders;
    }

    public boolean isAvailableAt(LocalDateTime t) {
        for (Order order : orders) {
            if (order.getArrivalDatetime() != null && order.getDepartureDatetime() != null) {
                if (!t.isBefore(order.getArrivalDatetime()) && t.isBefore(order.getDepartureDatetime())) {
                    return false;
                }
            }
        }
        return true;
    }
}