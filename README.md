# spring-ecommerce-order
## Controller
### Admin
#### AdminProductController
- [x] `GET /api/admin/products` all products (paginated)
- [x] `GET /api/admin/products/:id` product by ID
- [x] `POST /api/admin/products` Create a Product
- [x] `PUT /api/admin/products/:id` update the whole product by ID
- [x] `PATCH /api/admin/products/:id` update one or more attributes of Product by ID
- [x] `DELETE /api/admin/products/:id` delete the product by ID
#### AdminAuthController
- [x] `POST /api/admin/auth/signIn`
#### Product Option
- [x] `GET /api/admin/products/:id/options` get all options of a product
- [x] `POST /api/admin/products/:id/options` create a new option
- [x] `PUT /api/admin/products/:productId/options/:optionId` update the whole option
- [x] `PATCH /api/admin/products/:productId/options/:optionId`  update one or more fields of option
- [x] `DELETE /api/admin/products/:productId/options/:optionId` delete an option 
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

## Config
### AuthInterceptor
### LoginMemberArgumentResolver
### WebConfig

## Service
### AdminAuthService
- [x] `signIn(loginRequest: LoginRequest)`: String
### AdminProductService
- [x] `getAllProducts()`: (page: Int = 1, perPage: Int = 10): Page<ProductResponseDto>
- [x] `getProductById(id: Long)`: ProductResponseDto
- [x] `createProduct(product: ProductDto):`  URI
- [x] `updateProduct(id: Long, product: ProductDto)`: Void
- [x] `patchProduct(id: Long, productPatchDto: ProductPatchDto)`
- [x] `fun deleteProduct(id: Long)`: Void
- [x] `getProductOptions(productId: Long`:  ProductResponseDto
- [x] `createOption(productId: Long, optionDto: OptionDto)`: URI
- [x] `updateOption(productId: Long, optionId: Long, optionDto: OptionDto)`
- [x] `patchOption(productId: Long, optionId: Long, patchDto: OptionPatchDto)`
- [x] `deleteOption(productId: Long, optionId: Long)`
### AdminStatisticsService
- [x] `getTopAddedProducts()`: List<TopAddedProductsDto>
- [x] `getMembersWhoAddedToCart()`: List<MembersWhoAddedToCartDto>
### CartService
- [x] `getCartProducts(member: User)`: CartProductResponse
- [x] `addProductToCart(member: User, optionId: Long)`: Long
- [x] `removeProductFromCart(member: User, optionId: Long)`: Void
- [x] `clearCart(member: User)`
### LoginService
- [x] `fun login(loginRequest: LoginRequest, expectedRole: UserRole = UserRole.USER)`: String
### MemberAuthService
- [x] `signUp(user: UserRequestDto)`: UserCreateResponse
- [x] `fun login(loginRequest: LoginRequest)`: String
### GuestProductService
- [x] `getListProducts(page: Int, perPage: Int)`: Page<ProductResponseDto> 

## Model
### Cart
#### Columns
- [x] id: Long
- [x] items: MutableList<CartProduct> `OneToMany`
#### Methods
- [x] `addProduct(option: Option, quantity: Int = 1)`
- [x] `fun decrementProduct(option: Option, decrement: Int = 1)`
- [x] `fun clear()`
### CartProduct
#### Columns
- [x] id: Long
- [x] cart: Cart (ManyToOne)
- [x] option: Option (ManyToOne)
- [x] quantity: Int
### CartStatistics
#### Columns
- [x] id: Long
- [x] user: User `ManyToOne`
- [x] product: Product `ManyToOne`
- [x] action: CartAction (Enum)
- [x] createdAt: LocalDateTime
### Product
#### Columns
- [x] id: Long
- [x] name: String (unique)
- [x] options: List<Option> (OneToMany)
- [x] createdAt: LocalDateTime
- [x] Validations:
  - must have at least one unique option
