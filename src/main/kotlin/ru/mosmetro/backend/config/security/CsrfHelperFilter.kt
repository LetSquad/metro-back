package ru.mosmetro.backend.config.security

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.security.web.server.csrf.CsrfToken
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono

@Component
@ConditionalOnProperty("metro.security.csrf-enables")
class CsrfHelperFilter : WebFilter {

    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        val key = CsrfToken::class.java.name
        val csrfToken: Mono<CsrfToken> = exchange.getAttribute(key) ?: Mono.empty()
        return csrfToken.doOnSuccess { }
            .then(chain.filter(exchange))
    }
}
