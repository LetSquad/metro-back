package ru.mosmetro.backend.service

import org.springframework.stereotype.Service
import ru.mosmetro.backend.exception.NoSuchOrderException
import ru.mosmetro.backend.mapper.OrderMapper
import ru.mosmetro.backend.model.dto.ListWithTotal
import ru.mosmetro.backend.model.dto.order.NewPassengerOrderDTO
import ru.mosmetro.backend.model.dto.order.PassengerOrderDTO
import ru.mosmetro.backend.model.dto.order.UpdatedPassengerOrderDTO
import ru.mosmetro.backend.repository.PassengerOrderEntityRepository

@Service
class OrderService(
    private val passengerOrderEntityRepository: PassengerOrderEntityRepository,
    private val orderMapper: OrderMapper
) {
    /**
     *
     * Метод возвращает список заявок в системе
     *
     * */
    fun getOrders(): ListWithTotal<PassengerOrderDTO> {
        val passengerOrderDTOS = passengerOrderEntityRepository.findAll()
            .map { orderMapper.entityToDomain(it) }
            .map { orderMapper.domainToDto(it) }
        return ListWithTotal(passengerOrderDTOS.size, passengerOrderDTOS)
    }

    /**
     *
     * Метод возвращает заявку по её идентификатору
     *
     * @param id - идентификатор заявки
     * @return сущность PassengerOrderDTO в которой предоставлена информация о заявке
     *
     * */
    fun getOrderById(id: Long): PassengerOrderDTO {
        return passengerOrderEntityRepository.findById(id).orElseThrow {
            NoSuchOrderException(id)
        }
            .let { orderMapper.entityToDomain(it) }
            .let { orderMapper.domainToDto(it) }
    }

    /**
     *
     * Метод создает новую заявку
     *
     * @param newPassengerOrderDTO - модель данных новой заявки
     * @return сущность PassengerOrderDTO в которой предоставлена информация о заявке
     *
     * */
    fun createOrder(newPassengerOrderDTO: NewPassengerOrderDTO): PassengerOrderDTO {
        return newPassengerOrderDTO
            .let { orderMapper.dtoToDomain(it) }
            .let { orderMapper.domainToEntity(it) }
            .let { passengerOrderEntityRepository.save(it) }
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
    fun updateOrder(id: Long, updatedPassengerOrderDTO: UpdatedPassengerOrderDTO): PassengerOrderDTO {
        val passengerOrderEntity = passengerOrderEntityRepository.findById(id).orElseThrow {
            NoSuchOrderException(id)
        }

        return updatedPassengerOrderDTO
            .let { orderMapper.dtoToDomain(it, passengerOrderEntity.createdAt) }
            .let { orderMapper.domainToEntity(it) }
            .let { passengerOrderEntityRepository.save(it) }
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
    fun deleteOrder(id: Long) {
        passengerOrderEntityRepository.deleteById(id)
    }

    fun getCurrentUserOrders(): ListWithTotal<PassengerOrderDTO> {
        TODO()
    }
}