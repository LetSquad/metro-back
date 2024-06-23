package ru.mosmetro.backend.service

import jakarta.persistence.EntityNotFoundException
import org.springframework.stereotype.Service
import ru.mosmetro.backend.exception.NoSuchOrderException
import ru.mosmetro.backend.mapper.EmployeeMapper
import ru.mosmetro.backend.mapper.MetroStationMapper
import ru.mosmetro.backend.mapper.OrderMapper
import ru.mosmetro.backend.mapper.PassengerMapper
import ru.mosmetro.backend.model.domain.PassengerOrder
import ru.mosmetro.backend.model.dto.EntityForEdit
import ru.mosmetro.backend.model.dto.ListWithTotal
import ru.mosmetro.backend.model.dto.metro.MetroStationTransferDTO
import ru.mosmetro.backend.model.dto.order.NewPassengerOrderDTO
import ru.mosmetro.backend.model.dto.order.OrderFilterRequestDTO
import ru.mosmetro.backend.model.dto.order.PassengerOrderDTO
import ru.mosmetro.backend.model.dto.order.UpdateOrderStatusDTO
import ru.mosmetro.backend.model.dto.order.UpdatedPassengerOrderDTO
import ru.mosmetro.backend.model.entity.EmployeeEntity
import ru.mosmetro.backend.model.entity.EmployeeShiftOrderEntity
import ru.mosmetro.backend.model.enums.OrderStatusType
import ru.mosmetro.backend.model.enums.TimeListActionType
import ru.mosmetro.backend.repository.EmployeeEntityRepository
import ru.mosmetro.backend.repository.EmployeeShiftEntityRepository
import ru.mosmetro.backend.repository.EmployeeShiftOrderEntityRepository
import ru.mosmetro.backend.repository.OrderStatusEntityRepository
import ru.mosmetro.backend.repository.PassengerOrderEntityRepository
import ru.mosmetro.backend.repository.PassengerPhoneEntityRepository
import ru.mosmetro.backend.util.MetroTimeUtil
import ru.mosmetro.backend.util.jpaContext
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit

