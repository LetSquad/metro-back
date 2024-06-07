package ru.mosmetro.backend.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.*
import ru.mosmetro.backend.model.dto.ListWithTotal
import ru.mosmetro.backend.model.dto.order.NewPassengerOrderDTO
import ru.mosmetro.backend.model.dto.order.PassengerOrderDTO
import ru.mosmetro.backend.model.dto.order.UpdatedPassengerOrderDTO
import ru.mosmetro.backend.service.DistributionService
import ru.mosmetro.backend.service.OrderService

@Tag(name = "Методы работы с заявками")
@RestController
@RequestMapping("/api/orders")
class OrderController(
        private val orderService: OrderService,
        private val distributionService: DistributionService,
) {
    @Operation(
        summary = "Получение всех заявок"
    )
    @GetMapping
    fun getOrders(): ListWithTotal<PassengerOrderDTO> {
        return orderService.getOrders()
    }

    @Operation(
        summary = "Получение всех заявок текущего пользователя"
    )
    @GetMapping("current")
    fun getCurrentUserOrders(): ListWithTotal<PassengerOrderDTO> {
        return orderService.getCurrentUserOrders()
    }

    @Operation(
        summary = "Получение заявки по её идентификатору"
    )
    @GetMapping("{id}")
    fun getOrderById(@Parameter(description = "ID заявки") @PathVariable id: Long): PassengerOrderDTO {
        return orderService.getOrderById(id)
    }

    @Operation(
        summary = "Создание новой заявки"
    )
    @PostMapping
    fun createOrder(@RequestBody newPassengerOrderDTO: NewPassengerOrderDTO): PassengerOrderDTO {
        return orderService.createOrder(newPassengerOrderDTO)
    }

    @Operation(
        summary = "Обновление заявки по её идентификатору"
    )
    @PutMapping("{id}")
    fun updateOrder(
        @Parameter(description = "ID заявки") @PathVariable id: Long,
        @RequestBody updatedPassengerOrderDTO: UpdatedPassengerOrderDTO
    ): PassengerOrderDTO {
        return orderService.updateOrder(id, updatedPassengerOrderDTO)
    }

    @Operation(
        summary = "Удаление заявки по её идентификатору"
    )
    @DeleteMapping("{id}")
    fun deleteOrder(@Parameter(description = "ID заявки") @PathVariable id: Long) {
        orderService.deleteOrder(id)
    }

    @Operation(
            summary = "Рассчет маршрута по заявки"
    )
    @PostMapping("/calculations")
    fun calculateOrder(@RequestBody passengerOrderDTO: PassengerOrderDTO): PassengerOrderDTO {
        return distributionService.calculateOrder(passengerOrderDTO)
    }

    @Operation(
            summary = "Рассчет маршрута по заявки"
    )
    @PostMapping("/distribution")
    fun calculateOrderDistribution(): PassengerOrderDTO {
        return distributionService.calculateOrderDistribution()
    }
}
