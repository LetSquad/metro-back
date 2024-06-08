package ru.mosmetro.backend.websocket.processor

import org.springframework.web.reactive.socket.WebSocketSession
import ru.mosmetro.backend.model.dto.websocket.WebsocketInputMessageDTO

interface WebsocketMessageProcessor {

    val action: String

    fun processMessage(message: WebsocketInputMessageDTO, session: WebSocketSession)
}
