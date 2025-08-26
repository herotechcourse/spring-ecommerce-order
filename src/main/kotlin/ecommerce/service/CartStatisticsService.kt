package ecommerce.service

import ecommerce.dto.member.ActiveMemberInfo
import ecommerce.dto.product.TopProductStats
import ecommerce.model.Product
import ecommerce.repository.CartItemRepository
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class CartStatisticsService(
    private val cartItemRepository: CartItemRepository,
) {
    fun getTop5AddedProductsInLast30Days(): List<TopProductStats> {
        val cartItems = cartItemRepository.findAll()

        val filteredCartItems =
            cartItems
                .filter { it.createdAt >= LocalDateTime.now().minusDays(30) }
                .sortedByDescending { it.createdAt }

        val productsInCartItems = filteredCartItems.map { it.product }.toSet().toList()
        val productCountMap = mutableMapOf<Product, Int>()
        productsInCartItems.forEach { product ->
            productCountMap[product] = filteredCartItems.count { it.product == product }
        }
        val sortedProductCount = productCountMap.toList().sortedByDescending { (_, value) -> value }
        val topFiveProducts = sortedProductCount.subList(0, 5)

        val listOfTopProductStats =
            topFiveProducts.map { productPair ->
                val createdAt = filteredCartItems.find { it.product.name == productPair.first.name }!!.createdAt
                TopProductStats(productPair.first.name, productPair.second, createdAt)
            }
        return listOfTopProductStats
    }

    fun getActiveMembersInLast7Days(): List<ActiveMemberInfo> {
        val cartItems = cartItemRepository.findAll()
        val filteredCartItems =
            cartItems
                .filter { it.createdAt >= LocalDateTime.now().minusDays(7) }
        val activeMembers = filteredCartItems.map { it.member }.toSet().toList()
        return activeMembers.map { member ->
            ActiveMemberInfo(member.id, member.email)
        }
    }
}