### User
- [x] id: Long
- [x] email: String (unique)
- [x] password: String
- [x] name: String
- [x] role: UserRole (Enum)
- [x] cart: Cart? `OneToOne`

### Option
- [x] id: Long
- [x] name: String
- [x] price: Double
- [x] quantity: Int
- [x] imageUrl: String
- [x] Validations:
  - [x] name: not blank, max 50, matches pattern
  - [x] price ≥ 0.01
  - [x] quantity in 1..100_000_000
  - [x] valid image URL

## Repository
### CartProductRepository
### CartRepository
### CartStatisticsRepository
### ProductRepository
### UserRepository
### OptionRepository

## Dto
### Auth
#### AuthTokenPayload
- email: String
#### LoginRequest
- email: String
- password: String

### cartProduct
#### CartProductDto
#### CartProductResponse

### cartStatistics
#### MembersWhoAddedToCartDto
#### TopAddedProductDto

### error
#### ErrorResponse

### products
#### OptionDto
#### OptionPatchDto
#### OptionResponseDto
#### ProductDto
#### ProductPatchDto
#### ProductResponseDto

### response
#### MessageResponse
#### TokenResponse

### user
#### UserCreateResponse
#### UserRequestDto

## utils
### annotation
### exception
- `DuplicateProductNameException`
- `EntityNotFoundException`
- `UserAlreadyExistsException`
- `UserCredentialException`
- `CartOperationException`
- `UnauthorisedUserException`
### extensions
### infrastructure
#### JwtProvider
- [x] `createToken`: String
- [x] `getPayload`: AuthTokenPayload
- [x] `validateToken`: Boolean

## enums
### UserRoles
- [x] Admin
- [x] User

### CartActions
- [x] ADD
- [x] DELETE


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
### Dto
- [x] ProductDtoTest
- [x] UserRequestDtoTest
### Repository
- [x] CartRepositoryTest
- [x] CartStatisticsRepositoryTest
- [x] ProductRepositoryTest
- [x] UserRepositoryTest
### Service
- [x] MemberAuthServiceTest
- [x] ProductServiceTest


## Step 2-1 and Step 2-2
### Feature list
- [x] Stripe configuration
  - [x] add  properties in application.properties - ``stripe.secret-key=``
  - [x] add `@EnableConfigurationProperties(StripeProperties::class)` in `Application.kt`
  - [x] add `StripeProperties` class with a `secretKey` field
- [x] StripeClient
- [x] dto
  - [x] PlaceOrderRequest
  - [x] PlaceOrderResponse
  - [x] PaymentRequest
  - [x] PaymentResponse
  - [x] OptionQuantity
- [x] model
  - [x] Order
    - [x] `user: User` - reference to the user  (`@ManyToOne`)
    - [x] `stripeSessionId: String` - Stripe checkout session ID
    - [x] `amount: Double` - total payment amount
    - [x] `status: String` - order status (`PENDING`, `SUCCESS`, `FAILED`)
    - [x] `items: MutableList<OrderItem>` - list of order items (`@OneToMany(mappedBy = "order", cascade = [ALL], orphanRemoval = true)`)
    - [x] `createdAt: LocalDateTime` - order creation timestamp
    - [x] `id: Long` - primary key 
  - [x] OrderItem
    - [x] `order: Order` - reference to the parent order (`@ManyToOne(fetch = LAZY)`)
    - [x] `productOption: Option` - reference to the selected product option (`@ManyToOne(fetch = LAZY)`)
    - [x] `quantity: Int` - number of items in this line (`@Column(nullable = false)`)
    - [x] `id: Long` - primary key (`@Id`, `@GeneratedValue(strategy = IDENTITY)`)
- [x] repository 
  - [x] OrderRepository
  - [x] OrderItemRepository
- [x] service
  - [x] OrderService
- [x] controller
  - [x] OrderController
- [x] exception
  - [x] PaymentException

## Step 2-3
- [x] Deployment script 
- [x] Environment & secrets management (no secrets in repo; use env vars).
- [x] Server CORS configuration.