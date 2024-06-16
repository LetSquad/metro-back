package ru.mosmetro.backend.mapper

import java.time.LocalDateTime
import org.springframework.stereotype.Component
import ru.mosmetro.backend.model.domain.EmployeeShiftOrder
import ru.mosmetro.backend.model.dto.order.EmployeeShiftOrderDTO
import ru.mosmetro.backend.model.entity.EmployeeShiftOrderEntity
import ru.mosmetro.backend.model.entity.PassengerPhoneEntity
import ru.mosmetro.backend.model.enums.TimeListActionType
import ru.mosmetro.backend.util.MetroTimeUtil.TIME_ZONE_MOSCOW
import ru.mosmetro.backend.util.MetroTimeUtil.TIME_ZONE_UTC

@Component
class EmployeeShiftOrderMapper(
    private val orderMapper: OrderMapper,
    private val passangerOrderMapper: OrderMapper,
) {
    fun entityToDomain(mapper: EmployeeShiftOrderEntity, passengerPhones: Set<PassengerPhoneEntity>) =
        EmployeeShiftOrder(
            timeStart = LocalDateTime.ofInstant(mapper.timeStart, TIME_ZONE_UTC),
            timeFinish = LocalDateTime.ofInstant(mapper.timeFinish, TIME_ZONE_UTC),
            actionType = TimeListActionType.valueOf(mapper.actionType),
            order = mapper.order?.let { passangerOrderMapper.entityToDomain(it, passengerPhones) }
    )

    fun domainToDto(mapper: EmployeeShiftOrder) = EmployeeShiftOrderDTO(
            timeStart = mapper.timeStart.toInstant(TIME_ZONE_MOSCOW),
            timeEnd = mapper.timeFinish.toInstant(TIME_ZONE_MOSCOW),
            actionType = mapper.actionType,
            order = mapper.order?.let { orderMapper.domainToDto(it) }
    )

}