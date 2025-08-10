package ecommerce.validation

import ecommerce.repository.ProductStore
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component

@Component
class UniqueProductNameValidator(
    @Qualifier("jdbcProductStore")
    private val productStore: ProductStore,
) : ConstraintValidator<UniqueProductName, String> {
    override fun isValid(
        value: String,
        context: ConstraintValidatorContext?,
    ): Boolean {
        return productStore.findByName(value) == null
    }
}
