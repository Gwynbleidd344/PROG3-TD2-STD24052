import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class Ingredient {
    private Integer id;
    private String name;
    private Double price;
    private CategoryEnum category;
    private List<StockMovement> stockMovementList = new ArrayList<>();

    public Ingredient() {
    }

    public Ingredient(Integer id, String name, Double price, CategoryEnum category) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.category = category;
    }
    
    public Ingredient(Integer id, String name, Double price, CategoryEnum category, List<StockMovement> stockMovementList) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.category = category;
        if (stockMovementList != null) {
            this.stockMovementList = stockMovementList;
        }
    }

    public Ingredient(String name, Double price, CategoryEnum category) {
        this.name = name;
        this.price = price;
        this.category = category;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public CategoryEnum getCategory() {
        return category;
    }

    public void setCategory(CategoryEnum category) {
        this.category = category;
    }
    
    public List<StockMovement> getStockMovementList() {
        return stockMovementList;
    }

    public void setStockMovementList(List<StockMovement> stockMovementList) {
        this.stockMovementList = stockMovementList;
    }

    public StockValue getStockValueAt(Instant dateTime) {
        if (stockMovementList == null || stockMovementList.isEmpty()) {
            return new StockValue(0, UnitType.KG);
        }

        double totalQuantity = 0;
        UnitType unit = stockMovementList.get(0).getValue().getUnit();

        for (StockMovement movement : stockMovementList) {
            if (!movement.getCreationDateTime().isAfter(dateTime)) {
                if (movement.getType() == MovementTypeEnum.IN) {
                    totalQuantity += movement.getValue().getQuantity();
                } else if (movement.getType() == MovementTypeEnum.OUT) {
                    totalQuantity -= movement.getValue().getQuantity();
                }
            }
        }
        return new StockValue(totalQuantity, unit);
    }


    @Override
    public String toString() {
        return "Ingredient{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", price=" + price +
                ", category=" + category +
                ", stockMovementList=" + stockMovementList +
                '}';
    }
}