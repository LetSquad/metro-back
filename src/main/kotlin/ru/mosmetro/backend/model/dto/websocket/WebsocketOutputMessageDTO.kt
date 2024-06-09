package ru.mosmetro.backend.model.dto.websocket

import com.fasterxml.jackson.annotation.JsonInclude

data class WebsocketOutputMessageDTO(

    val action: String,

    @JsonInclude(JsonInclude.Include.NON_NULL)
    val data: WebsocketDataDTO? = null
)
