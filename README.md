# spring-ecommerce-order

This project is the continuation of the previous ecommerce-product mission.
In this step, the codebase will integrate Spring Data JPA for database interactions, replacing the previous JDBC-based implementation.

## Step1 – spring-ecommerce-product
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

# Step2 - HTTP Clients
## Step 2-1 - External API: Place Order with Stripe

### Functional Requirements
When placing an order with a selected product option and quantity:

1. **Stock Update**
    - [x] The stock of the selected product option must be decreased according to the ordered quantity.

2. **Cart Cleanup**
   - [x] If the ordered product exists in the user’s cart, it should be removed from the cart after placing the order.

3. **Stripe Payment Integration**
    - [x] Use Stripe’s **Payment Intent API** to create and confirm a payment.
    - [x] Use the **sandbox secret key** for development and testing.

4. **Payment Failure Handling**
    - [x] If the payment approval API call fails, handle the error safely.
    - [x] Inform the user clearly about the failure reason.
    - [x] Possible failure reasons include:
        - [x] Expired payment session
        - [x] Invalid payment method
        - [x] Insufficient balance

---

## ✨ Features
- Integration with **Stripe Payment Intent API** for secure and real-time payment processing.
- Automatic **stock deduction** for the purchased product option.
- Automatic **cart item removal** when the ordered product is purchased.
- Comprehensive **error handling** with user-friendly failure messages.
- Support for **Stripe test cards** to simulate successful and failed payments during development.

---

## 🔗 References
- [Stripe API Documentation](https://stripe.com/docs/api/payment_intents)
- [Stripe Test Cards](https://stripe.com/docs/testing#international-cards)

## Step 2-2 - Order
### Functional Requirements

1. **Order**
   - [x] Implement the "Orders" feature
     - Order date and time 
     - Order status 
     - Purchased items 
     - Checkout session Id (issued by stripe)
   - [x] Implement the "Payment" feature
     - Payment amount
     - currency
     - paymentMethod
     - Checkout session Id (issued by stripe)

2. **Error**
   - [x] Error handling: validate amount/currency; map Stripe errors clearly; return appropriate HTTP statuses and error messages.

## Step 2-2 - Deployment
This step focuses on deploying the mission eCommerce so it can interact with the client application, ensuring stable operation and solving cross-origin issues.  
The process is automated using a shell deployment script.

### Features
- [x] Automated Deployment Script (`deploy.sh`)
    - Stops currently running application instance.
    - Copies latest JAR build to the deployment directory.
    - Starts the new application in the background.
- [x] Configure CORS
    - Resolve issues caused when the server and client have different Origin values.
    - Allow all origins.
    - Allow "simple" methods: GET, HEAD and POST.
    - Allow all headers.
    - Set max age to 1800 seconds (30 minutes).
- [x] Ensure stable network communication between server and client.
