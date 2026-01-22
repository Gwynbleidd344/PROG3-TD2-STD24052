import java.util.ArrayList;
import java.util.List;

public class Dish {
    private Integer id;
    private String name;
    private DishTypeEnum dishType;
    private Double price;
    private List<DishIngredient> dishIngredients;

    public Dish() {
        this.dishIngredients = new ArrayList<>();
    }

    public Dish(Integer id, String name, DishTypeEnum dishType) {
        this.id = id;
        this.name = name;
        this.dishType = dishType;
        this.dishIngredients = new ArrayList<>();
    }

    public Dish(String name, DishTypeEnum dishType) {
        this.name = name;
        this.dishType = dishType;
        this.dishIngredients = new ArrayList<>();
    }

    public Dish(Integer id, String name, DishTypeEnum dishType, List<DishIngredient> dishIngredients) {
        this.id = id;
        this.name = name;
        this.dishType = dishType;
        this.dishIngredients = dishIngredients != null ? dishIngredients : new ArrayList<>();
    }

    public Dish(Integer id, String name, DishTypeEnum dishType, Double price, List<DishIngredient> dishIngredients) {
        this.id = id;
        this.name = name;
        this.dishType = dishType;
        this.price = price;
        this.dishIngredients = dishIngredients != null ? dishIngredients : new ArrayList<>();
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

    public DishTypeEnum getDishType() {
        return dishType;
    }

    public void setDishType(DishTypeEnum dishType) {
        this.dishType = dishType;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public List<DishIngredient> getDishIngredients() {
        return dishIngredients;
    }

    public void setDishIngredients(List<DishIngredient> dishIngredients) {
        this.dishIngredients = dishIngredients;
    }

    public Double getDishCost() {
        if (dishIngredients == null || dishIngredients.isEmpty()) {
            return 0.0;
        }

        return dishIngredients.stream()
                .mapToDouble(di -> di.getIngredient().getPrice() * di.getQuantityRequired())
                .sum();
    }

    public Double getGrossMargin() {
        if (price == null) {
            throw new RuntimeException("Cannot calculate gross margin: selling price is not set for dish '" + name + "'");
        }

        return price - getDishCost();
    }

    @Override
    public String toString() {
        return "Dish{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", dishType=" + dishType +
                ", price=" + price +
                ", dishIngredients=" + dishIngredients +
                '}';
    }
}