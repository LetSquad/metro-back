package ru.mosmetro.backend.service.jwt

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.reactor.mono
import org.springframework.security.core.userdetails.ReactiveUserDetailsService
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import ru.mosmetro.backend.mapper.UserMapper
import ru.mosmetro.backend.repository.EmployeeEntityRepository

@Service
class MetroUserDetailsService(
    private val userMapper: UserMapper,
    private val employeeRepository: EmployeeEntityRepository
): ReactiveUserDetailsService {

    override fun findByUsername(username: String): Mono<UserDetails> {
        return mono(Dispatchers.IO) {
            employeeRepository.findByUserLogin(username)
                .let { userMapper.employeeEntityToDetailsDomain(it) }
        }
    }
}
