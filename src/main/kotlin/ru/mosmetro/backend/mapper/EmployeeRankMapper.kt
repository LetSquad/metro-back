package ru.mosmetro.backend.mapper

import org.springframework.stereotype.Component
import ru.mosmetro.backend.model.domain.EmployeeRank
import ru.mosmetro.backend.model.dto.employee.EmployeeRankDTO
import ru.mosmetro.backend.model.entity.EmployeeRankEntity

@Component
class EmployeeRankMapper {
    fun entityToDomain(mapper: EmployeeRankEntity) = EmployeeRank(
            code = mapper.code,
            name = mapper.name,
            shortName = mapper.shortName,
            role = mapper.role,
    )

    fun domainToDto(mapper: EmployeeRank) = EmployeeRankDTO(
            code = mapper.code,
            name = mapper.name,
            shortName = mapper.shortName,
            role = mapper.role,
    )
}