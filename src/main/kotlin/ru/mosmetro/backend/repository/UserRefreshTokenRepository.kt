package ru.mosmetro.backend.repository

interface UserRefreshTokenRepository {

    fun findByEmail(login: String): String

    fun save(login: String)

    fun update(login: String, refreshToken: String)
}
