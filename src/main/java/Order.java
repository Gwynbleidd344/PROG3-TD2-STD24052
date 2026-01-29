import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Order {
    private Integer id;
    private String reference;
    private Instant creationDatetime;
    private List<DishOrder> dishOrderList = new ArrayList<>();
    private Integer idTable;
    private LocalDateTime arrivalDatetime;
    private LocalDateTime departureDatetime;

    public Order(Integer id, String reference, Instant creationDatetime,List<DishOrder> dishOrderList, Integer idTable, LocalDateTime arrivalDatetime, LocalDateTime departureDatetime) {
        this.id = id;
        this.reference = reference;
        this.creationDatetime = creationDatetime;
        this.idTable = idTable;
        this.arrivalDatetime = arrivalDatetime;
        this.departureDatetime = departureDatetime;
    }

    public Order() {

    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public Instant getCreationDatetime() {
        return creationDatetime;
    }

    public void setCreationDatetime(Instant creationDatetime) {
        this.creationDatetime = creationDatetime;
    }

    public List<DishOrder> getDishOrderList() {
        return dishOrderList;
    }

    public void setDishOrderList(List<DishOrder> dishOrderList) {
        if (dishOrderList == null) {
            this.dishOrderList = new ArrayList<>();
        } else {
            this.dishOrderList = dishOrderList;
        }
    }

    public Integer getIdTable() {
        return idTable;
    }
    public void setIdTable(Integer idTable) {
        this.idTable = idTable;
    }

    public LocalDateTime getArrivalDatetime() {
        return arrivalDatetime;
    }
    public void setArrivalDatetime(LocalDateTime arrivalDatetime) {
        this.arrivalDatetime = arrivalDatetime;
    }

    public LocalDateTime getDepartureDatetime() {
        return departureDatetime;
    }
    public void setDepartureDatetime(LocalDateTime departureDatetime) {
        this.departureDatetime = departureDatetime;
    }

    @Override
    public String toString() {
        return "Order{" +
                "id=" + id +
                ", reference='" + reference + '\'' +
                ", creationDatetime=" + creationDatetime +
                ", dishOrderList=" + dishOrderList +
                '}';
    }

    public Double getTotalAmountWithoutVat() {
        if (dishOrderList == null) return 0.0;
        return dishOrderList.stream()
                .mapToDouble(DishOrder -> DishOrder.getDish().getPrice() * DishOrder.getQuantity())
                .sum();
    }

    public Double getTotalAmountWithVat(Double vatRate) {
        return getTotalAmountWithoutVat() * (1 + vatRate);
    }


    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Order order)) return false;
        return Objects.equals(id, order.id) && Objects.equals(reference, order.reference) && Objects.equals(creationDatetime, order.creationDatetime) && Objects.equals(dishOrderList, order.dishOrderList);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, reference, creationDatetime, dishOrderList);
    }
}