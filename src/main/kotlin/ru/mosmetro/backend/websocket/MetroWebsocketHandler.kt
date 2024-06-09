package ru.mosmetro.backend.websocket

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.beans.factory.ObjectProvider
import org.springframework.stereotype.Component
import org.springframework.web.reactive.socket.WebSocketHandler
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.publisher.Mono
import ru.mosmetro.backend.model.WebsocketConstants.ATTRIBUTE_EMPLOYEE_LOCK
import ru.mosmetro.backend.model.WebsocketConstants.ATTRIBUTE_ORDER_LOCK
import ru.mosmetro.backend.model.WebsocketConstants.ATTRIBUTE_PASSENGER_LOCK
import ru.mosmetro.backend.model.WebsocketConstants.ATTRIBUTE_SINK
import ru.mosmetro.backend.model.dto.websocket.WebsocketInputMessageDTO
import ru.mosmetro.backend.model.enums.WebsocketMessageType
import ru.mosmetro.backend.service.EntityLockService
import ru.mosmetro.backend.service.EntitySubscriptionService
import ru.mosmetro.backend.util.activeSubscription
import ru.mosmetro.backend.util.getLogger
import ru.mosmetro.backend.websocket.processor.WebsocketMessageProcessor

@Component
class MetroWebsocketHandler(
    private val objectMapper: ObjectMapper,
    websocketProcessors: List<WebsocketMessageProcessor>,
    private val websocketSinkProvider: ObjectProvider<MetroWebsocketSink>,
    private val lockService: EntityLockService,
    private val subscriptionService: EntitySubscriptionService
) : WebSocketHandler {

    private val processor: Map<String, WebsocketMessageProcessor> = websocketProcessors.associateBy { it.action }

    override fun handle(session: WebSocketSession): Mono<Void> {
        log.info("Opened websocket session with id = ${session.id}")

        val sink = websocketSinkProvider.getObject()
        session.attributes[ATTRIBUTE_SINK] = sink

        val input: Mono<Void> = session.receive()
            .map { objectMapper.readValue<WebsocketInputMessageDTO>(it.payloadAsText) }
            .doOnNext { message ->
                log.debug("Received websocket message with action = ${message.action} for sessionId = ${session.id}")
                if (processor.containsKey(message.action)) {
                    processor.getValue(message.action).processMessage(message, session)
                } else {
                    log.error("There is no websocket processor for action = ${message.action} (sessionId = ${session.id})")
                }
            }
            .onErrorContinue { e, _ ->
                log.error("Exception during processing websocket message for sessionId = ${session.id}", e)
            }
            .doOnComplete {
                session.resetLocks()
                log.info("Closed websocket session with id = ${session.id}")
            }
            .then()

        val output: Mono<Void> = sink.flux
            .doOnNext { log.debug("Sending websocket message with action = ${it.action} for sessionId = ${session.id}") }
            .map { objectMapper.writeValueAsString(it) }
            .map { session.textMessage(it) }
            .let { session.send(it) }
            .doOnError { e -> log.error("Exception during sending websocket message for sessionId = ${session.id}") }
            .then()

        return Mono.zip(input, output)
            .then()
    }

    private fun WebSocketSession.resetLocks() {
        val employeeIdLock = attributes[ATTRIBUTE_EMPLOYEE_LOCK] as Long?
        employeeIdLock?.let { lockService.unlockEmployee(employeeIdLock, id) }

        val orderIdLock = attributes[ATTRIBUTE_ORDER_LOCK] as Long?
        orderIdLock?.let { lockService.unlockOrder(orderIdLock, id) }

        val passengerIdLock = attributes[ATTRIBUTE_PASSENGER_LOCK] as Long?
        passengerIdLock?.let { lockService.unlockPassenger(passengerIdLock, id) }

        when (activeSubscription) {
            WebsocketMessageType.ORDER_LIST_UPDATE -> subscriptionService.unsubscribeFromOrderUpdates(id)
            WebsocketMessageType.PASSENGER_LIST_UPDATE -> subscriptionService.unsubscribeFromPassengerUpdates(id)
            else -> {}
        }
    }

    companion object {
        private val log = getLogger<MetroWebsocketHandler>()
    }
}
