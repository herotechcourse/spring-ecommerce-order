package ecommerce.controller.admin.usecase

import ecommerce.dto.TopProductDTO

interface FindTopProductsUseCase {
    fun findProducts(): List<TopProductDTO>
}