@Service
class OrderService(
    private val orderMapper: OrderMapper,
    private val lockService: EntityLockService,
    private val subscriptionService: EntitySubscriptionService,
    private val passengerOrderEntityRepository: PassengerOrderEntityRepository,
    private val passengerPhoneEntityRepository: PassengerPhoneEntityRepository,
    private val employeeEntityRepository: EmployeeEntityRepository,
    private val orderStatusEntityRepository: OrderStatusEntityRepository,
    private val employeeShiftOrderEntityRepository: EmployeeShiftOrderEntityRepository,
    private val employeeShiftEntityRepository: EmployeeShiftEntityRepository,
    private val passengerService: PassengerService,
    private val passengerMapper: PassengerMapper,
    private val employeeMapper: EmployeeMapper,
    private val metroService: MetroService,
    private val metroStationMapper: MetroStationMapper,
) {

    /**
     *
     * Метод возвращает список заявок в системе
     *
     * */
    suspend fun getOrders(r: OrderFilterRequestDTO): ListWithTotal<PassengerOrderDTO> {
        val dateFrom: Instant = r.dateFrom
            .atStartOfDay()
            .toInstant(ZoneOffset.UTC)
        val dateTo: Instant = r.dateTo
            .plusDays(1)
            .atStartOfDay()
            .toInstant(ZoneOffset.UTC)

        val passengerOrders = jpaContext { passengerOrderEntityRepository.findAllByOrderTimeBetween(dateFrom, dateTo) }
            .map {
                val passengerPhones = passengerService.passengerPhoneCache.getOrElse(it.passenger.id) { emptyList() }
                    .toSet()
                orderMapper.entityToDomain(it, passengerPhones)
            }
            .filter { o ->
                if (r.passengerFirstName != null && o.passenger.firstName != r.passengerFirstName) {
                    return@filter false
                }
                if (r.passengerLastName != null && o.passenger.lastName != r.passengerLastName) {
                    return@filter false
                }
                if (r.passengerPhone != null &&
                    passengerService.getPassengerPhones(o.passenger.id!!).contains(r.passengerPhone)
                ) {
                    return@filter false
                }

                if (r.employeeFirstName != null || r.employeeLastName != null || r.employeePhone != null) {
                    val employees: List<EmployeeEntity> = employeeShiftOrderEntityRepository.findAllByOrderId(o.id!!)
                        .map { it.employeeShift.employee }

                    return@filter employees.any { e ->
                        if (r.employeeFirstName != null && e.firstName != r.employeeFirstName) {
                            return@any false
                        }
                        if (r.employeeLastName != null && e.lastName != r.employeeLastName) {
                            return@any false
                        }
                        if (r.employeePhone != null &&
                            (e.workPhone != r.employeePhone || e.personalPhone != r.employeePhone)
                        ) {
                            return@any false
                        }

                        return@any true
                    }
                }

                if (r.orderCategories != null &&
                    (!r.orderCategories.contains(o.passengerCategory.name))
                ) {
                    return@filter false
                }

                if (r.orderStatuses != null && !r.orderStatuses.contains(o.orderStatus.code.name)) {
                    return@filter false
                }

                return@filter true
            }
            .sortedBy { it.orderTime }
            .map { orderMapper.domainToDto(it) }

        return ListWithTotal(passengerOrders.size, passengerOrders)
    }

    fun getOrdersBetweenOrderDate(
        dateStart: LocalDateTime,
        dateFinish: LocalDateTime,
    ): List<PassengerOrder> {
        return passengerOrderEntityRepository.findAllByOrderTimeBetween(
            dateStart.toInstant(ZoneOffset.UTC),
            dateFinish.toInstant(ZoneOffset.UTC)
        )
            .map {
                val passengerPhones = passengerPhoneEntityRepository.findByPassengerId(it.passenger.id!!)
                    .toSet()
                orderMapper.entityToDomain(it, passengerPhones)
            }
    }

    /**
     *
     * Метод возвращает заявку по её идентификатору
     *
     * @param id - идентификатор заявки
     * @return сущность PassengerOrderDTO в которой предоставлена информация о заявке
     *
     * */
    suspend fun getOrderById(id: Long): EntityForEdit<PassengerOrderDTO> {
        val order: PassengerOrderDTO = jpaContext { passengerOrderEntityRepository.findById(id) }
            .orElseThrow { NoSuchOrderException(id) }
            .let {
                val passengerPhones = passengerPhoneEntityRepository.findByPassengerId(it.passenger.id!!)
                    .toSet()
                orderMapper.entityToDomain(it, passengerPhones)
            }
            .let { orderMapper.domainToDto(it) }

        return EntityForEdit(
            isLockedForEdit = lockService.checkOrderLock(id),
            data = order
        )
    }

    /**
     *
     * Метод создает новую заявку
     *
     * @param newPassengerOrderDTO - модель данных новой заявки
     * @return сущность PassengerOrderDTO в которой предоставлена информация о заявке
     *
     * */
    suspend fun createOrder(newPassengerOrderDTO: NewPassengerOrderDTO): PassengerOrderDTO {
        val orderStatusEntity = jpaContext { orderStatusEntityRepository.findByCode(OrderStatusType.REVIEW.name) }
            .orElseThrow { EntityNotFoundException(OrderStatusType.REVIEW.name) }
        val passenger = passengerService.getPassengerById(newPassengerOrderDTO.passenger).data
            .let { passengerMapper.dtoToDomain(it) }
        val startStation = metroService.getMetroStationById(newPassengerOrderDTO.startStation)
            .let { metroStationMapper.dtoToDomain(it) }
        val finishStation = metroService.getMetroStationById(newPassengerOrderDTO.finishStation)
            .let { metroStationMapper.dtoToDomain(it) }
        val transfers = newPassengerOrderDTO.transfers.map { MetroStationTransferDTO(
            metroService.getMetroStationById(it.startStation),
            metroService.getMetroStationById(it.finishStation),
            it.duration,
            it.isCrosswalking
        ) }

        return newPassengerOrderDTO
            .let { orderMapper.dtoToDomain(it, startStation, finishStation, passenger, transfers) }
            .let {
                orderMapper.domainToEntity(
                    it, orderStatusEntity, passengerMapper.domainToEntity(passenger),
                    metroStationMapper.domainToEntity(startStation), metroStationMapper.domainToEntity(finishStation)
                )
            }
            .let { jpaContext { passengerOrderEntityRepository.save(it) } }
            .also { subscriptionService.notifyOrderUpdate() }
            .let {
                val passengerPhones = passengerPhoneEntityRepository.findByPassengerId(it.passenger.id!!)
                    .toSet()
                orderMapper.entityToDomain(it, passengerPhones)
            }
            .let { orderMapper.domainToDto(it) }
    }

    /**
     *
     * Метод обновляет существующую заявку
     *
     * @param updatedPassengerOrderDTO - модель данных обновленной заявки
     * @param orderId - идентификатор заявки
     * @return сущность PassengerOrderDTO в которой предоставлена информация о заявке
     *
     * */
    suspend fun updateOrder(orderId: Long, updatedPassengerOrderDTO: UpdatedPassengerOrderDTO): PassengerOrderDTO {
        val passengerOrderEntity = jpaContext { passengerOrderEntityRepository.findById(orderId) }
            .orElseThrow { NoSuchOrderException(orderId) }

        val orderStatusEntity = jpaContext { orderStatusEntityRepository.findByCode(updatedPassengerOrderDTO.orderApplication.name) }
            .orElseThrow { EntityNotFoundException(OrderStatusType.REVIEW.name) }

        val passenger = passengerService.getPassengerById(updatedPassengerOrderDTO.passenger).data
            .let { passengerMapper.dtoToDomain(it) }
        val startStation = metroService.getMetroStationById(updatedPassengerOrderDTO.startStation)
            .let { metroStationMapper.dtoToDomain(it) }
        val finishStation = metroService.getMetroStationById(updatedPassengerOrderDTO.finishStation)
            .let { metroStationMapper.dtoToDomain(it) }

        val transfers = updatedPassengerOrderDTO.transfers.map { MetroStationTransferDTO(
            metroService.getMetroStationById(it.startStation),
            metroService.getMetroStationById(it.finishStation),
            it.duration,
            it.isCrosswalking
        ) }

        val orderEntity = updatedPassengerOrderDTO
            .let {
                orderMapper.dtoToDomain(
                    it,
                    passengerOrderEntity.createdAt,
                    orderId,
                    startStation,
                    finishStation,
                    passenger,
                    transfers
                )
            }
            .let {
                orderMapper.domainToEntity(
                    it, orderStatusEntity, passengerMapper.domainToEntity(passenger),
                    metroStationMapper.domainToEntity(startStation), metroStationMapper.domainToEntity(finishStation)
                )
            }
            .let { jpaContext { passengerOrderEntityRepository.save(it) } }
            .also { subscriptionService.notifyOrderUpdate() }

        val employeeList =
            if (updatedPassengerOrderDTO.employees != null) {
                // удаляем предыдущие связи
                jpaContext {employeeShiftOrderEntityRepository.findAllByOrderId(orderId) }
                    .forEach { employeeShiftOrderEntityRepository.delete(it) }

                // TODO добавить проверку на возможность сотруднику брать эту задачу
                updatedPassengerOrderDTO.employees
                    .map { employeeEntityRepository.findById(it).orElseThrow { NoSuchOrderException(orderId) } }
                    .map { employeeMapper.entityToDomain(it) }
                    .also { employeeList ->

                        employeeList.forEach { employee ->
                            val shiftDay =
                                LocalDateTime.ofInstant(updatedPassengerOrderDTO.orderTime, MetroTimeUtil.TIME_ZONE_UTC).truncatedTo(ChronoUnit.DAYS)
                            val employeeShift =
                                employeeShiftEntityRepository.findByShiftDateBetweenAndEmployeeId(
                                    shiftDay.toInstant(ZoneOffset.UTC),
                                    shiftDay.plusDays(1).toInstant(ZoneOffset.UTC),
                                    employee.id!!
                                ).orElseThrow { NoSuchOrderException(orderId) }

                            val employeeShiftTimeOrder = EmployeeShiftOrderEntity(
                                id = null,
                                employeeShift = employeeShift,
                                order = orderEntity,
                                isAttached = true,
                                actionType = TimeListActionType.ORDER.name,
                                // TODO add period before order
                                timeStart = orderEntity.orderTime.minusSeconds(15 * 60),
                                timeFinish = orderEntity.orderTime.plusSeconds(orderEntity.duration.toSeconds()),
                                createdAt = Instant.now()
                            )

                            employeeShiftOrderEntityRepository.save(employeeShiftTimeOrder)
                        }
                    }
            }
            else {
                emptyList()
            }

        return orderEntity
            .let {
                val passengerPhones = passengerPhoneEntityRepository.findByPassengerId(it.passenger.id!!)
                    .toSet()
                orderMapper.entityToDomain(it, passengerPhones)
            }
            .let { orderMapper.domainToDto(it, employeeList) }
    }

    /**
     *
     * Метод удаляет заявку
     *
     * @param id - идентификатор заявки
     *
     * */
    suspend fun deleteOrder(id: Long) {
        jpaContext { passengerOrderEntityRepository.deleteById(id) }
            .also { subscriptionService.notifyOrderUpdate() }
    }

    suspend fun getCurrentUserOrders(): ListWithTotal<PassengerOrderDTO> {
        return ListWithTotal(0, emptyList()) //TODO: реализовать получение заявок пользователя
    }

    /**
     *
     * Метод обновляет статус заявки
     *
     * @param id - идентификатор заявки
     * @param dto - новый статус заявки
     *
     * */
    suspend fun updateOrderStatus(
        id: Long,
        dto: UpdateOrderStatusDTO
    ): PassengerOrderDTO {
        val orderEntity = jpaContext { passengerOrderEntityRepository.findById(id) }
            .orElseThrow { NoSuchOrderException(id) }
        val orderStatusEntity = jpaContext { orderStatusEntityRepository.findByCode(dto.status) }
            .orElseThrow { EntityNotFoundException(dto.status) }

        return dto
            .let {
                orderEntity.orderStatusCode = orderStatusEntity
                return@let orderEntity
            }
            .let { jpaContext { passengerOrderEntityRepository.save(it) } }
            .also { subscriptionService.notifyOrderUpdate() }
            .let {
                val passengerPhones = passengerPhoneEntityRepository.findByPassengerId(it.passenger.id!!)
                    .toSet()
                orderMapper.entityToDomain(it, passengerPhones)
            }
            .let { orderMapper.domainToDto(it) }
    }
}
