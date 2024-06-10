package ru.mosmetro.backend.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.HandlerMapping
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.session.WebSessionManager
import reactor.core.publisher.Mono
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

    @Bean
    fun webSessionManager(): WebSessionManager {
        return WebSessionManager { _: ServerWebExchange -> Mono.empty() }
    }

    private companion object {
        private const val WEBSOCKET_MAPPING_ORDER = -1
    }
}
