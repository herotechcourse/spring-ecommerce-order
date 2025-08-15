package ecommerce.config.security

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.validation.annotation.Validated

@Validated
@ConfigurationProperties("security.jwt.token")
class JwtProperties(
    @field:NotBlank
    val secretKey: String,
    @field:Min(1000)
    val expireLengthMs: Long = 3600000,
)
