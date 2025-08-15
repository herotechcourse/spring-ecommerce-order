# spring-ecommerce-product

## Controller
### Admin
#### AdminProductController
- [x] `GET /api/admin/products` all products (paginated)
- [x] `GET /api/admin/products/:id` product by ID
- [x] `POST /api/admin/products` Create a Product
- [x] `PUT /api/admin/products/:id` update the whole product by ID
- [x] `PATCH /api/admin/products/:id` update one or more attributes of Product by ID
- [x] `DELETE /api/admin/products/:id` delete the product by ID
- [x] `GET /api/admin/products/:id/options` get all product options
- [x] `POST /api/admin/products/:id/options` Create a product option
- [x] `PUT /api/admin/products/:id/options/:id` Update an option
- [x] `PATCH /api/admin/products/:id/options/:id` Update a one or more attributes of Option by ID
- [x] `DELETE /api/admin/products/:id/options/:id` delete the option by ID
#### AdminAuthController
- [x] `POST /api/admin/auth/login`
#### AdminCartStatisticsController
- [x] `GET /api/admin/cart-statistics/top-products` get top added products
- [x] `GET /api/admin/cart-statistics/members-added-cart`  get members who added products to cart
### Guest
#### GuestProductController
- [x] `GET /api/products` all products (public, paginated)

### Member
#### AuthController
- [x] `POST /api/member/auth/signUp` creates user and returns JWT token
- [x] `POST /api/member/auth/signIn` checks and returns JWT token
#### CartController
- [x] `GET /api/member/cart` getCartProducts
- [x] `POST /api/member/cart/:id` add (or increment) option in cart
- [x] `DELETE /api/member/cart/:id` decrement or remove option from cart
- [x] `DELETE /api/member/cart/clear` clear entire cart
#### OrderController
- [x] `GET /api/member/order` getAllOrders
- [x] `GET /api/member/order/:id` getOrderById
- [x] `POST /api/member/order/cart-checkout`
- [x] `POST /api/member/order/confirm-checkout/{orderId}`

## Config
- [x] DotenvConfig
- [x] WebConfig
### Advice
- [x] GlobalAdvice
### ArgumentResolver
- [x] LoginMemberArgumentResolver
### Interceptor
- [x] AdminInterceptor
- [x] BaseAuthInterceptor
- [x] MemberInterceptor
### WebConfig
### DotenvConfig

## Service
### AdminAuthService
- [x] `signIn(loginRequest: LoginRequest)`: String
### AdminProductService
- [x] `getProductById(id: Long)`: ProductResponseDTO
- [x] `createProduct(product: ProductDTO):`  URI
- [x] `updateProduct(id: Long, product: ProductDTO)`: Void
- [x] `patchProduct(id: Long, productPatchDTO: ProductPatchDTO)`
- [x] `fun deleteProduct(id: Long)`: Void
- [x] `getProductOptions(productId: Long`:  ProductResponseDTO
- [x] `createOption(productId: Long, optionDTO: OptionDTO)`: URI
- [x] `updateOption(productId: Long, optionId: Long, optionDTO: OptionDTO)`
- [x] `patchOption(productId: Long, optionId: Long, patchDTO: OptionPatchDTO)`
- [x] `deleteOption(productId: Long, optionId: Long)`
### AdminStatisticsService
- [x] `getTopAddedProducts()`: List<TopAddedProductsDTO>
- [x] `getMembersWhoAddedToCart()`: List<MembersWhoAddedToCartDTO>
### CartService
- [x] `getCartProducts(member: User)`: CartProductResponse
- [x] `addProductToCart(member: User, optionId: Long)`: Long
- [x] `removeProductFromCart(member: User, optionId: Long)`: Void
- [x] `clearCart(member: User)`
- [x] `fun checkoutCart(member: User)`
### LoginService
- [x] `fun login(loginRequest: LoginRequest, expectedRole: UserRole = UserRole.USER)`: String
### MemberAuthService
- [x] `signUp(user: UserRequestDTO)`: UserCreateResponse
- [x] `fun login(loginRequest: LoginRequest)`: String
### MemberOrderService
- [x] `fun getUserOrders(userId: Long): List<OrderResponse>`
- [x] `fun getOrderById(orderId: Long): OrderResponse`
- [x] `fun createCheckoutCartIntent(userId: Long): OrderIntentResponse`
- [x] `fun confirmCheckout(orderId: Long): StripeResponse?`
### PaginatedProductsService
- [x] `fun getListProducts(page: Int, perPage: Int): Page<ProductResponseDTO>`

