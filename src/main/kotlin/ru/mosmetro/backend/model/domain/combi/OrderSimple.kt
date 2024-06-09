package ru.mosmetro.backend.model.domain.combi

import java.time.LocalTime
import kotlin.time.Duration

data class OrderSimple(
        val id: Long?,
        val start: LocalTime,
        val finish: LocalTime,
        val duration: Duration,
        val passengerCategoryCode: String,
        val employeeCount: Int
//        val male: Int,
//        val female: Int,
)
