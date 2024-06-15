package ru.mosmetro.backend.service

import jakarta.persistence.EntityNotFoundException
import kotlinx.coroutines.coroutineScope
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
    suspend fun getOrders(): ListWithTotal<PassengerOrderDTO> = coroutineScope {
        val passengerOrderDTOS = jpaContext { passengerOrderEntityRepository.findAll() }
            .map { orderMapper.entityToDomain(it) }
            .map { orderMapper.domainToDto(it) }
        return@coroutineScope ListWithTotal(passengerOrderDTOS.size, passengerOrderDTOS)
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
    suspend fun getOrderById(id: Long): EntityForEdit<PassengerOrderDTO> = coroutineScope {
        val order: PassengerOrderDTO = jpaContext { passengerOrderEntityRepository.findById(id) }
            .orElseThrow { NoSuchOrderException(id) }
            .let { orderMapper.entityToDomain(it) }
            .let { orderMapper.domainToDto(it) }

        return@coroutineScope EntityForEdit(
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
    suspend fun createOrder(newPassengerOrderDTO: NewPassengerOrderDTO): PassengerOrderDTO = coroutineScope {
        val orderStatusEntity = orderStatusEntityRepository.findByCode(newPassengerOrderDTO.orderStatus.code.name)
            .orElseThrow { EntityNotFoundException(newPassengerOrderDTO.orderStatus.code.name) }
        val passengerEntity = passengerService.getPassengerById(newPassengerOrderDTO.passenger.id!!).data
            .let { passengerMapper.dtoToDomain(it) }
            .let { passengerMapper.domainToEntity(it) }
        val startStation = metroService.getMetroStationById(newPassengerOrderDTO.startMetroStation.id!!)
            .let { metroStationMapper.dtoToDomain(it) }
            .let { metroStationMapper.domainToEntity(it) }
        val finishStation = metroService.getMetroStationById(newPassengerOrderDTO.finishMetroStation.id!!)
            .let { metroStationMapper.dtoToDomain(it) }
            .let { metroStationMapper.domainToEntity(it) }

        return@coroutineScope newPassengerOrderDTO
            .let { orderMapper.dtoToDomain(it) }
            .let { orderMapper.domainToEntity(it, orderStatusEntity, passengerEntity, startStation, finishStation) }
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
    suspend fun updateOrder(id: Long, updatedPassengerOrderDTO: UpdatedPassengerOrderDTO): PassengerOrderDTO = coroutineScope {
        val passengerOrderEntity = jpaContext { passengerOrderEntityRepository.findById(id) }
            .orElseThrow { NoSuchOrderException(id) }

        val orderStatusEntity = orderStatusEntityRepository.findByCode(updatedPassengerOrderDTO.orderStatus.code.name)
            .orElseThrow { EntityNotFoundException(updatedPassengerOrderDTO.orderStatus.code.name) }

        val passengerEntity = passengerService.getPassengerById(updatedPassengerOrderDTO.passenger.id!!).data
            .let { passengerMapper.dtoToDomain(it) }
            .let { passengerMapper.domainToEntity(it) }
        val startStation = metroService.getMetroStationById(updatedPassengerOrderDTO.startStation.id!!)
            .let { metroStationMapper.dtoToDomain(it) }
            .let { metroStationMapper.domainToEntity(it) }
        val finishStation = metroService.getMetroStationById(updatedPassengerOrderDTO.finishStation.id!!)
            .let { metroStationMapper.dtoToDomain(it) }
            .let { metroStationMapper.domainToEntity(it) }

        return@coroutineScope updatedPassengerOrderDTO
            .let { orderMapper.dtoToDomain(it, passengerOrderEntity.createdAt, id) }
            .let { orderMapper.domainToEntity(it, orderStatusEntity, passengerEntity, startStation, finishStation) }
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
    suspend fun deleteOrder(id: Long) = coroutineScope {
        jpaContext { passengerOrderEntityRepository.deleteById(id) }
            .also { subscriptionService.notifyOrderUpdate() }
    }

    suspend fun getCurrentUserOrders(): ListWithTotal<PassengerOrderDTO> = coroutineScope {
        return@coroutineScope ListWithTotal(0, emptyList()) //TODO: реализовать получение заявок пользователя
    }
}
