package ru.mosmetro.backend.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.runBlocking
import org.springframework.transaction.support.TransactionTemplate

suspend fun <T> TransactionTemplate.executeSuspended(block: suspend CoroutineScope.() -> T): T = jpaContext {
    return@jpaContext execute {
        runBlocking { block() }
    }
}
