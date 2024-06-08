package ru.mosmetro.backend.websocket.processor

import org.springframework.stereotype.Component
import org.springframework.web.reactive.socket.WebSocketSession
import ru.mosmetro.backend.model.WebsocketConstants.ACTION_INIT
import ru.mosmetro.backend.model.dto.websocket.WebsocketInputMessageDTO
import ru.mosmetro.backend.util.getLogger

@Component
class InitWebsocketProcessor : WebsocketMessageProcessor {

    override val action: String
        get() = ACTION_INIT

    override fun processMessage(message: WebsocketInputMessageDTO, session: WebSocketSession) {
        log.debug("Received init message for session with id = ${session.id}")
    }

    companion object {
        private val log = getLogger<InitWebsocketProcessor>()
    }
}
