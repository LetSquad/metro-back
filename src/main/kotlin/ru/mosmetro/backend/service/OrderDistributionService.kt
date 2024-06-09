package ru.mosmetro.backend.service

import org.springframework.stereotype.Service
import ru.mosmetro.backend.model.dto.order.PassengerOrderDTO

@Service
class OrderDistributionService(
    private val subscriptionService: EntitySubscriptionService
) {

    fun calculateOrderDistribution(): PassengerOrderDTO {
        subscriptionService.notifyOrderUpdate()

        TODO("Not yet implemented")
    }
}
