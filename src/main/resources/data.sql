INSERT INTO products (name, price, quantity, image_url) VALUES
                                                            ('Car', 1000.0, 5, 'https://images.unsplash.com/photo-1494905998402-395d579af36f?w=400&h=400&fit=crop'),
                                                            ('Bike', 200.0, 10, 'https://images.unsplash.com/photo-1571068316344-75bc76f77890?w=400&h=400&fit=crop'),
                                                            ('Truck', 30000.0, 2, 'https://images.unsplash.com/photo-1586190848861-99aa4a171e90?w=400&h=400&fit=crop'),
                                                            ('Laptop', 1500.0, 15, 'https://images.unsplash.com/photo-1496181133206-80ce9b88a853?w=400&h=400&fit=crop'),
                                                            ('Phone', 800.0, 25, 'https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?w=400&h=400&fit=crop');

INSERT INTO product_options (name, quantity, product_id, price) VALUES
                                                             ('Blue', 5, 1, 1100.0),
                                                             ('Red', 10, 2, 220.0),
                                                             ('Yellow', 10, 2, 210.0);

--  admin pw: secret
INSERT INTO members (id, email, password, name, role, created_at, updated_at) VALUES
    (1, 'test@example.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'Test User', 'USER', TIMESTAMP '2025-08-01 10:00:00.000', TIMESTAMP '2025-08-01 10:00:00.000'),
    (2, 'admin@example.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'Admin User', 'ADMIN', TIMESTAMP '2025-08-01 10:00:00.000', TIMESTAMP '2025-08-01 10:00:00.000'),
    (3, 'test2@example.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'Test User2', 'USER', TIMESTAMP '2025-08-01 10:00:00.000', TIMESTAMP '2025-08-01 10:00:00.000');

INSERT INTO carts (member_id, quantity, created_at, updated_at) VALUES
    (1, 1, TIMESTAMP '2025-07-25 08:00:00.000', TIMESTAMP '2025-07-25 08:00:00.000'),
    (3, 2, TIMESTAMP '2025-08-25 08:00:00.000', TIMESTAMP '2025-08-25 08:00:00.000');

INSERT INTO cart_items (cart_id, product_option_id, quantity, created_at, updated_at) VALUES
  (1, 1, 1, TIMESTAMP '2025-08-24 08:00:00.000', TIMESTAMP '2025-08-24 08:00:00.000'),
  (2, 2, 2, TIMESTAMP '2025-08-23 08:00:00.000', TIMESTAMP '2025-08-23 08:00:00.000'),
  (1, 3, 2, TIMESTAMP '2025-08-25 08:00:00.000', TIMESTAMP '2025-08-25 08:00:00.000');

INSERT INTO cart_statistics (cart_item_id, cart_id, product_option_id, quantity) VALUES
  (1, 1, 1, 1),  -- Car: 1 quantity
  (2, 2, 2, 2),  -- Bike: 2 quantity
  (3, 1, 3, 2);  -- Bike: 2 more quantity (total 4 for Bike)

-- Sample Orders
INSERT INTO orders (member_id, stripe_checkout_session_id, stripe_payment_intent_id, order_status, payment_status, currency, total_amount, created_at, updated_at) VALUES
  (1, 'cs_test_12345', 'pi_test_12345', 'CONFIRMED', 'COMPLETED', 'USD', 1100.0, TIMESTAMP '2025-08-20 10:00:00.000', TIMESTAMP '2025-08-20 10:00:00.000'),
  (3, 'cs_test_67890', 'pi_test_67890', 'PENDING', 'PENDING', 'EUR', 220.0, TIMESTAMP '2025-08-21 14:30:00.000', TIMESTAMP '2025-08-21 14:30:00.000');

-- Sample Order Items
INSERT INTO order_items (order_id, product_option_id, quantity, unit_price, total_price, product_name, option_name, created_at, updated_at) VALUES
  (1, 1, 1, 1100.0, 1100.0, 'Car', 'Blue', TIMESTAMP '2025-08-20 10:00:00.000', TIMESTAMP '2025-08-20 10:00:00.000'),
  (2, 2, 1, 220.0, 220.0, 'Bike', 'Red', TIMESTAMP '2025-08-21 14:30:00.000', TIMESTAMP '2025-08-21 14:30:00.000');

-- Sample Payments
INSERT INTO payments (order_id, stripe_payment_intent_id, amount, currency, status, payment_method, stripe_charge_id, created_at, updated_at) VALUES
  (1, 'pi_test_12345', 1100.0, 'USD', 'COMPLETED', 'card', 'ch_test_12345', TIMESTAMP '2025-08-20 10:05:00.000', TIMESTAMP '2025-08-20 10:05:00.000'),
  (2, 'pi_test_67890', 220.0, 'EUR', 'PENDING', null, null, TIMESTAMP '2025-08-21 14:30:00.000', TIMESTAMP '2025-08-21 14:30:00.000');

