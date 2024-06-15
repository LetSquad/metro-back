package ru.mosmetro.backend.service

import jakarta.persistence.EntityNotFoundException
import org.springframework.stereotype.Service
import ru.mosmetro.backend.exception.NoSuchOrderException
import ru.mosmetro.backend.mapper.MetroStationMapper
import ru.mosmetro.backend.mapper.OrderMapper
import ru.mosmetro.backend.mapper.PassengerMapper
import ru.mosmetro.backend.model.domain.PassengerOrder
import ru.mosmetro.backend.model.dto.EntityForEdit
import ru.mosmetro.backend.model.dto.ListWithTotal
import ru.mosmetro.backend.model.dto.order.NewPassengerOrderDTO
import ru.mosmetro.backend.model.dto.order.OrderFilterRequestDTO
import ru.mosmetro.backend.model.dto.order.PassengerOrderDTO
import ru.mosmetro.backend.model.dto.order.UpdateOrderStatusDTO
import ru.mosmetro.backend.model.dto.order.UpdatedPassengerOrderDTO
import ru.mosmetro.backend.model.entity.EmployeeEntity
import ru.mosmetro.backend.model.enums.OrderStatusType
import ru.mosmetro.backend.repository.EmployeeShiftOrderEntityRepository
import ru.mosmetro.backend.repository.OrderStatusEntityRepository
import ru.mosmetro.backend.repository.PassengerOrderEntityRepository
import ru.mosmetro.backend.util.jpaContext
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset

@Service
class OrderService(
    private val orderMapper: OrderMapper,
    private val lockService: EntityLockService,
    private val subscriptionService: EntitySubscriptionService,
    private val passengerOrderEntityRepository: PassengerOrderEntityRepository,
    private val orderStatusEntityRepository: OrderStatusEntityRepository,
    private val employeeShiftOrderEntityRepository: EmployeeShiftOrderEntityRepository,
    private val passengerService: PassengerService,
    private val passengerMapper: PassengerMapper,
    private val metroService: MetroService,
    private val metroStationMapper: MetroStationMapper
) {

    /**
     *
     * Метод возвращает список заявок в системе
     *
     * */
    suspend fun getOrders(r: OrderFilterRequestDTO): ListWithTotal<PassengerOrderDTO> {
        val dateFrom: Instant = r.dateFrom.toLocalDate()
            .atStartOfDay()
            .toInstant(ZoneOffset.UTC)
        val dateTo: Instant = r.dateTo.toLocalDate()
            .plusDays(1)
            .atStartOfDay()
            .toInstant(ZoneOffset.UTC)

        val passengerOrders = jpaContext { passengerOrderEntityRepository.findAllByOrderTimeBetween(dateFrom, dateTo) }
            .map { orderMapper.entityToDomain(it) }
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
            .map { orderMapper.domainToDto(it) }

        return ListWithTotal(passengerOrders.size, passengerOrders)
    }

    fun getOrdersBetweenStartDate(
        dateStart: LocalDateTime,
        dateFinish: LocalDateTime,
    ): List<PassengerOrder> {
        return  passengerOrderEntityRepository.findAllByStartTimeBetween(
            dateStart.toInstant(ZoneOffset.UTC),
            dateFinish.toInstant(ZoneOffset.UTC))
            .map { orderMapper.entityToDomain(it) }
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
            .let { orderMapper.entityToDomain(it) }
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

        return newPassengerOrderDTO
            .let { orderMapper.dtoToDomain(it, startStation, finishStation, passenger) }
            .let { orderMapper.domainToEntity(it, orderStatusEntity, passengerMapper.domainToEntity(passenger),
                metroStationMapper.domainToEntity(startStation), metroStationMapper.domainToEntity(finishStation)) }
            .let { jpaContext { passengerOrderEntityRepository.save(it) } }
            .also { subscriptionService.notifyOrderUpdate() }
            .let { orderMapper.entityToDomain(it) }
            .let { orderMapper.domainToDto(it) }
    }

    /**
     *
     * Метод обновляет существующую заявку
     *
     * @param updatedPassengerOrderDTO - модель данных обновленной заявки
     * @param id - идентификатор заявки
     * @return сущность PassengerOrderDTO в которой предоставлена информация о заявке
     *
     * */
    suspend fun updateOrder(id: Long, updatedPassengerOrderDTO: UpdatedPassengerOrderDTO): PassengerOrderDTO {
        val passengerOrderEntity = jpaContext { passengerOrderEntityRepository.findById(id) }
            .orElseThrow { NoSuchOrderException(id) }

        //TODO Поправить тут логику на нужную
        val orderStatusEntity = jpaContext { orderStatusEntityRepository.findByCode(OrderStatusType.ACCEPTED.name) }
            .orElseThrow { EntityNotFoundException(OrderStatusType.REVIEW.name) }

        val passenger = passengerService.getPassengerById(updatedPassengerOrderDTO.passenger).data
            .let { passengerMapper.dtoToDomain(it) }
        val startStation = metroService.getMetroStationById(updatedPassengerOrderDTO.startStation)
            .let { metroStationMapper.dtoToDomain(it) }
        val finishStation = metroService.getMetroStationById(updatedPassengerOrderDTO.finishStation)
            .let { metroStationMapper.dtoToDomain(it) }

        return updatedPassengerOrderDTO
            .let { orderMapper.dtoToDomain(it, passengerOrderEntity.createdAt, id, startStation, finishStation, passenger) }
            .let { orderMapper.domainToEntity(it, orderStatusEntity, passengerMapper.domainToEntity(passenger),
                metroStationMapper.domainToEntity(startStation), metroStationMapper.domainToEntity(finishStation)) }
            .let { jpaContext { passengerOrderEntityRepository.save(it) } }
            .also { subscriptionService.notifyOrderUpdate() }
            .let { orderMapper.entityToDomain(it) }
            .let { orderMapper.domainToDto(it) }
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
            .let { orderMapper.entityToDomain(it) }
            .let { orderMapper.domainToDto(it) }
    }
}
