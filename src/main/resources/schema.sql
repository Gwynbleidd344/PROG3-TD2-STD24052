CREATE TYPE Category AS ENUM (VEGETABLE,ANIMAL, MARINE, DAIRY, OTHER);
CREATE TYPE Dish_type AS ENUM (START, MAIN, DESSERT);
CREATE TABLE Ingredient (
    id serial PRIMARY KEY NOT NULL,
    name VARCHAR(50) NOT NULL,
    price numeric(10,2),
    category Category,
    id_dish int,
    CONSTRAINT fk_id_dish foreign key (id_dish) references Dish(id)
);
CREATE TABLE Dish (
    id serial PRIMARY KEY NOT NULL,
    name varchar(100),
    dish_type Dish_type
);