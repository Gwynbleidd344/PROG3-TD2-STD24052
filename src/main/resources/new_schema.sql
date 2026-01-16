CREATE TYPE unit_type AS ENUM ('PCS', 'KG', 'L');

ALTER TABLE Ingredient DROP COLUMN id_dish;

CREATE TABLE DishIngredient (
                                id serial PRIMARY KEY,
                                id_dish int NOT NULL,
                                id_ingredient int NOT NULL,
                                quantity_required numeric(10,2) NOT NULL,
                                unit unit_type NOT NULL,
                                CONSTRAINT fk_dish FOREIGN KEY (id_dish) REFERENCES Dish(id),
                                CONSTRAINT fk_ingredient FOREIGN KEY (id_ingredient) REFERENCES Ingredient(id)
);