package ecommerce.model

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToMany
import java.time.LocalDateTime

@Entity
class Product(
    @Column(name = "name", nullable = false, unique = true)
    var name: String,
    @Column(name = "image_url", nullable = false)
    var imageUrl: String,
    @OneToMany(cascade = [CascadeType.ALL], fetch = FetchType.LAZY, orphanRemoval = true)
    @JoinColumn(name = "product_id")
    var options: MutableList<Option>,
    @Column(name = "created_at", nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,
) {
    init {
        require(options.isNotEmpty()) { "at least one option must be specified" }
        require(options.map { it.name }.distinct().size == options.size) { "Options must be distinct" }
    }
}
