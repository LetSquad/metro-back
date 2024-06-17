package ru.mosmetro.backend.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.Resource
import org.springframework.http.MediaType
import org.springframework.web.reactive.HandlerMapping
import org.springframework.web.reactive.function.server.RequestPredicates.GET
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.RouterFunctions
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.session.WebSessionManager
import reactor.core.publisher.Mono
import ru.mosmetro.backend.websocket.MetroWebsocketHandler

@Configuration
class WebConfig {

    @Bean
    fun indexRouter(@Value("classpath:/public/index.html") html: Resource): RouterFunction<ServerResponse> {
        return RouterFunctions.route(
            GET("/{path1:(?!api|v3|\\.).[^.]*}")
                .or(GET("/{path1:(?!api|v3|\\.).[^.]*}/{path2:[^.]*}"))
                .or(GET("/{path1:(?!api|v3|\\.).[^.]*}/{path2:[^.]*}/{path3:[^.]*}"))
        ) { _: ServerRequest? ->
            ServerResponse.ok()
                .contentType(MediaType.TEXT_HTML)
                .bodyValue(html)
        }
    }

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
