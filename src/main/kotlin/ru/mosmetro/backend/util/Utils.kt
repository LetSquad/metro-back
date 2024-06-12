package ru.mosmetro.backend.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.slf4j.Logger
import org.slf4j.LoggerFactory

inline fun <reified T> getLogger(): Logger = LoggerFactory.getLogger(T::class.java)

suspend fun <T> jpaContext(block: suspend CoroutineScope.() -> T): T {
    return withContext(Dispatchers.IO, block)
}
