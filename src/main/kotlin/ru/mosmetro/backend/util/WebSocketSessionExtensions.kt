package ru.mosmetro.backend.util

import org.springframework.web.reactive.socket.WebSocketSession
import ru.mosmetro.backend.model.WebsocketConstants.ATTRIBUTE_ACTIVE_SUBSCRIPTION
import ru.mosmetro.backend.model.WebsocketConstants.ATTRIBUTE_SINK
import ru.mosmetro.backend.model.enums.WebsocketMessageType
import ru.mosmetro.backend.websocket.MetroWebsocketSink

val WebSocketSession.sink: MetroWebsocketSink
    get() = attributes[ATTRIBUTE_SINK] as MetroWebsocketSink

val WebSocketSession.activeSubscription: WebsocketMessageType?
    get() = attributes[ATTRIBUTE_ACTIVE_SUBSCRIPTION] as WebsocketMessageType?
