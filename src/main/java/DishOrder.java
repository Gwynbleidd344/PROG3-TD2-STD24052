public class DishOrder {
    private int id;
    private Dish dish;
    private Integer quantity;

    public DishOrder() {
    }

    public DishOrder(int id, Dish dish, Integer quantity) {
        this.id = id;
        this.dish = dish;
        this.quantity = quantity;
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

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}
