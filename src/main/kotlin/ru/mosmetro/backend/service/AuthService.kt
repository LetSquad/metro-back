package ru.mosmetro.backend.service

import org.springframework.stereotype.Service
import ru.mosmetro.backend.model.dto.AuthDTO

@Service
class AuthService {

    fun auth(): AuthDTO {
        return AuthDTO("ADMIN")
    }

    fun refresh(): AuthDTO {
        return AuthDTO("ADMIN")
    }
}