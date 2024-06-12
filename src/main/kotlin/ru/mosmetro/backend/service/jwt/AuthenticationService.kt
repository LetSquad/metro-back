package ru.mosmetro.backend.service.jwt

import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.boot.autoconfigure.web.ServerProperties
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Service
import ru.mosmetro.backend.config.properties.MetroSecurityProperties
import ru.mosmetro.backend.exception.JwtValidationException
import ru.mosmetro.backend.mapper.UserMapper
import ru.mosmetro.backend.model.cookie.JwtCookies
import ru.mosmetro.backend.model.dto.AuthDTO
import ru.mosmetro.backend.model.dto.SignInDTO

@Service
class AuthenticationService(
    private val serverProperties: ServerProperties,
    private val securityProperties: MetroSecurityProperties,
    private val userMapper: UserMapper,
    private val authenticationManager: ReactiveAuthenticationManager,
    private val jwtTokenService: JwtTokenService,
    private val userDetailsService: MetroUserDetailsService,
    private val refreshTokenService: RefreshTokenService
) {

    suspend fun authUser(signIn: SignInDTO): Pair<AuthDTO, JwtCookies> {
        val userDetails: UserDetails = doAuth(signIn.phone.lowercase(), signIn.password)
        return userMapper.detailsDomainToAuthDto(userDetails) to createAuthenticationTokens(userDetails)
    }

    suspend fun updateTokens(authToken: String, refreshToken: String): Pair<AuthDTO, JwtCookies> {
        if (!jwtTokenService.checkTokenValidOrExpired(authToken)) {
            throw JwtValidationException("Invalid auth token")
        }

        val username: String = jwtTokenService.retrieveSubject(refreshToken)
        if (!refreshTokenService.isRefreshTokenValid(username, refreshToken)) {
            throw JwtValidationException("Invalid refresh token for user with username = $username")
        }

        val userDetails: UserDetails = userDetailsService.findByUsername(username)
            .awaitSingle()
        return userMapper.detailsDomainToAuthDto(userDetails) to createAuthenticationTokens(userDetails)
    }

    private suspend fun doAuth(email: String, password: String): UserDetails {
        return UsernamePasswordAuthenticationToken(email, password)
            .let { authenticationManager.authenticate(it) }
            .awaitSingle()
            .principal as UserDetails
    }

    private suspend fun createAuthenticationTokens(userDetails: UserDetails): JwtCookies {
        val refreshToken: String = jwtTokenService.generateRefreshToken(userDetails)
        refreshTokenService.updateRefreshToken(userDetails.username, refreshToken)

        return JwtCookies(
            authToken = jwtTokenService.generateAuthToken(userDetails),
            refreshToken = refreshToken,
            cookiesValidity = securityProperties.refreshTokenValidity,
            isSecure = serverProperties.ssl?.isEnabled ?: false,
            noneSameSite = securityProperties.noneSameSiteCookies
        )
    }
}
