CREATE DATABASE mini_dish_db;
CREATE USER mini_dish_db_manager;
GRANT CONNECT ON DATABASE mini_dish_db TO mini_dish_db_manager;
GRANT CREATE ON DATABASE mini_dish_db TO mini_dish_db_manager;
GRANT SELECT, INSERT, UPDATE, DELETE ON Ingredient TO mini_dish_db_manager;
GRANT SELECT, INSERT, UPDATE, DELETE ON Dish TO mini_dish_db_manager;