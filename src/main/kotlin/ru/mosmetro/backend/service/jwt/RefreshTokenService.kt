package ru.mosmetro.backend.service.jwt

import org.springframework.stereotype.Service
import ru.mosmetro.backend.repository.UserRefreshTokenRepository

@Service
class RefreshTokenService(private val refreshTokenRepository: UserRefreshTokenRepository) {

    fun isRefreshTokenValid(login: String, refreshToken: String): Boolean {
        return refreshTokenRepository.findByEmail(login) == refreshToken
    }

    fun initUser(login: String) {
        refreshTokenRepository.save(login)
    }

    fun updateRefreshToken(login: String, refreshToken: String) {
        refreshTokenRepository.update(login, refreshToken)
    }
}
