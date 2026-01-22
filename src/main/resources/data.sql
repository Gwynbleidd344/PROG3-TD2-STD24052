INSERT INTO Dish (id, name, dish_type)
VALUES (1, 'Salade fraîche', 'START'),
       (2, 'Poulet grillé', 'MAIN'),
       (3, 'Riz aux légumes', 'MAIN'),
       (4, 'Gâteau au chocolat', 'DESSERT'),
       (5, 'Salade de fruits', 'DESSERT');

INSERT INTO Ingredient (id, name, price, category, id_dish)
VALUES (1, 'Laitue', 800.00, 'VEGETABLE', 1),
       (2, 'Tomate', 600.00, 'VEGETABLE', 1),
       (3, 'Poulet', 4500.00, 'ANIMAL', 2),
       (4, 'Chocolat', 3000.00, 'OTHER', 4),
       (5, 'Beurre', 2500.00, 'DAIRY', 4);

select setval(pg_get_serial_sequence('Dish', 'id'),
              (select max(id) from Dish));
select setval(pg_get_serial_sequence('Ingredient', 'id'),
              (select max(id) from Ingredient));

UPDATE Dish
SET price = CASE name
                WHEN 'Salade fraîche' THEN 2000
                WHEN 'Poulet grillé' THEN 6000
                WHEN 'Riz au légume' THEN NULL
                WHEN 'Gâteau au chocolat' THEN NULL
                WHEN 'Salade de fruit' THEN NULL
                ELSE price
    END;

INSERT INTO DishIngredient (id, id_dish, id_ingredient, quantity_required, unit)
VALUES (1, 1, 1, 0.20, 'KG'),
       (2, 1, 2, 0.15, 'KG'),
       (3, 2, 3, 1.00, 'KG'),
       (4, 4, 4, 0.30, 'KG'),
       (5, 4, 5, 0.20, 'KG');

UPDATE Dish
SET price = 3500.00
WHERE id = 1;
UPDATE Dish
SET price = 12000.00
WHERE id = 2;
UPDATE Dish
SET price = NULL
WHERE id = 3;
UPDATE Dish
SET price = 8000.00
WHERE id = 4;
UPDATE Dish
SET price = NULL
WHERE id = 5;

INSERT INTO stock_movement (id, id_ingredient, quantity, type, unit, creation_datetime)
VALUES (1, 1, 5.0, 'IN', 'KG', '2024-01-05 08:00'),
       (2, 1, 0.2, 'OUT', 'KG', '2024-01-06 12:00'),
       (3, 2, 4.0, 'IN', 'KG', '2024-01-05 08:00'),
       (4, 2, 0.15, 'OUT', 'KG', '2024-01-06 12:00'),
       (5, 3, 10.0, 'IN', 'KG', '2024-01-04 09:00'),
       (6, 3, 1.0, 'OUT', 'KG', '2024-01-06 13:00'),
       (7, 4, 3.0, 'IN', 'KG', '2024-01-05 10:00'),
       (8, 4, 0.3, 'OUT', 'KG', '2024-01-06 14:00'),
       (9, 5, 2.5, 'IN', 'KG', '2024-01-05 10:00'),
       (10, 5, 0.2, 'OUT', 'KG', '2024-01-06 14:00')
ON CONFLICT (id) DO NOTHING;