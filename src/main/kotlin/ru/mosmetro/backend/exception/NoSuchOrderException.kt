package ru.mosmetro.backend.exception

class NoSuchOrderException(id: Long) : MetroException("No such order with id = $id")
