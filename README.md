# spring-ecommerce-order

- **App URL** → [https://3.35.139.129/](https://3.35.139.129/) *(Self-signed certificate — browser will show a warning)*
- To run the app locally, fill in the `example.env` variables and rename the file to `.env`.
- To run the app with PostgreSQL, use:
  ```bash
  docker-compose -f docker-compose.dev.yml up -d
  ```

## Features

### Step 1.1

- [x] Replace JdbcTemplate for spring data jpa.
- [x] Use JPA in the entities and services.
- [x] Add logging to JPA

### Step 1.2

- [x] Add Pagination for Products
- [x] Add wishlist items
- [x] Add Pagination for wishlist items

### Step 1.3

- [x] Create an Option entity
- [x] The entity should contain a name up to 50 characters
- [x] The Option quantity must be at least 1 and less than 100_000_000
- [x] Duplicated option names are not allowed within the same product to prevent confusion during purchase.

- [x] Add Admin functionality to create new option for product
- [x] Create an endpoint in the admin controller for adding new options to products

### Step 2.1

### Step 2.1 and 2.2 — Stripe Integration & Order Management

- [x] Add DotEnv dependency to manage environment variables securely (.env files).
- [x] Implement EnvironmentPostProcessor to load `.env` variables into Spring Environment at startup.
- [x] Add `.env` file at the project root (excluded from version control).
- [x] Define `StripeProperties` as configuration properties for Stripe API keys and settings.
- [x] Create `StripeClientConfiguration` bean to build a reusable HTTP client for Stripe API calls.
- [x] Define `StripeClient` interface leveraging `HttpExchange` for clean and testable Stripe operations.
- [x] Implement `StripeService` to encapsulate Stripe API interactions; include unit tests to verify behavior.
- [x] Refactor `CartItem` entity to replace `productId` with `optionId` for precise product variant handling.
- [x] Validate that `CartItem.quantity` does not exceed available stock quantity in the associated `Option`.
- [x] Add unitPrice to the `Option` entity and dto to reflect the price of each option.

- [x] Create `Order` entity with the following fields:
- [x] Design `OrderItem` entity to represent individual purchased options with:
- [x] Establish relationships:
    - `Order` to `Member` as Many-to-One.
    - `Order` to `OrderItem` as One-to-Many.
    - `OrderItem` to `Option` as Many-to-One.
    - `OrderItem` to `Order` as Many-to-One.
- [x] Create `Payment` entity to store Stripe payment information with fields:
- [x] Implement `PaymentService` to handle creation and persistence of Payment entities.
- [x] Update `OrderService` to:
    - Create and save `Order` and `OrderItem` entities.
    - Call `StripeService` to create payment intents.
    - Use `PaymentService` to persist payment details linked to orders.
    - Handle payment statuses to update stock and cart accordingly.
    - Ensure transactional consistency across order creation and payment persistence.
- [x] Handle different payment statuses explicitly, logging or saving failure states instead of silently ignoring them.

### Step 2.3
- [x] Integrated PostgreSQL into the project
- [x] Added Liquibase for database change management
- [x] Implemented separate dev and prod modes
- [x] Created Dockerfiles and Docker Compose files for dev/prod environments
- [x] Added IntelliJ run configurations to start the app and DB separately for testing with PostgreSQL
- [x] Prepared a GitHub Action for automated deployment (to be enabled when allowed)
- [x] Deployed to AWS EC2
