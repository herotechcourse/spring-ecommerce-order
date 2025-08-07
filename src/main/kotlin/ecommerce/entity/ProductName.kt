package ecommerce.entity

@JvmInline
value class ProductName(val value: String) {
    init {
        NameValidator.validate(value, 15)
    }

    override fun toString(): String = value
}
