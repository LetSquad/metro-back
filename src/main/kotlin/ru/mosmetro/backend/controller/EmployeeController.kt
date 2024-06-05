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
import ru.mosmetro.backend.model.dto.employee.EmployeeDTO
import ru.mosmetro.backend.model.dto.employee.NewEmployeeDTO
import ru.mosmetro.backend.model.dto.employee.UpdateEmployeeDTO
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
    fun getEmployeeById(@Parameter(description = "ID заявки") @PathVariable id: Long): EmployeeDTO {
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
        @Parameter(description = "ID заявки") @PathVariable id: Long,
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
        summary = "Обновление пароля для работника"
    )
    @PostMapping("{id}/reset-password")
    fun resetEmployeePassword(@Parameter(description = "ID заявки") @PathVariable id: Long) {
        employeeService.resetEmployeePassword()
    }
}
