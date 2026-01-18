import java.util.List;
import java.util.Objects;

public class Dish {
    private int id;
    private String name;
    private DishTypeEnum dishType;
    private List<Ingredient> ingredients;
    private Double price;

    public Dish() {}

    public Dish(int id, String name, DishTypeEnum dishType, Double price, List<Ingredient> ingredients) {
        this.id = id;
        this.name = name;
        this.dishType = dishType;
        this.price = price;
        this.ingredients = ingredients;
    }

    public Double getDishCost() {
        if (ingredients == null || ingredients.isEmpty()) return 0.0;
        return ingredients.stream()
                .mapToDouble(i -> {
                    if (i.getQuantity() == null) {
                        throw new RuntimeException("Quantité manquante pour l'ingrédient: " + i.getName());
                    }
                    return i.getPrice() * i.getQuantity();
                })
                .sum();
    }

    public Double getGrossMargin() {
        if (this.price == null) {
            throw new RuntimeException("Le prix de vente est nul, calcul de marge impossible pour le plat : " + this.name);
        }
        return this.price - this.getDishCost();
    }

    public int getId() {
        return id;
    }
    public void setId(int id) {
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
    public List<Ingredient> getIngredients() {
        return ingredients;
    }
    public void setIngredients(List<Ingredient> ingredients) {
        this.ingredients = ingredients;
    }
}