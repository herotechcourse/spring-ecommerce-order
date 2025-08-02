//package ecommerce.controller
//
//import io.restassured.RestAssured
//import io.restassured.http.ContentType
//import org.assertj.core.api.Assertions.assertThat
//import org.junit.jupiter.api.BeforeEach
//import org.junit.jupiter.api.Test
//import org.springframework.beans.factory.annotation.Autowired
//import org.springframework.boot.test.context.SpringBootTest
//import org.springframework.http.HttpStatus
//import org.springframework.jdbc.core.JdbcTemplate
//import org.springframework.test.context.ActiveProfiles
//
//@ActiveProfiles("test")
//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
//class ProductRestControllerTest {
//    @Autowired
//    lateinit var jdbcTemplate: JdbcTemplate
//
//    private var productId: Long = 1
//
//    @BeforeEach
//    fun setUp() {
//        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY FALSE")
//        jdbcTemplate.execute("TRUNCATE TABLE CART")
//        jdbcTemplate.execute("TRUNCATE TABLE MEMBERS")
//        jdbcTemplate.execute("TRUNCATE TABLE PRODUCTS")
//        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY TRUE")
//
//        val productJson =
//            """
//            {
//                "name": "cafe",
//                "price": 39.0,
//                "imageUrl": "https://test.com/image.jpg",
//                "options": [
//                    {
//                        "name": "small",
//                        "quantity": 1
//                    }
//                ]
//            }
//            """.trimIndent()
//
//        val response =
//            RestAssured
//                .given().log().all()
//                .contentType(ContentType.JSON)
//                .body(productJson)
//                .`when`().post("/api/products")
//                .then().log().all()
//                .extract().response()
//
//        productId = response.jsonPath().getLong("id")
//    }
//
//    @Test
//    fun addProduct() {
//        val productJson =
//            """
//            {
//                "name": "table",
//                "price": 45.0,
//                "imageUrl": "https://test.com/image2.jpg",
//                 "options": [
//                    {
//                        "name": "small",
//                        "quantity": 1
//                    }
//                ]
//            }
//            """.trimIndent()
//
//        val response =
//            RestAssured
//                .given().log().all()
//                .accept(ContentType.JSON)
//                .contentType(ContentType.JSON)
//                .body(productJson)
//                .`when`().post("/api/products")
//                .then().extract().response()
//
//        assertThat(response.statusCode).isEqualTo(HttpStatus.OK.value())
//        val created = response.jsonPath()
//        assertThat(created.getString("name")).isEqualTo("table")
//        assertThat(created.getDouble("price")).isEqualTo(45.0)
//        assertThat(created.getString("imageUrl")).isEqualTo("https://test.com/image2.jpg")
//    }
//
//    @Test
//    fun getProducts_returnsList() {
//        val response =
//            RestAssured
//                .given().log().all()
//                .`when`().get("/api/products")
//                .then().extract().response()
//
//        assertThat(response.statusCode).isEqualTo(HttpStatus.OK.value())
//        val productList = response.jsonPath().getList("", Map::class.java)
//        assertThat(productList).isNotEmpty
//    }
//}
