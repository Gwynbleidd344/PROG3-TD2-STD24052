import org.junit.jupiter.api.Test;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DataRetrieverTest {
    DataRetriever dataRetriever = new DataRetriever();

    @Test
    void saveOrder_should_throw_exception_when_table_is_already_taken() {
        // Préparation d'une commande qui chevauche la commande existante (Table 1, 12h30)
        Order conflictingOrder = new Order();
        conflictingOrder.setReference("NEW_REQ_001");
        conflictingOrder.setCreationDatetime(Instant.now());
        conflictingOrder.setIdTable(1); // Même table que l'existante
        conflictingOrder.setArrivalDatetime(LocalDateTime.of(2026, 1, 30, 12, 30));
        conflictingOrder.setDepartureDatetime(LocalDateTime.of(2026, 1, 30, 13, 30));

        // On ajoute un plat pour que la validation des stocks passe aussi
        DishOrder dishOrder = new DishOrder();
        dishOrder.setDish(dataRetriever.findDishById(1));
        dishOrder.setQuantity(1);
        conflictingOrder.setDishOrderList(List.of(dishOrder));

        // Vérification que l'exception est levée
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            dataRetriever.saveOrder(conflictingOrder);
        });

        // Vérification que le message propose les tables 2 et 3
        assertTrue(exception.getMessage().contains("La table n'est pas disponible"));
        assertTrue(exception.getMessage().contains("numéro 2"));
        assertTrue(exception.getMessage().contains("numéro 3"));
        assertFalse(exception.getMessage().contains("numéro 1")); // La 1 ne doit pas être proposée
    }

    @Test
    void saveOrder_should_succeed_when_table_is_free() {
        Order validOrder = new Order();
        validOrder.setReference("SUCCESS_ORD_99");
        validOrder.setCreationDatetime(Instant.now());
        validOrder.setIdTable(2); // Table 2 est libre
        validOrder.setArrivalDatetime(LocalDateTime.of(2026, 1, 30, 12, 0));
        validOrder.setDepartureDatetime(LocalDateTime.of(2026, 1, 30, 14, 0));

        DishOrder dishOrder = new DishOrder();
        dishOrder.setDish(dataRetriever.findDishById(1));
        dishOrder.setQuantity(1);
        validOrder.setDishOrderList(List.of(dishOrder));

        // Ne doit pas lever d'exception
        assertDoesNotThrow(() -> {
            dataRetriever.saveOrder(validOrder);
        });
    }

    @Test
    void saveOrder_should_succeed_on_same_table_at_different_time() {
        Order validOrder = new Order();
        validOrder.setReference("LATE_ORD_01");
        validOrder.setCreationDatetime(Instant.now());
        validOrder.setIdTable(1);
        // La table 1 est prise de 12h à 14h, donc à 15h c'est libre
        validOrder.setArrivalDatetime(LocalDateTime.of(2026, 1, 30, 15, 0));
        validOrder.setDepartureDatetime(LocalDateTime.of(2026, 1, 30, 17, 0));

        DishOrder dishOrder = new DishOrder();
        dishOrder.setDish(dataRetriever.findDishById(1));
        dishOrder.setQuantity(1);
        validOrder.setDishOrderList(List.of(dishOrder));

        assertDoesNotThrow(() -> {
            dataRetriever.saveOrder(validOrder);
        });
    }
}