CREATE TYPE Category AS ENUM ('VEGETABLE','ANIMAL','MARINE', 'DAIRY', 'OTHER');
CREATE TYPE Dish_type AS ENUM ('START', 'MAIN', 'DESSERT');
CREATE TABLE Dish (
    id serial PRIMARY KEY NOT NULL,
    name varchar(100) NOT NULL ,
    dish_type Dish_type NOT NULL
);
CREATE TABLE Ingredient (
    id serial PRIMARY KEY NOT NULL,
    name VARCHAR(50) NOT NULL,
    price numeric(10,2) NOT NULL ,
    category Category NOT NULL ,
    id_dish int,
    CONSTRAINT fk_id_dish foreign key (id_dish) references Dish(id)
);

ALTER TABLE Dish ADD COLUMN IF NOT EXISTS price numeric(10,2);
