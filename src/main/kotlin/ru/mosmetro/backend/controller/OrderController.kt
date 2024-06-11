package ru.mosmetro.backend.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.mosmetro.backend.model.dto.EntityForEdit
import ru.mosmetro.backend.model.dto.ListWithTotal
import ru.mosmetro.backend.model.dto.order.NewPassengerOrderDTO
import ru.mosmetro.backend.model.dto.order.OrderTransfersRequestDTO
import ru.mosmetro.backend.model.dto.order.OrderTransfersResponseDTO
import ru.mosmetro.backend.model.dto.order.PassengerOrderDTO
import ru.mosmetro.backend.model.dto.order.OrderTimeListDTO
import ru.mosmetro.backend.model.dto.order.UpdatedPassengerOrderDTO
import ru.mosmetro.backend.service.MetroTransfersService
import ru.mosmetro.backend.service.OrderDistributionService
import ru.mosmetro.backend.service.OrderService
import ru.mosmetro.backend.service.TimeListService
import java.time.LocalDate

@Tag(name = "Методы работы с заявками")
@RestController
@RequestMapping("/api/orders")
class OrderController(
    private val orderService: OrderService,
    private val timeListService: TimeListService,
    private val transfersService: MetroTransfersService,
    private val distributionService: OrderDistributionService,
) {

    @Operation(
        summary = "Получение всех заявок"
    )
    @GetMapping
    suspend fun getOrders(): ListWithTotal<PassengerOrderDTO> {
        return orderService.getOrders()
    }

    @Operation(
        summary = "Получение временного графика всех заявок на завтра"
    )
    @GetMapping("time-list")
    suspend fun getOrdersTimeList(): OrderTimeListDTO {
        val tomorrowDate = LocalDate.now().plusDays(1)
        return timeListService.getOrderTimeList(tomorrowDate)
    }

    @Operation(
        summary = "Получение всех заявок текущего пользователя"
    )
    @GetMapping("current")
    suspend fun getCurrentUserOrders(): ListWithTotal<PassengerOrderDTO> {
        return orderService.getCurrentUserOrders()
    }

    @Operation(
        summary = "Создание новой заявки"
    )
    @PostMapping
    suspend fun createOrder(@RequestBody newPassengerOrderDTO: NewPassengerOrderDTO): PassengerOrderDTO {
        return orderService.createOrder(newPassengerOrderDTO)
    }

    @Operation(
        summary = "Обновление заявки по её идентификатору"
    )
    @PutMapping("{id}")
    suspend fun updateOrder(
        @Parameter(description = "ID заявки") @PathVariable id: Long,
        @RequestBody updatedPassengerOrderDTO: UpdatedPassengerOrderDTO
    ): PassengerOrderDTO {
        return orderService.updateOrder(id, updatedPassengerOrderDTO)
    }

    @Operation(
        summary = "Удаление заявки по её идентификатору"
    )
    @DeleteMapping("{id}")
    suspend fun deleteOrder(@Parameter(description = "ID заявки") @PathVariable id: Long) {
        orderService.deleteOrder(id)
    }

    @Operation(
        summary = "Получение заявки по её идентификатору"
    )
    @GetMapping("{id}")
    suspend fun getOrderById(
        @Parameter(description = "ID заявки") @PathVariable id: Long
    ): EntityForEdit<PassengerOrderDTO> {
        return orderService.getOrderById(id)
    }

    @Operation(
        summary = "Расчет маршрута по заявки"
    )
    @PostMapping("/transfers-calculation")
    fun calculateOrder(@RequestBody request: OrderTransfersRequestDTO): OrderTransfersResponseDTO {
        return transfersService.calculateTransfers(request)
    }

    @Operation(
        summary = "Распределение заявок"
    )
    @PostMapping("/distribution")
    fun calculateOrderDistribution(): PassengerOrderDTO {
        return distributionService.calculateOrderDistribution()
    }
}
