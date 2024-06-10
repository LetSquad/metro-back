package ru.mosmetro.backend.model.cookie

import org.springframework.http.ResponseCookie
import ru.mosmetro.backend.model.cookie.CookieName.AUTH_TOKEN
import ru.mosmetro.backend.model.cookie.CookieName.REFRESH_TOKEN
import java.time.Duration

data class JwtCookies(
    private val authToken: String,
    private val refreshToken: String,
    private val cookiesValidity: Duration,
    private val isSecure: Boolean,
    private val noneSameSite: Boolean
) {

    fun retrieveAuthCookie(): ResponseCookie = ResponseCookie.fromClientResponse(AUTH_TOKEN, authToken)
        .maxAge(cookiesValidity)
        .path("/")
        .secure(isSecure)
        .httpOnly(true)
        .sameSite(if (noneSameSite) NONE_SAME_SITE else null)
        .build()

    fun retrieveRefreshCookie(): ResponseCookie = ResponseCookie.fromClientResponse(REFRESH_TOKEN, refreshToken)
        .maxAge(cookiesValidity)
        .path("/")
        .secure(isSecure)
        .httpOnly(true)
        .sameSite(if (noneSameSite) NONE_SAME_SITE else null)
        .build()

    companion object {
        private const val NONE_SAME_SITE = "None"
    }
}
