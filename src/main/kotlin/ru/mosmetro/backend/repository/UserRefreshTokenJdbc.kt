package ru.mosmetro.backend.repository

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository

@Repository
class UserRefreshTokenJdbc(private val jdbcTemplate: JdbcTemplate) : UserRefreshTokenRepository {

    override fun findByEmail(login: String): String {
        return jdbcTemplate.queryForObject(
            """
                SELECT refresh_token FROM user_refresh_token WHERE user_login = ?
            """, { rs, _ -> rs.getString("refresh_token") }, login
        )!!
    }

    override fun save(login: String) {
        jdbcTemplate.update("INSERT INTO user_refresh_token (user_login) VALUES (?)", login)
    }

    override fun update(login: String, refreshToken: String) {
        jdbcTemplate.update("UPDATE user_refresh_token SET refresh_token = ? WHERE user_login = ?", refreshToken, login)
    }
}
