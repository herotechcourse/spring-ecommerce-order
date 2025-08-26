package ecommerce.controller

import ecommerce.DatabaseFixture
import ecommerce.dto.TokenRequest
import ecommerce.model.Cart
import ecommerce.model.CartItem
import ecommerce.model.Option
import ecommerce.model.Product
import ecommerce.repository.CartItemRepository
import ecommerce.repository.CartRepository
import ecommerce.repository.MemberRepository
import ecommerce.repository.OptionRepository
import ecommerce.repository.ProductRepository
import io.restassured.RestAssured
import io.restassured.http.ContentType
import io.restassured.response.ExtractableResponse
import io.restassured.response.Response
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDateTime

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class StatisticsE2ETest() {
    @Autowired
    private lateinit var memberRepository: MemberRepository

    @Autowired
    private lateinit var productRepository: ProductRepository

    @Autowired
    private lateinit var cartRepository: CartRepository

    @Autowired
    private lateinit var cartItemRepository: CartItemRepository

    @Autowired
    private lateinit var optionRepository: OptionRepository

    @LocalServerPort
    private var port: Int = 0

    val baseUrl get() = "http://localhost:$port"

    private lateinit var mostRecentCartItems: List<CartItem>

    @BeforeEach
    fun setUp() {
        val fortyDaysAgo = LocalDateTime.now().minusDays(40)

        // --- Members ---
        val mina = memberRepository.save(DatabaseFixture.createMina())
        val petra = memberRepository.save(DatabaseFixture.createPetra())
        memberRepository.save(DatabaseFixture.createAdmin())

        // --- Carts ---
        val minasCart = cartRepository.save(Cart(mina))
        val petrasCart = cartRepository.save(Cart(petra))

        // --- Products & Options ---
        fun createAndSaveOption(
            product: Product,
            optionName: String,
            qty: Int,
        ): Option {
            val savedProduct = productRepository.save(product)
            val option =
                Option(name = optionName, quantity = qty).apply {
                    this.product = savedProduct
                }
            return optionRepository.save(option)
        }

        val brushOption = createAndSaveOption(DatabaseFixture.createBrush(), "Brush Large", 9)
        val paletteOption = createAndSaveOption(DatabaseFixture.createPalette(), "Palette Small", 8)
        val canvasOption = createAndSaveOption(DatabaseFixture.createCanvas(), "Canvas A4", 10)
        val acrylicsOption = createAndSaveOption(DatabaseFixture.createAcrylics(), "Acrylics Set", 6)
        val penOption = createAndSaveOption(DatabaseFixture.createPen(), "Pen Black", 12)
        val pencilOption = createAndSaveOption(DatabaseFixture.createPencil(), "Pencil HB", 15)

        // --- Cart Items ---
        fun createCartItem(
            option: Option,
            cart: Cart,
            qty: Int,
            created: LocalDateTime,
        ) = CartItem(option = option, cart = cart, quantity = qty, createdAt = created, updatedAt = created)

        val recentItems =
            listOf(
                createCartItem(brushOption, minasCart, 7, now()),
                createCartItem(paletteOption, minasCart, 6, now()),
                createCartItem(canvasOption, minasCart, 5, now()),
            )

        val oldItems =
            listOf(
                createCartItem(penOption, minasCart, 4, fortyDaysAgo),
                createCartItem(acrylicsOption, minasCart, 3, fortyDaysAgo),
                createCartItem(pencilOption, minasCart, 2, fortyDaysAgo),
                createCartItem(pencilOption, petrasCart, 2, fortyDaysAgo),
            )

        cartItemRepository.saveAll(recentItems + oldItems)
        mostRecentCartItems = recentItems
    }

    private fun now() = LocalDateTime.now()

    @AfterEach
    fun tearDown() {
        cartItemRepository.deleteAll()
        cartRepository.deleteAll()
        memberRepository.deleteAll()
        productRepository.deleteAll()
    }

    private fun loginAS(
        email: String,
        password: String,
    ): String {
        val loginRequest = TokenRequest(email, password)
        val loginResponse =
            RestAssured
                .given()
                .baseUri(baseUrl)
                .body(loginRequest)
                .contentType(ContentType.JSON)
                .`when`()
                .post("/api/members/login")
                .then().log().all()
                .extract()
        val token = loginResponse.body().jsonPath().getString("token")
        return token
    }

    private fun getStatistics(
        token: String,
        path: String,
    ): ExtractableResponse<Response> =
        RestAssured
            .given()
            .baseUri(baseUrl)
            .log().all()
            .header("Authorization", "Bearer $token")
            .get(path)
            .then().log().all()
            .extract()

    @Test
    fun `should return top 5 most added products in the past 30 days for admin`() {
        val token = loginAS(DatabaseFixture.ADMIN.email, DatabaseFixture.ADMIN.password)

        val stats = getStatistics(token, "/admin/statistics/top-products")

        Assertions.assertThat(stats.statusCode()).isEqualTo(HttpStatus.OK.value())
        val json = stats.body().jsonPath()
        val productNames = json.getList<String>("productName")
        val topProducts =
            listOf(mostRecentCartItems[0].option.name, mostRecentCartItems[1].option.name, mostRecentCartItems[2].option.name)
        Assertions.assertThat(productNames).containsExactlyInAnyOrderElementsOf(topProducts)
    }

    @Test
    fun `should return active members in the past 7 days for admin`() {
        val token = loginAS(DatabaseFixture.ADMIN.email, DatabaseFixture.ADMIN.password)

        val stats = getStatistics(token, "/admin/statistics/active-members")

        Assertions.assertThat(stats.statusCode()).isEqualTo(HttpStatus.OK.value())
        val json = stats.body().jsonPath()
        val emails = json.getList<String>("email")
        Assertions.assertThat(emails).containsExactly(DatabaseFixture.MINA.email)
    }

    @ParameterizedTest
    @ValueSource(strings = ["/admin/statistics/top-products", "/admin/statistics/active-members"])
    fun `should not return statistics for user`(path: String) {
        val token = loginAS(DatabaseFixture.MINA.email, DatabaseFixture.MINA.password)

        val stats = getStatistics(token, path)

        Assertions.assertThat(stats.statusCode()).isEqualTo(HttpStatus.FORBIDDEN.value())
    }
}
