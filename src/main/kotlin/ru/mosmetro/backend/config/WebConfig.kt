package ru.mosmetro.backend.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.HandlerMapping
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping
import ru.mosmetro.backend.websocket.MetroWebsocketHandler

@Configuration
class WebConfig {

    @Bean
    fun handlerMapping(websocketHandler: MetroWebsocketHandler): HandlerMapping {
        return SimpleUrlHandlerMapping(
            mapOf(
                "/websocket" to websocketHandler
            ), WEBSOCKET_MAPPING_ORDER
        )
    }

    private companion object {
        private const val WEBSOCKET_MAPPING_ORDER = -1
    }
}
