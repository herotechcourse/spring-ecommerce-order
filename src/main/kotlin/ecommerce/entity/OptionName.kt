package ecommerce.entity

@JvmInline
value class OptionName(val value: String) {
    init {
        NameValidator.validate(value, 50)
    }

    override fun toString(): String = value
}
