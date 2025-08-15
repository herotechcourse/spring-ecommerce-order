package ecommerce.infrastructure

import mu.KotlinLogging

class ApplicationLogger {
    private val logger = KotlinLogging.logger {}

    fun logError(message: String?) {
        logger.error(message)
    }
}
