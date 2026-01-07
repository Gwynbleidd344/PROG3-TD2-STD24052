import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestMethodOrder;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DataRetrieverTest {

    private DataRetriever dataRetriever;
    private Connection conn;

    @BeforeEach
    void setUp() {
        dataRetriever = new DataRetriever();
        conn = dataRetriever.dbConnection.getDBConnection();
        try (Statement stmt = conn.createStatement()) {
            // Drop existing objects in reverse order of dependency
            stmt.executeUpdate("DROP TABLE IF EXISTS \"Ingredient\";");
            stmt.executeUpdate("DROP TABLE IF EXISTS \"Dish\";");
            stmt.executeUpdate("DROP TYPE IF EXISTS \"Dish_type\";");
            stmt.executeUpdate("DROP TYPE IF EXISTS \"Category\";");

            // Recreate objects from schema
            stmt.executeUpdate("CREATE TYPE Category AS ENUM ('VEGETABLE','ANIMAL','MARINE', 'DAIRY', 'OTHER');");
            stmt.executeUpdate("CREATE TYPE Dish_type AS ENUM ('START', 'MAIN', 'DESSERT');");
            stmt.executeUpdate("CREATE TABLE Dish (" +
                               "    id serial PRIMARY KEY NOT NULL," +
                               "    name varchar(100) NOT NULL ," +
                               "    dish_type Dish_type NOT NULL" +
                               ");");
            stmt.executeUpdate("CREATE TABLE Ingredient (" +
                               "    id serial PRIMARY KEY NOT NULL," +
                               "    name VARCHAR(50) NOT NULL," +
                               "    price numeric(10,2) NOT NULL ," +
                               "    category Category NOT NULL ," +
                               "    id_dish int," +
                               "    CONSTRAINT fk_id_dish foreign key (id_dish) references Dish(id)" +
                               ");");
        } catch (SQLException e) {
            fail("Failed to set up test database by recreating schema: " + e.getMessage());
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

    private Ingredient getIngredientByName(String name) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT id, name, price, category FROM Ingredient WHERE name = '" + name + "'");
            if (rs.next()) {
                return new Ingredient(rs.getInt("id"), rs.getString("name"), rs.getDouble("price"), CategoryEnum.valueOf(rs.getString("category")), null);
            }
        }
        return null;
    }

    @Test
    @Order(1)
    void a_findDishById_found() throws SQLException {
        Ingredient laitue = new Ingredient(0, "Laitue", 1500.0, CategoryEnum.VEGETABLE, null);
        Ingredient tomate = new Ingredient(0, "Tomate", 1000.0, CategoryEnum.VEGETABLE, null);
        dataRetriever.createIngredients(new ArrayList<>(List.of(laitue, tomate)));
        laitue = getIngredientByName("Laitue");
        tomate = getIngredientByName("Tomate");

        Dish salade = new Dish(0, "Salade Fraîche", DishTypeEnum.START, new ArrayList<>(List.of(laitue, tomate)));
        salade = dataRetriever.saveDish(salade);

        Dish found = dataRetriever.findDishById(salade.getId());
        assertNotNull(found);
        assertEquals("Salade Fraîche", found.getName());
        assertEquals(2, found.getIngredients().size());
    }

    @Test
    @Order(2)
    void b_findDishById_notFound() throws SQLException {
        Dish dish = dataRetriever.findDishById(999);
        assertNull(dish);
    }
    
    @Test
    @Order(3)
    void c_findIngredients_pagination() throws SQLException {
        dataRetriever.createIngredients(new ArrayList<>(List.of(
            new Ingredient(0, "I1", 1.0, CategoryEnum.OTHER, null),
            new Ingredient(0, "I2", 1.0, CategoryEnum.OTHER, null),
            new Ingredient(0, "Poulet", 1.0, CategoryEnum.ANIMAL, null),
            new Ingredient(0, "Chocolat", 1.0, CategoryEnum.OTHER, null),
            new Ingredient(0, "I5", 1.0, CategoryEnum.OTHER, null)
        )));
        List<Ingredient> ingredients = dataRetriever.findIngredients(2, 2);
        assertEquals(2, ingredients.size());
        assertEquals("Poulet", ingredients.get(0).getName());
        assertEquals("Chocolat", ingredients.get(1).getName());
    }

    @Test
    @Order(4)
    void d_findIngredients_pagination_empty() throws SQLException {
        dataRetriever.createIngredients(new ArrayList<>(List.of(
            new Ingredient(0, "I1", 1.0, CategoryEnum.OTHER, null),
            new Ingredient(0, "I2", 1.0, CategoryEnum.OTHER, null)
        )));
        List<Ingredient> ingredients = dataRetriever.findIngredients(3, 5);
        assertTrue(ingredients.isEmpty());
    }

    @Test
    @Order(5)
    void e_findDishsByIngredientName() throws SQLException {
        Ingredient farine = new Ingredient(0, "Farine", 2000.0, CategoryEnum.OTHER, null);
        dataRetriever.createIngredients(new ArrayList<>(List.of(farine)));
        farine = getIngredientByName("Farine");

        Dish gateau = new Dish(0, "Gâteau au chocolat", DishTypeEnum.DESSERT, new ArrayList<>(List.of(farine)));
        dataRetriever.saveDish(gateau);

        List<Dish> dishes = dataRetriever.findDishsByIngredientName("eur");
        assertEquals(1, dishes.size());
        assertEquals("Gâteau au chocolat", dishes.get(0).getName());
    }
    
    @Test
    @Order(6)
    void f_findIngredientsByCriteria_category() throws SQLException {
        dataRetriever.createIngredients(new ArrayList<>(List.of(
            new Ingredient(0, "Laitue", 1.0, CategoryEnum.VEGETABLE, null),
            new Ingredient(0, "Tomate", 1.0, CategoryEnum.VEGETABLE, null),
            new Ingredient(0, "Poulet", 1.0, CategoryEnum.ANIMAL, null)
        )));
        List<Ingredient> ingredients = dataRetriever.findIngredientsByCriteria(null, CategoryEnum.VEGETABLE, null, 1, 10);
        assertEquals(2, ingredients.size());
    }

    @Test
    @Order(7)
    void g_findIngredientsByCriteria_noResult() throws SQLException {
        Ingredient chocolat = new Ingredient(0, "Chocolat", 5000.0, CategoryEnum.OTHER, null);
        Ingredient laitue = new Ingredient(0, "Laitue", 1500.0, CategoryEnum.VEGETABLE, null);
        dataRetriever.createIngredients(new ArrayList<>(List.of(chocolat, laitue)));
        laitue = getIngredientByName("Laitue");
        Dish salade = new Dish(0, "Salade", DishTypeEnum.START, new ArrayList<>(List.of(laitue)));
        dataRetriever.saveDish(salade);

        List<Ingredient> ingredients = dataRetriever.findIngredientsByCriteria("cho", null, "Sal", 1, 10);
        assertTrue(ingredients.isEmpty());
    }
    
    @Test
    @Order(8)
    void h_findIngredientsByCriteria_withResult() throws SQLException {
        Ingredient chocolat = new Ingredient(0, "Chocolat", 5000.0, CategoryEnum.OTHER, null);
        dataRetriever.createIngredients(new ArrayList<>(List.of(chocolat)));
        chocolat = getIngredientByName("Chocolat");
        Dish gateau = new Dish(0, "Gâteau au chocolat", DishTypeEnum.DESSERT, new ArrayList<>(List.of(chocolat)));
        dataRetriever.saveDish(gateau);

        List<Ingredient> ingredients = dataRetriever.findIngredientsByCriteria("cho", null, "gâteau", 1, 10);
        assertEquals(1, ingredients.size());
        assertEquals("Chocolat", ingredients.get(0).getName());
    }

    @Test
    @Order(9)
    void i_createIngredients_success() {
        List<Ingredient> newIngredients = new ArrayList<>();
        newIngredients.add(new Ingredient(0, "Fromage", 1200.0, CategoryEnum.DAIRY, null));
        newIngredients.add(new Ingredient(0, "Oignon", 500.0, CategoryEnum.VEGETABLE, null));

        List<Ingredient> created = dataRetriever.createIngredients(newIngredients);
        assertNotNull(created);
        assertEquals(2, created.size());
    }

    @Test
    @Order(10)
    void j_createIngredients_duplicateThrowsException() {
        dataRetriever.createIngredients(new ArrayList<>(List.of(new Ingredient(0, "Laitue", 2000.0, CategoryEnum.VEGETABLE, null))));
        
        List<Ingredient> ingredientsWithDuplicate = new ArrayList<>();
        ingredientsWithDuplicate.add(new Ingredient(0, "Carotte", 2000.0, CategoryEnum.VEGETABLE, null));
        ingredientsWithDuplicate.add(new Ingredient(0, "Laitue", 2000.0, CategoryEnum.VEGETABLE, null));

        assertThrows(RuntimeException.class, () -> dataRetriever.createIngredients(ingredientsWithDuplicate));
    }

    @Test
    @Order(11)
    void k_saveDish_newDish() throws SQLException {
        Ingredient oignon = new Ingredient(0, "Oignon", 500.0, CategoryEnum.VEGETABLE, null);
        dataRetriever.createIngredients(new ArrayList<>(List.of(oignon)));
        oignon = getIngredientByName("Oignon");

        Dish soupe = new Dish(0, "Soupe de légumes", DishTypeEnum.START, new ArrayList<>(List.of(oignon)));
        Dish saved = dataRetriever.saveDish(soupe);

        assertNotEquals(0, saved.getId());
        assertEquals(1, saved.getIngredients().size());
        assertEquals("Oignon", saved.getIngredients().get(0).getName());
    }

    @Test
    @Order(12)
    void l_saveDish_addIngredients() throws SQLException {
        Ingredient laitue = new Ingredient(0, "Laitue", 1.0, CategoryEnum.VEGETABLE, null);
        Ingredient tomate = new Ingredient(0, "Tomate", 1.0, CategoryEnum.VEGETABLE, null);
        dataRetriever.createIngredients(new ArrayList<>(List.of(laitue, tomate)));
        laitue = getIngredientByName("Laitue");
        tomate = getIngredientByName("Tomate");

        Dish salade = new Dish(0, "Salade Fraîche", DishTypeEnum.START, new ArrayList<>(List.of(laitue, tomate)));
        salade = dataRetriever.saveDish(salade);

        Ingredient oignon = new Ingredient(0, "Oignon", 1.0, CategoryEnum.VEGETABLE, null);
        Ingredient fromage = new Ingredient(0, "Fromage", 1.0, CategoryEnum.DAIRY, null);
        dataRetriever.createIngredients(new ArrayList<>(List.of(oignon, fromage)));
        oignon = getIngredientByName("Oignon");
        fromage = getIngredientByName("Fromage");
        
        salade.getIngredients().add(oignon);
        salade.getIngredients().add(fromage);
        
        Dish saved = dataRetriever.saveDish(salade);
        
        Dish finalDish = dataRetriever.findDishById(saved.getId());
        assertEquals(4, finalDish.getIngredients().size());
    }

    @Test
    @Order(13)
    void m_saveDish_removeAndUpdateIngredients() throws SQLException {
        Ingredient laitue = new Ingredient(0, "Laitue", 1.0, CategoryEnum.VEGETABLE, null);
        Ingredient tomate = new Ingredient(0, "Tomate", 1.0, CategoryEnum.VEGETABLE, null);
        Ingredient fromage = new Ingredient(0, "Fromage", 1.0, CategoryEnum.DAIRY, null);
        dataRetriever.createIngredients(new ArrayList<>(List.of(laitue, tomate, fromage)));
        laitue = getIngredientByName("Laitue");
        tomate = getIngredientByName("Tomate");
        fromage = getIngredientByName("Fromage");

        Dish salade = new Dish(0, "Salade Fraîche", DishTypeEnum.START, new ArrayList<>(List.of(laitue, tomate, fromage)));
        salade = dataRetriever.saveDish(salade);

        Dish updatedSalade = new Dish(salade.getId(), "Salade de fromage", DishTypeEnum.START, new ArrayList<>(List.of(fromage)));
        Dish saved = dataRetriever.saveDish(updatedSalade);

        Dish finalDish = dataRetriever.findDishById(saved.getId());
        assertEquals("Salade de fromage", finalDish.getName());
        assertEquals(1, finalDish.getIngredients().size());
        assertEquals("Fromage", finalDish.getIngredients().get(0).getName());
    }
}