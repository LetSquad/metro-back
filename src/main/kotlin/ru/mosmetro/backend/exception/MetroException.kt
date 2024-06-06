package ru.mosmetro.backend.exception

abstract class MetroException(
    message: String,
    cause: Throwable? = null
) : RuntimeException(message, cause)