# spring-ecommerce-order

## Features List

1. Refactor model into entity
   - [x] `Product` into entity
   - [x] `Member` into entity
   - [x] `CartItem` into entity
   - [x] introduce new entity `Cart`
   - [x] find way to deal with cascade
   - [x] find way to deal with one-to-one &one-to-many & many-to-one & many-to-many

2. Refactor Repository
   - [x] `CartItemRepository` using `JpaRepository`
   - [x] `ProductRepository` using `JpaRepository`
   - [x] `MemberRepository` using `JpaRepository`
   - [x] `CartRepository` using `JpaRepository`
   - [x] refactor tests, using `@DataJpaTest` 
3. Refactor Service
   - ex. validation wit `require()`

4. Add feature **pagination** for `Product` and `CartItem`
   - [x] Apply interface `PagingAndSortingRepository` to product repository and cart item repository
   - [x] Apply `Pagable` to cast output into `Page` type
   - [x] Refactor `ProductController` to return with `Page` response

5. Add `options` to product information.
   - Property
     - [x] `id`
     - [x] `name` - up to 50 characters
     - [x] `quantity` - 1 to 100_000_000
   - Method
     - [x] decrease the quantity of the option by a specific amount
     - [x] A `product` must always have at least one `option`
   - DTO
     - [x] implement `ProductResponse` to organize product response with pagination
     - [x] implement `OptionResponse` to organize options response for a product

6. Implement the `Place Order` feature using the Stripe Payment Create API
    - [x] create `Order Entity` with id, relation to Cart (cart has relation to member), list of CartItems (purchased items), quantity, totalPrice (payment amount), relation to `Payment Entity`(one to one), createdAt, orderStatus, checkout session id (issued by stripe)
    - [x] create `Payment Entity` wit id, amount, currency, createdAt
    - [x] add a method to the cart repository if the cartItem that was ordered exists in the cart, it should be removed
    - [x] add a method that decreases the quantity of the ordered item (in cartItem repo?) 
