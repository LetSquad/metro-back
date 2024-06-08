package ru.mosmetro.backend.model.dto.websocket

data class WebsocketInputMessageDTO(
    val action: String,
    val data: WebsocketDataDTO?
)
