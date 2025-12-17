import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DataRetriever {
    DBConnection dbConnection = new DBConnection();

    public Dish findDishById(Integer id) throws SQLException {
        String query = "SELECT d.id AS Dish_id,d.name AS Dish_name,d.dish_type,i.id AS Ingredient_id,i.name AS Ingredient_name,i.price,i.category FROM Dish d JOIN Ingredient i ON d.id = i.id_dish WHERE d.id = ?";
        PreparedStatement Statement = dbConnection.getDBConnection().prepareStatement(query);
        Statement.setInt(1, id);
        ResultSet rs = Statement.executeQuery();

        Dish dish = null;
        while (rs.next()) {
            if (dish == null) {
                dish = new Dish(
                        rs.getInt("Dish_id"),
                        rs.getString("Dish_name"),
                        DishTypeEnum.valueOf(rs.getString("dish_type")),
                        new ArrayList<>()
                );
            }
            Ingredient ingredient = new Ingredient(
                    rs.getInt("Ingredient_id"),
                    rs.getString("Ingredient_name"),
                    rs.getDouble("price"),
                    CategoryEnum.valueOf(rs.getString("category")),
                    dish
            );
            dish.getIngredients().add(ingredient);
        }
        return dish;
    }
    public List<Ingredient> findIngredients(int page, int size) throws SQLException {
        List<Ingredient> ingredients = new ArrayList<>();
        int offset = (page - 1) * size;
        String query = "Select i.id AS Ingredient_id,i.name AS Ingredient_name,i.price,i.category from Ingredient i LIMIT ? OFFSET ?";
        PreparedStatement Statement = dbConnection.getDBConnection().prepareStatement(query);
        Statement.setInt(1, size);
        Statement.setInt(2, offset);
        ResultSet rs = Statement.executeQuery();
        while (rs.next()) {
            Ingredient ingredient = new Ingredient(
                    rs.getInt("Ingredient_id"),
                    rs.getString("Ingredient_name"),
                    rs.getDouble("price"),
                    CategoryEnum.valueOf(rs.getString("category")),
                    null
            );
              ingredients.add(ingredient);
        }
        return ingredients;
    }
}
