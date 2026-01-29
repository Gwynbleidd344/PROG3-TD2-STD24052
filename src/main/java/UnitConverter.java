import java.util.Map;

public class UnitConverter {
    private static final Map<String, Map<Unit, Double>> rates = Map.of(
            "Tomate",  Map.of(Unit.KG, 1.0, Unit.PCS, 10.0),
            "Laitue",  Map.of(Unit.KG, 1.0, Unit.PCS, 2.0),
            "Chocolat", Map.of(Unit.KG, 1.0, Unit.PCS, 10.0, Unit.L, 2.5),
            "Poulet",  Map.of(Unit.KG, 1.0, Unit.PCS, 8.0),
            "Beurre",  Map.of(Unit.KG, 1.0, Unit.PCS, 4.0, Unit.L, 5.0)
    );

    public static double convertToKg(String name, double qty, Unit unit) {
        if (unit == Unit.KG) return qty;
        if (!rates.containsKey(name) || !rates.get(name).containsKey(unit)) {
            throw new RuntimeException("Conversion impossible pour " + name + " en " + unit);
        }
        return qty / rates.get(name).get(unit);
    }
}