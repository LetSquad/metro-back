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
import ru.mosmetro.backend.model.dto.order.PassengerOrderDTO
import ru.mosmetro.backend.model.dto.order.UpdatedPassengerOrderDTO
import ru.mosmetro.backend.model.enums.OrderStatusType
import ru.mosmetro.backend.repository.OrderStatusEntityRepository
import ru.mosmetro.backend.repository.PassengerOrderEntityRepository
import ru.mosmetro.backend.util.jpaContext
import java.time.LocalDateTime
import java.time.ZoneOffset

@Service
class OrderService(
    private val orderMapper: OrderMapper,
    private val lockService: EntityLockService,
    private val subscriptionService: EntitySubscriptionService,
    private val passengerOrderEntityRepository: PassengerOrderEntityRepository,
    private val orderStatusEntityRepository: OrderStatusEntityRepository,
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
    suspend fun getOrders(): ListWithTotal<PassengerOrderDTO> {
        val passengerOrderDTOS = jpaContext { passengerOrderEntityRepository.findAll() }
            .map { orderMapper.entityToDomain(it) }
            .map { orderMapper.domainToDto(it) }
        return ListWithTotal(passengerOrderDTOS.size, passengerOrderDTOS)
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
            .let { passengerOrderEntityRepository.save(it) }
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
}
