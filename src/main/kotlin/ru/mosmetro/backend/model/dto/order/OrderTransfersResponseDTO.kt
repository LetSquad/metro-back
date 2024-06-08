package ru.mosmetro.backend.model.dto.order

import ru.mosmetro.backend.model.dto.metro.MetroStationTransferDTO

data class OrderTransfersResponseDTO(
    val duration: Long,
    val transfers: List<MetroStationTransferDTO>
)
