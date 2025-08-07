package ecommerce.entity

internal object NameValidator {
    fun validate(
        name: String,
        maxLength: Int,
    ) {
        require(name.isNotBlank()) { "Name cannot be blank" }
        require(name.length <= maxLength) { "Name must be at most $maxLength characters" }
        val allowedCharacters = Regex("""^[\w\s\[\]\(\)\+\-\&/_]+$""")
        require(name.matches(allowedCharacters)) {
            "Name contains invalid characters"
        }
    }
}
