package ecommerce.entities

import ecommerce.dto.OptionDTO
import ecommerce.dto.ProductPatchDTO
import ecommerce.dto.ProductRequestDTO
import ecommerce.exception.InvalidOptionNameException
import ecommerce.mappers.toEntity
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table

@Entity
@Table(name = "product")
class ProductEntity(
    @Column(name = "name", nullable = false, length = 15)
    var name: String,
    @Column(name = "price", nullable = false)
    var price: Double,
    @Column(name = "image_url", nullable = false, length = 255)
    var imageUrl: String,
    @OneToMany(mappedBy = "product", cascade = [CascadeType.ALL], orphanRemoval = true)
    private val _options: MutableSet<OptionEntity> = mutableSetOf(),
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,
) {
    var options: List<OptionEntity>
        get() = _options.toList()
        set(value) {
            _options.clear()
            _options.addAll(value)
        }

    fun addOption(option: OptionEntity) {
        if (_options.any { it.name == option.name }) {
            throw InvalidOptionNameException("Option with name '${option.name}' already exists")
        }
        _options.add(option)
    }

    fun copyFrom(
        other: ProductRequestDTO,
        optionDTOs: Set<OptionDTO> = emptySet(),
    ) {
        this.name = other.name
        this.price = other.price
        this.imageUrl = other.imageUrl
        this.options = mapOptionDTOs(optionDTOs)
    }

    fun copyFrom(
        other: ProductPatchDTO,
        optionDTOs: Set<OptionDTO> = emptySet(),
    ) {
        other.name?.let { if (it.isNotBlank()) this.name = it }
        other.price?.let { this.price = it }
        other.imageUrl?.let { if (it.isNotBlank()) this.imageUrl = it }
        if (optionDTOs.isNotEmpty()) {
            this.options = mapOptionDTOs(optionDTOs)
        }
    }

    private fun mapOptionDTOs(optionDTOs: Set<OptionDTO>): List<OptionEntity> {
        return optionDTOs.map { dto ->
            _options.find { it.id == dto.id }?.apply {
                name = dto.name
                quantity = dto.quantity
            } ?: dto.toEntity(this)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ProductEntity) return false
        return id != 0L && id == other.id
    }

    override fun hashCode(): Int = id.hashCode()
}
