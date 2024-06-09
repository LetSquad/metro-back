package ru.mosmetro.backend.service

import org.springframework.stereotype.Service
import org.springframework.web.reactive.socket.WebSocketSession
import ru.mosmetro.backend.model.WebsocketConstants
import ru.mosmetro.backend.model.dto.websocket.WebsocketDataDTO
import ru.mosmetro.backend.model.dto.websocket.WebsocketOutputMessageDTO
import ru.mosmetro.backend.model.enums.WebsocketMessageType
import ru.mosmetro.backend.util.sink
import ru.mosmetro.backend.websocket.MetroWebsocketSink
import java.util.concurrent.ConcurrentHashMap

@Service
class EntitySubscriptionService {

    private val orderUpdateSubscribers = ConcurrentHashMap<String, WebSocketSession>()
    private val passengerUpdateSubscribers = ConcurrentHashMap<String, WebSocketSession>()

    fun subscribeForOrderUpdates(session: WebSocketSession): Boolean {
        return orderUpdateSubscribers.putIfAbsent(session.id, session) == null
    }

    fun subscribeForPassengerUpdates(session: WebSocketSession): Boolean {
        return passengerUpdateSubscribers.putIfAbsent(session.id, session) == null
    }

    fun unsubscribeFromOrderUpdates(sessionId: String): Boolean {
        return orderUpdateSubscribers.remove(sessionId) != null
    }

    fun unsubscribeFromPassengerUpdates(sessionId: String): Boolean {
        return passengerUpdateSubscribers.remove(sessionId) != null
    }

    fun notifyOrderUpdate(userId: Long? = null) {
        orderUpdateSubscribers.values
            .map { it.sink }
            .notifyAll(WebsocketMessageType.ORDER_LIST_UPDATE)
    }

    fun notifyPassengerUpdate() {
        passengerUpdateSubscribers.values
            .map { it.sink }
            .notifyAll(WebsocketMessageType.PASSENGER_LIST_UPDATE)
    }

    private fun List<MetroWebsocketSink>.notifyAll(messageType: WebsocketMessageType) = forEach { sink ->
        sink.sendMessage(
            WebsocketOutputMessageDTO(
                action = WebsocketConstants.ACTION_UPDATE,
                WebsocketDataDTO(type = messageType)
            )
        )
    }
}
