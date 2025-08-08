package ecommerce.dto

interface PageItem

class PagedResponse<T : PageItem>(
    val content: List<T>,
    val number: Int,
    val size: Int,
    val totalPages: Int,
    val totalElements: Long,
)
