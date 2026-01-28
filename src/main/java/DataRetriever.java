import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class DataRetriever {

    public Dish findDishById(Integer id) {
        return findDishById(id, null);
    }

    public Dish findDishById(Integer id, Connection connection) {
        boolean closeConnection = false;
        Connection conn = connection;
        if (conn == null) {
            DBConnection dbConnection = new DBConnection();
            conn = dbConnection.getDBConnection();
            closeConnection = true;
        }
        try {
            PreparedStatement preparedStatement = conn.prepareStatement(
                    """
                    SELECT dish.id as dish_id, dish.name as dish_name, dish_type, dish.price as dish_price
                    FROM Dish
                    WHERE dish.id = ?
                    """);
            preparedStatement.setInt(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                Dish dish = new Dish();
                dish.setId(resultSet.getInt("dish_id"));
                dish.setName(resultSet.getString("dish_name"));
                dish.setDishType(DishTypeEnum.valueOf(resultSet.getString("dish_type")));
                dish.setPrice(resultSet.getObject("dish_price") == null
                        ? null : resultSet.getDouble("dish_price"));
                dish.setDishIngredients(findDishIngredientsByDishId(id, conn));
                return dish;
            }
            throw new RuntimeException("Dish not found " + id);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            if (closeConnection && conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public List<Ingredient> findIngredients(int page, int size) {
        DBConnection dbConnection = new DBConnection();
        Connection connection = dbConnection.getDBConnection();
        List<Ingredient> ingredients = new ArrayList<>();

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(
                    """
                    SELECT id, name, price, category
                    FROM Ingredient
                    ORDER BY id
                    LIMIT ? OFFSET ?
                    """);
            preparedStatement.setInt(1, size);
            preparedStatement.setInt(2, (page - 1) * size);

            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                Ingredient ingredient = new Ingredient();
                ingredient.setId(resultSet.getInt("id"));
                ingredient.setName(resultSet.getString("name"));
                ingredient.setPrice(resultSet.getDouble("price"));
                ingredient.setCategory(CategoryEnum.valueOf(resultSet.getString("category")));
                ingredients.add(ingredient);
            }

            dbConnection.closeConnection(connection);
            return ingredients;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Ingredient> createIngredients(List<Ingredient> newIngredients) {
        if (newIngredients == null || newIngredients.isEmpty()) {
            return List.of();
        }

        List<Ingredient> savedIngredients = new ArrayList<>();
        DBConnection dbConnection = new DBConnection();
        Connection conn = dbConnection.getDBConnection();

        try {
            conn.setAutoCommit(false);

            String checkSql = "SELECT COUNT(*) FROM Ingredient WHERE name = ?";
            for (Ingredient ingredient : newIngredients) {
                try (PreparedStatement ps = conn.prepareStatement(checkSql)) {
                    ps.setString(1, ingredient.getName());
                    ResultSet rs = ps.executeQuery();
                    if (rs.next() && rs.getInt(1) > 0) {
                        conn.rollback();
                        throw new RuntimeException("Ingredient with name '" + ingredient.getName() + "' already exists");
                    }
                }
            }

            String insertSql = """
                    INSERT INTO Ingredient (id, name, category, price)
                    VALUES (?, ?, ?::category_enum, ?)
                    RETURNING id
                    """;

            try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                for (Ingredient ingredient : newIngredients) {
                    if (ingredient.getId() != null) {
                        ps.setInt(1, ingredient.getId());
                    } else {
                        ps.setInt(1, getNextSerialValue(conn, "ingredient", "id"));
                    }
                    ps.setString(2, ingredient.getName());
                    ps.setString(3, ingredient.getCategory().name());
                    ps.setDouble(4, ingredient.getPrice());

                    try (ResultSet rs = ps.executeQuery()) {
                        rs.next();
                        int generatedId = rs.getInt(1);
                        ingredient.setId(generatedId);
                        savedIngredients.add(ingredient);
                    }
                }
                conn.commit();
                return savedIngredients;
            } catch (SQLException e) {
                conn.rollback();
                throw new RuntimeException(e);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            dbConnection.closeConnection(conn);
        }
    }

    public Dish saveDish(Dish toSave) {
        String upsertDishSql = """
                INSERT INTO Dish (id, price, name, dish_type)
                VALUES (?, ?, ?, ?::dish_type_enum)
                ON CONFLICT (id) DO UPDATE
                SET name = EXCLUDED.name,
                    dish_type = EXCLUDED.dish_type,
                    price = EXCLUDED.price
                RETURNING id
                """;

        try (Connection conn = new DBConnection().getDBConnection()) {
            conn.setAutoCommit(false);
            Integer dishId;

            try (PreparedStatement ps = conn.prepareStatement(upsertDishSql)) {
                if (toSave.getId() != null) {
                    ps.setInt(1, toSave.getId());
                } else {
                    ps.setInt(1, getNextSerialValue(conn, "dish", "id"));
                }

                if (toSave.getPrice() != null) {
                    ps.setDouble(2, toSave.getPrice());
                } else {
                    ps.setNull(2, Types.DOUBLE);
                }

                ps.setString(3, toSave.getName());
                ps.setString(4, toSave.getDishType().name());

                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    dishId = rs.getInt(1);
                }
            }

            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM DishIngredient WHERE id_dish = ?")) {
                ps.setInt(1, dishId);
                ps.executeUpdate();
            }

            String insertDishIngredientSql = """
                    INSERT INTO DishIngredient (id_dish, id_ingredient, quantity_required, unit)
                    VALUES (?, ?, ?, ?::unit_type)
                    """;
            
            try (PreparedStatement ps = conn.prepareStatement(insertDishIngredientSql)) {
                if (toSave.getDishIngredients() != null) {
                    for (DishIngredient di : toSave.getDishIngredients()) {
                        ps.setInt(1, dishId);
                        ps.setInt(2, di.getIngredient().getId());
                        ps.setDouble(3, di.getQuantityRequired());
                        ps.setString(4, di.getUnit().name());
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }
            }

            conn.commit();
            return findDishById(dishId);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Dish> findDishsByIngredientName(String ingredientName) {
        DBConnection dbConnection = new DBConnection();
        Connection connection = dbConnection.getDBConnection();
        List<Dish> dishes = new ArrayList<>();

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(
                    """
                    SELECT DISTINCT d.id
                    FROM Dish d
                    JOIN DishIngredient di ON d.id = di.id_dish
                    JOIN Ingredient i ON di.id_ingredient = i.id
                    WHERE i.name LIKE ?
                    """);
            preparedStatement.setString(1, "%" + ingredientName + "%");

            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                int dishId = resultSet.getInt("id");
                Dish dish = findDishById(dishId, connection);
                dishes.add(dish);
            }

            dbConnection.closeConnection(connection);
            return dishes;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Ingredient> findIngredientsByCriteria(String ingredientName, CategoryEnum category,
                                                      String dishName, int page, int size) {
        StringBuilder query = new StringBuilder(
                """
                SELECT DISTINCT i.id, i.name, i.price, i.category
                FROM Ingredient i
                LEFT JOIN DishIngredient di ON i.id = di.id_ingredient
                LEFT JOIN Dish d ON di.id_dish = d.id
                WHERE 1=1
                """);

        List<Object> params = new ArrayList<>();

        if (ingredientName != null && !ingredientName.isEmpty()) {
            query.append(" AND i.name LIKE ?");
            params.add("%" + ingredientName + "%");
        }

        if (category != null) {
            query.append(" AND i.category = ?::category_enum");
            params.add(category.name());
        }

        if (dishName != null && !dishName.isEmpty()) {
            query.append(" AND d.name LIKE ?");
            params.add("%" + dishName + "%");
        }

        query.append(" ORDER BY i.id LIMIT ? OFFSET ?");
        params.add(size);
        params.add((page - 1) * size);

        List<Ingredient> ingredients = new ArrayList<>();
        DBConnection dbConnection = new DBConnection();
        Connection connection = dbConnection.getDBConnection();

        try (PreparedStatement stmt = connection.prepareStatement(query.toString())) {
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Ingredient ingredient = new Ingredient();
                ingredient.setId(rs.getInt("id"));
                ingredient.setName(rs.getString("name"));
                ingredient.setPrice(rs.getDouble("price"));
                ingredient.setCategory(CategoryEnum.valueOf(rs.getString("category")));
                ingredients.add(ingredient);
            }

            dbConnection.closeConnection(connection);
            return ingredients;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Order saveOrder(Order orderToSave) {
        DBConnection dbConnection = new DBConnection();
        Connection connection = dbConnection.getDBConnection();
        try {
            connection.setAutoCommit(false);

            Map<Integer, Double> requiredQuantities = new HashMap<>();
            Map<Integer, UnitType> ingredientUnits = new HashMap<>();

            for (DishOrder dishOrder : orderToSave.getDishOrders()) {
                Dish dish = findDishById(dishOrder.getDish().getId(), connection);
                for (DishIngredient di : dish.getDishIngredients()) {
                    requiredQuantities.merge(
                            di.getIngredient().getId(),
                            di.getQuantityRequired() * dishOrder.getQuantity(),
                            Double::sum
                    );
                    ingredientUnits.put(di.getIngredient().getId(), di.getUnit());
                }
            }

            for (Map.Entry<Integer, Double> entry : requiredQuantities.entrySet()) {
                int ingredientId = entry.getKey();
                UnitConverter converter = new UnitConverter();

                StockValue currentStock = getCurrentStock(ingredientId, connection);
                UnitType stockUnit = currentStock.getUnit();

                UnitType requiredUnit = ingredientUnits.get(ingredientId);
                double requiredQuantity = entry.getValue();

                Ingredient ingredient = findIngredientById(ingredientId, connection);

                double requiredInStockUnit;
                try {
                    requiredInStockUnit = converter.convert(
                            ingredient.getName(),
                            requiredQuantity,
                            requiredUnit,
                            stockUnit
                    );
                } catch (RuntimeException e) {
                    throw new RuntimeException("Cannot convert required quantity for ingredient "
                            + ingredient.getName() + " from " + requiredUnit + " to stock unit " + stockUnit);
                }

                if (currentStock.getQuantity() < requiredInStockUnit) {
                    throw new RuntimeException("Insufficient stock for ingredient: " + ingredient.getName()
                            + " (need " + requiredInStockUnit + " " + stockUnit
                            + ", have " + currentStock.getQuantity() + " " + stockUnit + ")");
                }

            }

            String insertOrderSql = "INSERT INTO \"order\" (reference, creation_datetime) VALUES (?, ?) RETURNING id, creation_datetime";
            PreparedStatement orderPs = connection.prepareStatement(insertOrderSql);
            String reference = "ORD-" + UUID.randomUUID();
            orderToSave.setReference(reference);
            orderPs.setString(1, reference);
            orderToSave.setCreationDateTime(Instant.now());
            orderPs.setTimestamp(2, Timestamp.from(orderToSave.getCreationDateTime()));
            ResultSet rs = orderPs.executeQuery();
            if (rs.next()) {
                orderToSave.setId(rs.getInt(1));
                orderToSave.setCreationDateTime(rs.getTimestamp(2).toInstant());
            } else {
                throw new SQLException("Failed to create order, no ID obtained.");
            }

            String insertDishOrderSql = "INSERT INTO dishorder (id_order, id_dish, quantity) VALUES (?, ?, ?)";
            PreparedStatement dishOrderPs = connection.prepareStatement(insertDishOrderSql);
            for (DishOrder dishOrder : orderToSave.getDishOrders()) {
                dishOrderPs.setInt(1, orderToSave.getId());
                dishOrderPs.setInt(2, dishOrder.getDish().getId());
                dishOrderPs.setInt(3, dishOrder.getQuantity());
                dishOrderPs.addBatch();
            }
            dishOrderPs.executeBatch();

            String insertStockMovementSql = "INSERT INTO stockmovement (id_ingredient, quantity, type, unit, creation_datetime) VALUES (?, ?, 'OUT', ?::unit_type, ?)";
            PreparedStatement stockMovementPs = connection.prepareStatement(insertStockMovementSql);
            for (Map.Entry<Integer, Double> entry : requiredQuantities.entrySet()) {
                int ingredientId = entry.getKey();
                double requiredQuantity = entry.getValue();

                UnitType requiredUnit = ingredientUnits.get(ingredientId);

                StockValue currentStock = getCurrentStock(ingredientId, connection);
                UnitType stockUnit = currentStock.getUnit();

                Ingredient ingredient = findIngredientById(ingredientId, connection);

                double quantityToDecreaseInStockUnit = UnitConverter.convert(
                        ingredient.getName(),
                        requiredQuantity,
                        requiredUnit,
                        stockUnit
                );

                stockMovementPs.setInt(1, ingredientId);
                stockMovementPs.setDouble(2, quantityToDecreaseInStockUnit);
                stockMovementPs.setString(3, stockUnit.name());
                stockMovementPs.setTimestamp(4, Timestamp.from(Instant.now()));
                stockMovementPs.addBatch();
            }
            connection.commit();
            return orderToSave;
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                throw new RuntimeException("Rollback failed: " + ex.getMessage(), ex);
            }
            throw new RuntimeException(e);
        } finally {
            dbConnection.closeConnection(connection);
        }
    }

    public Order findOrderByReference(String reference) {
        DBConnection dbConnection = new DBConnection();
        Connection connection = dbConnection.getDBConnection();
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(
                    "SELECT id, reference, creation_datetime FROM \"order\" WHERE reference = ?");
            preparedStatement.setString(1, reference);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                int orderId = resultSet.getInt("id");
                Order order = new Order(
                        orderId,
                        resultSet.getString("reference"),
                        resultSet.getTimestamp("creation_datetime").toInstant(),
                        findDishOrdersByOrderId(orderId, connection)
                );
                return order;
            } else {
                throw new RuntimeException("Order not found with reference " + reference);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            dbConnection.closeConnection(connection);
        }
    }

    private List<DishOrder> findDishOrdersByOrderId(int orderId, Connection connection) throws SQLException {
        List<DishOrder> dishOrders = new ArrayList<>();
        String sql = "SELECT id, id_dish, quantity FROM dishorder WHERE id_order = ?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, orderId);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            Dish dish = findDishById(rs.getInt("id_dish"), connection);
            DishOrder dishOrder = new DishOrder(
                    rs.getInt("id"),
                    dish,
                    rs.getInt("quantity")
            );
            dishOrders.add(dishOrder);
        }
        return dishOrders;
    }

    private StockValue getCurrentStock(int ingredientId, Connection connection) throws SQLException {
        Double totalQuantity = 0.0;
        UnitType unit = null;

        String sql = "SELECT quantity, type, unit FROM stockmovement WHERE id_ingredient = ?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, ingredientId);

        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            double quantity = rs.getDouble("quantity");
            String type = rs.getString("type");
            UnitType movementUnit = UnitType.valueOf(rs.getString("unit"));

            if (unit == null) {
                unit = movementUnit;
            } else if (unit != movementUnit) {
                throw new RuntimeException("Stock unit mismatch for ingredientId=" + ingredientId
                        + " (found " + unit + " and " + movementUnit + ")");
            }

            if ("IN".equals(type)) {
                totalQuantity += quantity;
            } else if ("OUT".equals(type)) {
                totalQuantity -= quantity;
            }
        }

        if (unit == null) {
            unit = UnitType.KG;
        }
        return new StockValue(totalQuantity, unit);
    }


    private List<DishIngredient> findDishIngredientsByDishId(Integer dishId) {
        return findDishIngredientsByDishId(dishId, null);
    }

    private List<DishIngredient> findDishIngredientsByDishId(Integer dishId, Connection connection) {
        boolean closeConnection = false;
        Connection conn = connection;
        if (conn == null) {
            DBConnection dbConnection = new DBConnection();
            conn = dbConnection.getDBConnection();
            closeConnection = true;
        }
        List<DishIngredient> dishIngredients = new ArrayList<>();

        try {
            PreparedStatement preparedStatement = conn.prepareStatement(
                    """
                    SELECT di.id, di.id_dish, di.id_ingredient, di.quantity_required, di.unit,
                           i.name, i.price, i.category
                    FROM DishIngredient di
                    JOIN Ingredient i ON di.id_ingredient = i.id
                    WHERE di.id_dish = ?
                    """);
            preparedStatement.setInt(1, dishId);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                Ingredient ingredient = new Ingredient(
                        resultSet.getInt("id_ingredient"),
                        resultSet.getString("name"),
                        resultSet.getDouble("price"),
                        CategoryEnum.valueOf(resultSet.getString("category"))
                );

                Dish dish = new Dish();
                dish.setId(resultSet.getInt("id_dish"));

                DishIngredient dishIngredient = new DishIngredient(
                        resultSet.getInt("id"),
                        dish,
                        ingredient,
                        resultSet.getDouble("quantity_required"),
                        UnitType.valueOf(resultSet.getString("unit"))
                );
                dishIngredients.add(dishIngredient);
            }
            
            return dishIngredients;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            if (closeConnection && conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    public Ingredient findIngredientById(int id) {
        return findIngredientById(id, null);
    }
    
    public Ingredient findIngredientById(int id, Connection connection) {
        boolean closeConnection = false;
        Connection conn = connection;
        if (conn == null) {
            DBConnection dbConnection = new DBConnection();
            conn = dbConnection.getDBConnection();
            closeConnection = true;
        }
        try {
            PreparedStatement preparedStatement = conn.prepareStatement(
                    "SELECT id, name, price, category FROM Ingredient WHERE id = ?");
            preparedStatement.setInt(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                Ingredient ingredient = new Ingredient();
                ingredient.setId(resultSet.getInt("id"));
                ingredient.setName(resultSet.getString("name"));
                ingredient.setPrice(resultSet.getDouble("price"));
                ingredient.setCategory(CategoryEnum.valueOf(resultSet.getString("category")));
                ingredient.setStockMovementList(findStockMovementsByIngredientId(id, conn));
                return ingredient;
            }
            throw new RuntimeException("Ingredient not found " + id);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            if (closeConnection && conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    private List<StockMovement> findStockMovementsByIngredientId(int ingredientId) {
        return findStockMovementsByIngredientId(ingredientId, null);
    }

    private List<StockMovement> findStockMovementsByIngredientId(int ingredientId, Connection connection) {
        boolean closeConnection = false;
        Connection conn = connection;
        if (conn == null) {
            DBConnection dbConnection = new DBConnection();
            conn = dbConnection.getDBConnection();
            closeConnection = true;
        }
        List<StockMovement> movements = new ArrayList<>();
        try {
            PreparedStatement preparedStatement = conn.prepareStatement(
                    "SELECT id, id_ingredient, quantity, type, unit, creation_datetime FROM StockMovement WHERE id_ingredient = ?");
            preparedStatement.setInt(1, ingredientId);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                StockValue stockValue = new StockValue(
                        resultSet.getDouble("quantity"),
                        UnitType.valueOf(resultSet.getString("unit")));
                StockMovement movement = new StockMovement(
                        resultSet.getInt("id"),
                        stockValue,
                        MovementTypeEnum.valueOf(resultSet.getString("type")),
                        resultSet.getTimestamp("creation_datetime").toInstant());
                movements.add(movement);
            }
            return movements;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            if (closeConnection && conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public Ingredient saveIngredient(Ingredient toSave) {
        try (Connection conn = new DBConnection().getDBConnection()) {
            conn.setAutoCommit(false);

            String upsertIngredientSql = """
                INSERT INTO Ingredient (id, name, price, category)
                VALUES (?, ?, ?, ?::category_enum)
                ON CONFLICT (id) DO UPDATE
                SET name = EXCLUDED.name,
                    price = EXCLUDED.price,
                    category = EXCLUDED.category
                RETURNING id
                """;
            
            int ingredientId;
            try (PreparedStatement ps = conn.prepareStatement(upsertIngredientSql)) {
                if (toSave.getId() != null) {
                    ps.setInt(1, toSave.getId());
                } else {
                    ps.setInt(1, getNextSerialValue(conn, "ingredient", "id"));
                }
                ps.setString(2, toSave.getName());
                ps.setDouble(3, toSave.getPrice());
                ps.setString(4, toSave.getCategory().name());
                
                ResultSet rs = ps.executeQuery();
                rs.next();
                ingredientId = rs.getInt(1);
                toSave.setId(ingredientId);
            }

            if (toSave.getStockMovementList() != null && !toSave.getStockMovementList().isEmpty()) {
                saveStockMovements(conn, ingredientId, toSave.getStockMovementList());
            }

            conn.commit();
            return findIngredientById(ingredientId); 

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void saveStockMovements(Connection conn, int ingredientId, List<StockMovement> movements) throws SQLException {
        String sql = """
            INSERT INTO StockMovement (id, id_ingredient, quantity, type, unit, creation_datetime)
            VALUES (?, ?, ?, ?::movement_type, ?::unit_type, ?)
            ON CONFLICT (id) DO NOTHING
            """;
        
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (StockMovement movement : movements) {
                ps.setInt(1, movement.getId());
                ps.setInt(2, ingredientId);
                ps.setDouble(3, movement.getValue().getQuantity());
                ps.setString(4, movement.getType().name());
                ps.setString(5, movement.getValue().getUnit().name());
                ps.setTimestamp(6, Timestamp.from(movement.getCreationDateTime()));
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private String getSerialSequenceName(Connection conn, String tableName, String columnName)
            throws SQLException {
        String sql = "SELECT pg_get_serial_sequence(?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tableName);
            ps.setString(2, columnName);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString(1);
                }
            }
        }
        return null;
    }

    private int getNextSerialValue(Connection conn, String tableName, String columnName)
            throws SQLException {
        String sequenceName = getSerialSequenceName(conn, tableName, columnName);
        if (sequenceName == null) {
            throw new IllegalArgumentException(
                    "No sequence found for " + tableName + "." + columnName
            );
        }
        updateSequenceNextValue(conn, tableName, columnName, sequenceName);

        String nextValSql = "SELECT nextval(?)";

        try (PreparedStatement ps = conn.prepareStatement(nextValSql)) {
            ps.setString(1, sequenceName);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    private void updateSequenceNextValue(Connection conn, String tableName, String columnName, String sequenceName)
            throws SQLException {
        String setValSql = String.format(
                "SELECT setval('%s', (SELECT COALESCE(MAX(%s), 0) FROM %s))",
                sequenceName, columnName, tableName
        );

        try (PreparedStatement ps = conn.prepareStatement(setValSql)) {
            ps.executeQuery();
        }
    }
}
