import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DataRetriever {
    DBConnection dbConnection = new DBConnection();

    public Dish findDishById(Integer id) {
        String dishSQL = "SELECT id, name, dish_type, selling_price FROM dish WHERE id = ?";
        String ingredientsSQL = "SELECT i.* FROM ingredient i " +
                "JOIN DishIngredient di ON i.id = di.id_ingredient " +
                "WHERE di.id_dish = ?";
        Dish dish = null;

        try (Connection conn = dbConnection.getDBConnection();
             PreparedStatement dishStmt = conn.prepareStatement(dishSQL)) {

            dishStmt.setInt(1, id);
            ResultSet rsDish = dishStmt.executeQuery();

            if (rsDish.next()) {
                dish = new Dish();
                dish.setId(rsDish.getInt("id"));
                dish.setName(rsDish.getString("name"));
                dish.setDishType(DishTypeEnum.valueOf(rsDish.getString("dish_type")));
                dish.setPrice(rsDish.getObject("selling_price") != null ? rsDish.getDouble("selling_price") : null);

                List<Ingredient> ingredients = new ArrayList<>();
                try (PreparedStatement ingredientsStmt = conn.prepareStatement(ingredientsSQL)) {
                    ingredientsStmt.setInt(1, id);
                    ResultSet rsIngredients = ingredientsStmt.executeQuery();
                    while (rsIngredients.next()) {
                        ingredients.add(new Ingredient(
                                rsIngredients.getInt("id"),
                                rsIngredients.getString("name"),
                                rsIngredients.getDouble("price"),
                                CategoryEnum.valueOf(rsIngredients.getString("category")),
                                null
                        ));
                    }
                }
                dish.setIngredients(ingredients);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return dish;
    }

    public List<Ingredient> findIngredients(int page, int size) {
        String sql = "SELECT * FROM ingredient LIMIT ? OFFSET ?";
        List<Ingredient> ingredients = new ArrayList<>();
        int offset = (page - 1) * size;

        try (Connection conn = dbConnection.getDBConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, size);
            stmt.setInt(2, offset);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                ingredients.add(new Ingredient(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getDouble("price"),
                        CategoryEnum.valueOf(rs.getString("category")),
                        null
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return ingredients;
    }

    public List<Ingredient> createIngredients(List<Ingredient> newIngredients) {
        String checkSql = "SELECT id FROM ingredient WHERE name = ?";
        String insertSql = "INSERT INTO ingredient (name, price, category) VALUES (?, ?, ?::Category)";
        Connection conn = null;

        try {
            conn = dbConnection.getDBConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql);
                 PreparedStatement insertStmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {

                for (Ingredient ingredient : newIngredients) {
                    checkStmt.setString(1, ingredient.getName());
                    if (checkStmt.executeQuery().next()) {
                        throw new RuntimeException("Ingredient already exists: " + ingredient.getName());
                    }
                    insertStmt.setString(1, ingredient.getName());
                    insertStmt.setDouble(2, ingredient.getPrice());
                    insertStmt.setString(3, ingredient.getCategory().name());
                    insertStmt.addBatch();
                }

                insertStmt.executeBatch();
                conn.commit();

                ResultSet generatedKeys = insertStmt.getGeneratedKeys();
                int i = 0;
                while (generatedKeys.next() && i < newIngredients.size()) {
                    newIngredients.get(i).setId(generatedKeys.getInt(1));
                    i++;
                }
            }

        } catch (SQLException | RuntimeException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                }
            }
            throw new RuntimeException(e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return newIngredients;
    }

    public Dish saveDish(Dish dishToSave) {
        Connection conn = null;

        try {
            conn = dbConnection.getDBConnection();
            conn.setAutoCommit(false);

            if (dishToSave.getId() == 0) {
                String insertDishSql = "INSERT INTO dish (name, dish_type, selling_price) VALUES (?, ?::Dish_type, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(insertDishSql, Statement.RETURN_GENERATED_KEYS)) {
                    stmt.setString(1, dishToSave.getName());
                    stmt.setString(2, dishToSave.getDishType().name());
                    if (dishToSave.getPrice() == null) {
                        stmt.setNull(3, Types.NUMERIC);
                    } else {
                        stmt.setDouble(3, dishToSave.getPrice());
                    }
                    stmt.executeUpdate();

                    try (ResultSet rs = stmt.getGeneratedKeys()) {
                        if (rs.next()) {
                            dishToSave.setId(rs.getInt(1));
                        }
                    }
                }
            } else {
                String updateDishSql = "UPDATE dish SET name = ?, dish_type = ?::Dish_type, selling_price = ? WHERE id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(updateDishSql)) {
                    stmt.setString(1, dishToSave.getName());
                    stmt.setString(2, dishToSave.getDishType().name());
                    if (dishToSave.getPrice() == null) {
                        stmt.setNull(3, Types.NUMERIC);
                    } else {
                        stmt.setDouble(3, dishToSave.getPrice());
                    }
                    stmt.setInt(4, dishToSave.getId());
                    stmt.executeUpdate();
                }
            }

            String deleteRelationsSql = "DELETE FROM DishIngredient WHERE id_dish = ?";
            try (PreparedStatement stmt = conn.prepareStatement(deleteRelationsSql)) {
                stmt.setInt(1, dishToSave.getId());
                stmt.executeUpdate();
            }

            conn.commit();
            return dishToSave;

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                }
            }
            throw new RuntimeException(e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public void saveDishIngredient(int dishId, int ingredientId, double quantity, String unit) {
        String sql = "INSERT INTO DishIngredient (id_dish, id_ingredient, quantity_required, unit) VALUES (?, ?, ?, ?::unit_type)";
        try (Connection conn = dbConnection.getDBConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, dishId);
            stmt.setInt(2, ingredientId);
            stmt.setDouble(3, quantity);
            stmt.setString(4, unit);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Dish> findDishsByIngredientName(String ingredientName) {
        String sql = "SELECT DISTINCT d.id FROM dish d " +
                "JOIN DishIngredient di ON d.id = di.id_dish " +
                "JOIN ingredient i ON di.id_ingredient = i.id " +
                "WHERE i.name ILIKE ?";

        List<Dish> dishes = new ArrayList<>();
        try (Connection conn = dbConnection.getDBConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "%" + ingredientName + "%");
            ResultSet rs = stmt.executeQuery();
            List<Integer> dishIds = new ArrayList<>();
            while (rs.next()) {
                dishIds.add(rs.getInt("id"));
            }

            for (Integer dishId : dishIds) {
                Dish dish = findDishById(dishId);
                if (dish != null) {
                    dishes.add(dish);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return dishes;
    }

    public List<Ingredient> findIngredientsByCriteria(String ingredientName, CategoryEnum category, String dishName, int page, int size) {
        List<Ingredient> ingredients = new ArrayList<>();
        List<Object> params = new ArrayList<>();

        StringBuilder sql = new StringBuilder("SELECT DISTINCT i.* FROM ingredient i ");

        if (dishName != null && !dishName.trim().isEmpty()) {
            sql.append("JOIN DishIngredient di ON i.id = di.id_ingredient ");
            sql.append("JOIN dish d ON di.id_dish = d.id ");
        }

        sql.append("WHERE 1=1 ");

        if (ingredientName != null && !ingredientName.trim().isEmpty()) {
            sql.append("AND i.name ILIKE ? ");
            params.add("%" + ingredientName.trim() + "%");
        }
        if (category != null) {
            sql.append("AND i.category = ?::Category ");
            params.add(category.name());
        }
        if (dishName != null && !dishName.trim().isEmpty()) {
            sql.append("AND d.name ILIKE ? ");
            params.add("%" + dishName.trim() + "%");
        }

        sql.append("ORDER BY i.name LIMIT ? OFFSET ?");
        params.add(size);
        params.add((page - 1) * size);

        try (Connection conn = dbConnection.getDBConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                ingredients.add(new Ingredient(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getDouble("price"),
                        CategoryEnum.valueOf(rs.getString("category")),
                        null
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return ingredients;
    }
}
