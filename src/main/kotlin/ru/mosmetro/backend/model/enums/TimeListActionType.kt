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

    // нужно для угадывания вреиени, не передается на фронт
    GUESSING_TECHNICAL_TYPE,
}