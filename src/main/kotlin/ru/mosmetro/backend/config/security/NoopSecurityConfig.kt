package ru.mosmetro.backend.config.security

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.session.WebSessionManager
import reactor.core.publisher.Mono


@Configuration
@EnableWebFluxSecurity
class NoopSecurityConfig {

    @Bean
    fun metroBackFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        return http.csrf { it.disable() }
            .cors { it.configurationSource(createUrlBasedCorsConfigurationSource()) }
            .formLogin { it.disable() }
            .httpBasic { it.disable() }
            .authorizeExchange {
                it.anyExchange().permitAll()
            }
            .build()
    }

    @Bean
    fun webSessionManager(): WebSessionManager {
        return WebSessionManager { _: ServerWebExchange -> Mono.empty() }
    }

    private fun createUrlBasedCorsConfigurationSource(): UrlBasedCorsConfigurationSource {
        val cors = CorsConfiguration()
        cors.applyPermitDefaultValues()
        cors.allowedMethods = listOf("*")
        cors.allowedOrigins = listOf("https://localhost:8888")
        cors.allowCredentials = true
        val ccs = UrlBasedCorsConfigurationSource()
        ccs.registerCorsConfiguration("/**", cors)
        return ccs
    }
}
