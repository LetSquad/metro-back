package ru.mosmetro.backend

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan
class MetroApplication

fun main(args: Array<String>) {
    runApplication<MetroApplication>(*args)
}
