# spring-ecommerce-order 


## Step 1.1 - Entity Mapping

### Features to Implement step 1.1

- [x] Refactor existing codebase from JdbcTemplateto use Spring Data JPA .
- [x] Model real domain objects and map them to database tables using JPA annotations.
- [x] Write learning tests using @DataJpaTest

## Step 1.2 - Pagination

### Features to Implement step 1.2

- [x] Implement pagination for the product list. 
- [x] Implement pagination for the cart view.

## Step 1.3 - Product Option

### Features to Implement step 1.3

- [x] A product must always have at least one option.
- [x] Option names can include up to 50 characters , including spaces.
- [x] Allowed special characters in option names.
- [x] Option quantity must be at least 1 and less than 100,000,000.
- [x] Duplicate option names are not allowed within the same product to prevent confusion during purchase.
- [x] Implement a method to decrease the quantity of a product option by a specified amount.

## Step 2.1 - External API

### Features to Implement step 2.1

-[x] Add StripeProperties bound to stripe.secret-key
-[x] Implement a StripeClient using Spring RestClient 
-[x] Implement Place Order application flow
-[x] Add acceptance & service tests covering

