# spring-ecommerce-order

This project is the continuation of the previous ecommerce-product mission.
In this step, the codebase will integrate Spring Data JPA for database interactions, replacing the previous JDBC-based implementation.

## Previous Mission – spring-ecommerce-product
### Step 1-1 — Basic Product API (In-Memory Storage)
- Implemented HTTP API for CRUD operations on products.
- Stored data in memory using Kotlin MutableMap.
- JSON request/response format.

### Step 1-2 — Admin Interface with Thymeleaf
- Server-Side Rendering (SSR) with Thymeleaf templates.
- Admin can view, add, update, delete products via HTML forms.

### Step 1-3 — Persistence with H2 Database & JdbcTemplate
- Replaced in-memory storage with H2 in-memory database.
- SQL scripts for schema creation and initial data.
- Centralized exception handling.

### Step 2-1 — Product Validation
- Validation rules for product name, price, and image URL.
- Unique product name check.
- Structured error responses.

### Step 2-2 — Member Authentication (JWT)
- Member registration and login.
- JWT token generation and validation.
- Custom argument resolver for authenticated users.

### Step 2-3 — Shopping Cart
- Cart created automatically on user registration.
- Add, update, remove products in the cart.
- Separate controllers for guests, members, and admins.

### Step 2-4 — Admin Statistics
- Top 5 most-added products in last 30 days.
- Recently active members (last 7 days).
- Admin-only endpoints.

# Current Mission – spring-ecommerce-order
## STEP 1-1 - Entity Mapping - Entity Mapping

### Features
- [x] Refactor existing codebase from spring-ecommerce-product from JdbcTemplate to use Spring Data JPA.
- [x] Model real domain objects and map them to database tables using JPA annotations
- [x] Write learning tests using @DataJpaTest.
- [x] objects should use references to navigate relationships like:
  ```kotlin
  val question = findQuestionById(questionId)
  val answers = question.answers
  ```

## STEP 1-2 - Pagination

### Features

1. **Product List Pagination**
- [x] Users can request a specific page and size.
- [x] Optional sorting by one or more fields (ascending or descending).

2. **Wishlist Pagination**
- [x] Similar functionality applied to the wishlist view.

3. **Sorting**
- [x]  The `sort` parameter defines how the data should be ordered.
- [x] Supports multiple sort fields, e.g. `sort=price,desc&sort=name,asc`.

## Step 1.3 - Product Option

- [x] Add options to product information.
### Example
Implement the feature so it can handle HTTP requests and responses as shown below.

#### Request
```kotlin
GET /api/products/1/options HTTP/1.1
```

#### Response
```kotlin
HTTP/1.1 200
Content-Type: application/json

[
  {
    "id": 464946561,
    "name": "01. [Best] Shea Butter Hand & Shea Stick Lip Balm",
    "quantity": 969
  }
]
```

### Constraints
1. [x] A product must always have at least one option. 
2. [x] Option names can include up to 50 characters, including spaces. 
3. [x] Allowed special characters in option names:
   - (, ), [, ], +, -, &, /, _
   - All other special characters are not allowed. 
4. [x] Option quantity must be at least 1 and less than 100,000,000. 
5. [x] Duplicate option names are not allowed within the same product to prevent confusion during purchase. 
6. [x] Implement a method to decrease the quantity of a product option by a specified amount:
   - No need to create a separate HTTP API. 
   - This logic should be implemented in the Service class or Entity class for future reuse.


## Step 1.4 - Place Order (Stripe Payment Integration)

### Features
1. **Cart products**
- [x] Users can choose a product with a selected option and quantity.
- [x] The system decreases the stock of the selected product option accordingly.
- [x] Products in cart can be removed after order completion.

2. **Stripe Payment Integration**
- [x] Integrated with the Stripe Payment Create API using the test sandbox API key.
- [x] Payments are processed using the Stripe test environment during development.

3. **Error Handling for Payment Failures**
- [x] If the payment API call fails, the system handles the error safely.
- [x] Displays message indicating the reason for failure.

## Step 2-2 — Order

### Features

- [x] Create and place orders with selected cart items.
- [x] Persist payment data linked to the order.
- [x] Design database relationships for orders and payments to support Stripe payment integration and status tracking.
- [x] Member API endpoint to view order history and details.
- [x] The following information must be visible in the Orders API response:
  - Order date and time
  - Order status
  - Purchased items
  - Checkout session ID (issued by Stripe)
  - Payment amount

## Step 2-3 - Deployment

### Features

- [x] Write a deployment script to automate the deployment process.
- [x] Handle security issues when interacting with the client API.
