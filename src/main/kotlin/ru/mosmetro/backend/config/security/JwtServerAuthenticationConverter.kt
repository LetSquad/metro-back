package ru.mosmetro.backend.config.security

import kotlinx.coroutines.reactor.mono
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import ru.mosmetro.backend.model.cookie.CookieName
import ru.mosmetro.backend.model.domain.UserWithRole
import ru.mosmetro.backend.service.jwt.JwtTokenService

@Component
class JwtServerAuthenticationConverter(
    private val tokenService: JwtTokenService
) : ServerAuthenticationConverter {

    override fun convert(exchange: ServerWebExchange): Mono<Authentication> {
        return mono {
            val user: UserWithRole = exchange.request
                .cookies[CookieName.AUTH_TOKEN]
                ?.first()
                ?.value
                ?.let { tokenService.retrieveUserContext(it) }
                ?: return@mono null

            return@mono UsernamePasswordAuthenticationToken(
                user.login,
                null,
                listOf(SimpleGrantedAuthority(user.role))
            )
        }
    }
}
