public class DishIngredient {
    private int id;
    private int idDish;
    private int idIngredient;
    private double quantityRequired;
    private UnitType unit;

    public DishIngredient(int id, int idDish, int idIngredient, double quantityRequired, UnitType unit) {
        this.id = id;
        this.idDish = idDish;
        this.idIngredient = idIngredient;
        this.quantityRequired = quantityRequired;
        this.unit = unit;
    }

    public double getQuantityRequired() {
        return quantityRequired;
    }
    public int getIdIngredient() {
        return idIngredient;
    }
}