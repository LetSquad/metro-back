package ru.mosmetro.backend.util

import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset

object MetroTimeUtil {

    // -3 для TIME_ZONE_UTC
    val METRO_TIME_START = LocalTime.of(2, 30)
    val METRO_TIME_FINISH = LocalTime.of(22, 0)

    val TIME_ZONE_UTC = ZoneId.of("UTC")
    val TIME_ZONE_MOSCOW = ZoneOffset.of("+03:00")
    val TRANSFER_TIME_PERIOD = 8 * 60
}