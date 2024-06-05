package ru.mosmetro.backend

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class MetroApplication

fun main(args: Array<String>) {
    runApplication<MetroApplication>(*args)
}
