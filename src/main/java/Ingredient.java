public class Ingredient {
    private int id;
    private String name;
    private Double price;
    private CategoryEnum category;
    private Double quantity;

    public Ingredient(int id, String name, Double price, CategoryEnum category) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.category = category;
    }

    public Ingredient(int id, String name, Double price, CategoryEnum category, Double quantity) {
        this(id, name, price, category);
        this.quantity = quantity;
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
    public Double getPrice() {
        return price;
    }
    public CategoryEnum getCategory() {
        return category;
    }
    public Double getQuantity() {
        return quantity;
    }
    public void setQuantity(Double quantity) {
        this.quantity = quantity;
    }

    @Override
    public String toString() {
        return "Ingredient{" + name + ", price=" + price + (quantity != null ? ", qty=" + quantity : "") + '}';
    }
}