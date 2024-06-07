package ru.mosmetro.backend.service

import org.springframework.stereotype.Service
import ru.mosmetro.backend.exception.EntityNotFoundException
import ru.mosmetro.backend.mapper.EmployeeMapper
import ru.mosmetro.backend.mapper.EmployeeRankMapper
import ru.mosmetro.backend.mapper.EmployeeShiftMapper
import ru.mosmetro.backend.model.dto.ListWithTotal
import ru.mosmetro.backend.model.dto.employee.EmployeeDTO
import ru.mosmetro.backend.model.dto.employee.EmployeeRankDTO
import ru.mosmetro.backend.model.dto.employee.EmployeeShiftDTO
import ru.mosmetro.backend.model.dto.employee.NewEmployeeDTO
import ru.mosmetro.backend.model.dto.employee.UpdateEmployeeDTO
import ru.mosmetro.backend.repository.EmployeeEntityRepository
import ru.mosmetro.backend.repository.EmployeeRankEntityRepository
import ru.mosmetro.backend.repository.EmployeeShiftEntityRepository
import ru.mosmetro.backend.repository.MetroUserEntityRepository

@Service
class EmployeeService(
    private val employeeEntityRepository: EmployeeEntityRepository,
    private val employeeRankEntityRepository: EmployeeRankEntityRepository,
    private val employeeShiftEntityRepository: EmployeeShiftEntityRepository,
    private val employeeMapper: EmployeeMapper,
    private val employeeRankMapper: EmployeeRankMapper,
    private val employeeShiftMapper: EmployeeShiftMapper,
    private val metroUserEntityRepository: MetroUserEntityRepository
) {
    /**
     *
     * Метод получает всех рабочих
     *
     * @return список сущностей EmployeeDTO в которых предоставлена информация о рабочих
     *
     * */
    fun getEmployees(): ListWithTotal<EmployeeDTO> {
        val employeeDTOList = employeeEntityRepository.findAll()
            .map { employeeMapper.entityToDomain(it) }
            .map { employeeMapper.domainToDto(it) }
        return ListWithTotal(employeeDTOList.size, employeeDTOList)
    }

    /**
     *
     * Метод получает всех должностей рабочих
     *
     * @return список сущностей EmployeeRankDTO в которых предоставлена информация о рабочих
     *
     * */
    fun getAllEmployeeRanks(): ListWithTotal<EmployeeRankDTO> {
        val employeeDTOList = employeeRankEntityRepository.findAll()
            .map { employeeRankMapper.entityToDomain(it) }
            .map { employeeRankMapper.domainToDto(it) }
        return ListWithTotal(employeeDTOList.size, employeeDTOList)
    }

    /**
     *
     * Метод получает всех должностей рабочих
     *
     * @return список сущностей EmployeeShiftDTO в которых предоставлена информация о рабочих
     *
     * */
    fun getAllEmployeeShifts(): ListWithTotal<EmployeeShiftDTO> {
        val employeeDTOList = employeeShiftEntityRepository.findAll()
            .map { employeeShiftMapper.entityToDomain(it) }
            .map { employeeShiftMapper.domainToDto(it) }
        return ListWithTotal(employeeDTOList.size, employeeDTOList)
    }

    /**
     *
     * Метод получает информацию о рабочем по его идентификатору
     *
     * @param id - идентификатор рабочего
     * @return сущность EmployeeDTO в которой предоставлена информация о рабочем
     *
     * */
    fun getEmployeeById(id: Long): EmployeeDTO {
        return employeeEntityRepository.findById(id)
            .orElseThrow { EntityNotFoundException(id.toString()) }
            .let { employeeMapper.entityToDomain(it) }
            .let { employeeMapper.domainToDto(it) }
    }

    /**
     *
     * Метод получает информацию о рабочем по его идентификатору
     *
     * @param newEmployeeDTO - информация о сотруднике
     * @return сущность EmployeeDTO в которой предоставлена информация о рабочем
     *
     * */
    fun createEmployee(newEmployeeDTO: NewEmployeeDTO): EmployeeDTO {
        val employeeRank = employeeRankEntityRepository.findById(newEmployeeDTO.rankCode)
            .orElseThrow { EntityNotFoundException(newEmployeeDTO.rankCode) }
            .let { employeeRankMapper.entityToDomain(it) }
            .let { employeeRankMapper.domainToDto(it) }
        val userEntity = metroUserEntityRepository.findByLogin(newEmployeeDTO.workPhone)
            .orElseThrow { EntityNotFoundException(newEmployeeDTO.workPhone) }
        return newEmployeeDTO
            .let { employeeMapper.dtoToDomain(it, employeeRank) }
            .let { employeeMapper.domainToEntity(it, userEntity) }
            .let { employeeEntityRepository.save(it) }
            .let { employeeMapper.entityToDomain(it) }
            .let { employeeMapper.domainToDto(it) }
    }

    /**
     *
     * Метод получает информацию о рабочем по его идентификатору
     *
     * @param id - идентификатор пользователя
     * @param updateEmployeeDTO - измененная информация о сотруднике
     * @return сущность EmployeeDTO в которой предоставлена информация о рабочем
     *
     * */
    fun updateEmployee(id: Long, updateEmployeeDTO: UpdateEmployeeDTO): EmployeeDTO {
        val employeeEntity = employeeEntityRepository.findById(id)
            .orElseThrow { EntityNotFoundException(id.toString()) }

        val employeeRankDTO = employeeEntity.rank
            .let { employeeRankMapper.entityToDomain(it) }
            .let { employeeRankMapper.domainToDto(it) }

        return updateEmployeeDTO
            .let { employeeMapper.dtoToDomain(updateEmployeeDTO, id, employeeRankDTO) }
            .let { employeeMapper.domainToEntity(it, employeeEntity.user) }
            .let { employeeEntityRepository.save(it) }
            .let { employeeMapper.entityToDomain(it) }
            .let { employeeMapper.domainToDto(it) }
    }

    /**
     *
     * Метод удаляет пользователя из системы по его идентификатору
     *
     * @param id - идентификатор пользователя
     *
     * */
    fun deleteEmployee(id: Long) {
        employeeEntityRepository.deleteById(id)
    }

    /**
     *
     * Метод обновляет пароль на временный для сотрудника
     *
     *
     * */
    fun resetEmployeePassword() {
        TODO()
    }
}