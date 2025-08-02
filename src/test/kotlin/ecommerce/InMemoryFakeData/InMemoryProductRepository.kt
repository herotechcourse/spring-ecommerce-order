//package ecommerce.InMemoryFakeData
//
//import ecommerce.entity.Option
//import ecommerce.repository.OptionJpaRepository
//import org.springframework.data.jpa.repository.JpaRepository
//
//class InMemoryOptionRepository : OptionJpaRepository{
//    private val options = mutableListOf<Option>()
//
//    override fun findByProductId(productId: Long): List<Option> {
//        return options.filter { it.product?.id == productId }
//    }
//
//    override fun save(option: Option): Option {
//        options.add(option.copy(id = (options.size + 1).toLong()))
//        return option
//    }
//
//    // Implement only the methods you need for the test. Throw for others:
//    override fun findAll(): List<Option> = throw NotImplementedError()
//    override fun findById(id: Long): Optional<Option> = throw NotImplementedError()
//    override fun deleteById(id: Long) = throw NotImplementedError()
//    // ...
//}
