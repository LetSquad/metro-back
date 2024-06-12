package ru.mosmetro.backend.model.dto.order

data class OrderTimeListDTO(
        val ordersNotInPlan: List<PassengerOrderDTO>,
        val ordersTime: List<OrderTimeDTO>,
)
