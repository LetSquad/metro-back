package ru.mosmetro.backend.websocket.processor

import org.springframework.stereotype.Component
import org.springframework.web.reactive.socket.WebSocketSession
import ru.mosmetro.backend.model.WebsocketConstants.ACTION_PING
import ru.mosmetro.backend.model.WebsocketConstants.ACTION_PONG
import ru.mosmetro.backend.model.dto.websocket.WebsocketInputMessageDTO
import ru.mosmetro.backend.model.dto.websocket.WebsocketOutputMessageDTO
import ru.mosmetro.backend.util.sink

@Component
class PingWebsocketProcessor : WebsocketMessageProcessor {

    override val action: String
        get() = ACTION_PING

    override fun processMessage(message: WebsocketInputMessageDTO, session: WebSocketSession) {
        session.sink.sendMessage(WebsocketOutputMessageDTO(action = ACTION_PONG))
    }
}
