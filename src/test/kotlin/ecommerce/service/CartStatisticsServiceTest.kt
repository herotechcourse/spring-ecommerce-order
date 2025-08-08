package ecommerce.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.transaction.annotation.Transactional

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional
class CartStatisticsServiceTest(
    @Autowired private val cartStatisticsService: CartStatisticsService,
) {
    @LocalServerPort
    private var port: Int = 0

    @Test
    fun getTop5AddedProductsInLast30Days() {
        val actual = cartStatisticsService.getTop5AddedProductsInLast30Days()
        actual.size
        assertThat(actual).hasSize(5)
    }

    @Test
    fun getActiveMembersInLast7Days() {
        val actual = cartStatisticsService.getActiveMembersInLast7Days()
        actual.size
        assertThat(actual).isNotEmpty()
    }
}
