package ru.mosmetro.backend.mapper

import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Component
import ru.mosmetro.backend.model.dto.AuthDTO
import ru.mosmetro.backend.model.entity.EmployeeEntity

@Component
class UserMapper {

    fun detailsDomainToAuthDto(details: UserDetails) = AuthDTO(
        role = details.authorities.first().authority
    )

    fun employeeEntityToDetailsDomain(employee: EmployeeEntity): UserDetails = User(
        employee.user.login,
        employee.user.password,
        true,
        true,
        true,
        true,
        setOf(SimpleGrantedAuthority(employee.rank.role))
    )
}
