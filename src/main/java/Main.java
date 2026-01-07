public class Main {

    private final DataRetriever dataRetriever;

    public Main() {
        this.dataRetriever = new DataRetriever();
    }

    public void run() {

        System.out.println("===== TEST findDishById =====");

        // 1️⃣ Récupération d’un plat existant
        Dish dish = dataRetriever.findDishById(1); // ID existant
        System.out.println("Plat récupéré : " + dish.getName());

        // Test de la marge brute
        try {
            Double margin = dish.getGrossMargin();
            System.out.println("Marge brute : " + margin);
        } catch (IllegalStateException e) {
            System.out.println("Exception attendue : " + e.getMessage());
        }

        System.out.println("\n===== TEST saveDish (CREATION) =====");

        // 2️⃣ Création d’un nouveau plat sans prix
        Dish newDish = new Dish();
        newDish.setName("Plat test");
        newDish.setPrice(null);

        Dish savedDish = dataRetriever.saveDish(newDish);
        System.out.println("Plat créé avec ID : " + savedDish.getId());

        // Vérification marge brute → exception
        try {
            savedDish.getGrossMargin();
        } catch (IllegalStateException e) {
            System.out.println("Exception attendue après création : " + e.getMessage());
        }

        System.out.println("\n===== TEST saveDish (MISE A JOUR + PRICE) =====");

        // 3️⃣ Mise à jour du prix de vente
        savedDish.setPrice(8000.00);
        Dish updatedDish = dataRetriever.saveDish(savedDish);

        System.out.println("Plat mis à jour (ID = " + updatedDish.getId() + ")");
        System.out.println("Nouveau prix : " + updatedDish.getPrice());

        // Recalcul de la marge brute
        try {
            Double margin = updatedDish.getGrossMargin();
            System.out.println("Marge brute après mise à jour : " + margin);
        } catch (IllegalStateException e) {
            System.out.println("Erreur inattendue : " + e.getMessage());
        }

        System.out.println("\n===== FIN DES TESTS =====");
    }
}
