# Spring E-commerce Orders

## JDBC to JPA Migration - Step 1

### Migration Setup
- [x] Update `build.gradle.kts` with JPA dependencies
- [x] Configure `application.properties` for H2/MySQL compatibility
- [x] Set up `data.sql` for test data

---

## Product
- [x] **Entity** - JPA annotations, validation, relationships
- [x] **Repository** - JpaRepository with custom queries
- [x] **Service** - CRUD operations, business logic
- [x] **Controller** - AdminController endpoints
- [x] **Tests** - Unit and integration tests

## ProductOption
- [x] **Entity** - JPA mapping, business methods (`subtract`, `toString`)
- [x] **Repository** - JpaRepository with name validation
- [x] **Service** - Unified add/update with `saveProductOption()`
- [x] **Controller** - REST endpoints in AdminController
- [x] **Tests** - Service layer testing

## Member
- [x] **Entity** - User management, roles, cart relationship
- [x] **Repository** - JpaRepository migration from JDBC
- [x] **Service** - Authentication, user operations
- [x] **Controller** - User management endpoints
- [x] **Tests** - Entity and service testing

## Cart
- [x] **Entity** - Shopping cart with member relationship
- [x] **Repository** - JpaRepository with cart operations
- [x] **Service** - Cart management logic
- [x] **Controller** - Cart operations
- [x] **Tests** - Integration testing

## CartItem
- [x] **Entity** - Cart-product relationship, `modify()` method
- [x] **Repository** - JpaRepository with `@Modifying` queries
- [x] **Service** - Refactored `saveCartItem()` (add/update unified)
- [x] **Controller** - CRUD operations
- [x] **Tests** - Unit tests with Mockito, integration tests with data.sql

---

## Key points

### Service Layer Refactoring
- [x] **Single Responsibility** - Broke down complex methods into focused private functions
- [x] **Unified Operations** - `saveProductOption()` and `saveCartItem()` handle both add/update
- [x] **Kotlin Style** - Nullable parameters with defaults, proper type inference

### Testing Strategy
- [x] **Unit Tests** - Mock-based testing for service logic
- [x] **Integration Tests** - Full Spring context with real database
- [x] **Test Data** - Leveraging `data.sql` for consistent test scenarios

### Next Steps
- [x] **Pagination** - Add `Pageable` support to controllers
- [x] **Inventory Management** - Stock tracking and validation
- [ ] **Performance** - Query optimization and caching

---

## External API Integration - Step 2 (Feature-list)

### Step 2-1: Stripe Payment Integration
- [x] Create Order related entities (Order, OrderItem, OrderStatus, PaymentStatus)
- [x] Create Payment related entities (Payment, PaymentMethod)
- [x] Basic order creation workflow (CheckoutController, OrderService)
- [x] Cart-based order placement (CreateOrderRequest with cartItemIds)
- [x] Order validation (cart ownership, stock availability)
- [x] Stock management methods (updateProductStock, clearCartItems)
- [x] Implement Stripe Payment API integration (/config)
    - [x] StripeProperties configuration class
    - [x] StripeClient for API calls
    - [x] Enable configuration properties in Application
- [x] Connect payment flow to order creation
    - [x] Create payment intent before order creation
    - [x] Handle payment confirmation/failure
    - [x] Integrate stock decrease on successful payment
    - [x] Integrate cart cleanup on successful payment

### Step 2-2: Orders Management
- [x] Implement Orders API endpoints
- [x] Display order information:
    - [x] (mandatory) Order date and time, Order status, Purchased items, Checkout session Id (issued by stripe), Payment amount
    - [] (optional) other payment-related fields
- [x] Design database schema for orders and payments

### Step 2-3: Deployment
- [x] Create automated deployment script (start.sh)
    - [x] Build JAR file with gradle
    - [x] Stop existing application process
    - [x] Copy new JAR to deployment location
    - [x] Start application with nohup
- [x] Configure CORS for client-server interaction
    - [x] Add CORS configuration to WebMvcConfig
    - [x] Allow cross-origin requests from frontend
    - [x] Test CORS with OPTIONS requests
- [x] Handle security considerations for production deployment
    - [x] Set up proper logging for production
    - [x] Ensure JWT secret is environment-based (via .env file)
    - [x] Configure proper CORS origins (not wildcard in production)
- [ ] (optional) Implement HTTPS
- [x] Test deployment on AWS instance
- [x] Verify application runs correctly in production environment

--- 
## Development-plan

### Product Structure
```
src/main/kotlin/ecommerce/
├── controller/                 # REST API endpoints (e.g., Products, Cart)
├── service/                    # Business logic (e.g., ProductService, CartService)
├── repository/                 # Spring Data JPA repositories
├── domain/                     # JPA entities (the core business objects)
├── web/dto/                    # Data Transfer Objects (Request/Response)
└── config/                     # Spring configurations (e.g., WebMvcConfig)
```

### Domain Models
* **`Member`**:
    * Represents a user account with an email, password, and role.
* **`Product`**:
    * A product in the catalog with basic information like name and brand.
    * It contains a list of `ProductOption`s.
* **`ProductOption`**:
    * A specific variant of a product (e.g., size, color) with its own price and stock quantity.
    * This is the purchasable unit.
* **`Cart`**:
    * A shopping cart linked to a `Member`.
* **`CartItem`**:
    * An item within a `Cart`, linked to a specific `ProductOption` and tracking the quantity.