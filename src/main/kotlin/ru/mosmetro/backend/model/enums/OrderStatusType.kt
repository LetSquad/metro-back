package ru.mosmetro.backend.model.enums

enum class OrderStatusType {
    NOT_CONFIRMED,
    UNDER_CONSIDERATION,
    ACCEPTED,
    INSPECTOR_LEFT,
    INSPECTOR_ON_SITE,
    DRIVE,
    COMPLETED,
    IDENTIFICATION,
    WAITING_LIST,
    CANCEL,
    REFUSAL,
    PASSENGER_IS_LATE,
    INSPECTOR_IS_LATE
}