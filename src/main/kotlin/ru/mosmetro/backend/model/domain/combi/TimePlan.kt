package ru.mosmetro.backend.model.domain.combi

import ru.mosmetro.backend.model.domain.Order
import java.time.LocalDateTime

data class TimePlan(
        val startTime: LocalDateTime,
        val finishTime: LocalDateTime,
        val order: Order,
        val isTransfer: Boolean,
)
