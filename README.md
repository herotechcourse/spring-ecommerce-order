# Spring Ecommerce Order

## Step 1-1 - Entity Mapping

Goal: Transform Repository and entities using Spring Data JPA.

### Feature List
- [x] **Transform Models into Entities**
    - [x] Product -> `Product @Entity`
    - [x] Member -> `Member @Entity`
    - [x] CartItem -> `CartItem @Entity`
    - [x] Cart -> `Cart @Entity`
- [x] **Decide on the relationships between databases**
    - [x] Product
    - [x] Member
    - [x] CartItem
        - [x] A CartItem should reference a Product via @ManyToOne, not @OneToOne. Multiple CartItems across different
          carts may reference the same Product.
    - [x] Cart
- [x] **Transform Repositories to JpaRepositories**
  - **Flow**
  - Rename repo to JdbcProductRepository to `JdbcProductRepository(private val jdbcTemplate)`
  - Create `interface ProductRepository`
  - Add `override` keyword before functions in JdbcProductRepository
  - Add functions to interface
  - Run tests
  - Change return type of functions to Jpa style
  - Create `interface JpaProductRepository : ProductRepository, JpaProductRepository<Product, Long>`
  - Remove `@Repository` keyword -> comment out!!
  - Run tests
    - [x] Product
    - [x] Member
    - [x] Cart
    - [x] CartItem

- [x] **Refactor Services**
    - [x] extract business logic to Entities -> most validation SHOULD happen inside the Entities!!
        - [x] e.g. addOption()

#### Tests

- [x] Refactor all tests to be less heavy
    - [x] Create companion objects with constant values
- [x] Use @Transactional @SpringBootTest for testing is still small -> use @ExtendWith(MockitoExtension::class)
    - [x] @Mock repos (lateinit)
    - [x] private service (lateinit)
    - [x] @BeforeEach setup -> init

## Step 1-2 - Pagination

Goal: Implement pagination for both the product list and the wishlist view.
Most web applications do not display all data at once. Instead, content is split into multiple pages. 
Pagination allows users to define how data should be sorted, how many items are shown per page, and which page number to retrieve.

### Feature List
- [x] Sorting can also be used to prioritize which data appears first.
- [x] Spring Data provides a convenient object called `Pageable`.
  - `Page<T>` – full pagination with total count, total pages, current page, etc.

## Step 1-3 - Product Option

Goal: Add options to product information.
Design and implement the feature considering the relationship between the Product and Option models.

### Feature List
- [x] has:
  - name 
  - id
  - quantity
  - products
- [x] Option names can include up to 50 characters, including spaces.
- [x] Allowed special characters in option names:
  - [x] (, ), [, ], +, -, &, /, _
  - [x] All other special characters are not allowed.
- [x] Option quantity must be at least 1 and less than 100,000,000.
- [x] Implement a method to decrease the quantity of a product option by a specified amount:
- [x] No need to create a separate HTTP API.
- [x] This logic should be implemented in the Service class or Entity class for future reuse.
- [x] check inside DTO or addOption()
  - [x] A product must always have at least one option.
  - [x] Duplicate option names are not allowed within the same product to prevent confusion during purchase.

#### Test
- [x] test Option

## Step 2-1 - External API
Goal: Implement the "Place Order" feature using Stripe Payment Integration. </br>
This feature allows users to place orders with online payment processing via **Stripe's Payment Intent API** (sandbox mode).
It handles product stock updates, cart cleanup, payment confirmation, robust error handling for failed transactions, and storage of essential payment/order details.
### Features
1. Order Placement
- [x] User selects:
    - [x] Product option (e.g., size, color)
    - [x] Quantity
    - [x] Payment method (Stripe test card in sandbox)
- [x] API calculates total price and sends request to Stripe's Payment Intent Create API.

2. Stripe Payment Integration 
- [x] Get Api key(use sandbox key)
  - [x] store in application-properties
  - [x] do not push!!
- [x] Uses POST https://api.stripe.com/v1/payment_intents with:
  - [x] amount 
  - [x] currency 
  - [x] payment_method 
  - [x] confirm=true 
  - [x] automatic payment methods enabled (automatic_payment_methods[enabled]=true)

3. Stock Management
- When payment is confirmed:
  - [x] Decrease stock for the purchased product option by the ordered quantity. 
  - [x] Ensure stock cannot go below zero. 
- If payment fails:
  - [x] Stock remains unchanged.
  
4. Cart Cleanup
- If the ordered product exists in the user’s cart:
  - [x] Remove the item from the cart after successful payment. 
- If payment fails:
  - [x] Cart remains unchanged.

5. Error Handling
- If Stripe API request fails:
  - [x] Catch exception and map to user-friendly error message.
  - [x] message based on Stripe's codes
    - [x] 4xx throws 400 BadRequestException()
    - [x] 5xx throws 503 ExternalServiceException()
