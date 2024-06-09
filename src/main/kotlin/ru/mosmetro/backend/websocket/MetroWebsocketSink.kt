package ru.mosmetro.backend.websocket

import org.springframework.beans.factory.config.BeanDefinition
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Sinks
import ru.mosmetro.backend.model.dto.websocket.WebsocketOutputMessageDTO

@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
class MetroWebsocketSink {

    private val sink = Sinks.many()
        .unicast()
        .onBackpressureError<WebsocketOutputMessageDTO>()

    val flux: Flux<WebsocketOutputMessageDTO>
        get() = sink.asFlux()

    fun sendMessage(message: WebsocketOutputMessageDTO) {
        sink.tryEmitNext(message)
    }
}
