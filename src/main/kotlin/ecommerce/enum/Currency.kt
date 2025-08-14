package ecommerce.enum

enum class Currency(val description: String, val symbol: String) {
    EUR("Euro", "€"),
    USD("US Dollar", "$"),
    GBP("British Pound", "£"),
}
