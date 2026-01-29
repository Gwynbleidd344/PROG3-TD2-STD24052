public class Main {
    public static void main(String[] args) {
        DataRetriever dataRetriever = new DataRetriever();

        System.out.println("--- TEST 1 : Récupération et Marge Brute (Salade fraîche) ---");
        try {
            Dish salade = dataRetriever.findDishById(1);
            System.out.println("Nom : " + salade.getName());
            System.out.println("Prix de vente : " + salade.getPrice());
            System.out.println("Coût des ingrédients : " + salade.getDishCost());
            System.out.println("Marge brute : " + salade.getGrossMargin());
        } catch (Exception e) {
            System.err.println("Erreur Test 1 : " + e.getMessage());
        }

        System.out.println("\n--- TEST 2 : Gestion de l'exception (Prix NULL) ---");
        try {
            Dish riz = dataRetriever.findDishById(3);
            System.out.println("Plat : " + riz.getName() + " | Prix : " + riz.getPrice());
            System.out.println("Tentative de calcul de marge...");
            System.out.println("Marge : " + riz.getGrossMargin());
        } catch (RuntimeException e) {
            System.out.println("Succès : L'exception a bien été levée car : " + e.getMessage());
        }

        System.out.println("\n--- TEST 3 : Sauvegarde et Mise à jour du prix ---");
        try {
            Dish rizToUpdate = dataRetriever.findDishById(3);
            rizToUpdate.setPrice(5000.0);
            dataRetriever.saveDish(rizToUpdate);

            Dish rizUpdated = dataRetriever.findDishById(3);
            System.out.println("Nouveau prix du riz : " + rizUpdated.getPrice());
            System.out.println("Nouvelle marge brute : " + rizUpdated.getGrossMargin());
        } catch (Exception e) {
            System.err.println("Erreur Test 3 : " + e.getMessage());
        }

        System.out.println("\n--- TEST 4 : Coût du Poulet Grillé (Validation PDF) ---");
        try {
            Dish poulet = dataRetriever.findDishById(2);
            System.out.println("Plat : " + poulet.getName());
            System.out.println("Coût calculé : " + poulet.getDishCost());
            System.out.println("Marge calculée : " + poulet.getGrossMargin());
        } catch (Exception e) {
            System.err.println("Erreur Test 4 : " + e.getMessage());
        }
    }
}