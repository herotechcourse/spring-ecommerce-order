package ecommerce.controller.admin.usecase

import ecommerce.dto.OptionDTO

interface CreateOptionUseCase {
    fun create(optionDTO: OptionDTO)
}
