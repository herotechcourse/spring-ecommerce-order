package ecommerce.auth

import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets
import java.util.Date
import javax.crypto.SecretKey

@Component
class JwtTokenProvider(
    @Value("\${security.jwt.token.secret-key}")
    secret: String,
    @Value("\${security.jwt.token.expire-length}")
    private val validityInMs: Long,
) {
    private val secretKey: SecretKey =
        Keys.hmacShaKeyFor(secret.toByteArray(StandardCharsets.UTF_8))

    fun createToken(memberId: Long): String {
        val now = Date()
        val exp = Date(now.time + validityInMs)
        return Jwts.builder()
            .subject(memberId.toString())
            .issuedAt(now)
            .expiration(exp)
            .signWith(secretKey, Jwts.SIG.HS256)
            .compact()
    }

    fun getSubject(token: String): String {
        return try {
            val claims =
                Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .payload
            claims.subject
        } catch (e: JwtException) {
            throw IllegalArgumentException("Invalid or expired token")
        }
    }

    fun validateToken(token: String): Boolean {
        return try {
            val claims =
                Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
            !claims.payload.expiration.before(Date())
        } catch (e: Exception) {
            false
        }
    }
}
