import java.util.HashMap;
import java.util.Map;

public class UnitConverter {

    private static final Map<String, Map<UnitType, Map<UnitType, Double>>> RULES = new HashMap<>();

    public UnitConverter(){
        addRule("Tomate", UnitType.KG, UnitType.PCS, 10.0);

        addRule("Laitue", UnitType.KG, UnitType.PCS, 2.0);

        addRule("Chocolat", UnitType.KG, UnitType.PCS, 10.0);
        addRule("Chocolat", UnitType.KG, UnitType.L, 2.5);

        addRule("Poulet", UnitType.KG, UnitType.PCS, 8.0);

        addRule("Beurre", UnitType.KG, UnitType.PCS, 4.0);
        addRule("Beurre", UnitType.KG, UnitType.L, 5.0);
    }

    private static void addRule(String ingredientName, UnitType from, UnitType to, double factor) {
        RULES.computeIfAbsent(ingredientName, k -> new HashMap<>())
                .computeIfAbsent(from, k -> new HashMap<>())
                .put(to, factor);

        RULES.computeIfAbsent(ingredientName, k -> new HashMap<>())
                .computeIfAbsent(to, k -> new HashMap<>())
                .put(from, 1.0 / factor);
    }

    public static double convert(String ingredientName, double quantity, UnitType from, UnitType to) {
        if (from == to) return quantity;

        Map<UnitType, Map<UnitType, Double>> ingredientRules = RULES.get(ingredientName);

        if (ingredientRules == null
                || ingredientRules.get(from) == null
                || ingredientRules.get(from).get(to) == null) {
            throw new RuntimeException(
                    "Impossible conversion for ingredient " + ingredientName
                            + " from " + from + " to " + to
            );
        }

        return quantity * ingredientRules.get(from).get(to);
    }
}
