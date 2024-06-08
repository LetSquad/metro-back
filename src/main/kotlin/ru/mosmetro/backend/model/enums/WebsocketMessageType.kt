package ru.mosmetro.backend.model.enums

enum class WebsocketMessageType {

    EMPLOYEE_EDIT,
    ORDER_EDIT,
    PASSENGER_EDIT,

    ORDER_LIST_UPDATE,
    CURRENT_ORDER_LIST_UPDATE,
    PASSENGER_LIST_UPDATE
}
