package ecommerce.infrastructure

import ecommerce.config.security.JwtProperties
import ecommerce.entities.MemberEntity
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets
import java.util.Date
import javax.crypto.SecretKey

@Component
class JwtTokenProvider(props: JwtProperties) {
    private val secretKey: SecretKey =
        Keys.hmacShaKeyFor(props.secretKey.toByteArray(StandardCharsets.UTF_8))
    private val validityInMs = props.expireLengthMs

    fun createToken(
        payload: String,
        role: MemberEntity.Role,
    ): String {
        val now = Date()
        val exp = Date(now.time + validityInMs)
        return Jwts.builder()
            .subject(payload)
            .claim("role", role)
            .issuedAt(now)
            .expiration(exp)
            .signWith(secretKey, Jwts.SIG.HS256)
            .compact()
    }

    fun getPayload(token: String): Pair<String, MemberEntity.Role> {
        val claims =
            Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .payload
        val subject = claims.subject
        val role = MemberEntity.Role.valueOf(claims["role"] as String)

        return Pair(subject, role)
    }

    fun validateToken(token: String): Boolean {
        return try {
            val claims =
                Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
            !claims.payload.expiration.before(Date())
        } catch (e: JwtException) {
            false
        } catch (e: IllegalArgumentException) {
            false
        }
    }
}
