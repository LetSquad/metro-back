package ru.mosmetro.backend.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.mosmetro.backend.model.dto.ListWithTotal
import ru.mosmetro.backend.model.dto.metro.MetroStationDTO
import ru.mosmetro.backend.service.MetroService

@Tag(name = "Методы работы со словарями метро")
@RestController
@RequestMapping("/api/metro")
class MetroController(
    private val metroService: MetroService
) {

    @Operation(
        summary = "Получение всех станций метро"
    )
    @GetMapping
    suspend fun getAllMetroStations(): ListWithTotal<MetroStationDTO> {
        return metroService.getAllMetroStations()
    }
}
