-- Seed Tunisian Restaurants and Dishes

INSERT INTO users (id, phone, name, role, password_hash) VALUES 
('c0000000-0000-0000-0000-000000000001', '+21698765432', 'Ahmed Ben Ali', 'RESTAURANT', 'hashed_pass_1'),
('c0000000-0000-0000-0000-000000000002', '+21698765433', 'Sami Trabelsi', 'RESTAURANT', 'hashed_pass_2');

INSERT INTO restaurants (id, owner_id, name, category, rating, delivery_time_min, delivery_fee, image_url) VALUES 
('r0000000-0000-0000-0000-000000000001', 'c0000000-0000-0000-0000-000000000001', 'Chez Weld El Hajj', 'Tunisian', 4.8, 35, 2.500, 'https://images.unsplash.com/photo-1555396273-367ea4eb4db5'),
('r0000000-0000-0000-0000-000000000002', 'c0000000-0000-0000-0000-000000000002', 'Le Bardo Fast Food', 'Tunisian', 4.6, 25, 2.000, 'https://images.unsplash.com/photo-1517248135467-4c7edcad34c4');

-- Seed 15 dishes sample
INSERT INTO dishes (restaurant_id, name, description, price, category, available_options) VALUES 
('r0000000-0000-0000-0000-000000000001', 'Lablabi Spécial', 'Chaud, pois chiches, pain rassasié, huile d olive, cumin', 6.500, 'Tunisian', '["Harissa", "Thon", "Double Oeuf"]'),
('r0000000-0000-0000-0000-000000000001', 'Brik au Thon', 'Feuille de brick croustillante, oeuf, thon, persil', 3.200, 'Tunisian', '["Harissa", "Fromage"]'),
('r0000000-0000-0000-0000-000000000001', 'Ojja Merguez', 'Sauce tomate pimentée, oeufs pochés, merguez artisanales', 12.000, 'Tunisian', '["Harissa", "Fromage"]'),
('r0000000-0000-0000-0000-000000000002', 'Fricassé', 'Petit pain frit garni de thon, pomme de terre, oeuf, olives', 1.800, 'Fast Food', '["Harissa"]'),
('r0000000-0000-0000-0000-000000000002', 'Chapati Mahdia', 'Pain cuit sur plaque, garni au choix', 5.500, 'Fast Food', '["Thon", "Oeuf", "Fromage"]');
