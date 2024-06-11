package ru.mosmetro.backend.mapper

import java.time.LocalTime
import java.time.temporal.ChronoUnit
import org.springframework.stereotype.Component
import ru.mosmetro.backend.model.domain.Employee
import ru.mosmetro.backend.model.dto.employee.EmployeeDTO
import ru.mosmetro.backend.model.dto.employee.EmployeeRankDTO
import ru.mosmetro.backend.model.dto.employee.NewEmployeeDTO
import ru.mosmetro.backend.model.dto.employee.UpdateEmployeeDTO
import ru.mosmetro.backend.model.entity.EmployeeEntity
import ru.mosmetro.backend.model.entity.MetroUserEntity
import ru.mosmetro.backend.model.enums.SexType

@Component
class EmployeeMapper(
        private val employeeRankMapper: EmployeeRankMapper
) {
    fun entityToDomain(mapper: EmployeeEntity) = Employee(
        id = mapper.id,
        firstName = mapper.firstName,
        lastName = mapper.lastName,
        middleName = mapper.middleName,
        sex = SexType.valueOf(mapper.sex),
        workStart = mapper.workStart,
        workFinish = mapper.workFinish,
        shiftType = mapper.shiftType,
        workPhone = mapper.workPhone,
        personalPhone = mapper.personalPhone,
        employeeNumber = mapper.employeeNumber,
        lightDuties = mapper.lightDuties,
        rank = employeeRankMapper.entityToDomain(mapper.rank),
        login = mapper.user.login
    )

    fun domainToDto(mapper: Employee) = EmployeeDTO(
        id = mapper.id,
        firstName = mapper.firstName,
        lastName = mapper.lastName,
        middleName = mapper.middleName,
        sex = mapper.sex,
        shift = mapper.workStart.truncatedTo(ChronoUnit.MINUTES).toString() + "-" + mapper.workFinish.truncatedTo(
            ChronoUnit.MINUTES
        ).toString(),
        workPhone = mapper.workPhone,
        personalPhone = mapper.personalPhone,
        employeeNumber = mapper.employeeNumber,
        lightDuties = mapper.lightDuties,
        rank = employeeRankMapper.domainToDto(mapper.rank),
        employeeRole = employeeRankMapper.domainToDto(mapper.rank).role,
        login = mapper.workPhone
    )

    fun dtoToDomain(mapper: NewEmployeeDTO, employeeRankDTO: EmployeeRankDTO) = Employee(
        id = null,
        firstName = mapper.firstName,
        lastName = mapper.lastName,
        middleName = mapper.middleName,
        sex = mapper.sex,
        workStart = LocalTime.of(mapper.shift.take(2).toInt(), mapper.shift.substring(3, 5).toInt()),
        workFinish = LocalTime.of(mapper.shift.substring(6, 8).toInt(), mapper.shift.takeLast(2).toInt()),
        shiftType = mapper.shift,
        workPhone = mapper.workPhone,
        personalPhone = mapper.personalPhone,
        employeeNumber = mapper.employeeNumber,
        lightDuties = mapper.lightDuties,
        rank = employeeRankMapper.dtoToDomain(employeeRankDTO),
        login = mapper.workPhone
    )

    fun dtoToDomain(mapper: UpdateEmployeeDTO, id: Long, employeeRankDTO: EmployeeRankDTO) = Employee(
        id = id,
        firstName = mapper.firstName,
        lastName = mapper.lastName,
        middleName = mapper.middleName,
        sex = mapper.sex,
        workStart = LocalTime.of(mapper.shift.take(2).toInt(), mapper.shift.substring(3, 5).toInt()),
        workFinish = LocalTime.of(mapper.shift.substring(6, 8).toInt(), mapper.shift.takeLast(2).toInt()),
        workPhone = mapper.workPhone,
        personalPhone = mapper.personalPhone,
        employeeNumber = mapper.employeeNumber,
        lightDuties = mapper.lightDuties,
        rank = employeeRankMapper.dtoToDomain(employeeRankDTO),
        login = mapper.workPhone,
        shiftType = mapper.shift //TODO это поле нужно??
    )

    fun domainToEntity(mapper: Employee, userEntity: MetroUserEntity) = EmployeeEntity(
        id = mapper.id,
        firstName = mapper.firstName,
        lastName = mapper.lastName,
        middleName = mapper.middleName,
        sex = mapper.sex.name,
        workStart = mapper.workStart,
        workFinish = mapper.workFinish,
        shiftType = mapper.shiftType,
        workPhone = mapper.workPhone,
        personalPhone = mapper.personalPhone,
        employeeNumber = mapper.employeeNumber,
        lightDuties = mapper.lightDuties,
        rank = employeeRankMapper.domainToEntity(mapper.rank),
        user = userEntity
    )
}