## Model
### Cart
#### Columns
- [x] id: Long
- [x] user: User `OneToOne`
- [x] items: MutableList<CartProduct> `OneToMany`
#### Methods
- [x] `addProduct(option: Option, quantity: Int = 1)`
- [x] `fun decrementProduct(option: Option, decrement: Int = 1)`
- [x] `fun clear()`
### CartProduct
#### Columns
- [x] id: Long
- [x] cart: Cart `ManyToOne`
- [x] option: Option `ManyToOne`
- [x] quantity: Int
#### Methods
- [x] `fun incrementQuantity(quantity: Int = 1)`
- [x] `fun decrementQuantity(quantity: Int = 1)`
### CartStatistics
#### Columns
- [x] id: Long
- [x] userId: Long
- [x] userEmail: String
- [x] userName: String
- [x] optionId: Long
- [x] optionName: String
- [x] optionPrice: Double
- [x] action: CartAction (Enum)
- [x] createdAt: LocalDateTime
### MemberOrder
#### Columns
- [x] optionProducts: List<OrderProduct> `OneToMany`
- [x] userId: Long
- [x] userEmail: String
- [x] paymentId: String
- [x] paymentOption: PaymentOption
- [x] totalAmount: Double
- [x] status: OrderStatus
- [x] attempt: Int = 1
- [x] createdAt: LocalDateTime
#### Methods
- [x] `fun incrementAttempt()`
### Option
#### Columns
- [x] id: Long
- [x] name: String
- [x] price: Double
- [x] quantity: Int
- [x] imageUrl: String
#### Methods
- [x] `fun updateFields(optionDTO: OptionDTO)`
- [x] `fun patchOption(optionPatchDTO: OptionPatchDTO)`
### OrderProduct
#### Columns
- [x] id: Long = 0L
- [x] optionId: Long
- [x] optionName: String
- [x] price: Double
- [x] quantity: Int
### Product
#### Columns
- [x] id: Long
- [x] name: String `unique`
- [x] imageUrl: String
- [x] options: List<Option> `OneToMany`
- [x] createdAt: LocalDateTime
### User
- [x] id: Long
- [x] email: String `unique`
- [x] password: String
- [x] name: String
- [x] role: UserRole

## Repository
### CartProductRepository
### CartRepository
### CartStatisticsRepository
### MemberOrderRepository
### OptionRepository
### ProductRepository
### UserRepository

## DTO
### Auth
#### AuthTokenPayload
#### LoginRequest

### cartProduct
#### CartProductDTO
#### CartProductResponse

### cartStatistics
#### MembersWhoAddedToCartDTO
#### TopAddedProductDto

### error
#### ErrorResponse

### order
#### OrderIntentResponse
#### OrderListResponse
#### OrderProductResponse
#### OrderResponse

### payment
#### PaymentRequest

### products
#### OptionDTO
#### OptionPatchDTO
#### OptionResponseDTO
#### ProductDTO
#### ProductPatchDTO
#### ProductResponseDTO

### response
#### MessageResponse
#### TokenResponse

### stripe
#### StripeResponse

### user
#### UserCreateResponse
#### UserRequestDTO

## utils
### annotation
- `LoginMember`
### exception
- `CartOperationException`
- `DuplicateProductNameException`
- `EntityNotFoundException`
- `StripeException`
- `UnauthorisedUserException`
- `UserAlreadyExistsException`
- `UserCredentialException`
### extensions
- `OptionUtil`
- `ProductUtil`
### infrastructure
#### ApplicationLogger
- [x] `fun logError(message: String)`
#### JwtProvider
- [x] `createToken`: String
- [x] `getPayload`: AuthTokenPayload
- [x] `validateToken`: Boolean
#### StripeClient
- [x] `fun createCheckoutSession(req: PaymentRequest): StripeResponse?`
- [x] `fun confirmPayment(intentId: String): StripeResponse?`

## enums
### CartAction
- [x] ADD
- [x] DELETE
### OrderStatus
- [x] PENDING
- [x] COMPLETED
- [x] REJECTED
- [x] CANCELLED
### PaymentOption
- [x] STRIPE
### UserRoles
- [x] Admin
- [x] User

## Tests
### Controller
#### Admin
- [x] AdminCartStatisticsControllerTest
- [x] AdminProductControllerTest
#### Guest
- [x] GuestProductControllerTest
#### Member
- [x] CartControllerTest
- [x] MemberAuthControllerTest
- [x] MemberOrderControllerTest
### DTO
- [x] ProductDTOTest
- [x] UserRequestDTOTest
### infrastructure
- [x] StripeClientTest
### model
- [x] CartProductTest
- [x] CartTest
- [x] MemberOrderTest
- [x] OptionTest
- [x] ProductTest
### Service
- [x] MemberAuthServiceTest
- [x] MemberOrderServiceTest
- [x] ProductServiceTest

## Dummy Data
- [x] Admin => email = "admin@test.com" | pw = "admin123"
- [x] User  => email = "user@test.com" | pw = "user123"
- [x] 10x Products

## Deployment
- [x] Used Docker for deployment
- [x] http://52.78.119.87

## Environment Variables

To run the application, create a `.env` file in the root directory based on the provided `.env.sample` file.
The following variables are required:

```env
JWT_SECRET=       # Secret key used for signing JWT tokens
JWT_TIME=         # Token expiration time (e.g., 3600s or 1h)
STRIPE_SECRET_KEY= # Secret key used for Stripe

```
For stripe [Read here](https://docs.stripe.com/api/payment_intents/create)
