package ru.mosmetro.backend.service

import kotlinx.coroutines.coroutineScope
import org.springframework.stereotype.Service
import ru.mosmetro.backend.exception.NoSuchOrderException
import ru.mosmetro.backend.mapper.OrderMapper
import ru.mosmetro.backend.model.dto.EntityForEdit
import ru.mosmetro.backend.model.dto.ListWithTotal
import ru.mosmetro.backend.model.dto.order.NewPassengerOrderDTO
import ru.mosmetro.backend.model.dto.order.PassengerOrderDTO
import ru.mosmetro.backend.model.dto.order.UpdatedPassengerOrderDTO
import ru.mosmetro.backend.repository.PassengerOrderEntityRepository
import ru.mosmetro.backend.util.jpaContext

@Service
class OrderService(
    private val orderMapper: OrderMapper,
    private val lockService: EntityLockService,
    private val subscriptionService: EntitySubscriptionService,
    private val passengerOrderEntityRepository: PassengerOrderEntityRepository
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
        return@coroutineScope newPassengerOrderDTO
            .let { orderMapper.dtoToDomain(it) }
            .let { orderMapper.domainToEntity(it) }
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

        return@coroutineScope updatedPassengerOrderDTO
            .let { orderMapper.dtoToDomain(it, passengerOrderEntity.createdAt) }
            .let { orderMapper.domainToEntity(it) }
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
    suspend fun deleteOrder(id: Long) = coroutineScope {
        jpaContext { passengerOrderEntityRepository.deleteById(id) }
            .also { subscriptionService.notifyOrderUpdate() }
    }

    suspend fun getCurrentUserOrders(): ListWithTotal<PassengerOrderDTO> = coroutineScope {
        TODO()
    }
}
