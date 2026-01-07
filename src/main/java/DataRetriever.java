import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public List<Ingredient> createIngredients(List<Ingredient> newIngredients) {
        String checkIngredientExistQuery = "SELECT COUNT(*) FROM Ingredient WHERE name = ?";
        String insertIngredientQuery = "INSERT INTO Ingredient (name, price, category) VALUES (?, ?, ?::Category)";
        Connection conn = dbConnection.getDBConnection();
        try {
            conn.setAutoCommit(false);
            try (PreparedStatement checkStmt = conn.prepareStatement(checkIngredientExistQuery);
                 PreparedStatement insertStmt = conn.prepareStatement(insertIngredientQuery)) {

                for (Ingredient ingredient : newIngredients) {
                    checkStmt.setString(1, ingredient.getName());
                    ResultSet rs = checkStmt.executeQuery();
                    if (rs.next() && rs.getInt(1) > 0) {
                        throw new RuntimeException("Ingredient already exists: " + ingredient.getName());
                    }

                    insertStmt.setString(1, ingredient.getName());
                    insertStmt.setDouble(2, ingredient.getPrice());
                    insertStmt.setString(3, ingredient.getCategory().toString());
                    insertStmt.addBatch();
                }
                insertStmt.executeBatch();
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw new RuntimeException("Error during ingredient creation, transaction rolled back.", e);
            } catch (RuntimeException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error managing transaction.", e);
        } finally {
            try {
                conn.setAutoCommit(true);
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return newIngredients;
    }

    public Dish saveDish(Dish dishToSave) {
        Connection conn = dbConnection.getDBConnection();
        try {
            conn.setAutoCommit(false);

            String checkDishExistsQuery = "SELECT id FROM Dish WHERE id = ?";
            String insertDishQuery = "INSERT INTO Dish (name, dish_type) VALUES (?, ?)";
            String updateDishQuery = "UPDATE Dish SET name = ?, dish_type = ? WHERE id = ?";

            if (dishToSave.getId() != 0) {
                try (PreparedStatement checkStmt = conn.prepareStatement(checkDishExistsQuery)) {
                    checkStmt.setInt(1, dishToSave.getId());
                    ResultSet rs = checkStmt.executeQuery();
                    if (rs.next()) {
                        try (PreparedStatement updateStmt = conn.prepareStatement(updateDishQuery)) {
                            updateStmt.setString(1, dishToSave.getName());
                            updateStmt.setString(2, dishToSave.getDishType().toString());
                            updateStmt.setInt(3, dishToSave.getId());
                            updateStmt.executeUpdate();
                        }
                    } else {
                        dishToSave.setId(0);
                    }
                }
            }

            if (dishToSave.getId() == 0) {
                try (PreparedStatement insertStmt = conn.prepareStatement(insertDishQuery, PreparedStatement.RETURN_GENERATED_KEYS)) {
                    insertStmt.setString(1, dishToSave.getName());
                    insertStmt.setString(2, dishToSave.getDishType().toString());
                    insertStmt.executeUpdate();
                    ResultSet generatedKeys = insertStmt.getGeneratedKeys();
                    if (generatedKeys.next()) {
                        dishToSave.setId(generatedKeys.getInt(1));
                    }
                }
            }

            String dissociateIngredientsQuery = "UPDATE Ingredient SET id_dish = NULL WHERE id_dish = ?";
            try (PreparedStatement dissociateStmt = conn.prepareStatement(dissociateIngredientsQuery)) {
                dissociateStmt.setInt(1, dishToSave.getId());
                dissociateStmt.executeUpdate();
            }

            if (dishToSave.getIngredients() != null && !dishToSave.getIngredients().isEmpty()) {
                String associateIngredientsQuery = "UPDATE Ingredient SET id_dish = ? WHERE id = ?";
                try (PreparedStatement associateStmt = conn.prepareStatement(associateIngredientsQuery)) {
                    for (Ingredient ingredient : dishToSave.getIngredients()) {
                        associateStmt.setInt(1, dishToSave.getId());
                        associateStmt.setInt(2, ingredient.getId());
                        associateStmt.addBatch();
                    }
                    associateStmt.executeBatch();
                }
            }

            conn.commit();
            return dishToSave;

        } catch (SQLException e) {
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException rollbackEx) {
                throw new RuntimeException("Error during rollback", rollbackEx);
            }
            throw new RuntimeException("Error saving dish, transaction rolled back", e);
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException closeEx) {
                closeEx.printStackTrace();
            }
        }
    }

    public List<Dish> findDishsByIngredientName(String ingredientName) throws SQLException {
        List<Dish> dishes = new ArrayList<>();
        Map<Integer, Dish> dishMap = new HashMap<>();
        String query = "SELECT d.id AS Dish_id, d.name AS Dish_name, d.dish_type, i.id AS Ingredient_id, i.name AS Ingredient_name, i.price, i.category FROM Dish d JOIN Ingredient i ON d.id = i.id_dish WHERE i.name LIKE ?";
        Connection conn = null;
        PreparedStatement Statement = null;
        ResultSet rs = null;

        try {
            conn = dbConnection.getDBConnection();
            Statement = conn.prepareStatement(query);
            Statement.setString(1, "%" + ingredientName + "%");
            rs = Statement.executeQuery();

            while (rs.next()) {
                Integer dishId = rs.getInt("Dish_id");
                Dish dish = dishMap.get(dishId);

                if (dish == null) {
                    dish = new Dish(
                            dishId,
                            rs.getString("Dish_name"),
                            DishTypeEnum.valueOf(rs.getString("dish_type")),
                            new ArrayList<>()
                    );
                    dishMap.put(dishId, dish);
                    dishes.add(dish);
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
        } finally {
            if (rs != null) rs.close();
            if (Statement != null) Statement.close();
            if (conn != null) conn.close();
        }
        return dishes;
    }

    public List<Ingredient> findIngredientsByCriteria(String ingredientName, CategoryEnum category, String dishName, int page, int size) throws SQLException {
        List<Ingredient> ingredients = new ArrayList<>();
        StringBuilder queryBuilder = new StringBuilder("SELECT i.id AS Ingredient_id, i.name AS Ingredient_name, i.price, i.category, d.id AS Dish_id, d.name AS Dish_name, d.dish_type FROM Ingredient i LEFT JOIN Dish d ON i.id_dish = d.id");
        List<Object> params = new ArrayList<>();
        List<String> conditions = new ArrayList<>();

        if (ingredientName != null && !ingredientName.isEmpty()) {
            conditions.add("i.name LIKE ?");
            params.add("%" + ingredientName + "%");
        }
        if (category != null) {
            conditions.add("i.category = ?::Category");
            params.add(category.toString());
        }
        if (dishName != null && !dishName.isEmpty()) {
            conditions.add("d.name LIKE ?");
            params.add("%" + dishName + "%");
        }

        if (!conditions.isEmpty()) {
            queryBuilder.append(" WHERE ").append(String.join(" AND ", conditions));
        }

        queryBuilder.append(" LIMIT ? OFFSET ?");
        int offset = (page - 1) * size;

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = dbConnection.getDBConnection();
            stmt = conn.prepareStatement(queryBuilder.toString());

            int paramIndex = 1;
            for (Object param : params) {
                stmt.setObject(paramIndex++, param);
            }
            stmt.setInt(paramIndex++, size);
            stmt.setInt(paramIndex, offset);

            rs = stmt.executeQuery();

            while (rs.next()) {
                Dish dish = null;
                int dishId = rs.getInt("Dish_id");
                if (!rs.wasNull()) {
                    dish = new Dish(
                            dishId,
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
                
                if(dish != null) {
                    dish.getIngredients().add(ingredient);
                }
                
                ingredients.add(ingredient);
            }

        } finally {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
            if (conn != null) conn.close();
        }

        return ingredients;
    }
}
