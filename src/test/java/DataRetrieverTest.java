import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DataRetrieverTest {

    private DataRetriever dataRetriever;
    private Connection conn;

    @BeforeEach
    void setUp() {
        dataRetriever = new DataRetriever();
        conn = dataRetriever.dbConnection.getDBConnection();
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DELETE FROM Ingredient");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @AfterEach
    void tearDown() {
        try {
            if (conn != null && !conn.isClosed()) {
                conn.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Test
    void createIngredients_success() {
        List<Ingredient> newIngredients = new ArrayList<>();
        newIngredients.add(new Ingredient(0, "Flour", 1.5, CategoryEnum.FOOD, null));
        newIngredients.add(new Ingredient(0, "Sugar", 2.0, CategoryEnum.FOOD, null));

        List<Ingredient> createdIngredients = dataRetriever.createIngredients(newIngredients);

        assertNotNull(createdIngredients);
        assertEquals(2, createdIngredients.size());
    }

    @Test
    void createIngredients_duplicateIngredient_shouldThrowRuntimeException() {
        List<Ingredient> newIngredients = new ArrayList<>();
        newIngredients.add(new Ingredient(0, "Flour", 1.5, CategoryEnum.FOOD, null));
        dataRetriever.createIngredients(newIngredients);

        List<Ingredient> ingredientsWithDuplicate = new ArrayList<>();
        ingredientsWithDuplicate.add(new Ingredient(0, "Sugar", 2.0, CategoryEnum.FOOD, null));
        ingredientsWithDuplicate.add(new Ingredient(0, "Flour", 1.5, CategoryEnum.FOOD, null));

        assertThrows(RuntimeException.class, () -> {
            dataRetriever.createIngredients(ingredientsWithDuplicate);
        });
    }

    @Test
    void createIngredients_rollbackOnError() throws SQLException {
        List<Ingredient> initialIngredients = new ArrayList<>();
        initialIngredients.add(new Ingredient(0, "Yeast", 0.5, CategoryEnum.FOOD, null));
        dataRetriever.createIngredients(initialIngredients);

        List<Ingredient> ingredientsToTestRollback = new ArrayList<>();
        ingredientsToTestRollback.add(new Ingredient(0, "Salt", 1.0, CategoryEnum.FOOD, null));
        ingredientsToTestRollback.add(new Ingredient(0, "Yeast", 0.5, CategoryEnum.FOOD, null)); // Duplicate

        assertThrows(RuntimeException.class, () -> {
            dataRetriever.createIngredients(ingredientsToTestRollback);
        });
        
        try (Statement stmt = conn.createStatement()) {
            var rs = stmt.executeQuery("SELECT COUNT(*) FROM Ingredient WHERE name = 'Salt'");
            assertTrue(rs.next());
            assertEquals(0, rs.getInt(1), "The ingredient 'Salt' should have been rolled back");
        }
    }

    @Test
    void findDishsByIngredientName_shouldReturnCorrectDishes() throws SQLException {
        // 1. Create and save ingredients
        List<Ingredient> ingredientsToCreate = new ArrayList<>();
        Ingredient flour = new Ingredient(0, "Flour", 1.5, CategoryEnum.FOOD, null);
        Ingredient sugar = new Ingredient(0, "Sugar", 2.0, CategoryEnum.FOOD, null);
        Ingredient egg = new Ingredient(0, "Egg", 0.3, CategoryEnum.FOOD, null);
        Ingredient salt = new Ingredient(0, "Salt", 0.1, CategoryEnum.FOOD, null);
        Ingredient chicken = new Ingredient(0, "Chicken", 5.0, CategoryEnum.FOOD, null);
        ingredientsToCreate.add(flour);
        ingredientsToCreate.add(sugar);
        ingredientsToCreate.add(egg);
        ingredientsToCreate.add(salt);
        ingredientsToCreate.add(chicken);
        dataRetriever.createIngredients(ingredientsToCreate);

        // Retrieve saved ingredients to get their IDs
        try (Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT id, name FROM Ingredient WHERE name IN ('Flour', 'Sugar', 'Egg', 'Salt', 'Chicken')");
            while (rs.next()) {
                String name = rs.getString("name");
                int id = rs.getInt("id");
                if (name.equals("Flour")) flour.setId(id);
                if (name.equals("Sugar")) sugar.setId(id);
                if (name.equals("Egg")) egg.setId(id);
                if (name.equals("Salt")) salt.setId(id);
                if (name.equals("Chicken")) chicken.setId(id);
            }
        }

        // 2. Create and save dishes with associated ingredients
        Dish cake = new Dish(0, "Cake", DishTypeEnum.DESSERT, new ArrayList<>(List.of(flour, sugar, egg)));
        Dish omelette = new Dish(0, "Omelette", DishTypeEnum.START, new ArrayList<>(List.of(egg, salt)));
        Dish roastChicken = new Dish(0, "Roast Chicken", DishTypeEnum.MAIN, new ArrayList<>(List.of(chicken, salt)));

        dataRetriever.saveDish(cake);
        dataRetriever.saveDish(omelette);
        dataRetriever.saveDish(roastChicken);

        // 3. Call findDishsByIngredientName
        List<Dish> dishesWithEgg = dataRetriever.findDishsByIngredientName("Egg");
        assertNotNull(dishesWithEgg);
        assertEquals(2, dishesWithEgg.size()); // Cake and Omelette

        // Verify dishes
        Dish foundCake = dishesWithEgg.stream().filter(d -> d.getName().equals("Cake")).findFirst().orElse(null);
        assertNotNull(foundCake);
        assertEquals(3, foundCake.getIngredients().size());
        assertTrue(foundCake.getIngredients().stream().anyMatch(i -> i.getName().equals("Flour")));
        assertTrue(foundCake.getIngredients().stream().anyMatch(i -> i.getName().equals("Sugar")));
        assertTrue(foundCake.getIngredients().stream().anyMatch(i -> i.getName().equals("Egg")));

        Dish foundOmelette = dishesWithEgg.stream().filter(d -> d.getName().equals("Omelette")).findFirst().orElse(null);
        assertNotNull(foundOmelette);
        assertEquals(2, foundOmelette.getIngredients().size());
        assertTrue(foundOmelette.getIngredients().stream().anyMatch(i -> i.getName().equals("Egg")));
        assertTrue(foundOmelette.getIngredients().stream().anyMatch(i -> i.getName().equals("Salt")));

        List<Dish> dishesWithSalt = dataRetriever.findDishsByIngredientName("Salt");
        assertNotNull(dishesWithSalt);
        assertEquals(2, dishesWithSalt.size()); // Omelette and Roast Chicken

        List<Dish> dishesWithNonExistentIngredient = dataRetriever.findDishsByIngredientName("Pineapple");
        assertNotNull(dishesWithNonExistentIngredient);
        assertTrue(dishesWithNonExistentIngredient.isEmpty());
    }
}
