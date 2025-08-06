package ecommerce.model

class Product(
    val id: Long? = null,
    var name: String = "",
    var price: Double = 0.0,
    var imageUrl: String = "",
) {
    init {
        require(name.isNotBlank()) { "Product name must not be blank" }
        require(price >= 0.01) { "Price must be at least 0.01" }
        require(imageUrl.startsWith("http://") || imageUrl.startsWith("https://")) {
            "Image URL must start with http:// or https://"
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Product) return false

        return id == other.id
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }
}
