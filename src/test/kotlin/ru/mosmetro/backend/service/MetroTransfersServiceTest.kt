package ru.mosmetro.backend.service

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import ru.mosmetro.backend.model.dto.order.OrderTransfersRequestDTO
import ru.mosmetro.backend.model.dto.order.OrderTransfersResponseDTO
import kotlin.test.assertEquals
import kotlin.test.assertFalse

@SpringBootTest
class MetroTransfersServiceTest {

    @Autowired
    lateinit var transfersService: MetroTransfersService

    @Test
    fun testRouteWithTransfers() {
        val response: OrderTransfersResponseDTO = transfersService.calculateTransfers(
            OrderTransfersRequestDTO(
                startStation = AIRPORT_STATION_ID,
                finishStation = NAGORNAYA_STATION_ID
            )
        )

        assertEquals(response.duration, response.transfers.sumOf { it.duration })

        assertEquals("Аэропорт", response.transfers.first().startStation.name)
        assertEquals("Тверская", response.transfers.first().finishStation.name)
        assertEquals("Чеховская", response.transfers.last().startStation.name)
        assertEquals("Нагорная", response.transfers.last().finishStation.name)

        val crosswalkings = response.transfers.filter { it.isCrosswalking }
        assertEquals("Тверская", crosswalkings.first().startStation.name)
        assertEquals("Чеховская", crosswalkings.first().finishStation.name)
        assertEquals(1, crosswalkings.size)

        assertEquals(3, response.transfers.size)
    }

    @Test
    fun testNeighborRoute() {
        val response: OrderTransfersResponseDTO = transfersService.calculateTransfers(
            OrderTransfersRequestDTO(
                startStation = SOKOL_STATION_ID,
                finishStation = AIRPORT_STATION_ID
            )
        )

        assertEquals(response.duration, response.transfers.first().duration)
        assertEquals("Сокол", response.transfers.first().startStation.name)
        assertEquals("Аэропорт", response.transfers.first().finishStation.name)
        assertFalse(response.transfers.first().isCrosswalking)
        assertEquals(1, response.transfers.size)
    }

    @Test
    fun testEmptyRoute() {
        val response: OrderTransfersResponseDTO = transfersService.calculateTransfers(
            OrderTransfersRequestDTO(
                startStation = AIRPORT_STATION_ID,
                finishStation = AIRPORT_STATION_ID
            )
        )

        assertEquals(response.duration, 0)
        assertEquals(response.transfers.size, 0)
    }

    companion object {
        private const val SOKOL_STATION_ID = 26L
        private const val AIRPORT_STATION_ID = 27L
        private const val NAGORNAYA_STATION_ID = 165L
    }
}
