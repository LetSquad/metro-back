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
import ru.mosmetro.backend.model.dto.ListWithTotal
import ru.mosmetro.backend.model.dto.passenger.NewPassengerDTO
import ru.mosmetro.backend.model.dto.passenger.PassengerDTO
import ru.mosmetro.backend.model.dto.passenger.UpdatePassengerDTO
import ru.mosmetro.backend.service.PassengerService

@Tag(name = "Методы работы с пассажирами")
@RestController
@RequestMapping("/api/passengers")
class PassengerController(
    private val passengerService: PassengerService
) {
    @Operation(
        summary = "Получение всех пассажиров"
    )
    @GetMapping
    fun getPassengers(): ListWithTotal<PassengerDTO> {
        return passengerService.getPassengers()
    }

    @Operation(
        summary = "Получение пассажира по его идентификатору"
    )
    @GetMapping("{id}")
    fun getPassengerById(@Parameter(description = "ID заявки") @PathVariable id: Long): PassengerDTO {
        return passengerService.getPassengerById(id)
    }

    @Operation(
        summary = "Создание нового пассажира"
    )
    @PostMapping
    fun createPassenger(@RequestBody newPassengerDTO: NewPassengerDTO): PassengerDTO {
        return passengerService.createPassenger(newPassengerDTO)
    }

    @Operation(
        summary = "Обновление пассажира по его идентификатору"
    )
    @PutMapping("{id}")
    fun updatePassenger(
        @Parameter(description = "ID заявки") @PathVariable id: Long,
        @RequestBody updatePassengerDTO: UpdatePassengerDTO
    ): PassengerDTO {
        return passengerService.updatePassenger(id, updatePassengerDTO)
    }

    @Operation(
        summary = "Удаление пассажира по его идентификатору"
    )
    @DeleteMapping("{id}")
    fun deletePassenger(@Parameter(description = "ID заявки") @PathVariable id: Long) {
        passengerService.deletePassenger(id)
    }
}
