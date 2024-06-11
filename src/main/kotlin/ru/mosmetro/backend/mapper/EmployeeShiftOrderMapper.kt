package ru.mosmetro.backend.mapper

import org.springframework.stereotype.Component
import ru.mosmetro.backend.model.domain.EmployeeShiftOrder
import ru.mosmetro.backend.model.dto.order.EmployeeShiftOrderDTO
import ru.mosmetro.backend.model.entity.EmployeeShiftOrderEntity
import ru.mosmetro.backend.model.enums.TimeListActionType

@Component
class EmployeeShiftOrderMapper(
    val orderMapper: OrderMapper,
    val passangerOrderMapper: OrderMapper,
) {
    fun entityToDomain(mapper: EmployeeShiftOrderEntity) = EmployeeShiftOrder(
            timeStart = mapper.timeStart,
            timeFinish = mapper.timeFinish,
            actionType = TimeListActionType.valueOf(mapper.actionType),
            order = mapper.order?.let { passangerOrderMapper.entityToDomain(it) }
    )

    fun domainToDto(mapper: EmployeeShiftOrder) = EmployeeShiftOrderDTO(
            timeStart = mapper.timeStart,
            timeFinish = mapper.timeFinish,
            actionType = mapper.actionType,
            order = mapper.order?.let { orderMapper.domainToDto(it) }
    )
}