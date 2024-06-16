package ru.mosmetro.backend.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.mosmetro.backend.service.DirtyHackService

@Tag(name = "Методы работы с dirty hack")
@RestController
@RequestMapping("/api/dirty-hack")
class DirtyHackControllerController(
    private val dirtyHackService: DirtyHackService
) {

    @Operation(
        summary = "Dirty Hack"
    )
    @GetMapping
    suspend fun dirtyHack() {
        return dirtyHackService.dirtyHack()
    }
}
