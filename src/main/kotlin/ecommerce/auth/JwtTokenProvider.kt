package ecommerce.auth

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.Date

@Component
class JwtTokenProvider(
    @Value("\${jwt.secret-key}")
    private val secret: String,
    @Value("\${jwt.expiration-ms}")
    private val validityInMilliseconds: Long,
) {
    private val secretKey = Keys.hmacShaKeyFor(secret.toByteArray())

    fun createToken(payload: String): String {
        val now = Date()
        val validity = Date(now.time + validityInMilliseconds)

        return Jwts.builder()
            .subject(payload)
            .issuedAt(now)
            .expiration(validity)
            .signWith(secretKey, Jwts.SIG.HS256)
            .compact()
    }

    fun getPayload(token: String): String {
        return Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)
            .payload
            .subject
    }

    fun validateToken(token: String): Boolean {
        val claims =
            Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
        return !claims.payload.expiration.before(Date())
    }
}
