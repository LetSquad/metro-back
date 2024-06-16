package ru.mosmetro.backend.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.ConstructorBinding
import java.time.Duration

@ConfigurationProperties(prefix = "metro.security")
data class MetroSecurityProperties @ConstructorBinding constructor(
    val keySecret: String,
    val authTokenValidity: Duration,
    val refreshTokenValidity: Duration,
    val noneSameSiteCookies: Boolean,
    val corsEnables: Boolean,
)
