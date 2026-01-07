public class Main {

    public static void main(String[] args) {
        DataRetriever dataRetriever = new DataRetriever();

        System.out.println("===== TEST findDishById =====");

        Dish dish = dataRetriever.findDishById(1);

        // Si le plat n'existe pas, on le crée pour le test
        if (dish == null) {
            System.out.println("Aucun plat trouvé avec l'ID 1, création d'un plat test.");
            Dish newDish = new Dish();
            newDish.setName("Plat test initial");
            newDish.setDishType(DishTypeEnum.MAIN);
            newDish.setPrice(5000.00);
            dish = dataRetriever.saveDish(newDish);
        }

        System.out.println("Plat récupéré : " + dish.getName());

        try {
            Double margin = dish.getGrossMargin();
            System.out.println("Marge brute : " + margin);
        } catch (IllegalStateException e) {
            System.out.println("Exception attendue : " + e.getMessage());
        }

        System.out.println("\n===== TEST saveDish (CREATION) =====");

        Dish newDish = new Dish();
        newDish.setName("Plat test");
        newDish.setDishType(DishTypeEnum.MAIN);
        newDish.setPrice(null);

        Dish savedDish = dataRetriever.saveDish(newDish);
        System.out.println("Plat créé avec ID : " + savedDish.getId());

        try {
            savedDish.getGrossMargin();
        } catch (IllegalStateException e) {
            System.out.println("Exception attendue après création : " + e.getMessage());
        }

        System.out.println("\n===== TEST saveDish (MISE A JOUR + PRICE) =====");

        savedDish.setPrice(8000.00);
        Dish updatedDish = dataRetriever.saveDish(savedDish);

        System.out.println("Plat mis à jour (ID = " + updatedDish.getId() + ")");
        System.out.println("Nouveau prix : " + updatedDish.getPrice());

        try {
            Double margin = updatedDish.getGrossMargin();
            System.out.println("Marge brute après mise à jour : " + margin);
        } catch (IllegalStateException e) {
            System.out.println("Erreur inattendue : " + e.getMessage());
        }

        System.out.println("\n===== FIN DES TESTS =====");
    }
}