~~- [ ] Test with test cards provided in [Stripe official document](https://docs.stripe.com/testing?testing-method=card-numbers#declined-payments)
    - 4000000000000069 – Expired card decline
    - 4000 0000 0000 9995 – Insufficient funds
    - 4000 0000 0000 0002 – Payment Declined
    - 4000000000000127 – Incorrect CVC decline
    - 4242424242424241 – Incorrect number decline
    - 4242424242424242 – Valid Visa Card
    - 5555555555554444 – Valid Master Card~~
- -> "When writing test code, use a PaymentMethod such as pm_card_visa instead of a card number. We don’t recommend using card numbers directly in API calls or server-side code, even in testing environments. If you do use them, your code might not be PCI-compliant when you go live. "
- [x] test with payment methods provided by the [Stripe official documentation](https://docs.stripe.com/testing?testing-method=payment-methods#visa)
  - pm_card_visa
  - pm_card_mastercard
  - pm_card_amex 
  - ...

6. API Endpoints
- `POST /orders/place`
- Request
`  {
  "productOptionId": 123,
  "quantity": 2,
  "paymentMethod": "pm_card_visa"
  }`
- Response Success
`  {
  "status": "success",
  "orderId": 456,
  "message": "Payment successful. Your order has been placed."
  }`
- Response Failure
`  {
  "status": "error",
  "message": "Insufficient funds. Please use another payment method."
  }`

## Step 2.2 - Orders Retrieval
### Features
1. Orders
- [x] Endpoint: `GET /orders`
- [ ] optional: add pagination
- [x] Each order record should include:
  - [x] Order Date & Time (when payment was completed)
  - [x] Order Status (e.g., PENDING, PAID, FAILED, CANCELLED)
  - [x] Purchased Items (product names, options, quantities)
  - [x] Stripe Checkout Session ID (issued by Stripe)
  - [x] Payment Amount (in currency format)
  - [x] Optional Payment Details (e.g., payment method, last 4 digits of card, currency)
    - [x] can be stored optionally in the DB
    - -> stored inside Order
- Example Response
`[
  {
    "orderId": 456,
    "orderDateTime": "2025-08-09T14:35:00Z",
    "status": "PAID",
    "items": [
      { "productName": "T-Shirt", "option": "Large", "quantity": 2 }
    ],
    "checkoutSessionId": "pi_3OKjdf9sjlkd09",
    "amount": 3999,
    "currency": "USD",
    "paymentMethod": "visa"
  }
]
`
## Step 2.3 - Deployment
You must deploy your existing service and ensure it can interact with the client.
### Features
- [x] Write a deployment script to automate the deployment process. (`deploy.sh`)
- [x] Handle security issues when interacting with the client API.
  - [x] For example, resolve issues caused when the server and client have different Origin values.
- [ ] HTTPS is optional
- [x] Cors Configuration #applyPermitDefaultValues()
  - [x] Allow all origins.
  - [x] Allow “simple” methods GET, HEAD and POST.
  - [x] Allow all headers.
  - [x] Set max age to 1800 seconds (30 minutes).
- [x] Test with MockMVC
  - [x] Test client → server calls in local & production.
  - [x] Confirm no CORS errors in browser console.

## Action Plan for Step 2
1. [x] Design Order domain & DTOs (Order, OrderItem, enums, payment metadata).
2. [x] Add repositories (or entities).
3. [x] Add Stripe config + SDK dependency. (Use test/sandbox key in application.properties, never commit it.) Stripe Docs
4. [x] Implement StripeService to create/confirm PaymentIntents (with idempotency). Stripe Docs+1
5. [x] Implement OrderService.placeOrder() orchestration: validate stock, create PENDING order, call Stripe, finalize (decrement stock + cart cleanup) on success. Use DB locking & transactions when changing stock.
6. [x] Implement error mapping & friendly messages for Stripe decline codes (expired_card, insufficient_funds, incorrect_cvc, card_declined, etc.). Stripe Docs+1
7. Add webhook endpoint (/webhooks/stripe) to process payment_intent.succeeded / fallback reconciliation (recommended). Stripe Docs
8. [x] Add controller endpoints: POST /orders/place and GET /orders (with pagination).
9. [x] Add tests: unit tests (mock Stripe), integration tests (Stripe test keys or WireMock), MockMvc tests for controllers.
10. [x] Deploy: script (deploy.sh), run migrations, restart service, test client-server flows (CORS config).


## Considerations

- [x] remove Boolean return type from all delete methods
- [ ] change Double to BigDecimal inside the Entity (Product/price)
- [x] decide on where and how to use Models
    - Entity == Model
- [x] don't use Cascade.All but Persist, Merge etc.
- [ ] move product-option mapping to the constructor
- [ ] effective test: Create InMemory fake repos -> service uses the fake repos
- [ ] add HTTPS
- [ ] add webhook endpoint (/webhooks/stripe)
