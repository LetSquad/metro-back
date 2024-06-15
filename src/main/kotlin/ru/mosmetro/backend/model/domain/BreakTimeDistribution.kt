package ru.mosmetro.backend.model.domain

import java.time.LocalDateTime

data class BreakTimeDistribution(
    val timeStart: LocalDateTime,
    val timeFinish: LocalDateTime,
    val orders: List<PassengerOrder>,

    // тут можно вставить больще критериев и увеличить точность угадывания
    val employeesNeedWork: Int,
    var employeesFree: Int,
)