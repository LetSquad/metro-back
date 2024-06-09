package ru.mosmetro.backend.model.dto.order

import ru.mosmetro.backend.model.dto.employee.EmployeeDTO

data class OrderTimeDTO(
        val employee: EmployeeDTO,
        val actions: List<EmployeeOrderActionDTO>,
)
