package ecommerce.model

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
data class Product(
    @Column(nullable = false, unique = true)
    val name: String,
    @Column(nullable = false)
    val price: BigDecimal,
    val imageUrl: String,
    @OneToMany(mappedBy = "product", cascade = [CascadeType.ALL], orphanRemoval = true)
    val options: MutableList<Option> = mutableListOf(),
    @CreationTimestamp
    var createdAt: LocalDateTime? = null,
    @UpdateTimestamp
    var lastUpdatedAt: LocalDateTime? = null,
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,
) {
    fun addOption(option: Option) {
        require(options.none { it.name == option.name }) { "Option name already exist in the product" }
        options.add(option)
        option.product = this
    }

    init {
        require(options.isNotEmpty())
        require(options.distinctBy { it.name }.size == options.size)
    }
}
