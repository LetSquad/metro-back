package ru.mosmetro.backend.websocket.processor

import org.springframework.stereotype.Component
import org.springframework.web.reactive.socket.WebSocketSession
import ru.mosmetro.backend.model.WebsocketConstants.ACTION_ERROR
import ru.mosmetro.backend.model.WebsocketConstants.ACTION_WANT
import ru.mosmetro.backend.model.WebsocketConstants.ATTRIBUTE_ACTIVE_SUBSCRIPTION
import ru.mosmetro.backend.model.WebsocketConstants.ATTRIBUTE_EMPLOYEE_LOCK
import ru.mosmetro.backend.model.WebsocketConstants.ATTRIBUTE_ORDER_LOCK
import ru.mosmetro.backend.model.WebsocketConstants.ATTRIBUTE_PASSENGER_LOCK
import ru.mosmetro.backend.model.dto.websocket.WebsocketDataDTO
import ru.mosmetro.backend.model.dto.websocket.WebsocketInputMessageDTO
import ru.mosmetro.backend.model.dto.websocket.WebsocketOutputMessageDTO
import ru.mosmetro.backend.model.enums.WebsocketMessageType
import ru.mosmetro.backend.service.EntityLockService
import ru.mosmetro.backend.service.EntitySubscriptionService
import ru.mosmetro.backend.util.activeSubscription
import ru.mosmetro.backend.util.getLogger
import ru.mosmetro.backend.util.sink

@Component
class WantWebsocketProcessor(
    private val lockService: EntityLockService,
    private val subscriptionService: EntitySubscriptionService
) : WebsocketMessageProcessor {

    override val action: String
        get() = ACTION_WANT

    override fun processMessage(message: WebsocketInputMessageDTO, session: WebSocketSession) {
        log.debug("New want message for session with = {}: {}", session.id, message.data?.type)
        val entityId: Long? = message.data!!.id

        val isSuccess: Boolean = when (message.data.type) {
            WebsocketMessageType.EMPLOYEE_EDIT -> processEmployeeEdit(entityId!!, session)
            WebsocketMessageType.ORDER_EDIT -> processOrderEdit(entityId!!, session)
            WebsocketMessageType.PASSENGER_EDIT -> processPassengerEdit(entityId!!, session)
            WebsocketMessageType.ORDER_LIST_UPDATE -> processOrderUpdateSubscription(message.data.type, session)
            WebsocketMessageType.CURRENT_ORDER_LIST_UPDATE -> processOrderUpdateSubscription(message.data.type, session)
            WebsocketMessageType.PASSENGER_LIST_UPDATE -> processPassengerUpdateSubscription(message.data.type, session)
        }

        if (!isSuccess) {
            session.sink.sendMessage(
                WebsocketOutputMessageDTO(ACTION_ERROR, WebsocketDataDTO(message.data.type, entityId))
            )
        }
    }

    private fun processEmployeeEdit(employeeId: Long, session: WebSocketSession): Boolean {
        if (session.attributes[ATTRIBUTE_EMPLOYEE_LOCK] != null) {
            return false
        }

        val isLocked: Boolean = lockService.lockEmployee(employeeId, session.id)
        if (isLocked) {
            session.attributes[ATTRIBUTE_EMPLOYEE_LOCK] = employeeId
        }
        return isLocked
    }

    private fun processOrderEdit(orderId: Long, session: WebSocketSession): Boolean {
        if (session.attributes[ATTRIBUTE_ORDER_LOCK] != null) {
            return false
        }

        val isLocked: Boolean = lockService.lockOrder(orderId, session.id)
        if (isLocked) {
            session.attributes[ATTRIBUTE_ORDER_LOCK] = orderId
        }
        return isLocked
    }

    private fun processPassengerEdit(passengerId: Long, session: WebSocketSession): Boolean {
        if (session.attributes[ATTRIBUTE_PASSENGER_LOCK] != null) {
            return false
        }

        val isLocked: Boolean = lockService.lockPassenger(passengerId, session.id)
        if (isLocked) {
            session.attributes[ATTRIBUTE_PASSENGER_LOCK] = passengerId
        }
        return isLocked
    }

    private fun processOrderUpdateSubscription(type: WebsocketMessageType, session: WebSocketSession): Boolean {
        if (session.activeSubscription != null) {
            return false
        }

        val isSubscribed: Boolean = subscriptionService.subscribeForOrderUpdates(session)
        if (isSubscribed) {
            session.attributes[ATTRIBUTE_ACTIVE_SUBSCRIPTION] = type
        }
        return isSubscribed
    }

    private fun processPassengerUpdateSubscription(type: WebsocketMessageType, session: WebSocketSession): Boolean {
        if (session.activeSubscription != null) {
            return false
        }

        val isSubscribed: Boolean = subscriptionService.subscribeForPassengerUpdates(session)
        if (isSubscribed) {
            session.attributes[ATTRIBUTE_ACTIVE_SUBSCRIPTION] = type
        }
        return isSubscribed
    }

    companion object {
        private val log = getLogger<WantWebsocketProcessor>()
    }
}
