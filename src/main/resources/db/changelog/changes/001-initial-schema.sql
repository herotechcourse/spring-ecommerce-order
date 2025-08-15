CREATE TABLE member
(
    id       BIGSERIAL PRIMARY KEY,
    name     VARCHAR(50)  NOT NULL,
    email    VARCHAR(100) NOT NULL,
    password VARCHAR(255) NOT NULL,
    role     VARCHAR(20)  NOT NULL
);

CREATE TABLE product
(
    id        BIGSERIAL PRIMARY KEY,
    name      VARCHAR(15)      NOT NULL,
    price     DOUBLE PRECISION NOT NULL,
    image_url VARCHAR(255)     NOT NULL
);

CREATE TABLE "option"
(
    id         BIGSERIAL PRIMARY KEY,
    product_id BIGINT           NOT NULL REFERENCES product (id) ON DELETE CASCADE,
    name       VARCHAR(50)      NOT NULL,
    quantity   INT              NOT NULL,
    unit_price DOUBLE PRECISION NOT NULL,
    CONSTRAINT uq_option_product_name UNIQUE (product_id, name)
);

CREATE TABLE cart_item
(
    id        BIGSERIAL PRIMARY KEY,
    member_id BIGINT    NOT NULL REFERENCES member (id) ON DELETE CASCADE,
    option_id BIGINT    NOT NULL REFERENCES "option" (id) ON DELETE CASCADE,
    quantity  INT       NOT NULL,
    added_at  TIMESTAMP NOT NULL,
    CONSTRAINT uq_cart_member_option UNIQUE (member_id, option_id)
);

CREATE TABLE wish_item
(
    id         BIGSERIAL PRIMARY KEY,
    added_at   TIMESTAMP NOT NULL,
    member_id  BIGINT    NOT NULL REFERENCES member (id) ON DELETE CASCADE,
    product_id BIGINT    NOT NULL REFERENCES product (id) ON DELETE CASCADE,
    CONSTRAINT uq_wish_member_product UNIQUE (member_id, product_id)
);

CREATE TABLE "order"
(
    id           BIGSERIAL PRIMARY KEY,
    status       VARCHAR(50) NOT NULL,
    total_amount BIGINT      NOT NULL,
    member_id    BIGINT      NOT NULL REFERENCES member (id),
    created_at   TIMESTAMP   NOT NULL,
    updated_at   TIMESTAMP   NOT NULL
);

CREATE TABLE order_item
(
    id         BIGSERIAL PRIMARY KEY,
    order_id   BIGINT           NOT NULL REFERENCES "order" (id) ON DELETE CASCADE,
    option_id  BIGINT           NOT NULL REFERENCES "option" (id),
    quantity   INT              NOT NULL,
    unit_price DOUBLE PRECISION NOT NULL
);

CREATE TABLE payment
(
    id                       BIGSERIAL PRIMARY KEY,
    stripe_payment_intent_id VARCHAR(255) NOT NULL UNIQUE,
    amount                   BIGINT       NOT NULL,
    currency                 VARCHAR(50)  NOT NULL,
    status                   VARCHAR(50)  NOT NULL,
    failure_code             VARCHAR(255),
    failure_message          VARCHAR(255),
    order_id                 BIGINT       NOT NULL REFERENCES "order" (id) ON DELETE CASCADE,
    created_at               TIMESTAMP    NOT NULL,
    updated_at               TIMESTAMP    NOT NULL
);
