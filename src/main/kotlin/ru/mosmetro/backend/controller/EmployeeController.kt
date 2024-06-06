package ru.mosmetro.backend.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.*
import ru.mosmetro.backend.model.dto.ListWithTotal
import ru.mosmetro.backend.model.dto.employee.*
import ru.mosmetro.backend.service.EmployeeService

@Tag(name = "Методы работы с сотрудниками")
@RestController
@RequestMapping("/api/employees")
class EmployeeController(
    private val employeeService: EmployeeService
) {
    @Operation(
        summary = "Получение всех сотрудников"
    )
    @GetMapping
    fun getEmployees(): ListWithTotal<EmployeeDTO> {
        return employeeService.getEmployees()
    }

    @Operation(
        summary = "Получение сотрудника по его идентификатору"
    )
    @GetMapping("{id}")
    fun getEmployeeById(@Parameter(description = "ID работника") @PathVariable id: Long): EmployeeDTO {
        return employeeService.getEmployeeById(id)
    }

    @Operation(
        summary = "Создание нового работника"
    )
    @PostMapping
    fun createEmployee(@RequestBody newEmployeeDTO: NewEmployeeDTO): EmployeeDTO {
        return employeeService.createEmployee(newEmployeeDTO)
    }

    @Operation(
        summary = "Обновление работника по его идентификатору"
    )
    @PutMapping("{id}")
    fun updateEmployee(
        @Parameter(description = "ID работника") @PathVariable id: Long,
        @RequestBody updateEmployeeDTO: UpdateEmployeeDTO
    ): EmployeeDTO {
        return employeeService.updateEmployee(id, updateEmployeeDTO)
    }

    @Operation(
        summary = "Удаление работника по его идентификатору"
    )
    @DeleteMapping("{id}")
    fun deleteEmployee(@Parameter(description = "ID заявки") @PathVariable id: Long) {
        employeeService.deleteEmployee(id)
    }

    @Operation(
        summary = "Получение должностей работников"
    )
    @GetMapping("ranks")
    fun getEmployeeRanks(): ListWithTotal<EmployeeRankDTO> {
        return employeeService.getAllEmployeeRanks()
    }

    @Operation(
        summary = "Получение времени работы работников"
    )
    @GetMapping("shifts")
    fun getEmployeeShifts(): ListWithTotal<EmployeeShiftDTO> {
        return employeeService.getAllEmployeeShifts()
    }

    @Operation(
        summary = "Обновление пароля для работника"
    )
    @PostMapping("{id}/reset-password")
    fun resetEmployeePassword(@Parameter(description = "ID работника") @PathVariable id: Long) {
        employeeService.resetEmployeePassword()
    }
}
