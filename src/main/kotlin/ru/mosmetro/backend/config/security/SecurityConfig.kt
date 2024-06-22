package ru.mosmetro.backend.config.security

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.SecurityWebFiltersOrder
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.core.userdetails.ReactiveUserDetailsService
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.authentication.AuthenticationWebFilter
import org.springframework.security.web.server.authentication.HttpStatusServerEntryPoint
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter
import org.springframework.security.web.server.authorization.HttpStatusServerAccessDeniedHandler
import org.springframework.security.web.server.csrf.CookieServerCsrfTokenRepository
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource
import reactor.core.publisher.Mono
import ru.mosmetro.backend.config.properties.MetroSecurityProperties


@Configuration
@EnableWebFluxSecurity
class SecurityConfig(
    private val metroSecurityProperties: MetroSecurityProperties
) {

    @Bean
    fun metroBackFilterChain(
        http: ServerHttpSecurity,
        authenticationFilter: AuthenticationWebFilter
    ): SecurityWebFilterChain {
        val config = http.formLogin { it.disable() }
            .httpBasic { it.disable() }
            .authorizeExchange {
                it.pathMatchers(HttpMethod.POST, "/api/auth").permitAll()
                    .pathMatchers(HttpMethod.POST, "/api/auth/refresh").permitAll()
                    .pathMatchers("/api/**").authenticated()
                    .anyExchange().permitAll()
            }
            .exceptionHandling {
                it.authenticationEntryPoint(HttpStatusServerEntryPoint(HttpStatus.UNAUTHORIZED))
                    .accessDeniedHandler(HttpStatusServerAccessDeniedHandler(HttpStatus.FORBIDDEN))
            }
            .addFilterAt(authenticationFilter, SecurityWebFiltersOrder.AUTHENTICATION)

        if (metroSecurityProperties.csrfEnables) {
            config.csrf { it.csrfTokenRepository(CookieServerCsrfTokenRepository.withHttpOnlyFalse()) }
        } else {
            config.csrf { it.disable() }
        }

        if (metroSecurityProperties.corsEnables) {
            config.cors { it.configurationSource(createLocalUrlBasedCorsConfigurationSource()) }
        }

        return config.build()
    }

    @Bean
    fun jwtAuthenticationWebFilter(
        manager: ReactiveAuthenticationManager,
        converter: ServerAuthenticationConverter
    ): AuthenticationWebFilter {
        return AuthenticationWebFilter(ReactiveAuthenticationManager { a -> Mono.just(a) })
            .also { it.setServerAuthenticationConverter(converter) }
    }

    @Bean
    fun passwordAuthenticationManager(
        userDetailsService: ReactiveUserDetailsService,
        passwordEncoder: PasswordEncoder
    ): ReactiveAuthenticationManager {
        return UserDetailsRepositoryReactiveAuthenticationManager(userDetailsService).also {
            it.setPasswordEncoder(passwordEncoder)
        }
    }

    private fun createLocalUrlBasedCorsConfigurationSource(): UrlBasedCorsConfigurationSource {
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
