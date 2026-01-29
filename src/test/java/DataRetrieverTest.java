import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DataRetrieverTest {
    DataRetriever dataRetriever = new DataRetriever();

    @Test
    void findDishById_should_return_correct_dish_when_exists() {
        // ID 1 doit exister dans ta base de données pour ce test
        Integer existingId = 1;

        Dish dish = dataRetriever.findDishById(existingId);

        assertNotNull(dish);
        assertEquals(existingId, dish.getId());
        assertNotNull(dish.getName());
        assertNotNull(dish.getDishType());

        assertNotNull(dish.getDishIngredients());
    }

    @Test
    void testFindDishById() {
        // [cite: 17, 18, 19]
        Dish dish = dataRetriever.findDishById(1);

        assertNotNull(dish); //
        assertEquals(1, dish.getId()); // [cite: 20, 124]
        assertNotNull(dish.getName()); // [cite: 20, 126]
        assertNotNull(dish.getDishType()); // [cite: 20, 128]

        // Verifies that ingredients are also loaded [cite: 21]
        assertNotNull(dish.getDishIngredients()); // [cite: 21, 112]
    }
}