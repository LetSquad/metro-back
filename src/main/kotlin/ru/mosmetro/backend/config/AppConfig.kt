package ru.mosmetro.backend.config

import io.jsonwebtoken.JwtParser
import io.jsonwebtoken.Jwts
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import ru.mosmetro.backend.config.properties.MetroSecurityProperties
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

@Configuration
class AppConfig(private val securityProperties: MetroSecurityProperties) {

    @Bean
    fun jwtPrivateKey(): SecretKey {
        return SecretKeySpec(securityProperties.keySecret.toByteArray(), "HmacSHA512")
    }

    @Bean
    fun jwtParser(jwtPrivateKey: SecretKey): JwtParser = Jwts.parser()
        .verifyWith(jwtPrivateKey)
        .build()

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()
}
