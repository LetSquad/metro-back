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
import ru.mosmetro.backend.model.dto.EntityForEdit
import ru.mosmetro.backend.model.dto.ListWithTotal
import ru.mosmetro.backend.model.dto.passenger.NewPassengerDTO
import ru.mosmetro.backend.model.dto.passenger.PassangerFilterRequestDTO
import ru.mosmetro.backend.model.dto.passenger.PassengerCategoryDTO
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
    suspend fun getPassengers(filterDto: PassangerFilterRequestDTO): ListWithTotal<PassengerDTO> {
        return passengerService.getPassengers(filterDto)
    }

    @Operation(
        summary = "Получение пассажира по его идентификатору"
    )
    @GetMapping("{id}")
    suspend fun getPassengerById(
        @Parameter(description = "ID пассажира") @PathVariable id: Long
    ): EntityForEdit<PassengerDTO> {
        return passengerService.getPassengerById(id)
    }

    @Operation(
        summary = "Создание нового пассажира"
    )
    @PostMapping
    suspend fun createPassenger(@RequestBody newPassengerDTO: NewPassengerDTO): PassengerDTO {
        return passengerService.createPassenger(newPassengerDTO)
    }

    @Operation(
        summary = "Обновление пассажира по его идентификатору"
    )
    @PutMapping("{id}")
    suspend fun updatePassenger(
        @Parameter(description = "ID пассажира") @PathVariable id: Long,
        @RequestBody updatePassengerDTO: UpdatePassengerDTO
    ): PassengerDTO {
        return passengerService.updatePassenger(id, updatePassengerDTO)
    }

    @Operation(
        summary = "Удаление пассажира по его идентификатору"
    )
    @DeleteMapping("{id}")
    suspend fun deletePassenger(@Parameter(description = "ID пассажира") @PathVariable id: Long) {
        passengerService.deletePassenger(id)
    }

    @Operation(
            summary = "Получение категорий пассажиров"
    )
    @GetMapping("/categories")
    suspend fun getPassengerCategories(): ListWithTotal<PassengerCategoryDTO> {
        return passengerService.getPassengerCategories()
    }
}
