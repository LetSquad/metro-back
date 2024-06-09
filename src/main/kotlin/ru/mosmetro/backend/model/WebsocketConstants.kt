package ru.mosmetro.backend.model

object WebsocketConstants {

    const val ACTION_INIT = "init"
    const val ACTION_PING = "ping"
    const val ACTION_PONG = "pong"
    const val ACTION_WANT = "want"
    const val ACTION_UPDATE = "update"
    const val ACTION_ERROR = "error"

    const val ATTRIBUTE_SINK = "sink"
    const val ATTRIBUTE_EMPLOYEE_LOCK = "employeeLock"
    const val ATTRIBUTE_ORDER_LOCK = "orderLock"
    const val ATTRIBUTE_PASSENGER_LOCK = "passengerLock"
    const val ATTRIBUTE_ACTIVE_SUBSCRIPTION = "activeSubscription"
}
