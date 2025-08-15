package ecommerce.service

import ecommerce.dto.cartStatistics.MembersWhoAddedToCartDTO
import ecommerce.dto.cartStatistics.TopAddedProductsDTO
import ecommerce.enums.CartAction
import ecommerce.model.CartStatistic
import ecommerce.model.Option
import ecommerce.model.User
import ecommerce.repository.CartStatisticRepository
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class AdminStatisticsService(
    private val cartStatisticRepository: CartStatisticRepository,
) {
    fun getTopAddedProducts(): List<TopAddedProductsDTO> {
        val since = LocalDateTime.now().minusDays(TOP_PRODUCTS_SINCE)
        val limit = PageRequest.of(0, TOP_PRODUCTS_LIMIT)
        return cartStatisticRepository.findTopProducts(CartAction.ADD, since, limit)
    }

    fun getMembersWhoAddedToCart(): List<MembersWhoAddedToCartDTO> {
        val since = LocalDateTime.now().minusDays(ACTIVE_USERS_SINCE)
        return cartStatisticRepository.findActiveUsersSince(since)
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun createStatistic(
        member: User,
        option: Option,
        action: CartAction,
    ) {
        cartStatisticRepository.save(
            CartStatistic(
                member.id,
                member.email,
                member.name,
                option.id,
                option.name,
                option.price,
                action,
            ),
        )
    }

    companion object {
        private const val TOP_PRODUCTS_SINCE = 30L
        private const val TOP_PRODUCTS_LIMIT = 5
        private const val ACTIVE_USERS_SINCE = 7L
    }
}
