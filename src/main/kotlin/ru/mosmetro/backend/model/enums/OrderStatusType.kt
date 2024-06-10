package ru.mosmetro.backend.model.enums

enum class OrderStatusType {
    NOT_CONFIRMED,
    REVIEW,
    ACCEPTED,
    INSPECTOR_WENT,
    INSPECTOR_ARRIVED,
    RIDE,
    COMPLETED,
    IDENTIFICATION,
    WAITING_LIST,
    CANCELED,
    REJECTED,
    PASSENGER_LATE,
    INSPECTOR_LATE
}
