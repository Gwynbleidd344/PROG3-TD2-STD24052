public class DishIngredient {
    private int id;
    private Dish dish;
    private Ingredient ingredient;
    private double quantityRequired;
    private UnitType unit;

    public DishIngredient(Dish dish, Ingredient ingredient, double quantityRequired, UnitType unit) {
        this.dish = dish;
        this.ingredient = ingredient;
        this.quantityRequired = quantityRequired;
        this.unit = unit;
    }

    public DishIngredient(int id, Dish dish, Ingredient ingredient, double quantityRequired, UnitType unit) {
        this.id = id;
        this.dish = dish;
        this.ingredient = ingredient;
        this.quantityRequired = quantityRequired;
        this.unit = unit;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Dish getDish() {
        return dish;
    }

    public void setDish(Dish dish) {
        this.dish = dish;
    }

    public Ingredient getIngredient() {
        return ingredient;
    }

    public void setIngredient(Ingredient ingredient) {
        this.ingredient = ingredient;
    }

    public double getQuantityRequired() {
        return quantityRequired;
    }

    public void setQuantityRequired(double quantityRequired) {
        this.quantityRequired = quantityRequired;
    }

    public UnitType getUnit() {
        return unit;
    }

    public void setUnit(UnitType unit) {
        this.unit = unit;
    }

    @Override
    public String toString() {
        return "DishIngredient{" +
                "id=" + id +
                ", dish=" + dish +
                ", ingredient=" + ingredient +
                ", quantityRequired=" + quantityRequired +
                ", unit=" + unit +
                '}';
    }
}