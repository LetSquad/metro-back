package ru.mosmetro.backend.service

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.mosmetro.backend.exception.EntityNotFoundException
import ru.mosmetro.backend.mapper.EmployeeMapper
import ru.mosmetro.backend.mapper.EmployeeRankMapper
import ru.mosmetro.backend.mapper.EmployeeShiftMapper
import ru.mosmetro.backend.model.dto.EntityForEdit
import ru.mosmetro.backend.model.dto.ListWithTotal
import ru.mosmetro.backend.model.dto.employee.EmployeeDTO
import ru.mosmetro.backend.model.dto.employee.EmployeeRankDTO
import ru.mosmetro.backend.model.dto.employee.EmployeeShiftDTO
import ru.mosmetro.backend.model.dto.employee.NewEmployeeDTO
import ru.mosmetro.backend.model.dto.employee.UpdateEmployeeDTO
import ru.mosmetro.backend.model.entity.MetroUserEntity
import ru.mosmetro.backend.repository.EmployeeEntityRepository
import ru.mosmetro.backend.repository.EmployeeRankEntityRepository
import ru.mosmetro.backend.repository.EmployeeShiftEntityRepository
import ru.mosmetro.backend.repository.MetroUserEntityRepository
import ru.mosmetro.backend.service.jwt.RefreshTokenService
import ru.mosmetro.backend.util.jpaContext

@Service
class EmployeeService(
    private val employeeMapper: EmployeeMapper,
    private val employeeRankMapper: EmployeeRankMapper,
    private val employeeShiftMapper: EmployeeShiftMapper,
    private val lockService: EntityLockService,
    private val refreshTokenService: RefreshTokenService,
    private val employeeEntityRepository: EmployeeEntityRepository,
    private val employeeRankEntityRepository: EmployeeRankEntityRepository,
    private val employeeShiftEntityRepository: EmployeeShiftEntityRepository,
    private val metroUserEntityRepository: MetroUserEntityRepository,
    private val passwordEncoder: PasswordEncoder
) {
    /**
     *
     * Метод получает всех рабочих
     *
     * @return список сущностей EmployeeDTO в которых предоставлена информация о рабочих
     *
     * */
    suspend fun getEmployees(): ListWithTotal<EmployeeDTO> = coroutineScope {
        val employeeDTOList = jpaContext { employeeEntityRepository.findAll() }
            .map { employeeMapper.entityToDomain(it) }
            .map { employeeMapper.domainToDto(it) }
        return@coroutineScope ListWithTotal(employeeDTOList.size, employeeDTOList)
    }

    /**
     *
     * Метод получает текущего рабочего
     *
     * @return EmployeeDTO в которых предоставлена информация о текущем рабочем
     *
     * */
    suspend fun getCurrentEmployee(): EmployeeDTO = coroutineScope {
        val login: String = ReactiveSecurityContextHolder.getContext()
            .awaitSingle()
            .authentication
            .principal as String

        return@coroutineScope jpaContext { employeeEntityRepository.findByUserLogin(login) }
            .let { employeeMapper.entityToDomain(it) }
            .let { employeeMapper.domainToDto(it) }
    }

    /**
     *
     * Метод получает всех должностей рабочих
     *
     * @return список сущностей EmployeeRankDTO в которых предоставлена информация о рабочих
     *
     * */
    suspend fun getAllEmployeeRanks(): ListWithTotal<EmployeeRankDTO> = coroutineScope {
        val employeeDTOList = jpaContext { employeeRankEntityRepository.findAll() }
            .map { employeeRankMapper.entityToDomain(it) }
            .map { employeeRankMapper.domainToDto(it) }
        return@coroutineScope ListWithTotal(employeeDTOList.size, employeeDTOList)
    }

    /**
     *
     * Метод получает всех должностей рабочих
     *
     * @return список сущностей EmployeeShiftDTO в которых предоставлена информация о рабочих
     *
     * */
    suspend fun getAllEmployeeShifts(): ListWithTotal<EmployeeShiftDTO> = coroutineScope {
        val employeeDTOList = jpaContext { employeeShiftEntityRepository.findAll() }
            .map { employeeShiftMapper.entityToDomain(it) }
            .map { employeeShiftMapper.domainToDto(it) }
        return@coroutineScope ListWithTotal(employeeDTOList.size, employeeDTOList)
    }

    /**
     *
     * Метод получает информацию о рабочем по его идентификатору
     *
     * @param id - идентификатор рабочего
     * @return сущность EmployeeDTO в которой предоставлена информация о рабочем
     *
     * */
    suspend fun getEmployeeById(id: Long): EntityForEdit<EmployeeDTO> = coroutineScope {
        val employee: EmployeeDTO = jpaContext { employeeEntityRepository.findById(id) }
            .orElseThrow { EntityNotFoundException(id.toString()) }
            .let { employeeMapper.entityToDomain(it) }
            .let { employeeMapper.domainToDto(it) }

        return@coroutineScope EntityForEdit(
            isLockedForEdit = lockService.checkEmployeeLock(id),
            data = employee
        )
    }

    /**
     *
     * Метод получает информацию о рабочем по его идентификатору
     *
     * @param newEmployeeDTO - информация о сотруднике
     * @return сущность EmployeeDTO в которой предоставлена информация о рабочем
     *
     * */
    @Transactional
    suspend fun createEmployee(newEmployeeDTO: NewEmployeeDTO): EmployeeDTO = coroutineScope {
        val employeeRank = jpaContext { employeeRankEntityRepository.findById(newEmployeeDTO.rankCode) }
            .orElseThrow { EntityNotFoundException(newEmployeeDTO.rankCode) }
            .let { employeeRankMapper.entityToDomain(it) }
            .let { employeeRankMapper.domainToDto(it) }

        val userEntity = metroUserEntityRepository.save(
            MetroUserEntity(
                null,
                newEmployeeDTO.workPhone,
                passwordEncoder.encode("temp"),
                true
            )
        )

        refreshTokenService.initUser(newEmployeeDTO.workPhone)

        return@coroutineScope newEmployeeDTO
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
    suspend fun updateEmployee(id: Long, updateEmployeeDTO: UpdateEmployeeDTO): EmployeeDTO = coroutineScope {
        val employeeEntity = jpaContext { employeeEntityRepository.findById(id) }
            .orElseThrow { EntityNotFoundException(id.toString()) }

        val employeeRankDTO = employeeEntity.rank
            .let { employeeRankMapper.entityToDomain(it) }
            .let { employeeRankMapper.domainToDto(it) }

        return@coroutineScope updateEmployeeDTO
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
    suspend fun deleteEmployee(id: Long) = coroutineScope {
        jpaContext { employeeEntityRepository.deleteById(id) }
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
