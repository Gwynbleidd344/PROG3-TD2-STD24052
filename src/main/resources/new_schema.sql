CREATE TYPE unit_type AS ENUM ('PCS', 'KG', 'L');
CREATE TYPE mouvement_type AS ENUM ('IN', 'OUT');

ALTER TABLE Ingredient
    DROP COLUMN id_dish;

CREATE TABLE DishIngredient (
    id                serial PRIMARY KEY,
    id_dish           int            NOT NULL,
    id_ingredient     int            NOT NULL,
    quantity_required numeric(10, 2) NOT NULL,
    unit              unit_type      NOT NULL,
    CONSTRAINT fk_dish FOREIGN KEY (id_dish) REFERENCES Dish (id),
    CONSTRAINT fk_ingredient FOREIGN KEY (id_ingredient) REFERENCES Ingredient (id)
);
CREATE TABLE IF NOT EXISTS StockMovement (
    id                SERIAL PRIMARY KEY,
    id_ingredient     INT REFERENCES ingredient (id),
    quantity          NUMERIC        NOT NULL,
    type              mouvement_type NOT NULL,
    unit              unit_type      NOT NULL,
    creation_datetime TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE Order (
    id SERIAL PRIMARY KEY,
    reference VARCHAR(50) NOT NULL UNIQUE,
    creation_datetime TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE DishOrder (
    id SERIAL PRIMARY KEY,
    id_order INT NOT NULL,
    id_dish INT NOT NULL,
    quantity INT NOT NULL,
    CONSTRAINT fk_order FOREIGN KEY (id_order) REFERENCES Order (id),
    CONSTRAINT fk_dish_order FOREIGN KEY (id_dish) REFERENCES Dish (id)
);