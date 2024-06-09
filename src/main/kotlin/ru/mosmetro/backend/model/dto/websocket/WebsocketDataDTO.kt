package ru.mosmetro.backend.model.dto.websocket

import com.fasterxml.jackson.annotation.JsonInclude
import ru.mosmetro.backend.model.enums.WebsocketMessageType

data class WebsocketDataDTO(

    val type: WebsocketMessageType,

    @JsonInclude(JsonInclude.Include.NON_NULL)
    val id: Long? = null
)
