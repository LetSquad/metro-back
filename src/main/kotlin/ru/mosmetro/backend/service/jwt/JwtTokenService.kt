package ru.mosmetro.backend.service.jwt

import io.jsonwebtoken.Claims
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.Jws
import io.jsonwebtoken.JwtParser
import io.jsonwebtoken.Jwts
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Service
import ru.mosmetro.backend.config.properties.MetroSecurityProperties
import ru.mosmetro.backend.model.domain.UserWithRole
import ru.mosmetro.backend.util.getLogger
import java.time.Duration
import java.util.*
import javax.crypto.SecretKey

@Service
class JwtTokenService(
    private val properties: MetroSecurityProperties,
    private val jwtPrivateKey: SecretKey,
    private val jwtParser: JwtParser
) {

    fun generateAuthToken(userDetails: UserDetails): String = generateToken(
        userDetails = userDetails,
        tokenValidity = properties.authTokenValidity,
        withClaims = true
    )

    fun generateRefreshToken(userDetails: UserDetails): String = generateToken(
        userDetails = userDetails,
        tokenValidity = properties.refreshTokenValidity,
        withClaims = false
    )

    fun checkTokenValidOrExpired(jwtToken: String): Boolean = try {
        jwtParser.parse(jwtToken)
        true
    } catch (e: ExpiredJwtException) {
        true
    } catch (e: Exception) {
        log.error("Invalid jwt token", e)
        false
    }

    fun retrieveUserContext(jwtToken: String?): UserWithRole? {
        if (jwtToken == null) return null

        return try {
            val parsedToken: Jws<Claims> = jwtParser.parseSignedClaims(jwtToken)
            UserWithRole(
                login = parsedToken.payload.subject,
                role = parsedToken.payload.get(CLAIM_ROLE, String::class.java)
            )
        } catch (e: ExpiredJwtException) {
            null
        }
    }

    fun retrieveSubject(jwtToken: String): String {
        val parsedToken: Jws<Claims> = jwtParser.parseSignedClaims(jwtToken)
        return parsedToken.payload.subject
    }

    private fun generateToken(userDetails: UserDetails, tokenValidity: Duration, withClaims: Boolean): String {
        val currentTime: Long = System.currentTimeMillis()

        val jwtBuilder = Jwts.builder()
            .subject(userDetails.username)
        if (withClaims) {
            jwtBuilder.claim(CLAIM_ROLE, userDetails.authorities.first().authority)
        }
        return jwtBuilder.issuedAt(Date(currentTime))
            .expiration(Date(currentTime + tokenValidity.toMillis()))
            .signWith(jwtPrivateKey, Jwts.SIG.HS512)
            .compact()
    }

    companion object {
        private const val CLAIM_ROLE = "role"

        private val log = getLogger<JwtTokenService>()
    }
}
