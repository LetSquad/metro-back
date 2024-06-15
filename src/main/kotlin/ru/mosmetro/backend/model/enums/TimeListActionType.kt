package ru.mosmetro.backend.model.enums

enum class TimeListActionType {
    // в пути на заявку
    TRANSFER,

    // отдыхает
    BREAK,

    // не занят, но работает по графику
    DOWNTIME,

    // на заявке
    ORDER,

    // не работает по графику
    NON_WORKING,

    // метро не работает в данный период
    METRO_NOT_WORKING
}