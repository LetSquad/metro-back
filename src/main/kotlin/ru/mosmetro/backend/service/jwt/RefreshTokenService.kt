package ru.mosmetro.backend.service.jwt

import org.springframework.stereotype.Service
import ru.mosmetro.backend.repository.UserRefreshTokenRepository
import ru.mosmetro.backend.util.jpaContext

@Service
class RefreshTokenService(private val refreshTokenRepository: UserRefreshTokenRepository) {

    suspend fun isRefreshTokenValid(login: String, refreshToken: String): Boolean {
        return jpaContext { refreshTokenRepository.findByLogin(login) == refreshToken }
    }

    fun initUser(login: String) {
        refreshTokenRepository.save(login)
    }

    suspend fun updateRefreshToken(login: String, refreshToken: String) {
        jpaContext { refreshTokenRepository.update(login, refreshToken) }
    }
}
