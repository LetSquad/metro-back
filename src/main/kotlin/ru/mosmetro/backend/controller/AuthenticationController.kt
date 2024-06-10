package ru.mosmetro.backend.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CookieValue
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.mosmetro.backend.model.cookie.CookieName
import ru.mosmetro.backend.model.dto.AuthDTO
import ru.mosmetro.backend.model.dto.SignInDTO
import ru.mosmetro.backend.service.jwt.AuthenticationService
import ru.mosmetro.backend.util.getLogger

@Tag(name = "Методы аутентификации")
@RestController
@RequestMapping("/api/auth")
class AuthenticationController(
    private val authService: AuthenticationService
) {

    @Operation(
        summary = "Аутентификация пользователя",
        description = "В куки проставляются auth и refresh токены для пользователя"
    )
    @PostMapping
    suspend fun postAuth(
        @RequestBody signIn: SignInDTO,
    ): ResponseEntity<AuthDTO> = try {
        val (userRole, jwtCookies) = authService.authUser(signIn)

        ResponseEntity.ok()
            .header(HttpHeaders.SET_COOKIE, jwtCookies.retrieveAuthCookie().toString())
            .header(HttpHeaders.SET_COOKIE, jwtCookies.retrieveRefreshCookie().toString())
            .body(userRole)
    } catch (e: Exception) {
        log.warn("Unauthorized auth request", e)

        ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .build()
    }

    @Operation(
        summary = "Обновление токенов пользователя",
        description = "В куки проставляются новые auth и refresh токены для пользователя"
    )
    @PostMapping("/refresh")
    suspend fun postAuthRefresh(
        @Parameter(hidden = true) @CookieValue(CookieName.AUTH_TOKEN) authToken: String,
        @Parameter(hidden = true) @CookieValue(CookieName.REFRESH_TOKEN) refreshToken: String
    ): ResponseEntity<AuthDTO> = try {
        val (userRole, jwtCookies) = authService.updateTokens(authToken, refreshToken)

        ResponseEntity.ok()
            .header(HttpHeaders.SET_COOKIE, jwtCookies.retrieveAuthCookie().toString())
            .header(HttpHeaders.SET_COOKIE, jwtCookies.retrieveRefreshCookie().toString())
            .body(userRole)
    } catch (e: Exception) {
        log.warn("Unauthorized token refresh request", e)

        ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .build()
    }

    companion object {
        private val log = getLogger<AuthenticationController>()
    }
}
