import java.time.Instant;
import java.util.List;

public class Order {
    private int id;
    private String reference;
    private Instant creationDateTime;
    private List<DishOrder> dishOrders;
    public Order(int id, String reference, Instant creationDateTime, List<DishOrder> dishOrders) {
        this.id = id;
        this.reference = reference;
        this.creationDateTime = creationDateTime;
        this.dishOrders = dishOrders;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public Instant getCreationDateTime() {
        return creationDateTime;
    }

    public void setCreationDateTime(Instant creationDateTime) {
        this.creationDateTime = creationDateTime;
    }

    public List<DishOrder> getDishOrders() {
        return dishOrders;
    }

    public void setDishOrders(List<DishOrder> dishOrders) {
        this.dishOrders = dishOrders;
    }
    public double getTotalAmountWithoutVAT() {
        double total = 0.0;
        for (DishOrder dishOrder : dishOrders) {
            total += dishOrder.getDish().getPrice() * dishOrder.getQuantity();
        }
        return total;
    }
    public double getTotalAmountWithVAT(double vatRate) {
        return getTotalAmountWithoutVAT() * (1+ vatRate);
    }
}
