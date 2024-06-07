package ru.mosmetro.backend.util

import org.postgresql.util.PGobject

fun String?.toPGObject(): PGobject {
    val pGobject = PGobject()
    pGobject.type = "jsonb"
    pGobject.value = this
    return pGobject
}
