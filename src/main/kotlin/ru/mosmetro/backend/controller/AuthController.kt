package ru.mosmetro.backend.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.mosmetro.backend.model.dto.AuthDTO
import ru.mosmetro.backend.model.dto.SignInDTO
import ru.mosmetro.backend.service.AuthService

@Tag(name = "Методы авторизации")
@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService
) {
    @Operation(
        summary = "Авторизация"
    )
    @PostMapping
    fun auth(@RequestBody signInDTO: SignInDTO): AuthDTO {
        return authService.auth()
    }

    @Operation(
        summary = "Обновление токена"
    )
    @PostMapping("refresh")
    fun refresh(): AuthDTO {
        return authService.refresh()
    }
}