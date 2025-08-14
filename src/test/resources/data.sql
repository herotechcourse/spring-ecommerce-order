INSERT INTO PRODUCT (name, price, image_url,created_at, last_updated_at)
VALUES ('Espresso', 2.50, 'https://upload.wikimedia.org/wikipedia/commons/4/45/A_small_cup_of_coffee.JPG', NOW(), NOW()),
       ('Cappuccino', 3.20, 'https://upload.wikimedia.org/wikipedia/commons/c/c8/Cappuccino_at_Sightglass_Coffee.jpg', NOW(), NOW()),
       ('Latte Macchiato', 3.80,
        'https://media.istockphoto.com/id/532485409/photo/a-glass-of-latte-macchiatto.webp?a=1&b=1&s=612x612&w=0&k=20&c=1_j2qv3r7x7opMaA51M1t2df26vusUgX-9S4-xq_b4I=', NOW(), NOW()),
       ('Iced Coffee', 3.50,
        'https://media.istockphoto.com/id/592378202/photo/iced-coffee-with-cream-swirling-into-it.webp?a=1&b=1&s=612x612&w=0&k=20&c=FCQZKClY8a_tD8dz7ALFPGdmpZ9Ik2cJ0O8Lm9E1zMA=', NOW(), NOW()),
       ('Croissant', 2.00,
        'https://images.unsplash.com/photo-1691480162735-9b91238080f6?w=900&auto=format&fit=crop&q=60&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxzZWFyY2h8Mnx8Q3JvaXNzYW50fGVufDB8fDB8fHww', NOW(), NOW()),
       ('Muffin', 2.50,
        'https://images.unsplash.com/photo-1607958996333-41aef7caefaa?w=900&auto=format&fit=crop&q=60&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxzZWFyY2h8Mnx8bXVmZmlufGVufDB8fDB8fHww', NOW(), NOW()),
       ('Coffee Beans 250g', 7.99,
        'https://upload.wikimedia.org/wikipedia/commons/thumb/c/c5/Roasted_coffee_beans.jpg/1200px-Roasted_coffee_beans.jpg', NOW(), NOW()),
       ('Reusable Coffee Cup', 9.99,
        'https://images.unsplash.com/photo-1576788903709-5c3eda911324?w=900&auto=format&fit=crop&q=60&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxzZWFyY2h8Mnx8UmV1c2FibGUlMjBDb2ZmZWUlMjBDdXB8ZW58MHx8MHx8fDA%3D', NOW(), NOW()),
       ('Cold Brew Bottle', 4.20,
        'https://images.unsplash.com/photo-1536638455623-a35d0fa09ab9?w=900&auto=format&fit=crop&q=60&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxzZWFyY2h8Nnx8Q29sZCUyMEJyZXclMjBCb3R0bGV8ZW58MHx8MHx8fDA%3D', NOW(), NOW()),
       ('Tiramisu', 4.50,
        'https://images.unsplash.com/photo-1571877227200-a0d98ea607e9?w=900&auto=format&fit=crop&q=60&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxzZWFyY2h8Mnx8dGlyYW1pc3V8ZW58MHx8MHx8fDA%3D', NOW(), NOW());

INSERT INTO MEMBER (NAME, EMAIL, PASSWORD, ROLE, CREATED_AT, LAST_UPDATED_AT)
VALUES ('Admin', 'admin@test.com', '$2a$10$y3rEiacoc/0F1Qh0mVweo.rYAAuyCbOGhuPI/fk3XnC20irt21.nm', 'ADMIN', NOW(), NOW()),
       ('user1', 'user1@example.com', '$2a$10$y3rEiacoc/0F1Qh0mVweo.rYAAuyCbOGhuPI/fk3XnC20irt21.nm', 'USER', NOW(), NOW()),
       ('user2', 'user2@example.com', '$2a$10$y3rEiacoc/0F1Qh0mVweo.rYAAuyCbOGhuPI/fk3XnC20irt21.nm', 'USER', NOW(), NOW()),
       ('user3', 'user3@example.com', '$2a$10$y3rEiacoc/0F1Qh0mVweo.rYAAuyCbOGhuPI/fk3XnC20irt21.nm', 'USER', NOW(), NOW());

INSERT INTO CART (MEMBER_ID, CREATED_AT, LAST_UPDATED_AT)
VALUES (1, NOW(), NOW()),
       (2, NOW(), NOW()),
       (3, NOW(), NOW()),
       (4, NOW(), NOW());

INSERT INTO OPTION (NAME, QUANTITY, PRODUCT_ID, CREATED_AT, LAST_UPDATED_AT)
VALUES ('option1', 4, 1, NOW(), NOW()),
       ('option2', 4, 2, NOW(), NOW()),
       ('option3', 4, 3, NOW(), NOW()),
       ('option4', 4, 4, NOW(), NOW()),
       ('option5', 4, 5, NOW(), NOW()),
       ('option6', 4, 6, NOW(), NOW()),
       ('option7', 4, 7, NOW(), NOW()),
       ('option8', 4, 8, NOW(), NOW()),
       ('option9', 4, 9, NOW(), NOW()),
       ('option10', 4, 10, NOW(), NOW());

INSERT INTO CART_ITEM (CART_ID, OPTION_ID, QUANTITY, CREATED_AT, LAST_UPDATED_AT)
VALUES (2, 7, 3, NOW(), NOW()),
       (2, 8, 2, NOW(), NOW()),
       (3, 8, 2, NOW(), NOW()),
       (3, 2, 1, NOW(), NOW());

INSERT INTO CART_HISTORY (MEMBER_ID, OPTION_ID, QUANTITY, STATUS, CREATED_AT, LAST_UPDATED_AT)
VALUES (2, 7, 3, 'ADDED', NOW(), NOW()),
       (2, 8, 2, 'ADDED', NOW(), NOW()),
       (3, 8, 2, 'ADDED', NOW(), NOW()),
       (3, 2, 1, 'ADDED', NOW(), NOW());
