package ecommerce.model

enum class Currency(val code: String) {
    USD("usd"),
    EUR("eur"),
    GBP("gbp"),
    ;

    companion object {
        fun fromCode(code: String): Currency =
            entries.find { it.code.equals(code, ignoreCase = true) }
                ?: throw IllegalArgumentException("Unknown currency code: $code")
    }
}
